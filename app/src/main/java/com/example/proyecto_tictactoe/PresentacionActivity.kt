package com.example.proyecto_tictactoe

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Modelo simple: cada "tarjeta" de presentación tiene nombre, descripción y contacto.
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