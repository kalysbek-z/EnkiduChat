package com.example.enkiduchat.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.enkiduchat.R
import com.example.enkiduchat.login.LoginActivity
import com.example.enkiduchat.login.MainActivity
import com.example.enkiduchat.models.Message
import com.example.enkiduchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_lastest_messages.*
import kotlinx.android.synthetic.main.latest_message_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class LastestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lastest_messages)

        recycler_v_latest_m.adapter = adapter
        recycler_v_latest_m.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val thisItem = item as LatestMessages
            intent.putExtra("user_key", thisItem.messageSender)
            startActivity(intent)
        }

        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
            .getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    val latestMessagesMap = HashMap<String, Message>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessages(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
            .getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return

                latestMessagesMap[p0.key!!] = message
                refreshRecyclerViewMessages()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return

                latestMessagesMap[p0.key!!] = message
                refreshRecyclerViewMessages()

                recycler_v_latest_m.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, UserListActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

class LatestMessages(val message: Message) : Item<ViewHolder>() {
    var messageSender: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val messageSenderId: String
        if (message.fromId == FirebaseAuth.getInstance().uid) {
            messageSenderId = message.toId
        } else {
            messageSenderId = message.fromId
        }

        val ref =
            FirebaseDatabase.getInstance("https://enkiduchat-default-rtdb.firebaseio.com/")
                .getReference("/users/$messageSenderId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                messageSender = p0.getValue(User::class.java)
                viewHolder.itemView.latest_m_username.text = messageSender?.username
                viewHolder.itemView.latest_m_time.text = getTime(message?.timestamp)
            }
        })

        viewHolder.itemView.latest_message_tv.text = message.text
    }

    private fun getTime(time: Long): String? {
        val sdf = SimpleDateFormat("hh:mm a")
        val date = Date(time * 1000)
        return sdf.format(date)
    }
}