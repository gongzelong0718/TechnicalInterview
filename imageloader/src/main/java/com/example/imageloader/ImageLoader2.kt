package com.example.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by superman on 2018/3/2.
 */
class ImageLoader2(context: Context) {

    private val mContext: Context = context

    companion object {
        lateinit var lruCache: LruCache<String, Bitmap>
        lateinit var mThreadPool: ExecutorService
        var handler = Handler()
    }

    init {
        val maxSize = (Runtime.getRuntime().freeMemory() / 4).toInt()
        lruCache = object : LruCache<String, Bitmap>(maxSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.rowBytes * value.height
            }
        }

        mThreadPool = Executors.newFixedThreadPool(3)
    }

    fun disPlay(url: String, imageView: ImageView) {
        //从内存获取
        var bitmap: Bitmap? = loadBitmapFromCache(url)
        if (bitmap != null) {
            log("从内存获取")
            imageView.setImageBitmap(bitmap)
            return
        }
        //从磁盘获取
        bitmap = loadBitmapFromLocal(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            return
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            //从网络获取
            getDataFromNet(url, imageView)
        } else {
//            val changeUrl = "file:/$url"
            bitmap = getLocalBitmap(url)
            imageView.setImageBitmap(bitmap)
        }

    }

    private fun decodeFile(f: File): Bitmap? {
        try {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            val stream1 = FileInputStream(f)
            BitmapFactory.decodeStream(stream1, null, o)
            stream1.close()

            val requiredSize = 140
            var tempWidth = o.outWidth
            var tempHeight = o.outHeight
            var scale = 1
            while (true) {
                if (tempWidth / 2 < requiredSize || tempHeight / 2 < requiredSize)
                    break
                tempWidth /= 2
                tempHeight /= 2
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            val stream2 = FileInputStream(f)
            val bitmap = BitmapFactory.decodeStream(stream2, null, o2)
            stream2.close()
            return bitmap
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    private fun getLocalBitmap(url: String): Bitmap? {
        try {
            val fis = FileInputStream(url)
            val bitmap = BitmapFactory.decodeStream(fis)
            putBitmapToCache(url, bitmap)
            return bitmap  ///把流转化为Bitmap图片

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }

    }


    //从内存中加载
    private fun loadBitmapFromCache(url: String): Bitmap? {
        return lruCache.get(url)
    }


    //从磁盘加载
    private fun loadBitmapFromLocal(url: String): Bitmap? {
        var name = MD5Utils.hashKeyForDisk(url)
        val file = File(getCacheDir(), name)
        if (file.exists()) {
            var bitmap: Bitmap = BitmapFactory.decodeStream(file.inputStream())
            log("从磁盘获取===width=${bitmap.width}==height==${bitmap.height}")
            // 存储到内存
            putBitmapToCache(url, bitmap)

            return bitmap
        }
        return null
    }


    //获取本地存放图片文件夹
    private fun getCacheDir(): String {

        val state = Environment.getExternalStorageState()
        val file: File
        file = if (state == (Environment.MEDIA_MOUNTED)) {
            File(Environment.getExternalStorageDirectory(), "imageCache")
        } else {
            File(mContext.cacheDir, "/imageCache");
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    //从网络获取
    private fun getDataFromNet(url: String, imageView: ImageView) {
        mThreadPool.execute(ImageLoadTask(url, object : DownloadFinishListener {
            override fun callBack(bitmap: Bitmap) {
                handler.post { imageView.setImageBitmap(bitmap) }
            }
        }))
    }

    interface DownloadFinishListener {
        fun callBack(bitmap: Bitmap)
    }

    //从网络下载
    inner class ImageLoadTask(url: String, downloadFinish: DownloadFinishListener) : Runnable {
        private var mPath = url
        private var downloadFinishListener = downloadFinish

        override fun run() {
            val httpUrlConnection: HttpURLConnection = URL(mPath).openConnection() as HttpURLConnection
            httpUrlConnection.requestMethod = "GET"
            httpUrlConnection.connectTimeout = 30 * 1000
            httpUrlConnection.readTimeout = 30 * 1000
            httpUrlConnection.connect()
            val code = httpUrlConnection.responseCode
            if (code == 200) {
                val input = httpUrlConnection.inputStream
                //将流转成bitmap
                val bitmap = BitmapFactory.decodeStream(input)
                downloadFinishListener.callBack(bitmap)
                log("网络下载")
                //存储到本地
                putBitmapToDisk(url = mPath, bitmap = bitmap)
                //存储到内存
                putBitmapToCache(url = mPath, bitmap = bitmap)
            }
        }

    }

    //保存到磁盘
    private fun putBitmapToDisk(url: String, bitmap: Bitmap) {
        var fos: FileOutputStream? = null
        try {
            val name = MD5Utils.hashKeyForDisk(url)
            log("存到磁盘name =$name")
            val file = File(getCacheDir(), name)
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
        } finally {
            if (fos != null) {
                fos.close()
            }
        }
    }

    //加载到内存
    private fun putBitmapToCache(url: String, bitmap: Bitmap) {
        log("存到内存url =$url bitmap =${bitmap == null}")
        lruCache.put(url, bitmap)
        log(lruCache.size().toString())
    }

    fun log(content: String) {
        Log.e("gongzelong", content)
    }
}