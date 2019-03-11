package vishal.uploadvideo

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

interface ApiConfig {

    @Multipart
    @POST("uploadvideo")
    fun upload(
        @Part requestBody: MultipartBody.Part
    ): Call<ServerResponse>
}

class ServerResponse {

    // variable name should be same as in the json response from nodejs
    @SerializedName("success")
    var success: Boolean = false
        internal set
    @SerializedName("message")
    var message: String? = null
        internal set

}

object AppConfig {

    var BASE_URL = "http://192.168.43.185:3000/"  //for pc but you have to set as per ur pc
    //var BASE_URL = "http://10.0.2.2:3000/"   //for emulater

    val retrofit: Retrofit
        get() {

            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(6000, TimeUnit.SECONDS)
                .connectTimeout(6000, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        }
}