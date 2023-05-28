package com.example.weatherforecast

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.weatherforecast.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.OkHttpClient
import okhttp3.Request

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.txtErrorMsg.visibility = View.GONE
        binding.txtWindLabel.visibility = View.GONE
        binding.txtHumidityLable.visibility = View.GONE
        binding.txtPressure.visibility = View.GONE
        val lastCity = getLastCity()
        if (lastCity.isNotEmpty()) {
            binding.etPlace.setText(lastCity)
            weatherForecast().execute()
        }
        binding.btnSearch.setOnClickListener {
            weatherForecast().execute()
        }
    }

    private fun getLastCity(): String {
        val sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("LastCity", "") ?: ""
    }

    inner class weatherForecast(): AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            binding.progressBar.visibility = View.VISIBLE
            binding.screen.visibility = View.VISIBLE

        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                var city: String = binding.etPlace.text.toString()
                saveCity(city)
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://yahoo-weather5.p.rapidapi.com/weather?location=$city&format=json&u=c")
                    .get()
                    .addHeader("X-RapidAPI-Key", "a0339919a5mshbc4e22fca609929p144e50jsn185f35928a7e")
                    .addHeader("X-RapidAPI-Host", "yahoo-weather5.p.rapidapi.com")
                    .build()
                val apiResponse = client.newCall(request).execute()
                response = apiResponse.body?.string()
                apiResponse.close()
            } catch (e: Exception) {
                response = null
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error in doInBackground: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
            return response
        }



        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (!result.isNullOrEmpty()){
                    val json = JSONObject(result)
                    val currentObservation = json.getJSONObject("current_observation")
                    val det = currentObservation.getJSONObject("condition")
                    val wind = currentObservation.getJSONObject("wind")
                    val humidity = currentObservation.getJSONObject("atmosphere")
                    val pressure = currentObservation.getJSONObject("atmosphere")
                    val date: Long = currentObservation.getLong("pubDate")
                    val weatherText = "Last viewed at : " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(date * 1000))
                    val temp = det.getInt("temperature").toString() + "\u2103"
                    val weatherDesc = det.getString("text")
                    val windSpeed = wind.getInt("speed").toString()
                    val humidityValue = humidity.getInt("humidity").toString()
                    val pressureValue = pressure.getDouble("pressure").toString()

                    binding.txtTemp.text = temp
                    binding.txtDAT.text = weatherText
                    binding.txtWeatherInfo.text = weatherDesc
                    binding.wind.text = windSpeed
                    binding.humidity.text = humidityValue
                    binding.pressure.text = pressureValue
                    binding.txtWindLabel.visibility = View.VISIBLE
                    binding.txtHumidityLable.visibility = View.VISIBLE
                    binding.txtPressure.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.screen.visibility = View.VISIBLE
                }else{
                    Toast.makeText(this@MainActivity,"error data couldn't be fetched",Toast.LENGTH_SHORT).show()
                }

            }catch (e: Exception){
                binding.progressBar.visibility = View.GONE
                binding.screen.visibility = View.GONE
                binding.txtErrorMsg.visibility = View.VISIBLE
                Toast.makeText(this@MainActivity, "Error: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveCity(city: String) {
        val sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("LastCity", city)
        editor.apply()
    }

}
