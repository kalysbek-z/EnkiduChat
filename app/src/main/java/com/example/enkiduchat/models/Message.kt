package com.example.enkiduchat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Message(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: Long
) : Parcelable {
    constructor() : this("", "", "", "", -1)
}