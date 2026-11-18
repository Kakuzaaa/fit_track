package com.example.fitnesstracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val tagName: String,
    val versionName: String,
    val title: String,
    val changelog: String,
    val downloadUrl: String,
    val apkSize: Long
)

sealed interface UpdateUiState {
    object Idle : UpdateUiState
    object Checking : UpdateUiState
    data class UpToDate(val currentVersion: String) : UpdateUiState
    data class Available(val release: ReleaseInfo, val currentVersion: String) : UpdateUiState
    data class Downloading(val progressPercent: Int, val downloadedMb: Float, val totalMb: Float) : UpdateUiState
    data class ReadyToInstall(val release: ReleaseInfo, val apkFile: File) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

class AppUpdateManager(private val context: Context) {

    private val _state = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    fun getCurrentVersionName(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.4.1"
        } catch (_: Exception) {
            "1.4.1"
        }
    }

    suspend fun checkForUpdates(): UpdateUiState = withContext(Dispatchers.IO) {
        _state.value = UpdateUiState.Checking
        try {
            val url = URL("https://api.github.com/repos/Kakuzaaa/fit_track/releases/latest")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "FitTrack-App")
            }
            if (conn.responseCode != 200) {
                val err = "Ошибка проверки обновлений (код ${conn.responseCode})"
                val newState = UpdateUiState.Error(err)
                _state.value = newState
                return@withContext newState
            }

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tagName = json.getString("tag_name")
            val name = json.optString("name", tagName)
            val releaseNotes = json.optString("body", "")
            val assets = json.optJSONArray("assets")

            var downloadUrl: String? = null
            var apkSize = 0L

            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.getString("name")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.getString("browser_download_url")
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (downloadUrl == null) {
                val newState = UpdateUiState.Error("В последнем релизе не найден файл APK")
                _state.value = newState
                return@withContext newState
            }

            val currentVer = getCurrentVersionName()
            val releaseInfo = ReleaseInfo(
                tagName = tagName,
                versionName = tagName.removePrefix("v").trim(),
                title = name,
                changelog = releaseNotes,
                downloadUrl = downloadUrl,
                apkSize = apkSize
            )

            val isNewer = isNewerVersion(releaseInfo.versionName, currentVer)
            val newState = if (isNewer) {
                val updatesDir = File(context.cacheDir, "updates")
                val targetFile = File(updatesDir, "FitTrack-${releaseInfo.tagName}.apk")
                if (targetFile.exists() && targetFile.length() == apkSize && apkSize > 0) {
                    UpdateUiState.ReadyToInstall(releaseInfo, targetFile)
                } else {
                    UpdateUiState.Available(releaseInfo, currentVer)
                }
            } else {
                UpdateUiState.UpToDate(currentVer)
            }
            _state.value = newState
            newState
        } catch (e: Exception) {
            val msg = if (e is java.net.UnknownHostException) {
                "Нет подключения к интернету"
            } else {
                "Не удалось проверить обновления: ${e.message ?: "ошибка сети"}"
            }
            val newState = UpdateUiState.Error(msg)
            _state.value = newState
            newState
        }
    }

    suspend fun downloadUpdate(release: ReleaseInfo): Result<File> = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val targetFile = File(updatesDir, "FitTrack-${release.tagName}.apk")

        if (targetFile.exists() && targetFile.length() == release.apkSize && release.apkSize > 0) {
            _state.value = UpdateUiState.ReadyToInstall(release, targetFile)
            return@withContext Result.success(targetFile)
        }

        try {
            var currentUrl = release.downloadUrl
            var connection: HttpURLConnection? = null
            var redirects = 0
            while (true) {
                val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "FitTrack-App")
                    instanceFollowRedirects = true
                }
                val code = conn.responseCode
                if (code in 300..399 && redirects < 5) {
                    val location = conn.getHeaderField("Location")
                    if (location == null) {
                        connection = conn
                        break
                    }
                    conn.disconnect()
                    currentUrl = location
                    redirects++
                } else {
                    connection = conn
                    break
                }
            }

            val activeConn = connection ?: error("Не удалось установить соединение")
            val totalBytes = if (release.apkSize > 0) release.apkSize else activeConn.contentLength.toLong()
            val totalMb = (totalBytes / (1024f * 1024f))

            val tempFile = File(updatesDir, "download_temp.apk")
            if (tempFile.exists()) tempFile.delete()

            activeConn.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var bytesRead: Int
                    var totalDownloaded = 0L
                    var lastReportPercent = -1

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead

                        val percent = if (totalBytes > 0) ((totalDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0
                        if (percent != lastReportPercent) {
                            lastReportPercent = percent
                            val downloadedMb = (totalDownloaded / (1024f * 1024f))
                            _state.value = UpdateUiState.Downloading(percent, downloadedMb, totalMb)
                        }
                    }
                    output.flush()
                }
            }

            if (tempFile.renameTo(targetFile) || (targetFile.delete() && tempFile.renameTo(targetFile))) {
                _state.value = UpdateUiState.ReadyToInstall(release, targetFile)
                Result.success(targetFile)
            } else {
                _state.value = UpdateUiState.ReadyToInstall(release, tempFile)
                Result.success(tempFile)
            }
        } catch (e: Exception) {
            val msg = "Ошибка при загрузке: ${e.message ?: "сбой соединения"}"
            _state.value = UpdateUiState.Error(msg)
            Result.failure(e)
        }
    }

    fun installUpdate(apkFile: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return false
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            _state.value = UpdateUiState.Error("Не удалось запустить установку: ${e.message}")
            false
        }
    }

    companion object {
        fun isNewerVersion(latestVer: String, currentVer: String): Boolean {
            val v1 = latestVer.removePrefix("v").trim().split(".").mapNotNull { it.toIntOrNull() }
            val v2 = currentVer.removePrefix("v").trim().split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(v1.size, v2.size)
            for (i in 0 until maxLen) {
                val num1 = v1.getOrElse(i) { 0 }
                val num2 = v2.getOrElse(i) { 0 }
                if (num1 > num2) return true
                if (num1 < num2) return false
            }
            return false
        }
    }
}
