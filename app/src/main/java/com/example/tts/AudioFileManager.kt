package com.example.tts

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioFileManager(private val context: Context) {
    private val client = OkHttpClient()

    /**
     * Downloads all audio chunks, merges them into one MP3 file,
     * and saves it to the public Download directory.
     */
    suspend fun downloadAndSaveAudio(
        audioUrls: List<String>,
        baseTitle: String = "Sedayar_AI"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (audioUrls.isEmpty()) {
                return@withContext Result.failure(Exception("آدرس صوتی برای دانلود یافت نشد."))
            }

            val mergedOutputStream = ByteArrayOutputStream()

            for (url in audioUrls) {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("خطا در دریافت فایل صوتی از سرور."))
                    }
                    response.body?.byteStream()?.copyTo(mergedOutputStream)
                }
            }

            val mergedBytes = mergedOutputStream.toByteArray()
            if (mergedBytes.isEmpty()) {
                return@withContext Result.failure(Exception("فایل صوتی نهایی خالی است."))
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${baseTitle}_$timeStamp.mp3"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("امکان ایجاد فایل در پوشه دانلود وجود ندارد."))

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(mergedBytes)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { it.write(mergedBytes) }
            }

            Log.i("AudioFileManager", "Successfully saved audio to Downloads: $fileName")
            Result.success(fileName)
        } catch (e: Exception) {
            Log.e("AudioFileManager", "Error saving audio to Downloads", e)
            Result.failure(e)
        }
    }
}
