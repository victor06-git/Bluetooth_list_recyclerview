package com.vasensio.bluetooth_list_recyclerview

import android.os.Bundle
import android.widget.Toast // 1. IMPORTANTE: Añadir este import
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Añadimos algunos datos iniciales (opcional)
        listaDispositivos.add(Dispositivo("Auriculares Sony", "00:11:22:33:44:55"))
        listaDispositivos.add(Dispositivo("Altavoz JBL", "AA:BB:CC:DD:EE:FF"))

        // 2. Inicializamos el adapter pasándole la lista mutable
        customAdapter = CustomAdapter(listaDispositivos) { dispositivo ->
            mostrarDialogo(dispositivo)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter

        // 3. Lógica del BOTÓN para AÑADIR items
        val boton = findViewById<Button>(R.id.button)

        boton.setOnClickListener {
            // Generamos datos nuevos (aquí podrías cogerlos de un EditText si quisieras)
            val nuevoNombre = "Dispositivo Nuevo ${listaDispositivos.size + 1}"
            val nuevaMac = generarMacAleatoria() // Función auxiliar inventada abajo

            // A. Añadimos el objeto a la lista
            listaDispositivos.add(Dispositivo(nuevoNombre, nuevaMac))

            // B. ¡IMPORTANTE! Avisamos al adapter de que han cambiado los datos
            // notifyItemInserted es más eficiente que notifyDataSetChanged para un solo item
            customAdapter.notifyItemInserted(listaDispositivos.size - 1)

            // Hacemos scroll hasta el último elemento para que el usuario lo vea
            recyclerView.scrollToPosition(listaDispositivos.size - 1)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mostrarDialogo(dispositivo: Dispositivo) {
        AlertDialog.Builder(this)
            .setTitle(dispositivo.nombre)
            .setMessage("MAC Address: ${dispositivo.mac}\n¿Conectar?")
            .setPositiveButton("Sí", null)
            .setNegativeButton("No", null)
            .show()
    }

    // Función simple para simular una MAC diferente cada vez
    private fun generarMacAleatoria(): String {
        val partes = List(6) { (0..255).random().toString(16).uppercase().padStart(2, '0') }
        return partes.joinToString(":")
    }

    data class Dispositivo(val nombre: String, val mac: String)

    data class Device(
        val name: String,
        val mac: String
    )
}