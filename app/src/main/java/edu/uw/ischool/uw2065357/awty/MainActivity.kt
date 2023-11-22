package edu.uw.ischool.uw2065357.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    companion object {
        const val ALARM_ACTION = "edu.uw.ischool.uw2065357.awty.ALARM"
    }
    private lateinit var phoneNumber: EditText
    private lateinit var message: EditText
    private lateinit var interval: EditText
    private lateinit var startBtn: Button
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phoneNumber = findViewById(R.id.editPhoneNumber)
        message = findViewById(R.id.editMessage)
        interval = findViewById(R.id.editInterval)
        startBtn = findViewById(R.id.btnStartStop)

        setupTextWatchers()

        startBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
            } else {
                toggleNag()
            }
        }
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            when (it.getStringExtra("task")) {
                "sendAudioMMS" -> sendAudioMMS(it.getStringExtra("phoneNumber") ?: "")
                "sendVideoMMS" -> sendVideoMMS(it.getStringExtra("phoneNumber") ?: "")
            }
        }
    }

    // I could not figure out how to implement this gave my best shot and filed with a dummy file

    private fun sendAudioMMS(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*" // MIME type for audio
            putExtra("address", phoneNumber)
            putExtra(Intent.EXTRA_STREAM, Uri.parse("file://path/to/your/audio/file")) // Path to your audio file
        }
        startActivity(Intent.createChooser(intent, "Send Audio MMS..."))
    }


    // I could not figure out how to implement this gave my best shot and filed with a dummy file
    private fun sendVideoMMS(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*" // MIME type for video
            putExtra("address", phoneNumber)
            putExtra(Intent.EXTRA_STREAM, Uri.parse("file://path/to/your/video/file")) // Path to your video file
        }
        startActivity(Intent.createChooser(intent, "Send Video MMS..."))
    }


    private fun setupTextWatchers() {
        val phoneTextWatcher = object : TextWatcher {
            private var isFormatting = false
            private var oldStr = ""

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true

                val newStr = formatPhoneNumber(s.toString())
                s.replace(0, s.length, newStr)

                isFormatting = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldStr = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        phoneNumber.addTextChangedListener(phoneTextWatcher)

        val generalTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phoneValid = phoneNumber.text.isNotEmpty() && isValidPhoneNumber(phoneNumber.text.toString())
                val messageValid = message.text.isNotEmpty()
                val intervalValid = interval.text.isNotEmpty() && interval.text.toString().toIntOrNull() != null

                startBtn.isEnabled = phoneValid && messageValid && intervalValid
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        message.addTextChangedListener(generalTextWatcher)
        interval.addTextChangedListener(generalTextWatcher)
    }

    private fun formatPhoneNumber(phone: String): String {
        // Remove all non-digit characters
        val digits = phone.filter { it.isDigit() }

        // Format based on the length of the digit-only string
        return when {
            digits.length >= 6 -> "(${digits.substring(0, 3)})${digits.substring(3, 6)}-${digits.substring(6)}"
            digits.length > 3 -> "(${digits.substring(0, 3)})${digits.substring(3)}"
            else -> "($digits"
        }
    }


    private fun toggleNag() {
        Log.d("NagFunction", "toggleNag called. PendingIntent is ${if (pendingIntent == null) "null" else "not null"}")
        if (pendingIntent == null) {
            startNag()
        } else {
            stopNag()
        }
    }

    private fun startNag() {
        Log.d("NagFunction", "Starting Nag")
        startBtn.text = "Stop"

        // Register the BroadcastReceiver if it hasn't been registered yet
        if (receiver == null) {
            receiver = SmsBroadcastReceiver()
            val filter = IntentFilter(ALARM_ACTION)
            registerReceiver(receiver, filter)
        }

        // Create an Intent with the custom action
        val intent = Intent(ALARM_ACTION).apply {
            putExtra("phoneNumber", phoneNumber.text.toString())
            putExtra("message", message.text.toString())
        }

        // Create the PendingIntent with the custom action intent
        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        pendingIntent = alarmIntent

        // Get the alarm manager and set the repeating alarm
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intervalMillis = interval.text.toString().toLong() * 60000 // Convert minutes to milliseconds

        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            intervalMillis,
            alarmIntent
        )

        Log.d("NagFunction", "Alarm Set for every $intervalMillis milliseconds")
    }


    private fun stopNag() {
        Log.d("NagFunction", "Stopping Nag")
        startBtn.text = "Start"
        pendingIntent?.let {
            alarmManager?.cancel(it)
            it.cancel()
        }
        pendingIntent = null
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toggleNag()
        }
    }

    override fun onDestroy() {
        receiver?.let { unregisterReceiver(it) }
        super.onDestroy()
        stopNag()
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Regex for phone number in format (XXX)XXX-XXXX
        val phoneRegex = Regex("\\(\\d{3}\\)\\d{3}-\\d{4}")
        return phone.matches(phoneRegex)
    }
}

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val phoneNumber = intent?.getStringExtra("phoneNumber") ?: return
        val message = intent?.getStringExtra("message") ?: return
        val messageType = intent.getStringExtra("messageType") ?: "sms"

        if (context != null) {
            when (messageType) {
                "sms" -> {
                    Log.d("SmsBroadcastReceiver", "Sending SMS to $phoneNumber: $message")
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                }

                "audio", "video" -> {
                    val newIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(
                            "task",
                            if (messageType == "audio") "sendAudioMMS" else "sendVideoMMS"
                        )
                        putExtra("phoneNumber", phoneNumber)
                    }
                    context.startActivity(newIntent)
                }
            }
        }
    }
}


