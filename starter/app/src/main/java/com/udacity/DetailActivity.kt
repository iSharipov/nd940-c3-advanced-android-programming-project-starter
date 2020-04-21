package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private var downloadId = -1
    private lateinit var downloadStatus: String
    private lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        clearNotification(this, downloadId)

        intent.extras?.let {
            downloadId = it.getInt(getString(R.string.channel_id))
            downloadStatus = it.getInt(getString(R.string.download_status)).toString()
            fileName = it.getString(getString(R.string.file_name_extra))!!
        }
        file_name.text = fileName

        status.text = if (downloadStatus.toInt() == DownloadManager.STATUS_SUCCESSFUL) {
            getString(R.string.success)
        } else {
            getString(R.string.fail)
        }

        transition.transitionToEnd()
        back_button.setOnClickListener {
            finish()
        }
    }

    private fun clearNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(notificationId)
    }

}
