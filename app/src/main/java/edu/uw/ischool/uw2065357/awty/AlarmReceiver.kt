package edu.uw.ischool.uw2065357.awty

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("working", "alarm recieved")
        val number = intent?.getStringExtra("number")
        val message = intent?.getStringExtra("message")
        Toast.makeText(context, "Texting{$number}: $message", Toast.LENGTH_SHORT).show()
    }
}




