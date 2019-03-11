package vishal.uploadvideo

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import java.io.File
import java.util.*
import vishal.uploadvideo.MainActivity.VideoObj

class VideoDatabase(var context: Context):
    SQLiteOpenHelper(context,DB_NAME,null, DB_VER){
    lateinit var db: SQLiteDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table ${DB_TABLE} (_id integer primary key autoincrement,url text);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop table if exists ${DB_TABLE};")
    }

    fun AddUrl(url:String){
        db = writableDatabase
        db.execSQL("insert into ${DB_TABLE} (url) values ('${url}');")
    }

    fun getVideos(): LinkedList<VideoObj>{
        db = readableDatabase
        var list  = LinkedList<VideoObj>()
        var cr:Cursor = db.rawQuery("Select * from ${DB_TABLE}",null)!!
        while (cr.moveToNext()){
            if(File("${Environment.getExternalStorageDirectory()}/video_app/${cr.getString(1)}").exists())
                list.add(VideoObj(cr.getInt(0), cr.getString(1)))
        }
        return list
    }

    companion object {
        private val DB_NAME:String = "VideoData"
        private val DB_TABLE:String = "Videos"
        private val DB_VER:Int = 1
    }

}