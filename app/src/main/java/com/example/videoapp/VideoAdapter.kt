package com.example.videoapp

import android.app.DownloadManager
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VideoAdapter(
    private val context: Context,
    private var videoList :ArrayList<Video>?
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>(){
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoView : VideoView = itemView.findViewById(R.id.videoView)
        var titleTv : TextView = itemView.findViewById(R.id.tvTitle)
        var timeTv : TextView = itemView.findViewById(R.id.timeTv)
        var progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)
        var downFab : FloatingActionButton = itemView.findViewById(R.id.fbDown)
        var deleteFab : FloatingActionButton = itemView.findViewById(R.id.fbDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_video,parent,false)
        )
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList!![position]
        val id : String? = video.id
        val title : String? = video.title
        val timeStamp : String? = video.timeStamp
        val videoUri : String? = video.videoUri
        val sdf = SimpleDateFormat("dd MM yyyy - HH:mm")
        val currentDate = sdf.format(Date())

        holder.titleTv.text = title
        holder.timeTv.text = currentDate
        setVideoUrl(video, holder)

        holder.deleteFab.setOnClickListener {
            deleteVideo(video)
        }
        holder.downFab.setOnClickListener {
            downloadVideo(video)
        }
    }

    private fun downloadVideo(video: Video) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(video.videoUri!!)
        storageRef.metadata
            .addOnSuccessListener {
                val fileName = it.name
                val fileType = it.contentType
                val fileDirectory = Environment.DIRECTORY_DOWNLOADS
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(video.videoUri)
                val request = DownloadManager.Request(uri)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir("$fileDirectory","$fileName.mp4")
                downloadManager.enqueue(request)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteVideo(video: Video) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(video.videoUri!!)
        storageRef.delete()
            .addOnSuccessListener {
                val dbRef = FirebaseDatabase.getInstance().getReference("Videos")
                dbRef.child(video.id!!)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setVideoUrl(video: Video, holder: VideoAdapter.VideoViewHolder) {
        holder.progressBar.visibility = View.VISIBLE
        val videoUrl : String? = video.videoUri
        val mediaController = MediaController(context)
        mediaController.setAnchorView(holder.videoView)

        holder.videoView.setMediaController(mediaController)
        holder.videoView.setVideoURI(videoUrl!!.toUri())
        holder.videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
        }
        holder.videoView.setOnInfoListener(MediaPlayer.OnInfoListener { mp, what, extra ->
            when(what){
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        holder.progressBar.visibility = View.GONE
                    return@OnInfoListener true
                }
            }
            false
        })
        holder.videoView.setOnCompletionListener {
            it.start()
        }
    }

    override fun getItemCount(): Int {
        return videoList!!.size
    }
}