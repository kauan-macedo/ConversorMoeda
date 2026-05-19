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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ConversionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var api: AwesomeAPI
    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerTarget: Spinner
    private lateinit var etValue: EditText

    private var walletReais = 0.0
    private var walletDolares = 0.0
    private var walletBitcoins = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conversion)

        walletReais    = intent.getDoubleExtra("reais", 0.0)
        walletDolares  = intent.getDoubleExtra("dolares", 0.0)
        walletBitcoins = intent.getDoubleExtra("bitcoins", 0.0)
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

        val textValue = etValue.text.toString()
        if (textValue.isEmpty()) {
            Toast.makeText(this, "Informe um valor.", Toast.LENGTH_SHORT).show()
            return
        }

        val valorAConverter = textValue.toDouble()

        if (moedaOrigem == moedaDestino) {
            Toast.makeText(this, "Escolha moedas diferentes.", Toast.LENGTH_SHORT).show()
            return
        }

        val par = "$moedaOrigem-$moedaDestino"
        val walletOrigem  = when (moedaOrigem)  { "BRL" -> walletReais; "USD" -> walletDolares; else -> walletBitcoins }
        val walletDestino = when (moedaDestino) { "BRL" -> walletReais; "USD" -> walletDolares; else -> walletBitcoins }

        converter(par, walletOrigem, walletDestino, valorAConverter)
    }

    fun converter(par: String, walletOrigem: Double, walletDestino: Double, valorAConverter: Double) {

        if (valorAConverter > walletOrigem) {
            Toast.makeText(this, "Saldo insuficiente.", Toast.LENGTH_SHORT).show()
            return
        }

        val moedas = par.split("-")
        val moedaOrigem = moedas[0]
        val moedaDestino = moedas[1]

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val parChamada = when (par) {
                    "BRL-USD" -> "USD-BRL"
                    "BRL-BTC" -> "BTC-BRL"
                    "USD-BTC" -> "BTC-USD"
                    else -> par
                }

                val response = withContext(Dispatchers.IO){ api.getCotacao(parChamada) }
                val chave = parChamada.replace("-", "")

                //if (response.isSuccessful) {
                    //val body = response.body()
                    //Log.i("Cotacao", "Resposta API: $body")
                    
                    val ask = response.ask.toDouble() //throw Exception("Chave $chave não encontrada no corpo")
                    Log.i("Cotacao", "Valor 'ask' recuperado: $ask")

                    val valorConvertido = when (par) {
                        "USD-BRL" -> valorAConverter * ask
                        "BRL-USD" -> valorAConverter / ask
                        "BTC-BRL" -> valorAConverter * ask
                        "BRL-BTC" -> valorAConverter / ask
                        "BTC-USD" -> valorAConverter * ask
                        "USD-BTC" -> valorAConverter / ask
                        else -> valorAConverter // Caso moedas iguais (já tratado no btn)
                    }
                    
                    Log.i("Cotacao", "Valor original: $valorAConverter ($moedaOrigem) -> Convertido: $valorConvertido ($moedaDestino)")

                    val novaOrigem  = walletOrigem - valorAConverter
                    val novaDestino = walletDestino + valorConvertido

                    walletReais    = when (moedaOrigem)  { "BRL" -> novaOrigem;  else -> walletReais    }
                                    .let { if (moedaDestino == "BRL") novaDestino else it }
                    walletDolares  = when (moedaOrigem)  { "USD" -> novaOrigem;  else -> walletDolares  }
                                    .let { if (moedaDestino == "USD") novaDestino else it }
                    walletBitcoins = when (moedaOrigem)  { "BTC" -> novaOrigem;  else -> walletBitcoins }
                                    .let { if (moedaDestino == "BTC") novaDestino else it }

                    Log.i("Cotacao", "Novos saldos -> R$: $walletReais, US$: $walletDolares, BTC: $walletBitcoins")

                    Toast.makeText(this@ConversionActivity, "Conversão realizada!", Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent()
                    intent.putExtra("reais",    walletReais)
                    intent.putExtra("dolares",  walletDolares)
                    intent.putExtra("bitcoins", walletBitcoins)
                    setResult(RESULT_OK, intent)
                    finish()
                //} else {
                //    Log.e("Cotacao", "Erro na API: ${response.code()} - ${response.message()}")
                //    Toast.makeText(this@ConversionActivity, "Erro no servidor da API.", Toast.LENGTH_SHORT).show()
                //}
            } catch (e: Exception) {
                Log.e("CotacaoController", "Erro ao converter", e)
                Toast.makeText(this@ConversionActivity, "Erro ao buscar cotação.", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}