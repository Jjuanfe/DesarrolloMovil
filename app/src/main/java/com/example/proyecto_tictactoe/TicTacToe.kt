package com.example.proyecto_tictactoe

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random


class TicTacToeActivity : AppCompatActivity() {
    companion object {
        private const val PREFS_NOMBRE = "TicTacToePrefs"
        private const val CLAVE_VICTORIAS_X = "victorias_x"
        private const val CLAVE_VICTORIAS_O = "victorias_o"
    }

    private var turnoDeX = true // true = X, false = O
    private var turnosJugados = 0
    private var juegoActivo = true
    private val tableroLogico = Array(3) { Array(3) { "" } }

    private lateinit var tvTurno: TextView
    private lateinit var tvContador: TextView
    private lateinit var matrizBotones: Array<Array<Button>>
    private lateinit var coloresOriginales: Array<Array<ColorStateList?>>

    private lateinit var preferencias: SharedPreferences

    private var victoriasX = 0
    private var victoriasO = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // sigue usando el mismo layout de siempre
        preferencias = getSharedPreferences(PREFS_NOMBRE, MODE_PRIVATE)
        victoriasX = preferencias.getInt(CLAVE_VICTORIAS_X, 0)
        victoriasO = preferencias.getInt(CLAVE_VICTORIAS_O, 0)

        tvTurno = findViewById(R.id.tvTurno)
        tvContador = findViewById(R.id.tvContador)
        tvContador.text = "X: $victoriasX  |  O: $victoriasO"
        val btnReiniciar: Button = findViewById(R.id.btnReiniciar)

        matrizBotones = arrayOf(
            arrayOf(findViewById(R.id.btn00), findViewById(R.id.btn01), findViewById(R.id.btn02)),
            arrayOf(findViewById(R.id.btn10), findViewById(R.id.btn11), findViewById(R.id.btn12)),
            arrayOf(findViewById(R.id.btn20), findViewById(R.id.btn21), findViewById(R.id.btn22))
        )
        coloresOriginales = Array(3) { fila ->
            Array(3) { columna -> matrizBotones[fila][columna].backgroundTintList }
        }

        for (fila in 0..2) {
            for (columna in 0..2) {
                matrizBotones[fila][columna].setOnClickListener {
                    botonPresionado(matrizBotones[fila][columna], fila, columna)
                }
            }
        }

        btnReiniciar.setOnClickListener {
            reiniciarJuego()
        }
        val btnVolverMenu: Button = findViewById(R.id.btnVolverMenu)
        btnVolverMenu.setOnClickListener {
            finish() // cierra esta actividad y regresa al MenuActivity
        }

    }

    private fun botonPresionado(boton: Button, fila: Int, columna: Int) {
        if (tableroLogico[fila][columna].isNotEmpty() || !juegoActivo || !turnoDeX) return

        boton.text = "X"
        tableroLogico[fila][columna] = "X"
        turnosJugados++

        val elJuegoContinua = finalizarJugada("X")
        if (elJuegoContinua) {
            jugarTurnoMaquina()
        }
    }

    private fun jugarTurnoMaquina() {
        var fila: Int
        var columna: Int
        do {
            fila = Random.nextInt(3)
            columna = Random.nextInt(3)
        } while (tableroLogico[fila][columna].isNotEmpty())

        matrizBotones[fila][columna].text = "O"
        tableroLogico[fila][columna] = "O"
        turnosJugados++

        finalizarJugada("O")
    }

    private fun finalizarJugada(simbolo: String): Boolean {
        val lineaGanadora = obtenerLineaGanadora(simbolo)
        if (lineaGanadora != null) {
            tvTurno.text = "¡Ganador: $simbolo!"
            juegoActivo = false
            if (simbolo == "X") victoriasX++ else victoriasO++
            tvContador.text = "X: $victoriasX  |  O: $victoriasO"
            guardarPuntuacion()
            for ((f, c) in lineaGanadora) {
                matrizBotones[f][c].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            }
            return false
        }

        if (turnosJugados == 9) {
            tvTurno.text = "¡Empate!"
            juegoActivo = false
            return false
        }

        turnoDeX = !turnoDeX
        val siguienteSimbolo = if (turnoDeX) "X" else "O"
        tvTurno.text = "Turno de: $siguienteSimbolo"
        return true
    }

    private fun guardarPuntuacion() {
        val editor = preferencias.edit()
        editor.putInt(CLAVE_VICTORIAS_X, victoriasX)
        editor.putInt(CLAVE_VICTORIAS_O, victoriasO)
        editor.apply()
    }

    private fun obtenerLineaGanadora(simbolo: String): List<Pair<Int, Int>>? {
        for (i in 0..2) {
            if (tableroLogico[i][0] == simbolo && tableroLogico[i][1] == simbolo && tableroLogico[i][2] == simbolo)
                return listOf(i to 0, i to 1, i to 2)
            if (tableroLogico[0][i] == simbolo && tableroLogico[1][i] == simbolo && tableroLogico[2][i] == simbolo)
                return listOf(0 to i, 1 to i, 2 to i)
        }
        if (tableroLogico[0][0] == simbolo && tableroLogico[1][1] == simbolo && tableroLogico[2][2] == simbolo)
            return listOf(0 to 0, 1 to 1, 2 to 2)
        if (tableroLogico[0][2] == simbolo && tableroLogico[1][1] == simbolo && tableroLogico[2][0] == simbolo)
            return listOf(0 to 2, 1 to 1, 2 to 0)
        return null
    }

    private fun reiniciarJuego() {
        turnosJugados = 0
        juegoActivo = true
        turnoDeX = true
        tvTurno.text = "Turno de: X"

        for (fila in 0..2) {
            for (columna in 0..2) {
                tableroLogico[fila][columna] = ""
                matrizBotones[fila][columna].text = ""
                matrizBotones[fila][columna].backgroundTintList = coloresOriginales[fila][columna]
            }
        }
    }
}