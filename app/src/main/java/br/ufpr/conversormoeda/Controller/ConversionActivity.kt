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

    private lateinit var progressBar: ProgressBar
    private lateinit var AwesomeAPI: AwesomeAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conversion)

        progressBar = findViewById(R.id.progressBar)

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

    fun getCotacao(par: String) {

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = api.getCotacao(par)
                val chave = par.replace("-", "")

                if (response.isSuccessful) {
                    val ask = response.body()?.get(chave)?.ask?.toDouble()

                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("CotacaoController", "Erro ao buscar cotação", e)
            }
        }
    }

}
