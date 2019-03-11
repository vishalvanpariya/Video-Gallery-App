package vishal.uploadvideo

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

internal class ProgressRequestBody(private val mFile: File, private val mListener: UploadCallbacks) : RequestBody() {

    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)

        fun onError()

        fun onFinish()

        fun uploadStart()
    }

    init {
        mListener.uploadStart()
    }

    override fun contentType(): MediaType {
        // i want to upload only images
        return MediaType.parse("" + "*/*")
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return mFile.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = mFile.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inputStream = FileInputStream(mFile)
        var uploaded: Long = 0

        try {
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (inputStream.read(buffer).let { read = it ; it!=-1 }) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                handler.post(ProgressUpdater(uploaded, fileLength))
            }
        } finally {
            inputStream.close()
        }
    }

    private inner class ProgressUpdater(private val mUploaded: Long, private val mTotal: Long) : Runnable {

        override fun run() {
            try {

                val progress = (100 * mUploaded / mTotal).toInt()

                if (progress == 100)
                    mListener.onFinish()
                else
                    mListener.onProgressUpdate(progress)
            } catch (e: ArithmeticException) {
                mListener.onError()
                e.printStackTrace()
            }

        }
    }

    companion object {

        private val DEFAULT_BUFFER_SIZE = 2048
    }
}