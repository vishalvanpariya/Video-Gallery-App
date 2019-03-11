package vishal.uploadvideo

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.videoitem.view.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity(),ProgressRequestBody.UploadCallbacks {
    var filename:Uri?=null
    var db = VideoDatabase(this)
    var dialog:ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionReq()
        loadRecyclview(db.getVideos())
        button.setOnClickListener(View.OnClickListener {
            filename = Uri.fromFile(getfilepath())
            var intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT,filename)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1)
            startActivityForResult(intent,VIDEO_REQUEST_CODE)
        })

    }

    fun PermissionReq(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),RECORD_REQUEST_CODE)
        }
    }
    fun getfilepath():File{
        var folder = File("${Environment.getExternalStorageDirectory()}/video_app")
        if (!folder.exists())
            folder.mkdir()
        var file = File(folder,"${System.currentTimeMillis()}_video.mp4")
        return file
    }
    fun loadRecyclview(list:LinkedList<VideoObj>){
        var adapter = VideoAdapter(context = this,list = list)
        videolist.adapter = adapter
        videolist.layoutManager = GridLayoutManager(this,2) as RecyclerView.LayoutManager
        adapter.setCallback(object :VideoAdapter.Callback{
            override fun onClickListner(position: Int) {
                var intent = Intent(this@MainActivity,VideoPlayer::class.java)
                intent.putExtra("url","${Environment.getExternalStorageDirectory()}/video_app/${list.get(position).url}")
                startActivity(intent)
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== RECORD_REQUEST_CODE){
            if (grantResults.get(0).equals(PackageManager.PERMISSION_GRANTED)
                &&
                grantResults.get(0).equals(PackageManager.PERMISSION_GRANTED)){
                    button.isClickable = true
            }
            else{
                button.isClickable = false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode.equals(VIDEO_REQUEST_CODE) && resultCode.equals(Activity.RESULT_OK)) {
            db.AddUrl(filename.toString().split("/").last())
            Toast.makeText(this, "Video Saved", Toast.LENGTH_SHORT).show()

            //uplodingvideo
            var file = File("${Environment.getExternalStorageDirectory()}/video_app/${filename.toString().split("/").last()}")
            var body:ProgressRequestBody = ProgressRequestBody(file,this)
            var filepart:MultipartBody.Part =MultipartBody.Part.createFormData("video",file.name,body)
            val getResponse = AppConfig.retrofit.create(ApiConfig::class.java)
            val call = getResponse.upload(filepart)
            call.enqueue(object : Callback<ServerResponse> {
                override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            Toast.makeText(this@MainActivity,"Video Uploded",Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity,t.message,Toast.LENGTH_SHORT).show()
                }
            })
            loadRecyclview(db.getVideos())
        }
        else
            Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show()
    }

    class VideoAdapter(var context:Context,var list:LinkedList<VideoObj>):
        RecyclerView.Adapter<VideoAdapter.MyHolder>() {

        private var callback: Callback? = null

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VideoAdapter.MyHolder {
            return MyHolder(LayoutInflater.from(context).inflate(R.layout.videoitem, p0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(p0: VideoAdapter.MyHolder, p1: Int) {
            Glide.with(context)
                .load("${Environment.getExternalStorageDirectory()}/video_app/${list.get(p1).url}")
                .into(p0.itemView.videoimage)
            p0.itemView.videoimage.setOnClickListener {
                if (callback != null) {
                    callback!!.onClickListner(p1)
                }
            }
        }

        fun setCallback(callback: Callback) {
            this.callback = callback
        }

        interface Callback {
            fun onClickListner(position: Int)
        }

        class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        }
    }

    //interface function
    override fun onProgressUpdate(percentage: Int) {
        dialog?.progress = percentage
    }
    //interface function
    override fun onError() {

    }
    //interface function
    override fun onFinish() {
        dialog?.progress = 100
        dialog?.dismiss()
    }
    //interface function
    override fun uploadStart() {
        dialog = ProgressDialog(this@MainActivity)
        dialog?.setMax(100);
        dialog?.setCancelable(false)
        dialog?.progress = 0
        dialog?.setMessage("Video Uploading...")
        dialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog?.show()
    }

    companion object {
        val VIDEO_REQUEST_CODE = 101
        val RECORD_REQUEST_CODE = 102
    }

    class VideoObj(var key: Int, var url: String?)
}
