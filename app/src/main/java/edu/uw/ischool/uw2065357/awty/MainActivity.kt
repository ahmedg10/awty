package edu.uw.ischool.uw2065357.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.telephony.PhoneNumberFormattingTextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var phoneNumber: EditText
    private lateinit var message: EditText
    private lateinit var interval: EditText
    private lateinit var startBtn: Button
    private lateinit var alarmManager: AlarmManager

    // Add this variable to the class to store handlers for each phone number
    private val handlerMap = HashMap<String, Handler>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Rename variables to match the original code
        phoneNumber = findViewById(R.id.editPhoneNumber)
        message = findViewById(R.id.editMessage)
        interval = findViewById(R.id.editInterval)
        startBtn = findViewById(R.id.btnStartStop)

        // Use the renamed variables and functions
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        phoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        startBtn.setOnClickListener {
            if (isValidPhone(phoneNumber) && isValidInterval(interval)) {
                val numMessage = formatPhoneNumber(phoneNumber.text.toString())
                val numInterval = interval.text.toString().toInt()

                val intent = Intent("edu.uw.ischool.uw2065357.awty")
                intent.putExtra("message", message.text.toString())
                intent.putExtra("phone", numMessage)

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                val intentFilter = IntentFilter("edu.uw.ischool.uw2065357.awty")
                registerReceiver(AlarmReceiver(), intentFilter)

                if (startBtn.text == "Start") {
                    alarmManager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + numInterval.toLong() * (60 * 1000),
                        numInterval.toLong() * (60 * 1000),
                        pendingIntent
                    )

                    showToastDuringInterval(
                        numInterval * 60 * 1000,
                        numMessage,
                        message.text.toString()
                    )

                    startBtn.text = "Stop"
                } else {
                    startBtn.text = "Start"
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }

    private fun isValidInterval(interval: EditText): Boolean {
        return !(interval.text.toString().toInt() == 0 || interval.text.toString().toInt() < 0 || interval.text.toString() == "")
    }

    private fun isValidPhone(phoneNumber: EditText): Boolean {
        return !(phoneNumber.text.toString() == "" || phoneNumber.text.toString().length != 14)
    }

    private fun formatPhoneNumber(number: String): String {
        return "(${number.substring(0, 3)}) ${number.substring(3, 6)}-${number.substring(6, 10)}"
    }

    private fun showToastDuringInterval(intervalMillis: Int, phoneNumber: String, message: String) {
        val showToastRunnable = object : Runnable {
            override fun run() {
                // Create custom Toast
                val toastView = layoutInflater.inflate(R.layout.custom_toast, null)
                val toastCaption = toastView.findViewById<TextView>(R.id.toastCaption)
                val toastMessage = toastView.findViewById<TextView>(R.id.toastMessage)

                toastCaption.text = "Texting $phoneNumber"
                toastMessage.text = message

                val toast = Toast(applicationContext)
                toast.duration = Toast.LENGTH_SHORT
                toast.view = toastView
                toast.show()

                handler.postDelayed(this, intervalMillis.toLong())
            }
        }

        handler.postDelayed(showToastRunnable, intervalMillis.toLong())
    }

    // Override onDestroy to remove all callbacks
    override fun onDestroy() {
        super.onDestroy()
        for (handler in handlerMap.values) {
            handler.removeCallbacksAndMessages(null)
        }
    }
}

