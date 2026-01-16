package com.vasensio.bluetooth_list_recyclerview

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val listaDispositivos = mutableListOf<Dispositivo>()
    private lateinit var customAdapter: CustomAdapter

    private val REQUEST_CODE_BLUETOOTH = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customAdapter = CustomAdapter(listaDispositivos) { dispositivo ->
            mostrarDialogo(dispositivo)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter

        val boton = findViewById<Button>(R.id.button)
        boton.setOnClickListener {
            requestBluetoothPermissionAndUpdate()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Solicitar permisos y actualizar la lista al iniciar
        requestBluetoothPermissionAndUpdate()
    }

    @SuppressLint("MissingPermission")
    fun updatePairedDevices() {
        // vaciamos la lista
        listaDispositivos.clear()

        // actualizamos la lista
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return // Salir si no hay Bluetooth
        for (elem in bluetoothAdapter.bondedDevices) {
            // afegim element al dataset
            listaDispositivos.add(Dispositivo(elem.name ?: "Desconocido", elem.address))
        }
        // Notificar al adapter que los datos han cambiado
        customAdapter.notifyDataSetChanged()
    }

    private fun requestBluetoothPermissionAndUpdate() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_BLUETOOTH)
        } else {
            updatePairedDevices()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updatePairedDevices()
            } else {
                Toast.makeText(this, "Permiso necesario para leer Bluetooth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogo(dispositivo: Dispositivo) {
        AlertDialog.Builder(this)
            .setTitle(dispositivo.nombre)
            .setMessage("MAC Address: ${dispositivo.mac}")
            .show()
    }
}

data class Dispositivo(val nombre: String, val mac: String)
