package com.example.advertiser

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*


class MainActivity : AppCompatActivity() {

    val STRATEGY: Strategy = Strategy.P2P_POINT_TO_POINT
    val SERVICE_ID = "120001"
    private var strendPointId: String? = null
    lateinit var context: Context
    private var PERMISSIONS: Array<String> = arrayOf<String>()
    lateinit var payloadListener: PayloadListener
    lateinit var button: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        this.context = this
        payloadListener = PayloadListener()
        PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            )
    }

    private fun hasPermissions(context: Context?, PERMISSIONS: Array<String>): Boolean {
        if (context != null && PERMISSIONS != null) {
            for (permission in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    inner class PayloadListener: PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {
            var s = p1.asBytes()?.let { String(it) }
            if (s.equals("Payment Success")) {
                Toast.makeText(context, "100 Rs Payment Received!", Toast.LENGTH_LONG).show()
            }
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
        }
    }

    fun startAdvertising(view: View) {
        if (!hasPermissions(this@MainActivity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, 1)
            return
        }
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(
            this
        ).startAdvertising("Device A", SERVICE_ID, object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endPointId: String, connectionInfo: ConnectionInfo) {
                Nearby.getConnectionsClient(context).acceptConnection(endPointId, payloadListener)
            }

            override fun onConnectionResult(
                endPointId: String,
                connectionResolution: ConnectionResolution
            ) {
                when (connectionResolution.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK ->   {
                        // We're connected! Can now start sending and receiving data.
                        button.text = "Advertising..!"
                        strendPointId = endPointId
                    sendPayLoad(strendPointId!!)
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onDisconnected(s: String) {
                button.text = "Advertise"
                strendPointId = null
            }
        }, advertisingOptions).addOnSuccessListener {

        }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(context, "Fail " + e, Toast.LENGTH_LONG).show()
            }
    }

    private fun sendPayLoad(endPointId: String) {
        val bytesPayload = Payload.fromBytes(java.lang.String.valueOf("Hi Phone pay Number is 12345").toByteArray())
        Nearby.getConnectionsClient(
            context
        ).sendPayload(endPointId, bytesPayload).addOnSuccessListener { }.addOnFailureListener { }
    }
}