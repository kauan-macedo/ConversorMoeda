package br.ufpr.conversormoeda.Controller


import android.icu.text.DecimalFormat
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.ufpr.conversormoeda.R
import br.ufpr.conversormoeda.model.Wallet

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var reaisView: TextView = findViewById<TextView>(R.id.reaisView)
        var dolaresView: TextView = findViewById<TextView>(R.id.dolaresView)
        var bitcoinsView: TextView = findViewById<TextView>(R.id.bitcoinsView)

        var wallet: Wallet = Wallet()

        reaisView.text = DecimalFormat("#0.00").format(wallet.reais)
        dolaresView.text = DecimalFormat("#0.00").format(wallet.dolares)
        bitcoinsView.text = DecimalFormat("#0.000000").format(wallet.bitcoins)

    }
}