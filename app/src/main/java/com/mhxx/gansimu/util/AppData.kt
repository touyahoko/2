package com.mhxx.gansimu.util

import android.content.Context
import android.content.SharedPreferences
import com.mhxx.gansimu.data.models.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * アプリ設定・状態のシングルトン
 */
object AppData {
    private const val PREF_NAME = "gansimu_prefs"

    // 検索設定
    var weaponType: Int = 0        // 0=両, 1=剣士, 2=ガンナー
    var gender: Int = 0            // 0=両, 1=男, 2=女
    var maxResults: Int = 20

    // 除外リスト
    val excludedEquipments: MutableSet<String> = mutableSetOf()
    val fixedEquipments: MutableMap<EquipSlot, String> = mutableMapOf()
    val excludedDecorations: MutableSet<String> = mutableSetOf()

    // 護石設定（現在持っている護石）
    var charmFamily1: String = ""
    var charmPoints1: Int = 0
    var charmFamily2: String = ""
    var charmPoints2: Int = 0
    var charmSlots: Int = 0
    var charmRarity: Int = 1

    // マイセット
    val mySets: MutableList<MySet> = mutableListOf()

    // 入手時期フィルタ
    var villageStarsFilter: Int = 99
    var hallStarsFilter: Int = 99

    fun buildSearchSettings(): SearchSettings {
        val charm = if (charmFamily1.isNotEmpty() || charmSlots > 0) {
            Charm(
                name = "所持護石",
                rarity = charmRarity,
                hallStars = 1,
                skillPoints = mutableMapOf<String, Int>().also { map ->
                    if (charmFamily1.isNotEmpty() && charmPoints1 != 0) map[charmFamily1] = charmPoints1
                    if (charmFamily2.isNotEmpty() && charmPoints2 != 0) map[charmFamily2] = charmPoints2
                },
                slots = charmSlots
            )
        } else null

        return SearchSettings(
            weaponType = weaponType,
            gender = gender,
            maxResults = maxResults,
            excludedEquipments = excludedEquipments.toSet(),
            fixedEquipments = fixedEquipments.toMap(),
            excludedDecorations = excludedDecorations.toSet(),
            availableCharm = charm,
            villageStarsFilter = villageStarsFilter,
            hallStarsFilter = hallStarsFilter
        )
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("weaponType", weaponType)
        editor.putInt("gender", gender)
        editor.putInt("maxResults", maxResults)
        editor.putStringSet("excludedEquipments", excludedEquipments)
        editor.putStringSet("excludedDecorations", excludedDecorations)
        editor.putString("charmFamily1", charmFamily1)
        editor.putInt("charmPoints1", charmPoints1)
        editor.putString("charmFamily2", charmFamily2)
        editor.putInt("charmPoints2", charmPoints2)
        editor.putInt("charmSlots", charmSlots)
        editor.putInt("charmRarity", charmRarity)
        editor.putInt("villageStarsFilter", villageStarsFilter)
        editor.putInt("hallStarsFilter", hallStarsFilter)

        // 固定装備を保存
        val fixedJson = JSONObject()
        for ((slot, name) in fixedEquipments) {
            fixedJson.put(slot.name, name)
        }
        editor.putString("fixedEquipments", fixedJson.toString())

        // マイセットを保存
        val setsJson = JSONArray()
        for (set in mySets) {
            val setObj = JSONObject()
            setObj.put("id", set.id)
            setObj.put("name", set.name)
            setObj.put("head", set.head)
            setObj.put("body", set.body)
            setObj.put("arm", set.arm)
            setObj.put("wst", set.wst)
            setObj.put("leg", set.leg)
            setObj.put("charmFamily1", set.charmFamily1)
            setObj.put("charmPoints1", set.charmPoints1)
            setObj.put("charmFamily2", set.charmFamily2)
            setObj.put("charmPoints2", set.charmPoints2)
            setObj.put("charmSlots", set.charmSlots)
            setObj.put("memo", set.memo)
            setsJson.put(setObj)
        }
        editor.putString("mySets", setsJson.toString())
        editor.apply()
    }

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        weaponType = prefs.getInt("weaponType", 0)
        gender = prefs.getInt("gender", 0)
        maxResults = prefs.getInt("maxResults", 20)
        excludedEquipments.clear()
        excludedEquipments.addAll(prefs.getStringSet("excludedEquipments", emptySet()) ?: emptySet())
        excludedDecorations.clear()
        excludedDecorations.addAll(prefs.getStringSet("excludedDecorations", emptySet()) ?: emptySet())
        charmFamily1 = prefs.getString("charmFamily1", "") ?: ""
        charmPoints1 = prefs.getInt("charmPoints1", 0)
        charmFamily2 = prefs.getString("charmFamily2", "") ?: ""
        charmPoints2 = prefs.getInt("charmPoints2", 0)
        charmSlots = prefs.getInt("charmSlots", 0)
        charmRarity = prefs.getInt("charmRarity", 1)
        villageStarsFilter = prefs.getInt("villageStarsFilter", 99)
        hallStarsFilter = prefs.getInt("hallStarsFilter", 99)

        // 固定装備を読み込み
        val fixedJson = prefs.getString("fixedEquipments", "{}") ?: "{}"
        try {
            val obj = JSONObject(fixedJson)
            fixedEquipments.clear()
            for (key in obj.keys()) {
                val slot = try { EquipSlot.valueOf(key) } catch (e: Exception) { continue }
                fixedEquipments[slot] = obj.getString(key)
            }
        } catch (e: Exception) { /* ignore */ }

        // マイセットを読み込み
        val setsJson = prefs.getString("mySets", "[]") ?: "[]"
        try {
            val arr = JSONArray(setsJson)
            mySets.clear()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                mySets.add(MySet(
                    id = obj.optLong("id", System.currentTimeMillis()),
                    name = obj.optString("name", "セット${i+1}"),
                    head = obj.optString("head", ""),
                    body = obj.optString("body", ""),
                    arm = obj.optString("arm", ""),
                    wst = obj.optString("wst", ""),
                    leg = obj.optString("leg", ""),
                    charmFamily1 = obj.optString("charmFamily1", ""),
                    charmPoints1 = obj.optInt("charmPoints1", 0),
                    charmFamily2 = obj.optString("charmFamily2", ""),
                    charmPoints2 = obj.optInt("charmPoints2", 0),
                    charmSlots = obj.optInt("charmSlots", 0),
                    memo = obj.optString("memo", "")
                ))
            }
        } catch (e: Exception) { /* ignore */ }
    }
}
