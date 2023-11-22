package edu.uw.ischool.rraftery.arewethereyet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener

const val ALARM_ACTION = "edu.uw.ischool.rraftery.ALARM"
class MainActivity : AppCompatActivity() {
    var message : String = ""
    var phone : String = ""
    var minutes : Int = 0
    var phoneIsLegal : Boolean = false
    var minutesIsLegal : Boolean = false
    var receiver : BroadcastReceiver? = null
    lateinit var alarmManager: AlarmManager
    lateinit var repeatPing : PendingIntent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val messageInput = this.findViewById<EditText>(R.id.editTextMessage)
        val phoneInput = this.findViewById<EditText>(R.id.editTextPhone)
        val minutesInput = this.findViewById<EditText>(R.id.editTextMinutes)
        val submit = this.findViewById<Button>(R.id.btnSubmit)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val checkLegality : () -> Unit = {
            submit.isEnabled = isLegal()
        }
        checkLegality()
        messageInput.addTextChangedListener {
            message = it.toString()
        }
        phoneInput.addTextChangedListener {
            val input : String = it.toString()

            Log.i("Phone Input", "Phone number is: $input")
            if(input.isNotEmpty() && input.length == 10){

                //Integer.parseInt(input)
                phone = "(${input.substring(0..2)}) ${input.substring(3..5)}-${input.substring(6..9)}"
                phoneIsLegal = true
            }
            else{
                phoneIsLegal = false
            }
            checkLegality()
        }
        minutesInput.addTextChangedListener {
            val input : String = it.toString()
            if(input.isNotEmpty() && input.toInt() > 0){
                minutes = input.toInt()
                Log.i("Minutes Input", "Num Minutes: $minutes")
                minutesIsLegal = true
            }
            else{
                minutesIsLegal = false
            }
            checkLegality()
        }
        submit.setOnClickListener {
            if(isLegal()){
                repeatAlarm()
                submit.setText("Stop")
                submit.removeCallbacks(checkLegality)
                submit.setOnClickListener {
                    alarmManager.cancel(repeatPing)
                }
            }
        }

    }


    fun isLegal() : Boolean {
        return phoneIsLegal && minutesIsLegal
    }
    fun repeatAlarm() {
        val activityThis = this
//        val smsManager : SmsManager = SmsManager.getDefault()//getSystemService(SmsManager::class.java)
        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                val sms:SmsManager = SmsManager.getDefault()
                val phoneNum : String = phone
                val messageText : String = message
                override fun onReceive(context: Context?, intent: Intent?) {
                    Toast.makeText(activityThis, "$phoneNum: Are we there yet?", Toast.LENGTH_SHORT).show()
                    sms.sendTextMessage(phoneNum, null, messageText, null, null)
                }
            }
            val filter = IntentFilter(ALARM_ACTION)
            registerReceiver(receiver, filter)
        }

        // Create the PendingIntent
        val intent = Intent(ALARM_ACTION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        repeatPing = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            60000 * minutes.toLong(),
            repeatPing)
    }
}