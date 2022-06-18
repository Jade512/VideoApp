package com.example.videoapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.videoapp.databinding.ActivityAddVideoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddVideoActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddVideoBinding
    private lateinit var cameraPermission : Array<String>
    private var videoUri : Uri ?= null
    private var title : String = ""
    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Add News Video"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val getVideo = registerForActivityResult(
           ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                videoUri = it
                Log.e("AAA",videoUri.toString())
            }
        )

        val pickVideo = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){  result ->
            result.data?.data?.let {
                videoUri = it
                Log.e("AAA",videoUri.toString())
            }
        }

        binding.btnLibrary.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val bottomSheetView = LayoutInflater.from(applicationContext).inflate(
                R.layout.bottom_sheet_dialog,
                binding.root,
                false
            )
            bottomSheetView.findViewById<ImageView>(R.id.camera).setOnClickListener {
                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                if (intent.resolveActivity(packageManager) != null){
                    pickVideo.launch(intent)
                }
                bottomSheetDialog.dismiss()
            }
            bottomSheetView.findViewById<ImageView>(R.id.gallery).setOnClickListener {
                getVideo.launch("video/*")
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        binding.btnUp.setOnClickListener {
            title = binding.edt.text.toString().trim()
            if (ContextCompat.checkSelfPermission(this@AddVideoActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                }
                else if (videoUri == null) {
                    Toast.makeText(this, "Video is required", Toast.LENGTH_SHORT).show()
                }
                else {
                    val timestamp = ""+System.currentTimeMillis()
                    val filePathAndName ="Videos/video_$timestamp"
                    val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
                    storageReference.putFile(videoUri!!)
                        .addOnSuccessListener { taskSnapshot ->
                            val uriTask = taskSnapshot.storage.downloadUrl
                            while (!uriTask.isSuccessful);
                            val downloadUri = uriTask.result
                            if (uriTask.isSuccessful){
                                val hashMap = HashMap<String, Any>()
                                hashMap["id"] = "$timestamp"
                                hashMap["title"] = "$title"
                                hashMap["videoUri"] = "$downloadUri"
                                val dbRef = FirebaseDatabase.getInstance().getReference("Videos")
                                dbRef.child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            else {

            }
        }

    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it){
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
     override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}