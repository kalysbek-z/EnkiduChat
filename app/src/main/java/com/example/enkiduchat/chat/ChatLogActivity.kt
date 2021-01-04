package com.example.enkiduchat.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.enkiduchat.R
import com.example.enkiduchat.fcm.MyFirebaseMessagingService
import com.example.enkiduchat.models.Message
import com.example.enkiduchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.any_message_item.view.*
import kotlinx.android.synthetic.main.my_message_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat.adapter = adapter

//        adapter.add(MessageToItem("olololol"))

        toUser = intent.getParcelableExtra<User>("user_key")
        supportActionBar?.title = toUser?.username

        listenForMessages()

        send_btn.setOnClickListener {
            performSendMessage()
        }

        recyclerview_chat.scrollToPosition(adapter.itemCount - 1)
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        if (fromId == null) return
        if (toId == null) return

        val ref = FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
            .getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)

                if (message != null) {
                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(MessageToItem(message.text, message.timestamp))
                    } else {
                        adapter.add(MessageFromItem(message.text, message.timestamp))
                    }
                    recyclerview_chat.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        val text = edit_text_message.text.toString()

        if (text.isEmpty()) {
            Toast.makeText(applicationContext, "Write your message", Toast.LENGTH_SHORT).show()
        } else {

            val fromId = FirebaseAuth.getInstance().uid
            val user = intent.getParcelableExtra<User>("user_key")
            val toId = user?.uid

            if (fromId == null) return
            if (toId == null) return

            val ref =
                FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
                    .getReference("/user-messages/$fromId/$toId").push()

            val toRef =
                FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
                    .getReference("/user-messages/$toId/$fromId").push()

            val message = Message(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

            ref.setValue(message).addOnSuccessListener {
                edit_text_message.text.clear()
                recyclerview_chat.scrollToPosition(adapter.itemCount - 1)
            }

            toRef.setValue(message)

            val latestMessageRef =
                FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
                    .getReference("/latest-messages/$fromId/$toId")
            latestMessageRef.setValue(message)

            val latestMessageToRef =
                FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
                    .getReference("/latest-messages/$toId/$fromId")
            latestMessageToRef.setValue(message)
        }
    }
}

class MessageFromItem(val text: String, val timestamp: Long) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.any_message_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.someone_message_tv.text = text
        viewHolder.itemView.message_time_tv_from.text = getTime(timestamp)
    }

    private fun getTime(time: Long): String? {
        val sdf = SimpleDateFormat("hh:mm a")
        val date = Date(time * 1000)
        return sdf.format(date)
    }
}

class MessageToItem(val text: String, val timestamp: Long) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.my_message_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.my_message_tv.text = text
        viewHolder.itemView.message_time_tv_to.text = getTime(timestamp)
    }

    private fun getTime(time: Long): String? {
        val sdf = SimpleDateFormat("hh:mm a")
        val date = Date(time * 1000)
        return sdf.format(date)
    }
}