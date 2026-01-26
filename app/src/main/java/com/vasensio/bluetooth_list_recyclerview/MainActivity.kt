package com.vasensio.bluetooth_list_recyclerview

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothCodecType
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
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
import java.io.File

class MainActivity : AppCompatActivity(), BLEconnDialog.BLEConnectionCallback  {

    private val listaDispositivos = mutableListOf<BluetoothDevice>()
    private lateinit var customAdapter: CustomAdapter

    private val REQUEST_CODE_BLUETOOTH = 100

    private lateinit var bleDialog : BLEconnDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customAdapter = CustomAdapter(listaDispositivos) { dispositivo ->
            showBLEDialog(dispositivo)
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
        customAdapter.notifyItemRangeRemoved(0, listaDispositivos.size - 1)
        listaDispositivos.clear()

        // actualizamos la lista
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return // Salir si no hay Bluetooth
        for (elem in bluetoothAdapter.bondedDevices.filter {
            device -> device.type == BluetoothDevice.DEVICE_TYPE_LE || device.type == BluetoothDevice.DEVICE_TYPE_DUAL ||
                device.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN
        }) {
            // afegim element al dataset
            listaDispositivos.add(elem)
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

    /*private fun mostrarDialogo(dispositivo: Dispositivo) {
        AlertDialog.Builder(this)
            .setTitle(dispositivo.nombre)
            .setMessage("MAC Address: ${dispositivo.mac}")
            .show()
    }*/

    // DIALOG : cridar aquesta funció per mostrar-lo
////////////////////////////////////////////////
    private fun showBLEDialog(device: BluetoothDevice) {
        bleDialog = BLEconnDialog(this, device, this)
        bleDialog?.apply {
            setCancelable(false)
            setOnCancelListener {
                onConnectionCancelled()
            }
            show()
        }
    }

    // DIALOG CALLBACKS
///////////////////////////////
    override fun onConnectionSuccess(gatt: BluetoothGatt) {
        runOnUiThread {
            Toast.makeText(this, "Connectat amb èxit!", Toast.LENGTH_SHORT).show()
            // Aquí pots fer operacions amb el gatt connectat
            // Per exemple: llegir/escribre característiques
        }
    }

    override fun onConnectionFailed(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Error de connexió: $error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onConnectionCancelled() {
        runOnUiThread {
            Toast.makeText(this, "Connexió cancel·lada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReceivedImage(file: File) {
        runOnUiThread {
            val filename = file.name
            Toast.makeText(this, "Imatge rebuda: $filename", Toast.LENGTH_SHORT).show()
        }
    }

    // Aquesta callback és de l'Activity
    override fun onDestroy() {
        super.onDestroy()
        bleDialog?.dismiss()
    }
}

// data class Dispositivo(val nombre: String, val mac: String)
