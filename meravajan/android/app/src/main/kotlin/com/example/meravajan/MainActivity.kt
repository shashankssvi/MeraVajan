package com.example.meravajan

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.UUID


class MainActivity : FlutterActivity() {
    private val channel = "com.example.bluet/blue"
    private val BLUETOOTH_PERMISSION_CODE = 100
    private val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    private val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

    private var bluetoothGatt : BluetoothGatt? = null

    private var received : String? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor,channel).setMethodCallHandler {
                call,result ->
            when(call.method){
                "connect"->{
                    bluetoothPermission()
                    bluetooth()
                }
                "disconnect"->{
                    bluetoothDisc()
                }
                "send"->{
                    var data = call.argument<String>("value")
                    sendData(data.toString())
                }
                "receive"->{
                    result.success(received)
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun sendData(data:String){

        val service = bluetoothGatt?.getService(UUID.fromString(SERVICE_UUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
        val valueToSend = data.toByteArray(Charsets.UTF_8)

        characteristic?.let {
            val result = bluetoothGatt!!.writeCharacteristic(
                it,
                valueToSend,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
            if (result != BluetoothStatusCodes.SUCCESS) {
                Log.e("BLE", "Write failed with status: $result")
            }
        }
    }

    private fun bluetoothPermission(){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN),BLUETOOTH_PERMISSION_CODE)
        }
        else{
            Toast.makeText(context,"enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        when(requestCode){
            BLUETOOTH_PERMISSION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    bluetooth()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun bluetooth(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            var bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
            val device = bluetoothAdapter.getRemoteDevice("B0:B2:1C:A7:69:62")
            bluetoothGatt = device.connectGatt(this,false,gattCallback)
            Toast.makeText(context,"connected", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun bluetoothDisc(){
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt=null
    }

    private val gattCallback = object: BluetoothGattCallback(){
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.d("BLE","Connected")
                gatt?.discoverServices()
            }
            else{
                Log.d("BLE","unable")
            }
        }

        @SuppressLint("MissingPermission", "NewApi")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS){
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

                if(characteristic != null){
                    gatt.setCharacteristicNotification(characteristic,true)

                    var descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            val received = String(value, Charsets.UTF_8)
            Log.d("BLE", "Received Notification: $received")

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            received = String(value, Charsets.UTF_8)
            Log.d("BLE", "Received Notification: $received")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Write successful: ${characteristic?.getStringValue(0)}")
            } else {
                Log.e("BLE", "Write failed with status: $status")
            }
        }
    }
}
