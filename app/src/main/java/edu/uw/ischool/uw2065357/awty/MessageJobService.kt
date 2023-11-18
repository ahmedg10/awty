package edu.uw.ischool.uw2065357.awty

// MessageService.kt
import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class MessageJobService : IntentService("MessageService") {

    companion object {
        const val EXTRA_MESSAGE = "extra_message"
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onHandleIntent(intent: Intent?) {
        val message = intent?.getStringExtra(EXTRA_MESSAGE)

        showToast(message ?: "Default Message")
    }

    private fun showToast(message: String) {
        handler.post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}


