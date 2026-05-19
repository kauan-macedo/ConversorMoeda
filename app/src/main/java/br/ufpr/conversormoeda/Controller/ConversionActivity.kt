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
import android.widget.Toast
import android.content.Intent
import android.widget.EditText

class ConversionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var api: AwesomeAPI
    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerTarget: Spinner
    private lateinit var etValue: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conversion)

        progressBar = findViewById(R.id.progressBar)
        spinnerSource = findViewById(R.id.spinnerSource)
        spinnerTarget = findViewById(R.id.spinnerTarget)
        etValue = findViewById(R.id.etValue)

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

    fun btnConverter(view: View) {
        val moedaOrigem  = spinnerSource.selectedItem.toString()
        val moedaDestino = spinnerTarget.selectedItem.toString()
        val par = "$moedaOrigem-$moedaDestino"

        val valorAConverter = etValue.text.toString().toDouble()

        converter(par, moedaOrigem, moedaDestino, valorAConverter)
    }


    fun converter(par: String, walletOrigem: Double, walletDestino: Double, valorAConverter: Double) {

        if (valorAConverter > walletOrigem) {
            Toast.makeText(this, "Saldo insuficiente.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val parChamada = when (par) {
                    "BRL-USD" -> "USD-BRL"
                    "BRL-BTC" -> "BTC-BRL"
                    "USD-BTC" -> "BTC-USD"
                    else -> par
                }

                val response = api.getCotacao(parChamada)
                val chave = parChamada.replace("-", "")

                if (response.isSuccessful) {
                    val ask = response.body()?.get(chave)?.ask?.toDouble() ?: return@launch

                    val valorConvertido = when (par) {
                        "USD-BRL" -> valorAConverter * ask
                        "BRL-USD" -> valorAConverter / ask

                        "BTC-BRL" -> valorAConverter * ask
                        "BRL-BTC" -> valorAConverter / ask

                        "BTC-USD" -> valorAConverter * ask
                        "USD-BTC" -> valorAConverter / ask

                        else -> return@launch
                    }

                    val novaOrigem  = walletOrigem - valorAConverter
                    val novaDestino = walletDestino + valorConvertido

                    progressBar.visibility = View.GONE
                    tvWalletOrigem.text = novaOrigem.toString()
                    tvWalletDestino.text = novaDestino.toString()

                    val intent = Intent()
                    intent.putExtra("walletOrigem", novaOrigem)
                    intent.putExtra("walletDestino", novaDestino)
                    setResult(RESULT_OK, intent)
                }
            } catch (e: Exception) {
                Log.e("CotacaoController", "Erro ao converter", e)
                Toast.makeText(this@MainActivity, "Erro ao buscar cotação.", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

}