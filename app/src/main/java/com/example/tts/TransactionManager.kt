package com.example.tts

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(val id: String, val description: String, val amount: Int, val timestamp: Long)

class TransactionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sedayar_transactions", Context.MODE_PRIVATE)

    fun addTransaction(description: String, amount: Int) {
        val transactions = getTransactions().toMutableList()
        val newTransaction = Transaction(
            id = System.currentTimeMillis().toString(),
            description = description,
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        transactions.add(0, newTransaction) // Add to top
        
        // Keep only last 50 transactions to save space
        val limitedTransactions = transactions.take(50)
        
        val editor = prefs.edit()
        limitedTransactions.forEachIndexed { index, tx ->
            editor.putString("tx_${index}_desc", tx.description)
            editor.putInt("tx_${index}_amt", tx.amount)
            editor.putLong("tx_${index}_time", tx.timestamp)
        }
        editor.putInt("tx_count", limitedTransactions.size)
        editor.apply()
    }

    fun getTransactions(): List<Transaction> {
        val count = prefs.getInt("tx_count", 0)
        val list = mutableListOf<Transaction>()
        for (i in 0 until count) {
            val desc = prefs.getString("tx_${i}_desc", "") ?: ""
            val amt = prefs.getInt("tx_${i}_amt", 0)
            val time = prefs.getLong("tx_${i}_time", 0L)
            list.add(Transaction(i.toString(), desc, amt, time))
        }
        return list
    }

    fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("fa")).format(Date(timestamp))
    }
}
