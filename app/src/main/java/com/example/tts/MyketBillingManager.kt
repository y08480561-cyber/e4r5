package com.example.tts

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import org.json.JSONObject

class MyketBillingManager(private val context: Context, private val onPurchaseResult: (sku: String, success: Boolean, message: String) -> Unit) {

    private var service: Any? = null
    private var isConnected = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d("MyketBilling", "Myket billing service connected")
            service = binder
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("MyketBilling", "Myket billing service disconnected")
            service = null
            isConnected = false
        }
    }

    init {
        try {
            val intent = Intent("ir.mservices.market.InAppBillingService.BIND").apply {
                setPackage("ir.mservices.market")
            }
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e("MyketBilling", "Failed to bind Myket billing service", e)
        }
    }

    fun isMyketInstalled(): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo("ir.mservices.market", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchPurchase(activity: Activity, sku: String, requestCode: Int) {
        if (!isMyketInstalled()) {
            onPurchaseResult(sku, false, "اپلیکیشن مایکت برای پرداخت یافت نشد.")
            return
        }

        if (!isConnected || service == null) {
            onPurchaseResult(sku, false, "اتصال به درگاه پرداخت برقرار نیست.")
            return
        }

        try {
            // Reflection call to IInAppBillingService.getBuyIntent
            val serviceClass = service!!.javaClass
            val getBuyIntentMethod = serviceClass.getMethod("getBuyIntent", Int::class.java, String::class.java, String::class.java, String::class.java, String::class.java)
            val bundle = getBuyIntentMethod.invoke(service, 3, context.packageName, sku, "inapp", "") as? Bundle
            
            val pendingIntent = bundle?.getParcelable<PendingIntent>("BUY_INTENT")
            if (pendingIntent != null) {
                activity.startIntentSenderForResult(
                    pendingIntent.intentSender,
                    requestCode,
                    Intent(),
                    0,
                    0,
                    0,
                    null
                )
            } else {
                val responseCode = bundle?.getInt("RESPONSE_CODE") ?: 0
                if (responseCode == 0) {
                    onPurchaseResult(sku, true, "خرید با موفقیت انجام شد.")
                } else {
                    onPurchaseResult(sku, false, "خطا در درگاه پرداخت مایکت (کد خطا: $responseCode)")
                }
            }
        } catch (e: Exception) {
            Log.e("MyketBilling", "Error launching purchase", e)
            onPurchaseResult(sku, false, "خطای غیرمنتظره در ارتباط با پرداخت.")
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?, expectedRequestCode: Int, sku: String) {
        if (requestCode == expectedRequestCode) {
            val responseCode = data?.getIntExtra("RESPONSE_CODE", 0) ?: 0
            val purchaseData = data?.getStringExtra("INAPP_PURCHASE_DATA")
            
            if (resultCode == Activity.RESULT_OK && responseCode == 0) {
                try {
                    val jo = JSONObject(purchaseData ?: "{}")
                    val orderId = jo.optString("orderId")
                    onPurchaseResult(sku, true, "خرید موفق (کد سفارش: $orderId)")
                } catch (e: Exception) {
                    onPurchaseResult(sku, true, "خرید با موفقیت انجام شد.")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                onPurchaseResult(sku, false, "خرید توسط کاربر لغو شد.")
            } else {
                onPurchaseResult(sku, false, "پرداخت ناموفق بود (کد: $responseCode)")
            }
        }
    }

    fun release() {
        try {
            if (isConnected) {
                context.unbindService(connection)
                isConnected = false
            }
        } catch (e: Exception) {
            Log.e("MyketBilling", "Error unbinding Myket service", e)
        }
    }
}
