package com.coopdev.pushautomatic.model

data class ScheduledNotification(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val title: String,
    val message: String,
    val enabled: Boolean
) 