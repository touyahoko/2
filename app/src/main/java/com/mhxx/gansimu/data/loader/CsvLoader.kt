package com.mhxx.gansimu.data.loader

import android.content.Context
import com.mhxx.gansimu.data.models.*

/**
 * CSVファイルからゲームデータをロードするクラス
 * ファイルはassets/data/ に配置（UTF-8）
 */
object CsvLoader {

    private var skills: List<Skill>? = null
    private var skillFamilies: List<SkillFamily>? = null
    private var headEquipments: List<Equipment>? = null
    private var bodyEquipments: List<Equipment>? = null
    private var armEquipments: List<Equipment>? = null
    private var wstEquipments: List<Equipment>? = null
    private var legEquipments: List<Equipment>? = null
    private var decorations: List<Decoration>? = null
    private var charms: List<Charm>? = null
    private var categories: Map<String, List<String>>? = null
    private var compoundSkills: Map<String, List<String>>? = null

    fun isLoaded() = skills != null

    fun loadAll(context: Context) {
        if (isLoaded()) return
        skills = loadSkills(context)
        skillFamilies = buildSkillFamilies(skills!!)
        headEquipments = loadEquipments(context, "MHXX_EQUIP_HEAD.csv", EquipSlot.HEAD)
        bodyEquipments = loadEquipments(context, "MHXX_EQUIP_BODY.csv", EquipSlot.BODY)
        armEquipments = loadEquipments(context, "MHXX_EQUIP_ARM.csv", EquipSlot.ARM)
        wstEquipments = loadEquipments(context, "MHXX_EQUIP_WST.csv", EquipSlot.WST)
        legEquipments = loadEquipments(context, "MHXX_EQUIP_LEG.csv", EquipSlot.LEG)
        decorations = loadDecorations(context)
        charms = loadCharms(context)
        categories = loadCategories(context)
        compoundSkills = loadCompoundSkills(context)
    }

    fun getSkills(): List<Skill> = skills ?: emptyList()
    fun getSkillFamilies(): List<SkillFamily> = skillFamilies ?: emptyList()
    fun getHeadEquipments(): List<Equipment> = headEquipments ?: emptyList()
    fun getBodyEquipments(): List<Equipment> = bodyEquipments ?: emptyList()
    fun getArmEquipments(): List<Equipment> = armEquipments ?: emptyList()
    fun getWstEquipments(): List<Equipment> = wstEquipments ?: emptyList()
    fun getLegEquipments(): List<Equipment> = legEquipments ?: emptyList()
    fun getDecorations(): List<Decoration> = decorations ?: emptyList()
    fun getCharms(): List<Charm> = charms ?: emptyList()
    fun getCategories(): Map<String, List<String>> = categories ?: emptyMap()
    fun getCompoundSkills(): Map<String, List<String>> = compoundSkills ?: emptyMap()

    fun getAllEquipments(): Map<EquipSlot, List<Equipment>> = mapOf(
        EquipSlot.HEAD to (headEquipments ?: emptyList()),
        EquipSlot.BODY to (bodyEquipments ?: emptyList()),
        EquipSlot.ARM to (armEquipments ?: emptyList()),
        EquipSlot.WST to (wstEquipments ?: emptyList()),
        EquipSlot.LEG to (legEquipments ?: emptyList())
    )

    fun getSkillFamilyByName(name: String): SkillFamily? =
        skillFamilies?.find { it.name == name }

    fun getSkillsByFamily(family: String): List<Skill> =
        skills?.filter { it.family == family } ?: emptyList()

    fun getActivationPoints(skillName: String): Int =
        skills?.find { it.name == skillName }?.points ?: 0

    private fun readCsv(context: Context, filename: String): List<List<String>> {
        val lines = mutableListOf<List<String>>()
        context.assets.open("data/$filename").bufferedReader().useLines { seq ->
            seq.forEach { line ->
                if (line.startsWith("#") || line.isBlank()) return@forEach
                lines.add(parseCsvLine(line))
            }
        }
        return lines
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuote = false
        val current = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuote -> inQuote = true
                c == '"' && inQuote -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else inQuote = false
                }
                c == ',' && !inQuote -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    private fun loadSkills(context: Context): List<Skill> {
        return context.assets.open("data/MHXX_SKILL.csv").bufferedReader().useLines { seq ->
            seq.filter { !it.startsWith("#") && it.isNotBlank() }
                .map { line ->
                    val cols = parseCsvLine(line)
                    if (cols.size >= 4) {
                        Skill(
                            name = cols[0],
                            family = cols[1],
                            points = cols[2].toIntOrNull() ?: 0,
                            type = cols[3].toIntOrNull() ?: 0
                        )
                    } else null
                }.filterNotNull().toList()
        }
    }

