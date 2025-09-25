package com.example.lab_week_05

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.ImageData
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }

    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getCatImageResponse()
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val firstImage = response.body()?.firstOrNull()

                    // Pastikan ada data gambar yang diterima
                    if (firstImage != null) {
                        // Muat gambar ke ImageView
                        if (firstImage.imageUrl.isNotBlank()) {
                            imageLoader.loadImage(firstImage.imageUrl, imageResultView)
                        } else {
                            Log.d(MAIN_ACTIVITY, "Missing image URL")
                        }

                        // Logika untuk mendapatkan nama ras kucing
                        val catBreed = if (firstImage.breeds?.isNotEmpty() == true) {
                            firstImage.breeds.first().name
                        } else {
                            "Unknown"
                        }

                        // Tampilkan nama ras di TextView
                        apiResponseView.text = getString(R.string.cat_breed_placeholder, catBreed)

                    } else {
                        Log.d(MAIN_ACTIVITY, "No image data found in response")
                        apiResponseView.text = getString(R.string.cat_breed_placeholder, "Unknown")
                    }
                } else {
                    Log.e(
                        MAIN_ACTIVITY, "Failed to get response\n" +
                                response.errorBody()?.string().orEmpty()
                    )
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }

}