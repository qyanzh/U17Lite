package com.example.u17lite.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.u17lite.R
import com.example.u17lite.activities.DownloadActivity
import com.example.u17lite.dataBeans.*
import com.example.u17lite.isWebConnect
import net.lingala.zip4j.core.ZipFile
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile


class DownloadService : Service() {

    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null
    lateinit var downloadDao: DownloadDao
    lateinit var comicDao: ComicDao
    lateinit var chapterDao: ChapterDao
    var networkConnected = true

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d("MyService", "handleMessage")
            val db = getDatabase(this@DownloadService)
            comicDao = db.comicDao()
            chapterDao = db.chapterDao()
            downloadDao = db.downloadDao()
            while (networkConnected) {
                Log.d("MyService", "while")
                downloadDao.getNext()?.let {
                    downloadFile(it)
                } ?: return
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onCreate() {
        Log.d("MyService", "onCreate")
        val thread = HandlerThread(
            "ServiceStartArguments",
            THREAD_PRIORITY_BACKGROUND
        )
        thread.start()
        mServiceLooper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLooper!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyService", "onStartCommand")
        val msg = mServiceHandler!!.obtainMessage()
        msg.arg1 = startId
        mServiceHandler!!.sendMessage(msg)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("MyService", "onDestroy")
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }


    fun downloadFile(downloadItem: DownloadItem) {
        Log.d("MyService", "downloadFile")
        var inputStream: InputStream? = null
        var savedFile: RandomAccessFile? = null
        try {
            var downloadedLength: Long = 0
            val downloadUrl = downloadItem.url
            val fileName = "${downloadItem.chapterId}.zip"
            val directory =
                getExternalFilesDir("zip").absolutePath + File.separator + downloadItem.comicId
            File(directory).mkdirs()
            file = File(directory + File.separator + fileName)
            if (file!!.exists()) {
                downloadedLength = file!!.length()
            }
            val contentLength = getContentLength(downloadUrl)
            if (contentLength == 0L) {
                return onPostExecute(TYPE_FAILED)
            } else if (contentLength == downloadedLength) {
                return onPostExecute(TYPE_SUCCESS)
            }
            startForeground(1, getNotification("正在下载漫画..", 0))
            Thread.sleep(10000)
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("RANGE", "bytes=$downloadedLength-")
                .url(downloadUrl)
                .build()
            val response = client.newCall(request).execute()
            inputStream = response.body()!!.byteStream()
            savedFile = RandomAccessFile(file, "rw")
            savedFile.seek(downloadedLength)
            val b = ByteArray(1024)
            var total = 0
            var len = inputStream.read(b)
            while (len != -1) {
                when {
                    isCanceled -> return onPostExecute(TYPE_CANCELED)
                    isPaused -> return onPostExecute(TYPE_PAUSED)
                    else -> {
                        total += len
                        savedFile.write(b, 0, len)
                        val progress = ((total + downloadedLength) * 100 / contentLength).toInt()
                        onProgressUpdate(progress)
                    }
                }
                len = inputStream.read(b)
            }
            response.body()!!.close()
            return onPostExecute(TYPE_SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                savedFile?.close()
                if (isCanceled && file != null) {
                    file!!.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return onPostExecute(TYPE_FAILED)
    }

    fun onProgressUpdate(vararg values: Int?) {
        val progress = values[0]
        if (progress!! > lastProgress) {
            onPostExecute(TYPE_PROGRESS)
            lastProgress = progress
        }
    }


    private fun onPostExecute(type: Int) {
        when (type) {
            TYPE_SUCCESS -> onSuccess()
            TYPE_FAILED -> onFailed()
            TYPE_PAUSED -> onPaused()
            TYPE_CANCELED -> onCanceled()
            TYPE_PROGRESS -> onProgress()
            else -> {
            }
        }
    }


    fun pauseDownload() {
        isPaused = true
    }

    fun cancelDownload() {
        isCanceled = true
    }

    private fun getContentLength(downloadUrl: String): Long {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(downloadUrl)
            .build()
        val response = client.newCall(request).execute()
        if (response != null && response.isSuccessful) {
            val contentLength = response.body()!!.contentLength()
            response.close()
            return contentLength
        }
        return 0
    }

    private var isCanceled = false
    private var isPaused = false
    private var lastProgress: Int = 0
    var file: File? = null

    fun onProgress() {
        getNotificationManager().notify(1, getNotification("正在下载漫画...", lastProgress))
    }

    fun onSuccess() {
        downloadDao.getNext()?.let { item ->
            val extractPath =
                getExternalFilesDir("imgs").absolutePath + File.separator + item.comicId + File.separator + item.chapterId
            File(extractPath).mkdirs()
            ZipFile(file).extractAll(extractPath)
            downloadDao.delete(item)
            sendBroadcast(Intent(RECIEVE).putExtra("msg", "success"))
        }
        stopForeground(true)
        getNotificationManager().notify(1, getNotification("下载成功", -1))
//        Toast.makeText(this, "Download Success", Toast.LENGTH_SHORT).show()
    }

    fun onFailed() {
        stopForeground(true)
        downloadDao.getNext()?.let {
            if (isWebConnect(this)) {
                downloadDao.delete(it)
                sendBroadcast(Intent(RECIEVE).putExtra("msg", "failed"))
            } else {
                networkConnected = false
            }
        }
        getNotificationManager().notify(1, getNotification("下载失败", -1))
//        Toast.makeText(this, "Download Failed", Toast.LENGTH_SHORT).show()

    }

    fun onPaused() {
        //TODO:暂停下载
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()
    }

    fun onCanceled() {
        //TODO:取消下载
        stopForeground(true)
        Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
    }

    private fun getNotification(
        title: String,
        progress: Int,
        description: String = ""
    ): Notification {
        val builder = NotificationCompat.Builder(this, "Download")
        builder.setSmallIcon(R.drawable.ic_download_white)
        // builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title)
        // builder.setContentText(description)
        builder.setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, DownloadActivity::class.java),
                0
            )
        )
        if (progress > 0) {
            builder.setContentText("$progress%")
            builder.setProgress(100, progress, false)
        }
        return builder.build()
    }

    private fun getNotificationManager(): NotificationManager {
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        val RECIEVE = "com.example.u17lite.services.RECIEVE"
        const val TYPE_SUCCESS: Int = 0
        const val TYPE_FAILED = 1
        const val TYPE_PAUSED = 2
        const val TYPE_CANCELED = 3
        const val TYPE_PROGRESS = 4
    }

}
