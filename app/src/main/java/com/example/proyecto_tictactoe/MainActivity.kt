package com.example.proyecto_tictactoe

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

// =====================================================================================
// ARCHIVO ÚNICO CON LAS 4 PANTALLAS DE LA APP.
// Kotlin permite varias clases "top-level" en un mismo archivo, así que esto es
// perfectamente válido. Cada clase sigue registrada por separado en el
// AndroidManifest.xml (MenuActivity, TicTacToeActivity, ImcActivity, PresentacionActivity).
//
// Orden de este archivo:
//   1. MenuActivity        -> pantalla de inicio (launcher), abre las otras 3
//   2. TicTacToeActivity   -> juego original
//   3. ImcActivity         -> calculadora de IMC
//   4. PersonaInfo         -> modelo de datos usado por PresentacionActivity
//   5. PresentacionActivity -> CRUD de presentación personal
// =====================================================================================


// =====================================================================================
// 1. MENU ACTIVITY — pantalla de inicio (launcher)
// =====================================================================================
class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnIrJuego: Button = findViewById(R.id.btnIrJuego)
        val btnIrImc: Button = findViewById(R.id.btnIrImc)
        val btnIrPresentacion: Button = findViewById(R.id.btnIrPresentacion)

        btnIrJuego.setOnClickListener {
            startActivity(Intent(this, TicTacToeActivity::class.java))
        }

        btnIrImc.setOnClickListener {
            startActivity(Intent(this, ImcActivity::class.java))
        }

        btnIrPresentacion.setOnClickListener {
            startActivity(Intent(this, PresentacionActivity::class.java))
        }
    }
}


// =====================================================================================
// 2. TIC TAC TOE ACTIVITY — juego original (misma lógica de siempre)
// =====================================================================================
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
        setContentView(R.layout.activity_main)
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


// =====================================================================================
// 3. IMC ACTIVITY — calculadora de índice de masa corporal
// Formula: IMC = peso (kg) / (altura (m) * altura (m))
// =====================================================================================
class ImcActivity : AppCompatActivity() {

    private lateinit var etPeso: EditText
    private lateinit var etAltura: EditText
    private lateinit var tvResultadoImc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imc)

        etPeso = findViewById(R.id.etPeso)
        etAltura = findViewById(R.id.etAltura)
        tvResultadoImc = findViewById(R.id.tvResultadoImc)

        val btnCalcularImc: Button = findViewById(R.id.btnCalcularImc)
        btnCalcularImc.setOnClickListener {
            calcularImc()
        }

        val btnVolverMenu: Button = findViewById(R.id.btnVolverMenu)
        btnVolverMenu.setOnClickListener {
            finish() // cierra esta actividad y regresa al MenuActivity
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


// =====================================================================================
// 4. MODELO DE DATOS — usado por PresentacionActivity
// =====================================================================================
// Cada "tarjeta" de presentación tiene nombre, descripción y contacto.
data class PersonaInfo(
    var nombre: String,
    var descripcion: String,
    var contacto: String
) {
    // Así se ve cada elemento en el ListView.
    override fun toString(): String {
        return "$nombre\n$descripcion — $contacto"
    }
}


// =====================================================================================
// 5. PRESENTACION ACTIVITY — CRUD (Agregar / Editar / Borrar)
// =====================================================================================
class PresentacionActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etContacto: EditText
    private lateinit var lvPresentacion: ListView

    // Lista en memoria con todas las personas agregadas.
    private val listaPersonas = ArrayList<PersonaInfo>()

    // Adapter que conecta listaPersonas con el ListView visual.
    private lateinit var adapter: ArrayAdapter<PersonaInfo>

    // Guarda qué elemento de la lista está seleccionado (-1 = ninguno).
    // Se usa para saber cuál editar o borrar.
    private var posicionSeleccionada = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentacion)

        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etContacto = findViewById(R.id.etContacto)
        lvPresentacion = findViewById(R.id.lvPresentacion)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaPersonas)
        lvPresentacion.adapter = adapter

        val btnAgregar: Button = findViewById(R.id.btnAgregar)
        val btnEditar: Button = findViewById(R.id.btnEditar)
        val btnBorrar: Button = findViewById(R.id.btnBorrar)

        btnAgregar.setOnClickListener { agregarPersona() }
        btnEditar.setOnClickListener { editarPersona() }
        btnBorrar.setOnClickListener { borrarPersona() }

        val btnVolverMenu: Button = findViewById(R.id.btnVolverMenu)
        btnVolverMenu.setOnClickListener {
            finish() // cierra esta actividad y regresa al MenuActivity
        }

        // Al tocar un elemento de la lista, se cargan sus datos en los campos
        // para poder editarlo o borrarlo.
        lvPresentacion.setOnItemClickListener { _, _, posicion, _ ->
            posicionSeleccionada = posicion
            val persona = listaPersonas[posicion]
            etNombre.setText(persona.nombre)
            etDescripcion.setText(persona.descripcion)
            etContacto.setText(persona.contacto)
        }
    }

    private fun camposValidos(): Boolean {
        if (etNombre.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun agregarPersona() {
        if (!camposValidos()) return

        val nueva = PersonaInfo(
            nombre = etNombre.text.toString().trim(),
            descripcion = etDescripcion.text.toString().trim(),
            contacto = etContacto.text.toString().trim()
        )
        listaPersonas.add(nueva)
        adapter.notifyDataSetChanged()
        limpiarCampos()
        Toast.makeText(this, "Agregado", Toast.LENGTH_SHORT).show()
    }

    private fun editarPersona() {
        if (posicionSeleccionada == -1) {
            Toast.makeText(this, "Selecciona un elemento de la lista primero", Toast.LENGTH_SHORT).show()
            return
        }
        if (!camposValidos()) return

        val persona = listaPersonas[posicionSeleccionada]
        persona.nombre = etNombre.text.toString().trim()
        persona.descripcion = etDescripcion.text.toString().trim()
        persona.contacto = etContacto.text.toString().trim()

        adapter.notifyDataSetChanged()
        limpiarCampos()
        posicionSeleccionada = -1
        Toast.makeText(this, "Editado", Toast.LENGTH_SHORT).show()
    }

    private fun borrarPersona() {
        if (posicionSeleccionada == -1) {
            Toast.makeText(this, "Selecciona un elemento de la lista primero", Toast.LENGTH_SHORT).show()
            return
        }

        listaPersonas.removeAt(posicionSeleccionada)
        adapter.notifyDataSetChanged()
        limpiarCampos()
        posicionSeleccionada = -1
        Toast.makeText(this, "Borrado", Toast.LENGTH_SHORT).show()
    }

    private fun limpiarCampos() {
        etNombre.setText("")
        etDescripcion.setText("")
        etContacto.setText("")
    }
}