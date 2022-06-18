package com.example.videoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.videoapp.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var list : ArrayList<Video>
    lateinit var adapter : VideoAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Videos"
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this,AddVideoActivity::class.java))
            finish()
        }

        loadVideoFromFireBase()
    }

    private fun loadVideoFromFireBase() {
        list = ArrayList()
        val dbRef = FirebaseDatabase.getInstance().getReference("Videos")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (data in snapshot.children){
                    val video = data.getValue(Video::class.java)
                    list.add(video!!)
                }
                adapter = VideoAdapter(this@MainActivity,list)
                binding.rvVideo.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}