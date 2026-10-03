package com.mhxx.gansimu.data.models

import android.os.Parcel
import android.os.Parcelable

data class DecoUsage(val decoration: Decoration, val count: Int) : Parcelable {
    constructor(p: Parcel) : this(p.readParcelable(Decoration::class.java.classLoader)!!, p.readInt())
    override fun writeToParcel(p: Parcel, f: Int) { p.writeParcelable(decoration, f); p.writeInt(count) }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<DecoUsage> {
        override fun createFromParcel(p: Parcel) = DecoUsage(p)
        override fun newArray(size: Int): Array<DecoUsage?> = arrayOfNulls(size)
    }
}

data class SearchResult(
    val head: Equipment?,
    val body: Equipment?,
    val arm: Equipment?,
    val wst: Equipment?,
    val leg: Equipment?,
    val charmSlot1Family: String = "",
    val charmSlot1Points: Int = 0,
    val charmSlot2Family: String = "",
    val charmSlot2Points: Int = 0,
    val charmSlots: Int = 0,
    val decoSlot1Family: String = "",
    val decoSlot1Size: Int = 0,
    val decoSlot2Family: String = "",
    val decoSlot2Size: Int = 0,
    val decoSlot3Family: String = "",
    val decoSlot3Size: Int = 0,
    val decorations: List<DecoUsage> = emptyList(),
    val activeSkills: List<String> = emptyList(),
    val difficultyScore: Double = 0.0,
    val existsProbability: String = "",
    val existsProbabilityOld: String = ""
) : Parcelable {
    constructor(p: Parcel) : this(
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readString() ?: "", p.readInt(), p.readString() ?: "", p.readInt(), p.readInt(),
        p.readString() ?: "", p.readInt(), p.readString() ?: "", p.readInt(),
        p.readString() ?: "", p.readInt(),
        mutableListOf<DecoUsage>().also { p.readList(it, DecoUsage::class.java.classLoader) },
        mutableListOf<String>().also { p.readStringList(it) },
        p.readDouble(), p.readString() ?: "", p.readString() ?: ""
    )
    override fun writeToParcel(p: Parcel, f: Int) {
        p.writeParcelable(head, f); p.writeParcelable(body, f); p.writeParcelable(arm, f)
        p.writeParcelable(wst, f); p.writeParcelable(leg, f)
        p.writeString(charmSlot1Family); p.writeInt(charmSlot1Points)
        p.writeString(charmSlot2Family); p.writeInt(charmSlot2Points); p.writeInt(charmSlots)
        p.writeString(decoSlot1Family); p.writeInt(decoSlot1Size)
        p.writeString(decoSlot2Family); p.writeInt(decoSlot2Size)
        p.writeString(decoSlot3Family); p.writeInt(decoSlot3Size)
        p.writeList(decorations); p.writeStringList(activeSkills)
        p.writeDouble(difficultyScore); p.writeString(existsProbability); p.writeString(existsProbabilityOld)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<SearchResult> {
        override fun createFromParcel(p: Parcel) = SearchResult(p)
        override fun newArray(size: Int): Array<SearchResult?> = arrayOfNulls(size)
    }
}

data class CharmSearchResult(
    val charmFamily1: String,
    val charmPoints1: Int,
    val charmFamily2: String = "",
    val charmPoints2: Int = 0,
    val charmSlots: Int = 0,
    val charmRarity: Int = 0,
    val decoFamily: String = "",
    val decoSize: Int = 0,
    val head: Equipment?,
    val body: Equipment?,
    val arm: Equipment?,
    val wst: Equipment?,
    val leg: Equipment?,
    val decorations: List<DecoUsage> = emptyList(),
    val difficultyScore: Double = 0.0,
    val existsProbabilityMH4G: String = "",
    val existsProbabilityOld: String = ""
) : Parcelable {
    constructor(p: Parcel) : this(
        p.readString() ?: "", p.readInt(), p.readString() ?: "", p.readInt(),
        p.readInt(), p.readInt(), p.readString() ?: "", p.readInt(),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        p.readParcelable(Equipment::class.java.classLoader),
        mutableListOf<DecoUsage>().also { p.readList(it, DecoUsage::class.java.classLoader) },
        p.readDouble(), p.readString() ?: "", p.readString() ?: ""
    )
    override fun writeToParcel(p: Parcel, f: Int) {
        p.writeString(charmFamily1); p.writeInt(charmPoints1)
        p.writeString(charmFamily2); p.writeInt(charmPoints2)
        p.writeInt(charmSlots); p.writeInt(charmRarity)
        p.writeString(decoFamily); p.writeInt(decoSize)
        p.writeParcelable(head, f); p.writeParcelable(body, f); p.writeParcelable(arm, f)
        p.writeParcelable(wst, f); p.writeParcelable(leg, f)
        p.writeList(decorations); p.writeDouble(difficultyScore)
        p.writeString(existsProbabilityMH4G); p.writeString(existsProbabilityOld)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<CharmSearchResult> {
        override fun createFromParcel(p: Parcel) = CharmSearchResult(p)
        override fun newArray(size: Int): Array<CharmSearchResult?> = arrayOfNulls(size)
    }
}

data class MySet(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val head: String = "",
    val body: String = "",
    val arm: String = "",
    val wst: String = "",
    val leg: String = "",
    val charmFamily1: String = "",
    val charmPoints1: Int = 0,
    val charmFamily2: String = "",
    val charmPoints2: Int = 0,
    val charmSlots: Int = 0,
    val decorations: String = "",
    val memo: String = ""
)

data class SearchSettings(
    val weaponType: Int = 0,
    val gender: Int = 0,
    val maxResults: Int = 20,
    val excludedEquipments: Set<String> = emptySet(),
    val fixedEquipments: Map<EquipSlot, String> = emptyMap(),
    val excludedDecorations: Set<String> = emptySet(),
    val availableCharm: Charm? = null,
    val villageStarsFilter: Int = 99,
    val hallStarsFilter: Int = 99
)
