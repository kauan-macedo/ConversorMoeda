package br.ufpr.conversormoeda.Controller

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import br.ufpr.conversormoeda.R
import br.ufpr.conversormoeda.model.AwesomeAPI
import br.ufpr.conversormoeda.model.ExchangeResponse
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConversionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var api: AwesomeAPI
    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerTarget: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conversion)

        progressBar = findViewById(R.id.progressBar)
        spinnerSource = findViewById(R.id.spinnerSource)
        spinnerTarget = findViewById(R.id.spinnerTarget)

        val moedas = arrayOf("BRL", "USD", "BTC")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moedas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSource.adapter = adapter
        spinnerTarget.adapter = adapter

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
                    Log.i("Cotacao", "Valor recuperado: $ask")
                }
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("CotacaoController", "Erro ao buscar cotação", e)
                progressBar.visibility = View.GONE
            }
        }
    }
}