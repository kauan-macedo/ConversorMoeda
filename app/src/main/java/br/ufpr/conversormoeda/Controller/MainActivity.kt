package br.ufpr.conversormoeda.Controller


import android.content.Intent
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.ufpr.conversormoeda.R
import br.ufpr.conversormoeda.model.Wallet

class MainActivity : AppCompatActivity() {

    var wallet: Wallet = Wallet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val converterBtn: Button = findViewById<Button>(R.id.converterBtn)

        converterBtn.setOnClickListener {
            converter()
        }

        var reaisView: TextView = findViewById<TextView>(R.id.reaisView)
        var dolaresView: TextView = findViewById<TextView>(R.id.dolaresView)
        var bitcoinsView: TextView = findViewById<TextView>(R.id.bitcoinsView)

        reaisView.text = DecimalFormat("#0.00").format(wallet.reais)
        dolaresView.text = DecimalFormat("#0.00").format(wallet.dolares)
        bitcoinsView.text = DecimalFormat("#0.000000").format(wallet.bitcoins)

    }

    fun converter() {
        val intent = Intent(this, ConversionActivity::class.java)
        intent.putExtra("reais", wallet.reais)
        intent.putExtra("dolares", wallet.dolares)
        intent.putExtra("bitcoins", wallet.bitcoins)
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == RESULT_OK) {
            wallet.reais = data?.getDoubleExtra("reais", -0.1) ?: 0.1
            wallet.dolares = data?.getDoubleExtra("dolares", -0.1) ?: -0.1
            wallet.bitcoins = data?.getDoubleExtra("bitcoins", -0.1) ?: -0.1
        }
    }
}