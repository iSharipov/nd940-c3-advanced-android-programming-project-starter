package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import com.udacity.ButtonState.Completed
import com.udacity.ButtonState.Loading
import com.udacity.R.id
import com.udacity.R.layout
import com.udacity.R.string
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    @RequiresApi(VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createNotificationChannel(this)

        loading_button.setOnClickListener {
            when (radio.checkedRadioButtonId) {
                id.radio_glide -> {
                    download(GLIDE)
                    loading_button.setState(Loading)
                }
                id.radio_load_app -> {
                    download(UDACITY)
                    loading_button.setState(Loading)
                }
                id.radio_retrofit -> {
                    download(RETROFIT)
                    loading_button.setState(Loading)
                }
                else -> {
                    Toast.makeText(
                        this,
                        getString(string.time_to_choose),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            loading_button.setState(Completed)
            DownloadStateRetriever(context, id, getSystemService(DOWNLOAD_SERVICE) as DownloadManager).retrieve()
        }
    }

    private fun download(uri: String) {
        val request =
            DownloadManager.Request(Uri.parse(uri))
                .setDescription(getString(string.downloading))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        const val UDACITY =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        const val GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        const val RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    @RequiresApi(VERSION_CODES.O)
    fun createNotificationChannel(context: Context) {
        val mChannel = NotificationChannel(context.getString(string.channel_id), getString(string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
        mChannel.description = getString(string.channel_description)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}

class DownloadStateRetriever(private val context: Context, private val downloadId: Long, private val downloadManager: DownloadManager) {

    fun retrieve() {
        val query = DownloadManager.Query()
            .setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val status = cursor.getInt(
                cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            )
            val fileName = cursor.getString(
                cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            )
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    notify(
                        context,
                        downloadId.toInt(),
                        DownloadManager.STATUS_SUCCESSFUL,
                        fileName
                    )
                }
                DownloadManager.STATUS_FAILED -> {
                    notify(
                        context,
                        downloadId.toInt(),
                        DownloadManager.STATUS_FAILED,
                        fileName
                    )
                }
            }
        }
    }

    private fun notify(context: Context, downloadId: Int, downloadStatus: Int, fileName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifyIntent = Intent(context, DetailActivity::class.java)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        notifyIntent.putExtras(Bundle().apply {
            putInt(context.getString(string.download_id), downloadId)
            putInt(context.getString(string.download_status), downloadStatus)
            putString(context.getString(string.file_name_extra), fileName)
        })

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, context.getString(string.channel_id))
            .setContentTitle(fileName)
            .setContentText(
                if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    context.getString(string.completed)
                } else {
                    context.getString(string.failed)
                }
            )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setAutoCancel(true)
            .setColor(context.getColor(R.color.colorAccent))
            .setLights(context.getColor(R.color.colorAccent), 1000, 3000)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(PRIORITY_MAX)
            .addAction(
                NotificationCompat.Action(
                    0,
                    context.getString(string.dive_into),
                    pendingIntent
                )
            )
            .build()

        notificationManager.notify(downloadId, notification)
    }
}