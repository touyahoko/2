package com.mhxx.gansimu.data.models

import android.os.Parcel
import android.os.Parcelable

enum class EquipSlot { HEAD, BODY, ARM, WST, LEG, CHARM }

data class Equipment(
    val name: String,
    val gender: Int,
    val type: Int,
    val rarity: Int,
    val slotCount: Int,
    val hallStars: Int,
    val villageStars: Int,
    val starsMode: Int,
    val defenseInit: Int,
    val defenseFinal: Int,
    val fireRes: Int,
    val waterRes: Int,
    val thunderRes: Int,
    val iceRes: Int,
    val dragonRes: Int,
    val skillPoints: Map<String, Int>,
    val slot: EquipSlot,
    val materials: List<Pair<String, Int>>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(),
        parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(),
        parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(),
        parcel.readInt(), parcel.readInt(),
        mutableMapOf<String, Int>().also { map ->
            val size = parcel.readInt()
            repeat(size) { map[parcel.readString() ?: ""] = parcel.readInt() }
        },
        EquipSlot.values()[parcel.readInt()],
        mutableListOf<Pair<String,Int>>().also { list ->
            val size = parcel.readInt()
            repeat(size) { list.add(Pair(parcel.readString() ?: "", parcel.readInt())) }
        }
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(gender); parcel.writeInt(type); parcel.writeInt(rarity)
        parcel.writeInt(slotCount); parcel.writeInt(hallStars); parcel.writeInt(villageStars)
        parcel.writeInt(starsMode); parcel.writeInt(defenseInit); parcel.writeInt(defenseFinal)
        parcel.writeInt(fireRes); parcel.writeInt(waterRes); parcel.writeInt(thunderRes)
        parcel.writeInt(iceRes); parcel.writeInt(dragonRes)
        parcel.writeInt(skillPoints.size)
        for ((k, v) in skillPoints) { parcel.writeString(k); parcel.writeInt(v) }
        parcel.writeInt(slot.ordinal)
        parcel.writeInt(materials.size)
        for ((k, v) in materials) { parcel.writeString(k); parcel.writeInt(v) }
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<Equipment> {
        override fun createFromParcel(parcel: Parcel) = Equipment(parcel)
        override fun newArray(size: Int): Array<Equipment?> = arrayOfNulls(size)
    }
}

data class Decoration(
    val name: String,
    val rarity: Int,
    val slotSize: Int,
    val hrRequired: Int,
    val villageRequired: Int,
    val starsMode: Int,
    val skillPoints: Map<String, Int>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(),
        mutableMapOf<String, Int>().also { map ->
            val size = parcel.readInt()
            repeat(size) { map[parcel.readString() ?: ""] = parcel.readInt() }
        }
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(rarity); parcel.writeInt(slotSize); parcel.writeInt(hrRequired)
        parcel.writeInt(villageRequired); parcel.writeInt(starsMode)
        parcel.writeInt(skillPoints.size)
        for ((k, v) in skillPoints) { parcel.writeString(k); parcel.writeInt(v) }
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<Decoration> {
        override fun createFromParcel(parcel: Parcel) = Decoration(parcel)
        override fun newArray(size: Int): Array<Decoration?> = arrayOfNulls(size)
    }
}

data class Charm(
    val name: String,
    val rarity: Int,
    val hallStars: Int,
    val skillPoints: MutableMap<String, Int> = mutableMapOf(),
    val slots: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(), parcel.readInt(),
        mutableMapOf<String, Int>().also { map ->
            val size = parcel.readInt()
            repeat(size) { map[parcel.readString() ?: ""] = parcel.readInt() }
        },
        parcel.readInt()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name); parcel.writeInt(rarity); parcel.writeInt(hallStars)
        parcel.writeInt(skillPoints.size)
        for ((k, v) in skillPoints) { parcel.writeString(k); parcel.writeInt(v) }
        parcel.writeInt(slots)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<Charm> {
        override fun createFromParcel(parcel: Parcel) = Charm(parcel)
        override fun newArray(size: Int): Array<Charm?> = arrayOfNulls(size)
    }
}
