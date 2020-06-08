package com.lifecycle.wifitransfer.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.ByteBufferList
import com.koushikdutta.async.DataEmitter
import com.koushikdutta.async.callback.CompletedCallback
import com.koushikdutta.async.callback.DataCallback
import com.koushikdutta.async.http.body.MultipartFormDataBody
import com.koushikdutta.async.http.body.MultipartFormDataBody.MultipartCallback
import com.koushikdutta.async.http.body.Part
import com.koushikdutta.async.http.body.UrlEncodedFormBody
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.lifecycle.wifitransfer.constant.Constants
import com.lifecycle.wifitransfer.eventbus.EventBusUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.DecimalFormat

/**
 * @author wang
 * @date   2020/6/7
 * des
 */
class WebService : Service() {

    companion object {
        const val ACTION_START_WEB_SERVICE =
            "com.lifecycle.wifitransfer.action.ACTION_START_WEB_SERVICE"
        const val ACTION_STOP_WEB_SERVICE =
            "com.lifecycle.wifitransfer.action.ACTION_STOP_WEB_SERVICE"


        private const val TEXT_CONTENT_TYPE = "text/html;charset=utf-8"
        private const val CSS_CONTENT_TYPE = "text/css;charset=utf-8"
        private const val BINARY_CONTENT_TYPE = "application/octet-stream"
        private const val JS_CONTENT_TYPE = "application/javascript"
        private const val PNG_CONTENT_TYPE = "application/x-png"
        private const val JPG_CONTENT_TYPE = "application/jpeg"
        private const val SWF_CONTENT_TYPE = "application/x-shockwave-flash"
        private const val WOFF_CONTENT_TYPE = "application/x-font-woff"
        private const val TTF_CONTENT_TYPE = "application/x-font-truetype"
        private const val SVG_CONTENT_TYPE = "image/svg+xml"
        private const val EOT_CONTENT_TYPE = "image/vnd.ms-fontobject"
        private const val MP3_CONTENT_TYPE = "audio/mp3"
        private const val MP4_CONTENT_TYPE = "video/mpeg4"


        fun start(context: Context) {
            val intent = Intent(context, WebService::class.java)
            intent.action = ACTION_START_WEB_SERVICE
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, WebService::class.java)
            intent.action = ACTION_STOP_WEB_SERVICE
            context.startService(intent)
        }
    }


    private val server = AsyncHttpServer()
    private val asyncServer = AsyncServer()
    private val fileUploadHolder = FileUploadeHolder()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_WEB_SERVICE -> startServer()
                ACTION_STOP_WEB_SERVICE -> stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
        asyncServer.stop()
    }

    private fun startServer() {
        initResListener()
        initIndexPageListener()
        initQueryUploadListener()
        initDeleteFileListener()
        initDownloadFileListener()
        initUploadFileListener()
        initUploadFileProgressListener()
        server.listen(asyncServer, Constants.HTTP_SERVER_PORT)
    }

    private fun initResListener() {
        // ::sendResources 表示指向sendResources函数的一个对象，代替匿名函数
        server.get("/images/.*", ::sendResources)

        server.get("/scripts/.*", ::sendResources)

        server.get("/css/.*", ::sendResources)
    }

    private fun initIndexPageListener() {
        server.get("/") { _: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->

            var indexPageStr = ""
            var bInputStream: BufferedInputStream? = null
            try {
                bInputStream = BufferedInputStream(assets.open("wifi/index.html"))
                val baos = ByteArrayOutputStream()
                var len = 0
                val tmp = ByteArray(10240)
                while (bInputStream.read(tmp).also { len = it } > 0) {
                    baos.write(tmp, 0, len)
                }
                indexPageStr = String(baos.toByteArray(), Charsets.UTF_8)
            } catch (e: IOException) {
                e.printStackTrace()
                throw e
            } finally {
                if (bInputStream != null) {
                    try {
                        bInputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            response.send(indexPageStr)
        }
    }

    private fun initQueryUploadListener() {

        server.get("/files") { _: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            val jsonArray = JSONArray()
            val dir = Constants.APK_DIR
            if (dir.exists() && dir.isDirectory) {
                val fileNames = dir.list()
                fileNames?.forEach { fileName ->
                    val file = File(dir, fileName)
                    if (file.exists() && file.isFile) {
                        val jsonObject = JSONObject()
                        jsonObject.put("name", fileName)
                        val fileLength = file.length()
                        val df = DecimalFormat("0.00")
                        when {
                            fileLength > 1024 * 1024 -> {
                                jsonObject.put(
                                    "size",
                                    df.format(fileLength * 1f / 1024 / 1024.toDouble()) + "MB"
                                )
                            }
                            fileLength > 1024 -> {
                                jsonObject.put(
                                    "size",
                                    df.format(fileLength * 1f / 1024.toDouble()) + "KB"
                                )
                            }
                            else -> {
                                jsonObject.put("size", fileLength.toString() + "B")
                            }
                        }
                        jsonArray.put(jsonObject)
                    }
                }
            }
            response.send(jsonArray.toString())
        }
    }

    private fun initDeleteFileListener() {
        server.post("/files/.*") { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            val body = request.body as UrlEncodedFormBody
            if ("delete".equals(body.get().getString("_method"), ignoreCase = true)) {
                var path: String? = request.path.replace("/files/", "")
                try {
                    path = URLDecoder.decode(path, "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                val file = File(Constants.APK_DIR, path)
                if (file.exists() && file.isFile) {
                    file.delete()
                    EventBusUtils.sendRefreshAppListMessage()
                }
            }
            response.end()
        }
    }

    private fun initDownloadFileListener() {
        server.get("/files/.*") { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            var path: String = request.path.replace("/files/", "")
            try {
                path = URLDecoder.decode(path, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            val file = File(Constants.APK_DIR, path)
            if (file.exists() && file.isFile) {
                try {
                    response.headers.add(
                        "Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(
                            file.name,
                            "utf-8"
                        )
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                response.sendFile(file)
            } else {
                response.code(404).send("Not found!")
            }
        }
    }

    private fun initUploadFileListener() {
        server.post("/files") { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            val body = request.body as MultipartFormDataBody
            body.multipartCallback = MultipartCallback { part: Part ->
                if (part.isFile) {
                    body.dataCallback = DataCallback { _: DataEmitter?, bb: ByteBufferList ->
                        fileUploadHolder.write(bb.allByteArray)
                        bb.recycle()
                    }
                } else {
                    if (body.dataCallback == null) {
                        body.dataCallback =
                            DataCallback { _: DataEmitter?, bb: ByteBufferList ->
                                try {
                                    val fileName = URLDecoder.decode(
                                        String(bb.allByteArray),
                                        "UTF-8"
                                    )
                                    fileUploadHolder.setFileName(fileName)
                                } catch (e: UnsupportedEncodingException) {
                                    e.printStackTrace()
                                }
                                bb.recycle()
                            }
                    }
                }
            }
            request.endCallback = CompletedCallback {
                fileUploadHolder.reset()
                response.end()
                EventBusUtils.sendRefreshAppListMessage()
            }
        }
    }

    private fun initUploadFileProgressListener() {
        server.get("/progress/.*") { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            val res = JSONObject()
            val path = request.path.replace("/progress/", "")
            if (path == fileUploadHolder.getFileName()) {
                try {
                    res.put("fileName", fileUploadHolder.getFileName())
                    res.put("size", fileUploadHolder.getTotalSize())
                    res.put(
                        "progress",
                        if (fileUploadHolder.getFileOutputStream() == null) 1 else 0.1
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            response.send(res)
        }
    }


    private fun sendResources(
        request: AsyncHttpServerRequest,
        response: AsyncHttpServerResponse
    ) {
        try {
            var fullPath = request.path
            fullPath = fullPath.replace("%20", " ")
            var resourceName = fullPath
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1)
            }
            if (resourceName.indexOf("?") > 0) {
                resourceName = resourceName.substring(0, resourceName.indexOf("?"))
            }
            if (!TextUtils.isEmpty(getContentTypeByResourceName(resourceName))) {
                response.setContentType(getContentTypeByResourceName(resourceName))
            }
            val bInputStream =
                BufferedInputStream(assets.open("wifi/$resourceName"))
            response.sendStream(bInputStream, bInputStream.available().toLong())
        } catch (e: IOException) {
            e.printStackTrace()
            response.code(404).end()
            return
        }
    }

    private fun getContentTypeByResourceName(resourceName: String): String? {
        if (resourceName.endsWith(".css")) {
            return CSS_CONTENT_TYPE
        } else if (resourceName.endsWith(".js")) {
            return JS_CONTENT_TYPE
        } else if (resourceName.endsWith(".swf")) {
            return SWF_CONTENT_TYPE
        } else if (resourceName.endsWith(".png")) {
            return PNG_CONTENT_TYPE
        } else if (resourceName.endsWith(".jpg") || resourceName.endsWith(".jpeg")) {
            return JPG_CONTENT_TYPE
        } else if (resourceName.endsWith(".woff")) {
            return WOFF_CONTENT_TYPE
        } else if (resourceName.endsWith(".ttf")) {
            return TTF_CONTENT_TYPE
        } else if (resourceName.endsWith(".svg")) {
            return SVG_CONTENT_TYPE
        } else if (resourceName.endsWith(".eot")) {
            return EOT_CONTENT_TYPE
        } else if (resourceName.endsWith(".mp3")) {
            return MP3_CONTENT_TYPE
        } else if (resourceName.endsWith(".mp4")) {
            return MP4_CONTENT_TYPE
        }
        return ""
    }

    inner class FileUploadeHolder {
        private var fileName = ""
        private var receiveFile: File? = null
        private var totalSize: Long = 0
        private var fileOutputStream: BufferedOutputStream? = null

        fun getFileOutputStream() = fileOutputStream
        fun getFileName() = fileName
        fun getTotalSize() = totalSize

        fun setFileName(fileName: String) {
            this.fileName = fileName
            totalSize = 0

            if (!Constants.APK_DIR.exists()) {
                Constants.APK_DIR.mkdirs()
            }
            receiveFile = File(Constants.APK_DIR, fileName)
            try {
                fileOutputStream = BufferedOutputStream(FileOutputStream(receiveFile))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }

        fun reset() {
            fileOutputStream?.close()
            fileOutputStream = null
        }

        fun write(data: ByteArray) {
            fileOutputStream?.write(data)
            totalSize += data.size
        }
    }

}