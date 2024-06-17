package at.interactivecuriosity.imagedownload

import android.app.IntentService
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadService : IntentService("DownloadService") {

    companion object {
        const val URL_KEY = "url"
        const val FILENAME_KEY = "filename"
        const val DOWNLOAD_COMPLETE = "at.interactivecuriosity.imagedownload.DOWNLOAD_COMPLETE"
        const val DOWNLOAD_FAILED = "at.interactivecuriosity.imagedownload.DOWNLOAD_FAILED"
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null){
            return
        }

        val urlString = intent.getStringExtra(URL_KEY)
        val fileName = intent.getStringExtra(FILENAME_KEY)
        downloadImage(urlString!!, fileName!!)
    }


    private fun downloadImage(urlString: String, fileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val file = File(getExternalFilesDir(null), fileName)
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
                broadcastDownloadComplete(fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                broadcastDownloadFailed()
            }
        }
    }


    private fun broadcastDownloadComplete(filename: String?) {
        val intent = Intent(DOWNLOAD_COMPLETE)
        intent.putExtra(FILENAME_KEY, filename)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastDownloadFailed() {
        val intent = Intent(DOWNLOAD_FAILED)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}