package com.example.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class UpdateManager(private val context: Context) {

    private var downloadId: Long = -1L
    private val downloadApkName = "update.apk"
    private val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)

    fun getUpdateUrl(): String {
        return prefs.getString("update_url", "https://github.com/saykun/ZikirmatikApp/releases/latest/download/app-release.apk")
            ?: "https://github.com/saykun/ZikirmatikApp/releases/latest/download/app-release.apk"
    }

    fun setUpdateUrl(url: String) {
        prefs.edit().putString("update_url", url).apply()
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId && context != null) {
                checkDownloadStatus(context)
                context.unregisterReceiver(this)
            }
        }
    }

    fun checkForUpdateAndDownload(updateUrl: String = getUpdateUrl()) {
        Toast.makeText(context, "Güncelleme kontrol ediliyor...", Toast.LENGTH_SHORT).show()

        // Clean up old apk file if exists
        val updateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (updateDir != null) {
            val apkFile = File(updateDir, downloadApkName)
            if (apkFile.exists()) {
                apkFile.delete()
            }
        }

        val request = try {
            DownloadManager.Request(Uri.parse(updateUrl))
                .setTitle("Zikirmatik Güncelleme")
                .setDescription("Yeni sürüm indiriliyor...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, downloadApkName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        } catch (e: Exception) {
            Toast.makeText(context, "Geçersiz güncelleme linki: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )

        try {
            downloadId = downloadManager.enqueue(request)
            Toast.makeText(context, "İndirme başlatıldı. Bildirimlerden takip edebilirsiniz.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "İndirme başlatılamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkDownloadStatus(context: Context) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor != null && cursor.moveToFirst()) {
            val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = if (statusColumnIndex != -1) cursor.getInt(statusColumnIndex) else -1
            
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                installApk()
            } else if (status == DownloadManager.STATUS_FAILED) {
                val reasonColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val reason = if (reasonColumnIndex != -1) cursor.getInt(reasonColumnIndex) else -1
                val errorMessage = when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME -> "Bağlantı kesildi, devam ettirilemiyor."
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Depolama aygıtı bulunamadı."
                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "Dosya zaten mevcut."
                    DownloadManager.ERROR_FILE_ERROR -> "Dosya erişim hatası."
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> "Ağ veri aktarım hatası."
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Yetersiz depolama alanı."
                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Çok fazla yönlendirme hatası."
                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Sunucu hatası veya geçersiz URL (404/500/Boş)."
                    DownloadManager.ERROR_UNKNOWN -> "Bilinmeyen bir hata oluştu."
                    else -> "Hata kodu: $reason"
                }
                Toast.makeText(context, "İndirme Başarısız: $errorMessage\nGüncelleme adresini Ayarlar kısmından değiştirebilirsiniz.", Toast.LENGTH_LONG).show()
            }
            cursor.close()
        } else {
            Toast.makeText(context, "İndirme durumu kontrol edilemedi.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installApk() {
        val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), downloadApkName)
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
            Toast.makeText(context, "Güncelleme yüklenemedi: ${e.message}\nLütfen bilinmeyen kaynaklardan yükleme izni verdiğinizden emin olun.", Toast.LENGTH_LONG).show()
        }
    }
}
