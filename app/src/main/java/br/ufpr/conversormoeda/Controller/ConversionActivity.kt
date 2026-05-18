package br.ufpr.conversormoeda.Controller

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.ufpr.conversormoeda.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConversionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conversion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AwesomeAPI::class.java)
    }

    suspend fun getCotacao(par: String): Double? {
    return try {
        val response = api.getCotacao(par)
        val chave = par.replace("-", "")

        if (response.isSuccessful) {
            response.body()?.get(chave)?.ask?.toDouble()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

}
