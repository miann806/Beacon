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
    companion object {
        private val PERMISSION_REQUEST_COARSE_LOCATION = 1//設1 for 新版 2 for舊版 if it don't work
    }


    private val myreceiver = object : BroadcastReceiver() {//Bluetoothadapter搜尋到裝置之後，為了要放置搜尋結果，需要註冊一個broadcastreceiver
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {//當找到一個新的device時
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //將設備名稱和位置放入arry中以便顯示
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, java.lang.Short.MIN_VALUE).toInt()
                //取得訊號強弱並轉成int的格式
                val txPower = -59.0
                val ratio = rssi * 1.0 / txPower
                if (ratio < 1.0) {
                    dis = Math.pow(ratio, 10.0)
                } else {
                    dis = 0.89976 * Math.pow(ratio, 7.7095) + 0.111
                }
                try {

                    TV1!!.text = device!!.name.toString() + "         " + java.lang.Double.toString(dis)

                } catch (e: Exception) {
                 //計算距離遠近
                }

            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BT_Search = findViewById<View>(R.id.button) as Button
        TV1 = findViewById<View>(R.id.textView) as TextView
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()//宣告藍芽adapter物件
        checkBluetoothPermission()//檢查權限
        SearchBluetooth()//搜尋藍芽設備列表
        BT_Search!!.setOnClickListener {//如果button被按下
            if (mBluetoothAdapter!!.isDiscovering) {//如果正在搜尋
                mBluetoothAdapter!!.cancelDiscovery()//停止搜尋
            }
            mBluetoothAdapter!!.startDiscovery()//開始搜尋
        }
    }

    private fun checkBluetoothPermission() {//確認藍芽權限固定寫法 裝置版本6.0以上固定寫法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//檢查目前授權狀態,參數為請求授權的名稱
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {//檢查如果未授權(請求授權名稱,請求代碼)
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)//請求授權
            }
        }
    }

    fun SearchBluetooth() {
        if (mBluetoothAdapter == null) {//如果設備不支援藍芽
            Toast.makeText(this, "not find the bluetooth", Toast.LENGTH_SHORT).show()
            finish()//顯示不支援藍芽並結束
        }
        if (!mBluetoothAdapter!!.isEnabled) { //如果藍芽未開
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 1)//跳出視窗提示使用者是否開啟藍芽
            val myDevices = mBluetoothAdapter!!.bondedDevices
            //當藍芽第一次連線時，會對裝置發出請求，bondedcDevice來取得這些資訊並連接
            if (myDevices.size > 0) {//如果有裝置連接了
                for (device in myDevices)//loop過一整圈的連接裝置，加進arry裡面
                    bluetoothdeviceslist.add(device.name + ":" + device.address + "\n")//藍芽連接的裝置資訊
            }
        }
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)//註冊BroadcastReceiver 用來接收搜尋到的結果
        registerReceiver(myreceiver, filter)//找到想要的裝置就放進去
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)//結束搜尋
        registerReceiver(myreceiver, filter)//當finnish searching 時跟receiver說結束了 更新狀態

    }
}
