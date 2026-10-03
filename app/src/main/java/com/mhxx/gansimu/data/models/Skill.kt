package com.mhxx.gansimu.data.models

import android.os.Parcel
import android.os.Parcelable

data class Skill(
    val name: String,
    val family: String,
    val points: Int,
    val type: Int
) : Parcelable {
    constructor(p: Parcel) : this(p.readString()!!, p.readString()!!, p.readInt(), p.readInt())
    override fun writeToParcel(p: Parcel, f: Int) { p.writeString(name); p.writeString(family); p.writeInt(points); p.writeInt(type) }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<Skill> {
        override fun createFromParcel(p: Parcel) = Skill(p)
        override fun newArray(size: Int): Array<Skill?> = arrayOfNulls(size)
    }
}

data class SkillFamily(val name: String, val skills: List<Skill>)

data class DesiredSkill(
    val skillName: String,
    val familyName: String,
    val requiredPoints: Int
) : Parcelable {
    constructor(p: Parcel) : this(p.readString()!!, p.readString()!!, p.readInt())
    override fun writeToParcel(p: Parcel, f: Int) { p.writeString(skillName); p.writeString(familyName); p.writeInt(requiredPoints) }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<DesiredSkill> {
        override fun createFromParcel(p: Parcel) = DesiredSkill(p)
        override fun newArray(size: Int): Array<DesiredSkill?> = arrayOfNulls(size)
    }
}

data class SkillCategory(val name: String, val skills: List<String>)
