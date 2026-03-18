package com.vasensio.bluetooth_list_recyclerview

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity(), BLEconnDialog.BLEConnectionCallback {

    private val listaDispositivos = mutableListOf<BluetoothDevice>()
    private lateinit var customAdapter: CustomAdapter
    private val REQUEST_CODE_BLUETOOTH = 100

    // Cambiado a anulable para mayor seguridad en el ciclo de vida
    private var bleDialog: BLEconnDialog? = null

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

        // Asegúrate de que el ID "main" exista en tu XML
        val mainLayout = findViewById<android.view.View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        requestBluetoothPermissionAndUpdate()
    }

    @SuppressLint("MissingPermission")
    fun updatePairedDevices() {
        listaDispositivos.clear()
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return

        val pairedDevices = bluetoothAdapter.bondedDevices.filter { device ->
            device.type == BluetoothDevice.DEVICE_TYPE_LE ||
                device.type == BluetoothDevice.DEVICE_TYPE_DUAL ||
                device.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN
        }

        listaDispositivos.addAll(pairedDevices)
        customAdapter.notifyDataSetChanged()
    }

    private fun requestBluetoothPermissionAndUpdate() {
        // En Android 12+ (API 31), se necesita BLUETOOTH_CONNECT
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BLUETOOTH && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updatePairedDevices()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBLEDialog(device: BluetoothDevice) {
        bleDialog = BLEconnDialog(this, device, this).apply {
            setCancelable(false)
            setOnCancelListener { onConnectionCancelled() }
            show()
        }
    }

    // CALLBACKS DEL DIÁLOGO
    override fun onConnectionSuccess(gatt: BluetoothGatt) {
        runOnUiThread { Toast.makeText(this, "Connectat!", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionFailed(error: String) {
        runOnUiThread { Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show() }
    }

    override fun onConnectionCancelled() {
        runOnUiThread { Toast.makeText(this, "Cancel·lada", Toast.LENGTH_SHORT).show() }
    }

    override fun onReceivedImage(file: File) {
        runOnUiThread { Toast.makeText(this, "Imatge: ${file.name}", Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleDialog?.dismiss()
    }
}
