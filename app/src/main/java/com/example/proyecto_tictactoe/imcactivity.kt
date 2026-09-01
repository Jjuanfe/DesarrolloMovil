package com.example.proyecto_tictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class ImcActivity : AppCompatActivity() {

    private lateinit var etPeso: EditText
    private lateinit var etAltura: EditText
    private lateinit var tvResultadoImc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imcactivity)

        etPeso = findViewById(R.id.etPeso)
        etAltura = findViewById(R.id.etAltura)
        tvResultadoImc = findViewById(R.id.tvResultadoImc)

        val btnCalcularImc: Button = findViewById(R.id.btnCalcularImc)
        btnCalcularImc.setOnClickListener {
            calcularImc()
        }
        val btnVolverMenu: Button = findViewById(R.id.btnVolverMenu)
        btnVolverMenu.setOnClickListener {
            finish()
        }

    }

    private fun calcularImc() {
        val textoPeso = etPeso.text.toString()
        val textoAltura = etAltura.text.toString()

        if (textoPeso.isEmpty() || textoAltura.isEmpty()) {
            Toast.makeText(this, "Ingresa peso y altura", Toast.LENGTH_SHORT).show()
            return
        }

        val peso = textoPeso.toDoubleOrNull()
        val altura = textoAltura.toDoubleOrNull()

        if (peso == null || altura == null || altura <= 0.0 || peso <= 0.0) {
            Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        val imc = peso / (altura * altura)
        val categoria = obtenerCategoria(imc)

        tvResultadoImc.text = "IMC: %.2f\n%s".format(imc, categoria)
    }

    private fun obtenerCategoria(imc: Double): String {
        return when {
            imc < 18.5 -> "Bajo peso"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obesidad"
        }
    }
}