    private fun buildSkillFamilies(skills: List<Skill>): List<SkillFamily> {
        return skills.groupBy { it.family }
            .map { (family, familySkills) -> SkillFamily(family, familySkills) }
            .sortedBy { it.name }
    }

    private fun loadEquipments(context: Context, filename: String, slot: EquipSlot): List<Equipment> {
        val rows = readCsv(context, filename)
        return rows.mapNotNull { cols ->
            if (cols.size < 16) return@mapNotNull null
            try {
                val skillPoints = mutableMapOf<String, Int>()
                // スキル系統1-5
                for (si in 0 until 5) {
                    val familyIdx = 15 + si * 2
                    val pointIdx = 16 + si * 2
                    if (familyIdx < cols.size && pointIdx < cols.size) {
                        val family = cols[familyIdx]
                        val pts = cols[pointIdx].toIntOrNull() ?: 0
                        if (family.isNotBlank() && pts != 0) {
                            skillPoints[family] = pts
                        }
                    }
                }
                val materials = mutableListOf<Pair<String, Int>>()
                for (mi in 0 until 4) {
                    val nameIdx = 25 + mi * 2
                    val countIdx = 26 + mi * 2
                    if (nameIdx < cols.size && countIdx < cols.size) {
                        val mat = cols[nameIdx]
                        val cnt = cols[countIdx].toIntOrNull() ?: 0
                        if (mat.isNotBlank()) materials.add(Pair(mat, cnt))
                    }
                }
                Equipment(
                    name = cols[0],
                    gender = cols[1].toIntOrNull() ?: 0,
                    type = cols[2].toIntOrNull() ?: 0,
                    rarity = cols[3].toIntOrNull() ?: 1,
                    slotCount = cols[4].toIntOrNull() ?: 0,
                    hallStars = cols[5].toIntOrNull() ?: 99,
                    villageStars = cols[6].toIntOrNull() ?: 99,
                    starsMode = cols[7].toIntOrNull() ?: 0,
                    defenseInit = cols[8].toIntOrNull() ?: 0,
                    defenseFinal = cols[9].toIntOrNull() ?: 0,
                    fireRes = cols[10].toIntOrNull() ?: 0,
                    waterRes = cols[11].toIntOrNull() ?: 0,
                    thunderRes = cols[12].toIntOrNull() ?: 0,
                    iceRes = cols[13].toIntOrNull() ?: 0,
                    dragonRes = cols[14].toIntOrNull() ?: 0,
                    skillPoints = skillPoints,
                    slot = slot,
                    materials = materials
                )
            } catch (e: Exception) { null }
        }
    }

    private fun loadDecorations(context: Context): List<Decoration> {
        val rows = readCsv(context, "MHXX_DECO.csv")
        return rows.mapNotNull { cols ->
            if (cols.size < 8) return@mapNotNull null
            try {
                val skillPoints = mutableMapOf<String, Int>()
                for (si in 0 until 2) {
                    val familyIdx = 6 + si * 2
                    val pointIdx = 7 + si * 2
                    if (familyIdx < cols.size && pointIdx < cols.size) {
                        val family = cols[familyIdx]
                        val pts = cols[pointIdx].toIntOrNull() ?: 0
                        if (family.isNotBlank() && pts != 0) {
                            skillPoints[family] = pts
                        }
                    }
                }
                Decoration(
                    name = cols[0],
                    rarity = cols[1].toIntOrNull() ?: 1,
                    slotSize = cols[2].toIntOrNull() ?: 1,
                    hrRequired = cols[3].toIntOrNull() ?: 0,
                    villageRequired = cols[4].toIntOrNull() ?: 0,
                    starsMode = cols[5].toIntOrNull() ?: 0,
                    skillPoints = skillPoints
                )
            } catch (e: Exception) { null }
        }
    }

    private fun loadCharms(context: Context): List<Charm> {
        val rows = readCsv(context, "MHXX_CHARM.csv")
        return rows.mapNotNull { cols ->
            if (cols.size < 3) return@mapNotNull null
            try {
                Charm(
                    name = cols[0],
                    rarity = cols[1].toIntOrNull() ?: 1,
                    hallStars = cols[2].toIntOrNull() ?: 1
                )
            } catch (e: Exception) { null }
        }
    }

    private fun loadCategories(context: Context): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        context.assets.open("data/conf/CATEGORY.txt").bufferedReader().useLines { seq ->
            seq.filter { it.isNotBlank() }.forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 2) {
                    result[parts[0]] = parts.drop(1).filter { it.isNotBlank() }
                }
            }
        }
        return result
    }

    private fun loadCompoundSkills(context: Context): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        context.assets.open("data/conf/FUKUGO.txt").bufferedReader().useLines { seq ->
            seq.filter { it.isNotBlank() }.forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 2) {
                    result[parts[0]] = parts.drop(1).filter { it.isNotBlank() }
                }
            }
        }
        return result
    }
}
