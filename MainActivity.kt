package com.example.beacon_java

import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var BT_Search: Button? = null
    private var TV1: TextView? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val bluetoothdeviceslist = ArrayList<String>()

    internal var dis: Double = 0.toDouble()

    private val myreceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, java.lang.Short.MIN_VALUE).toInt()
                val txPower = -59.0
                val ratio = rssi * 1.0 / txPower
                if (ratio < 1.0) {
                    dis = Math.pow(ratio, 10.0)
                } else {
                    dis = 0.89976 * Math.pow(ratio, 7.7095) + 0.111
                }
                try {

                    TV1!!.text = device!!.name.toString() + " " + java.lang.Double.toString(dis)

                } catch (e: Exception) {

                }

            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BT_Search = findViewById<View>(R.id.button) as Button
        TV1 = findViewById<View>(R.id.textView) as TextView
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        checkBluetoothPermission()
        SearchBluetooth()
        BT_Search!!.setOnClickListener {
            if (mBluetoothAdapter!!.isDiscovering) {
                mBluetoothAdapter!!.cancelDiscovery()
            }
            mBluetoothAdapter!!.startDiscovery()
        }
    }

    private fun checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
            }
        }
    }

    fun SearchBluetooth() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "not find the bluetooth", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (!mBluetoothAdapter!!.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 1)
            val myDevices = mBluetoothAdapter!!.bondedDevices
            if (myDevices.size > 0) {
                for (device in myDevices)
                    bluetoothdeviceslist.add(device.name + ":" + device.address + "\n")
            }
        }
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(myreceiver, filter)
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(myreceiver, filter)
    }

    companion object {
        private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }
}
