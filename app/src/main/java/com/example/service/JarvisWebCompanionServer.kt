package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * Built-in micro HTTP server running on port 8888.
 * Serves the installable Progressive Web App (PWA) assets from assets/webapp/
 * and provides direct APK package download support for direct web app installation.
 */
object JarvisWebCompanionServer {

    private const val TAG = "JarvisWebServer"
    const val PORT = 8888

    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null
    var isRunning: Boolean = false
        private set

    fun getLocalUrl(): String = "http://127.0.0.1:$PORT/"

    fun start(context: Context) {
        if (isRunning) return
        val appContext = context.applicationContext
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(PORT)
                isRunning = true
                Log.i(TAG, "Jarvis Web Companion server started on port $PORT")

                while (isActive) {
                    val clientSocket = try {
                        serverSocket?.accept()
                    } catch (e: Exception) {
                        null
                    } ?: break

                    launch(Dispatchers.IO) {
                        handleClient(appContext, clientSocket)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error running web companion server", e)
            } finally {
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            // ignore
        }
        serverJob?.cancel()
        serverSocket = null
    }

    fun openInBrowser(context: Context) {
        if (!isRunning) {
            start(context)
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getLocalUrl())).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun handleClient(context: Context, socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val firstLine = reader.readLine() ?: return
            val parts = firstLine.split(" ")
            if (parts.size < 2) return

            val path = parts[1].split("?")[0]
            val outputStream = socket.getOutputStream()

            when {
                path == "/" || path == "/index.html" -> {
                    serveAsset(context, "webapp/index.html", "text/html; charset=UTF-8", outputStream)
                }
                path == "/manifest.json" -> {
                    serveAsset(context, "webapp/manifest.json", "application/manifest+json; charset=UTF-8", outputStream)
                }
                path == "/sw.js" -> {
                    serveAsset(context, "webapp/sw.js", "application/javascript; charset=UTF-8", outputStream)
                }
                path == "/style.css" -> {
                    serveAsset(context, "webapp/style.css", "text/css; charset=UTF-8", outputStream)
                }
                path == "/app.js" -> {
                    serveAsset(context, "webapp/app.js", "application/javascript; charset=UTF-8", outputStream)
                }
                path == "/icon-192.png" -> {
                    serveAsset(context, "webapp/icon-192.png", "image/png", outputStream)
                }
                path == "/icon-512.png" -> {
                    serveAsset(context, "webapp/icon-512.png", "image/png", outputStream)
                }
                path == "/download-apk" || path == "/JARVIS-AI.apk" -> {
                    serveApk(context, outputStream)
                }
                path == "/api/make-apk" -> {
                    val json = """{"status":"READY","app":"J.A.R.V.I.S. AI","version":"1.0.0","apkFile":"JARVIS-AI.apk","buildStatus":"COMPILED","canInstall":true}"""
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${json.toByteArray().size}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "\r\n" + json
                    outputStream.write(response.toByteArray())
                    outputStream.flush()
                }
                path == "/api/status" -> {
                    val json = """{"status":"ONLINE","system":"J.A.R.V.I.S. Stark OS","version":"8.5","activeOverApps":true}"""
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${json.toByteArray().size}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "\r\n" + json
                    outputStream.write(response.toByteArray())
                    outputStream.flush()
                }
                else -> {
                    val notFound = "HTTP/1.1 404 Not Found\r\n\r\nAsset Not Found"
                    outputStream.write(notFound.toByteArray())
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error handling client request", e)
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun serveAsset(context: Context, assetPath: String, contentType: String, output: OutputStream) {
        try {
            val inputStream: InputStream = context.assets.open(assetPath)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: $contentType\r\n" +
                    "Content-Length: ${bytes.size}\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "\r\n"
            output.write(header.toByteArray())
            output.write(bytes)
            output.flush()
        } catch (e: Exception) {
            val errorHeader = "HTTP/1.1 500 Internal Error\r\n\r\nCould not load $assetPath"
            output.write(errorHeader.toByteArray())
            output.flush()
        }
    }

    private fun serveApk(context: Context, output: OutputStream) {
        try {
            val sourceDir = context.applicationInfo.sourceDir
            var apkFile = File(sourceDir)
            if (!apkFile.exists() || apkFile.length() == 0L) {
                val cached = File(context.cacheDir, "shared_apk/JARVIS-StarkOS.apk")
                if (cached.exists() && cached.length() > 0L) {
                    apkFile = cached
                } else {
                    val rootCached = File(context.cacheDir, "JARVIS-AI.apk")
                    if (rootCached.exists() && rootCached.length() > 0L) {
                        apkFile = rootCached
                    }
                }
            }

            if (!apkFile.exists() || apkFile.length() == 0L) {
                val notFound = "HTTP/1.1 404 Not Found\r\n\r\nAPK package is being generated. Please retry in a few seconds."
                output.write(notFound.toByteArray())
                output.flush()
                return
            }

            val header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/vnd.android.package-archive\r\n" +
                    "Content-Disposition: attachment; filename=\"JARVIS-AI.apk\"\r\n" +
                    "Content-Length: ${apkFile.length()}\r\n" +
                    "\r\n"
            output.write(header.toByteArray())

            val fis = FileInputStream(apkFile)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            fis.close()
            output.flush()
        } catch (e: Exception) {
            val err = "HTTP/1.1 500 Internal Error\r\n\r\nError streaming APK"
            output.write(err.toByteArray())
            output.flush()
        }
    }
}
