package com.schmidt.udpsender

import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class MainActivity : AppCompatActivity() {

    lateinit var broadcastReceiver: BroadcastReceiver
    lateinit var IP: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*        btn_send.setOnClickListener {
            Toast.makeText(MainActivity@this, "Esse botao nao faz nada", Toast.LENGTH_LONG).show()
        }*/

        showDialog()
        setButtons()

        broadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent!!.action
                if (action.equals("getting_data")) {
                    intent!!.getStringExtra("value")
                    Log.d("Adriano", "Chegou coisa no service: " + intent.getStringExtra("value"))
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction("getting_data")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun setButtons(){
        btn_1_menos.setOnClickListener {
            sendCommand("1")
        }

        btn_2_menos.setOnClickListener {
            sendCommand("4")
        }

        btn_3_menos.setOnClickListener {
            sendCommand("7")
        }

        btn_4_menos.setOnClickListener {
            sendCommand("B")
        }

        btn_1_mais.setOnClickListener {
            sendCommand("3")
        }

        btn_2_mais.setOnClickListener {
            sendCommand("6")
        }

        btn_3_mais.setOnClickListener {
            sendCommand("9")
        }

        btn_4_mais.setOnClickListener {
            sendCommand("A")
        }
    }

    private fun showDialog(){
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        val edittext = EditText(MainActivity@this)
        alert.setMessage("Configuração do IP")
        alert.setTitle("Entre com o IP do Raspberry")
        alert.setView(edittext)
        alert.setPositiveButton("OK") { dialog, which ->
            Log.d("Adriano", "Entoru no botao: " + edittext.text.toString())
            IP = edittext.text.toString()
            dialog.dismiss()
        }
        alert.setCancelable(false)
        alert.show()
    }

    private fun sendCommand(commmand: String) {
        Log.d("Adriano", "Entrou no sendCommand: $commmand")
        Log.d("Adriano", "Entrou no sendCommand IP: $IP")
        if (!commmand.isNotBlank()) {
            val thread = Thread(Runnable {
                var ds: DatagramSocket? = null
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(Intent(MainActivity@ this, UDPService::class.java))
                    } else {
                        startService(Intent(MainActivity@ this, UDPService::class.java))
                    }
                    ds = DatagramSocket()
                    // IP Address below is the IP address of that Device where server socket is opened.
                    val serverAddr: InetAddress =
                        InetAddress.getByName(IP)
                    val packet: DatagramPacket
                        packet = DatagramPacket(
                            commmand.toByteArray(),
                            commmand.length,
                            serverAddr,
                            9001
                        )
                    ds.send(packet)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("Adriano", "Faio miseravi: " + e.message)
                } finally {
                    ds?.close()
                }
            })

            thread.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver)
        }
    }
}
