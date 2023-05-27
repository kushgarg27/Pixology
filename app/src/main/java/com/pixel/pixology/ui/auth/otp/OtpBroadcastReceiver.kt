import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class OtpBroadcastReceiver(private val onOtpReceived: (String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    val otp = extractOtpFromMessage(message)
                    onOtpReceived.invoke(otp)
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Handle timeout error
                }
            }
        }
    }

    private fun extractOtpFromMessage(message: String): String {
        // Extract OTP from the message using regular expressions or any other method
        // Return the ex val otpRegex = Regex("\\b\\d{6}\\b") // Assuming OTP is a 6-digit number
        val otpRegex = Regex("\\b\\d{6}\\b")
            val matchResult = otpRegex.find(message)
            return matchResult?.value ?: ""
    }}