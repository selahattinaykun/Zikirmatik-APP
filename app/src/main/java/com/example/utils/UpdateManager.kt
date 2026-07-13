package com.example.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class UpdateManager(private val context: Context) {

    private var downloadId: Long = -1L
    private val downloadApkName = "update.apk"

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                installApk()
                context?.unregisterReceiver(this)
            }
        }
    }

    fun checkForUpdateAndDownload(updateUrl: String = "https://github.com/saykun/ZikirmatikApp/releases/latest/download/app-release.apk") {
        Toast.makeText(context, "Güncelleme kontrol ediliyor ve indiriliyor...", Toast.LENGTH_SHORT).show()

        // Create update directory if needed
        val updateDir = File(context.getExternalFilesDir(null), "update")
        if (!updateDir.exists()) {
            updateDir.mkdirs()
        }

        val apkFile = File(updateDir, downloadApkName)
        if (apkFile.exists()) {
            apkFile.delete()
        }

        val request = DownloadManager.Request(Uri.parse(updateUrl))
            .setTitle("Zikirmatik Güncelleme")
            .setDescription("Yeni sürüm indiriliyor...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, "update", downloadApkName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        context.registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )

        downloadId = downloadManager.enqueue(request)
    }

    private fun installApk() {
        val apkFile = File(context.getExternalFilesDir(null), "update/$downloadApkName")
        if (!apkFile.exists()) {
            Toast.makeText(context, "İndirilen APK bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Güncelleme yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
