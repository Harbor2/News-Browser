package com.habit.app.helper

import android.content.Context
import android.content.Intent
import android.net.Uri

object FeedbackUtils {
    private const val uriStr: String = "mailto:aleyvoncilyingbal@gmail.com"
    private const val subject: String = "Feedback:"
    private const val message: String = ""
    fun feedback(context: Context) {
        try {
            val uri = Uri.parse(uriStr)
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
            context.startActivity(Intent.createChooser(intent, "Please select your mail client"))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}