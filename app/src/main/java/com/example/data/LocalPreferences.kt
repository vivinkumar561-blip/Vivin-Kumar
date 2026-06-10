package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class LocalPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tn_market_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    private val watchlistKey = "favorites_watchlist"
    private val purchaseRecordsKey = "purchase_records_list"

    // --- Watchlist ---
    fun getWatchlist(): Set<String> {
        return prefs.getStringSet(watchlistKey, emptySet()) ?: emptySet()
    }

    fun toggleWatchlist(itemId: String): Set<String> {
        val current = getWatchlist().toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        prefs.edit().putStringSet(watchlistKey, current).apply()
        return current
    }

    // --- Purchase Records Ledger ---
    fun getPurchaseRecords(): List<PurchaseRecord> {
        val jsonStr = prefs.getString(purchaseRecordsKey, null) ?: return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, PurchaseRecord::class.java)
            val adapter = moshi.adapter<List<PurchaseRecord>>(type)
            adapter.fromJson(jsonStr) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addPurchaseRecord(record: PurchaseRecord): List<PurchaseRecord> {
        val current = getPurchaseRecords().toMutableList()
        current.add(0, record) // Add to top (newest first)
        savePurchaseRecords(current)
        return current
    }

    fun deletePurchaseRecord(recordId: String): List<PurchaseRecord> {
        val current = getPurchaseRecords().toMutableList()
        current.removeAll { it.id == recordId }
        savePurchaseRecords(current)
        return current
    }

    private fun savePurchaseRecords(records: List<PurchaseRecord>) {
        try {
            val type = Types.newParameterizedType(List::class.java, PurchaseRecord::class.java)
            val adapter = moshi.adapter<List<PurchaseRecord>>(type)
            val jsonStr = adapter.toJson(records)
            prefs.edit().putString(purchaseRecordsKey, jsonStr).apply()
        } catch (e: Exception) {
            // handle error if needed
        }
    }
}
