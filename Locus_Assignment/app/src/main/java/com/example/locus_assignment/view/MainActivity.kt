package com.example.locus_assignment.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locus_assignment.R
import com.example.locus_assignment.Utility
import com.example.locus_assignment.adapter.ImageAdapter
import com.example.locus_assignment.model.ImageData
import com.example.locus_assignment.model.ImageResponseItem
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

const val CAMERA_REQUEST_CODE = 100
const val IMAGE_PICK_CAMERA_CODE = 101

class MainActivity : AppCompatActivity(), ImageAdapter.SaveTextListener {
//    var imageDataList = kotlin.arrayOfNulls<ImageData>(4)
    var imageDataList = mutableListOf<ImageData>()
    private var position : Int  = 0
    private lateinit var mAdapter: ImageAdapter
    private lateinit var imageUri: Uri
    private lateinit var cameraPermission: Array<String>
    private lateinit var currentPhotoPath: String
    private lateinit var itemList: List<ImageResponseItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecycler()
        cameraPermission =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun initDataList(itemList : List<ImageResponseItem>) {
        for (index in 0..itemList.size-1)
            imageDataList.add(index, ImageData(-1, false, "", null))
    }

    private fun initRecycler() {
        val obj = JSONArray(readJSONFromAsset())
        recycler.layoutManager = LinearLayoutManager(this)
        val itemList = getJSONObject(obj)
        initDataList(itemList)
        mAdapter = ImageAdapter(this, itemList, this)
        recycler.adapter = mAdapter
    }

    private fun getJSONObject(obj: JSONArray): List<ImageResponseItem> {
        itemList =  Utility().getJsonObject(obj)
        return itemList
    }

    private fun showClearDialog( position: Int, mCloseIv: ImageView) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.message))
        builder.setTitle(getString(R.string.alert_title))
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, which ->
            imageDataList[position]?.imageUri = null
            mCloseIv.visibility = View.GONE
            mAdapter.notifyDataSetChanged()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun clearDrawable(position: Int, mCloseIv: ImageView) {
        showClearDialog(position,  mCloseIv)
    }
    fun showLargeImage(imageUri: Uri) {
        mRelative.visibility = View.VISIBLE
        recycler.visibility = View.GONE
        mZoomIv.setImageURI(imageUri)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (recycler.visibility == View.GONE) {
                recycler.visibility = View.VISIBLE
                mRelative.visibility = View.GONE
            } else {
                finish()
            }
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.submit) {
            saveData()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this)

                imageDataList[position]!!.imageUri = imageUri
                mAdapter.notifyDataSetChanged()
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                imageDataList[position]!!.imageUri = resultUri
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    lateinit var output : String

    private fun saveData() {
        for (index in 0..imageDataList.size-1) {
            output = ""
            val imageData = imageDataList[index]
            if (!imageData?.comment!!.isEmpty())
                output = "$output ${imageData?.comment}"
            imageData?.imageUri?.let {
                output = "$output ${imageData.imageUri}"
            }
            Log.i("Result : $index", output)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted) {
                    pickCamera()
                } else showToast("Permission Denied for Camera")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun readJSONFromAsset(): String? {
        var json: String? = null
        try {
            val inputStream = assets.open("imagefile.json")
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    override fun openCamera(position: Int) {
        this.position = position
        if (!checkCameraPermission()) {
            requestCameraPermission()
        } else {
            pickCamera()
        }
    }

    private fun pickCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createFilePath()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    null
                }

                photoFile?.also {
                    imageUri = FileProvider.getUriForFile(
                        this,
                        "com.example.locus_assignment",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePictureIntent,
                        IMAGE_PICK_CAMERA_CODE
                    )
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createFilePath(): File? {
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        var file = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(fileName, ".jpg", file).apply { currentPhotoPath = absolutePath }
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        return result
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission,
            CAMERA_REQUEST_CODE
        )
    }
}