package at.interactivecuriosity.imagedownload

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import at.interactivecuriosity.imagedownload.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var downloadButton: Button
    private lateinit var deleteButton: Button
    private val imageUrl = "https://www.markusmaurer.at/fhj/eyecatcher.jpg" // URL des herunterzuladenden Bildes
    private val fileName = "downloadedImage.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        downloadButton = findViewById(R.id.downloadButton)
        deleteButton = findViewById(R.id.deleteButton)

        downloadButton.setOnClickListener {
            downloadImage(imageUrl, fileName)
        }

        deleteButton.setOnClickListener {
            deleteImage(fileName)
        }

        val filter = IntentFilter().apply {
            addAction(DownloadService.DOWNLOAD_COMPLETE)
            addAction(DownloadService.DOWNLOAD_FAILED)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, filter)
    }

    private fun downloadImage(urlString: String, fileName: String) {
        val intent = Intent(this, DownloadService::class.java).apply {
            putExtra(DownloadService.URL_KEY, urlString)
            putExtra(DownloadService.FILENAME_KEY, fileName)
        }
        startService(intent)
    }

    private fun deleteImage(fileName: String) {
        val file = File(getExternalFilesDir(null), fileName)
        if (file.exists()) {
            file.delete()
            runOnUiThread {
                imageView.setImageBitmap(null)
                Toast.makeText(this, "Bild gelöscht", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                DownloadService.DOWNLOAD_COMPLETE -> {
                    val filePath = File(getExternalFilesDir(null), fileName).absolutePath
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    imageView.setImageBitmap(bitmap)
                    Toast.makeText(this@MainActivity, "Bild heruntergeladen",
                        Toast.LENGTH_SHORT).show()
                }
                DownloadService.DOWNLOAD_FAILED -> {
                    imageView.setImageBitmap(null)
                    Toast.makeText(this@MainActivity, "Fehler beim Herunterladen",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
