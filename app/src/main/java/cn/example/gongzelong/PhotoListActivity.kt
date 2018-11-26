package cn.example.gongzelong

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoListActivity : AppCompatActivity() {

    private var mRecyclerView: RecyclerView? = null
    private var mActivity: Activity? = null
    private val photoInfoList = ArrayList<PhotoInfo>()
    private val resultList = ArrayList<String>()//返回首页的集合

    private var createCameraPath: File? = null

    private fun checkPermissionReadExternalStorage(
            context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE)

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    context,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }
                return false
            } else {
                return true
            }

        } else {
            return true
        }
    }

    private fun checkPermissionWriteExternalStorage(
            context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialog2("External storage", context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    context,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }
                return false
            } else {
                return true
            }

        } else {
            return true
        }
    }

    private fun showDialog(msg: String, context: Context,
                           permission: String) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes
        ) { _, _ ->
            ActivityCompat.requestPermissions(context as Activity,
                    arrayOf(permission),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    private fun showDialog2(msg: String, context: Context,
                            permission: String) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes
        ) { _, _ ->
            ActivityCompat.requestPermissions(context as Activity,
                    arrayOf(permission),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)
        if (checkPermissionReadExternalStorage(this) && checkPermissionWriteExternalStorage(this)) {
            // do your stuff..
            init()
        }
    }

    private fun init() {
        initView()
        //        initListener();
        initPhoto()
        initRecycler()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do your stuff
                init()
            } else {
                Toast.makeText(this, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show()
            }
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do your stuff
                init()
            } else {
                Toast.makeText(this, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions,
                    grantResults)
        }
    }

    private fun initView() {
        mActivity = this
        mRecyclerView = findViewById<View>(R.id.recycler_view) as RecyclerView
    }

    private fun initRecycler() {
        val gridLayoutManager = GridLayoutManager(mActivity, 3)
        mRecyclerView!!.layoutManager = gridLayoutManager
        val photoAdapter = PhotoAdapter(mActivity!!, photoInfoList)
        mRecyclerView!!.adapter = photoAdapter

        photoAdapter.getPhoto(object : PhotoAdapter.GetPhoto {
            override fun getTakeCamera(v: View) {
                val intentPhone = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                createCameraPath = createTmpFile(mActivity, "/gongzelong/Pictures")
                intentPhone.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(createCameraPath))
                intentPhone.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                startActivityForResult(intentPhone, 100)
            }
        })
    }

    private fun initPhoto() {
        val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
            private val IMAGE_PROJECT = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID, MediaStore.Images.Media.SIZE)

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                return when (id) {
                    0 -> CursorLoader(mActivity!!,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            IMAGE_PROJECT, null, null, IMAGE_PROJECT[2] + " DESC")
                    1 -> CursorLoader(mActivity!!,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            IMAGE_PROJECT,
                            IMAGE_PROJECT[0] + "like '%" + args!!.getString("path") + "%'", null,
                            IMAGE_PROJECT[2] + " DESC")
                    else -> CursorLoader(mActivity!!,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            IMAGE_PROJECT,
                            IMAGE_PROJECT[0] + "like '%" + args!!.getString("path") + "%'", null,
                            IMAGE_PROJECT[2] + " DESC")
                }
            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                if (data != null) {
                    photoInfoList.clear()
                    val count = data.count
                    data.moveToFirst()
                    if (count > 0) {
                        do {
                            val path = data.getString(
                                    data.getColumnIndexOrThrow(IMAGE_PROJECT[0]))
                            val name = data.getString(
                                    data.getColumnIndexOrThrow(IMAGE_PROJECT[1]))
                            val time = data.getLong(
                                    data.getColumnIndexOrThrow(IMAGE_PROJECT[2]))
                            val size = data.getInt(
                                    data.getColumnIndexOrThrow(IMAGE_PROJECT[4]))
                            val photoInfo = PhotoInfo(path, name, time, false)
                            val isSize5k = size > 1024 * 5
                            if (isSize5k) {
                                photoInfoList.add(photoInfo)
                            }
                        } while (data.moveToNext())
                    }
                }
            }

            override fun onLoaderReset(loader: Loader<Cursor>) {

            }
        }

        @Suppress("DEPRECATION")
        supportLoaderManager.restartLoader(0, null, loaderCallbacks)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            resultList.clear()
            val absolutePath = createCameraPath!!.absolutePath
            resultList.add(absolutePath)
            val intent = Intent(mActivity, MainActivity::class.java)
            intent.putExtra("resultPath", resultList)
            setResult(200, intent)
            finish()
        }
    }

    companion object {

        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
        const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 124

        /**
         * 创建文件
         * @param context  context
         * @param filePath 文件路径
         * @return file
         */
        fun createTmpFile(context: Context?, filePath: String): File {

            val timeStamp = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss", Locale.CHINA).format(Date())

            val externalStorageState = Environment.getExternalStorageState()

            val dir = File(Environment.getExternalStorageDirectory().toString() + filePath)

            return if (externalStorageState == Environment.MEDIA_MOUNTED) {
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                File(dir, "$timeStamp.jpg")
            } else {
                val cacheDir = context!!.cacheDir
                File(cacheDir, "$timeStamp.jpg")
            }
        }
    }
}