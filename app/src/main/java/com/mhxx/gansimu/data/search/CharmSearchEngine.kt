package com.mhxx.gansimu.data.search

import com.mhxx.gansimu.data.loader.CsvLoader
import com.mhxx.gansimu.data.models.*
import kotlin.math.min

/**
 * 要求護石検索エンジン
 * スキルから、どんな護石が必要かを逆算して検索する
 * 頑シミュの特徴的な機能
 */
class CharmSearchEngine {

    companion object {
        private const val MAX_RESULTS = 50
        private const val NULL_EQUIP_NAME = "装備なし"
        // 護石スロット最大数
        private const val MAX_CHARM_SLOTS = 3
        // 護石スキルポイント最大値（系統ごと）
        private val CHARM_MAX_POINTS_BY_RARITY = mapOf(
            1 to 4, 2 to 5, 3 to 6, 4 to 7, 5 to 8,
            6 to 9, 7 to 10, 8 to 10, 9 to 10, 10 to 10
        )
    }

    /**
     * 要求護石検索
     * @param desiredSkills 欲しいスキルリスト
     * @param settings 検索設定
     * @return 要求護石リスト（どんな護石があれば発動できるか）
     */
    fun searchRequiredCharms(
        desiredSkills: List<DesiredSkill>,
        settings: SearchSettings
    ): List<CharmSearchResult> {
        if (desiredSkills.isEmpty()) return emptyList()

        val results = mutableListOf<CharmSearchResult>()
        val requiredFamilyPoints = desiredSkills.associate { it.familyName to it.requiredPoints }

        val filteredEquip = getFilteredEquipments(settings)
        val filteredDecos = getFilteredDecorations(settings)

        val nullEquip = createNullEquipment()
        val heads = (filteredEquip[EquipSlot.HEAD] ?: emptyList()) + nullEquip
        val bodies = (filteredEquip[EquipSlot.BODY] ?: emptyList()) + nullEquip
        val arms = (filteredEquip[EquipSlot.ARM] ?: emptyList()) + nullEquip
        val wsts = (filteredEquip[EquipSlot.WST] ?: emptyList()) + nullEquip
        val legs = (filteredEquip[EquipSlot.LEG] ?: emptyList()) + nullEquip

        val fixedHead = settings.fixedEquipments[EquipSlot.HEAD]?.let { heads.find { e -> e.name == it } }
        val fixedBody = settings.fixedEquipments[EquipSlot.BODY]?.let { bodies.find { e -> e.name == it } }
        val fixedArm = settings.fixedEquipments[EquipSlot.ARM]?.let { arms.find { e -> e.name == it } }
        val fixedWst = settings.fixedEquipments[EquipSlot.WST]?.let { wsts.find { e -> e.name == it } }
        val fixedLeg = settings.fixedEquipments[EquipSlot.LEG]?.let { legs.find { e -> e.name == it } }

        val searchHeads = if (fixedHead != null) listOf(fixedHead) else heads
        val searchBodies = if (fixedBody != null) listOf(fixedBody) else bodies
        val searchArms = if (fixedArm != null) listOf(fixedArm) else arms
        val searchWsts = if (fixedWst != null) listOf(fixedWst) else wsts
        val searchLegs = if (fixedLeg != null) listOf(fixedLeg) else legs

        for (head in searchHeads) {
            if (results.size >= MAX_RESULTS) break
            for (body in searchBodies) {
                if (results.size >= MAX_RESULTS) break
                for (arm in searchArms) {
                    if (results.size >= MAX_RESULTS) break
                    for (wst in searchWsts) {
                        if (results.size >= MAX_RESULTS) break
                        for (leg in searchLegs) {
                            if (results.size >= MAX_RESULTS) break

                            val armorPoints = combineSkillPoints(
                                head.skillPoints, body.skillPoints, arm.skillPoints,
                                wst.skillPoints, leg.skillPoints
                            )
                            val totalSlots = head.slotCount + body.slotCount + arm.slotCount +
                                    wst.slotCount + leg.slotCount

                            // 装飾品で補填可能な最大ポイントを計算
                            val (decoPoints, usedDecos, remainSlots) = fillWithDecorations(
                                totalSlots, armorPoints, requiredFamilyPoints, filteredDecos
                            )

                            val totalWithDeco = combineSkillPoints(armorPoints, decoPoints)

                            // 護石なしで発動する場合
                            val allActivated = requiredFamilyPoints.all { (family, required) ->
                                (totalWithDeco[family] ?: 0) >= required
                            }

                            if (allActivated) {
                                // 護石不要のケース
                                results.add(CharmSearchResult(
                                    charmFamily1 = "",
                                    charmPoints1 = 0,
                                    charmFamily2 = "",
                                    charmPoints2 = 0,
                                    charmSlots = remainSlots,
                                    charmRarity = 0,
                                    head = head.takeIf { it.name != NULL_EQUIP_NAME },
                                    body = body.takeIf { it.name != NULL_EQUIP_NAME },
                                    arm = arm.takeIf { it.name != NULL_EQUIP_NAME },
                                    wst = wst.takeIf { it.name != NULL_EQUIP_NAME },
                                    leg = leg.takeIf { it.name != NULL_EQUIP_NAME },
                                    decorations = usedDecos,
                                    difficultyScore = calcDifficulty(head, body, arm, wst, leg),
                                    existsProbabilityMH4G = "護石不要",
                                    existsProbabilityOld = "護石不要"
                                ))
                                continue
                            }

                            // 護石で補填が必要な場合
                            val shortage = requiredFamilyPoints.entries
                                .filter { (family, required) ->
                                    (totalWithDeco[family] ?: 0) < required
                                }
                                .associate { (family, required) ->
                                    family to (required - (totalWithDeco[family] ?: 0))
                                }

                            if (shortage.size > 2) continue  // 護石は最大2系統

                            // 護石スロットで補填できる分も考慮
                            for (charmSlots in 0..MAX_CHARM_SLOTS) {
                                if (results.size >= MAX_RESULTS) break

                                val charmFamilies = shortage.entries.toList()
                                val f1 = charmFamilies.getOrNull(0)
                                val f2 = charmFamilies.getOrNull(1)

                                if (f1 == null) continue

                                // 護石スロットで装飾品を追加して補填できるか
                                val charmDecoPoints = mutableMapOf<String, Int>()
                                var charmRemainSlots = charmSlots
                                val charmDecos = mutableListOf<DecoUsage>()

                                for (deco in filteredDecos.filter { it.slotSize <= charmSlots }) {
                                    if (charmRemainSlots < deco.slotSize) continue
                                    val useful = deco.skillPoints.any { (family, pts) ->
                                        pts > 0 && ((shortage[family] ?: 0) - (charmDecoPoints[family] ?: 0)) > 0
                                    }
                                    if (!useful) continue
                                    val cnt = charmRemainSlots / deco.slotSize
                                    charmDecos.add(DecoUsage(deco, cnt))
                                    for ((fam, pts) in deco.skillPoints) {
                                        charmDecoPoints[fam] = (charmDecoPoints[fam] ?: 0) + pts * cnt
                                    }
                                    charmRemainSlots -= deco.slotSize * cnt
                                }

                                // 護石本体のスキルポイント（装飾品補填後の残余）
                                val remainShortage = shortage.entries
                                    .associate { (fam, req) ->
                                        fam to maxOf(0, req - (charmDecoPoints[fam] ?: 0))
                                    }
                                    .filter { it.value > 0 }

                                if (remainShortage.size > 2) continue
                                if (remainShortage.isEmpty()) {
                                    // 護石のスロットだけで補填できた
                                    val decoFam = charmDecos.firstOrNull()?.decoration?.skillPoints?.entries?.firstOrNull()?.key ?: ""
                                    val decoSz = charmDecos.firstOrNull()?.decoration?.slotSize ?: 0

                                    results.add(CharmSearchResult(
                                        charmFamily1 = "",
                                        charmPoints1 = 0,
                                        charmFamily2 = "",
                                        charmPoints2 = 0,
                                        charmSlots = charmSlots,
                                        charmRarity = 1,
                                        decoFamily = decoFam,
                                        decoSize = decoSz,
                                        head = head.takeIf { it.name != NULL_EQUIP_NAME },
                                        body = body.takeIf { it.name != NULL_EQUIP_NAME },
                                        arm = arm.takeIf { it.name != NULL_EQUIP_NAME },
                                        wst = wst.takeIf { it.name != NULL_EQUIP_NAME },
                                        leg = leg.takeIf { it.name != NULL_EQUIP_NAME },
                                        decorations = usedDecos + charmDecos,
                                        difficultyScore = calcDifficulty(head, body, arm, wst, leg),
                                        existsProbabilityMH4G = calcExistsProbability("", 0, "", 0, charmSlots, false),
                                        existsProbabilityOld = calcExistsProbability("", 0, "", 0, charmSlots, true)
                                    ))
                                    continue
                                }

                                val rf1 = remainShortage.entries.getOrNull(0)
                                val rf2 = remainShortage.entries.getOrNull(1)

                                if (rf1 == null) continue
                                val needed1 = rf1.value
                                val needed2 = rf2?.value ?: 0

                                // 護石レアリティ確認（必要ポイントが最大値以内か）
                                val minRarity = getMinRarityForPoints(maxOf(needed1, needed2))
                                if (minRarity == null) continue  // 護石で出せないポイント

                                val decoFam = charmDecos.firstOrNull()?.decoration?.skillPoints?.entries?.firstOrNull()?.key ?: ""
                                val decoSz = charmDecos.firstOrNull()?.decoration?.slotSize ?: 0

                                results.add(CharmSearchResult(
                                    charmFamily1 = rf1.key,
                                    charmPoints1 = needed1,
                                    charmFamily2 = rf2?.key ?: "",
                                    charmPoints2 = needed2,
                                    charmSlots = charmSlots,
                                    charmRarity = minRarity,
                                    decoFamily = decoFam,
                                    decoSize = decoSz,
                                    head = head.takeIf { it.name != NULL_EQUIP_NAME },
                                    body = body.takeIf { it.name != NULL_EQUIP_NAME },
                                    arm = arm.takeIf { it.name != NULL_EQUIP_NAME },
                                    wst = wst.takeIf { it.name != NULL_EQUIP_NAME },
                                    leg = leg.takeIf { it.name != NULL_EQUIP_NAME },
                                    decorations = usedDecos + charmDecos,
                                    difficultyScore = calcDifficulty(head, body, arm, wst, leg),
                                    existsProbabilityMH4G = calcExistsProbability(rf1.key, needed1, rf2?.key ?: "", needed2, charmSlots, false),
                                    existsProbabilityOld = calcExistsProbability(rf1.key, needed1, rf2?.key ?: "", needed2, charmSlots, true)
                                ))
                            }
                        }
                    }
                }
            }
        }

        return results.sortedWith(
            compareBy({ it.charmRarity }, { it.charmPoints1 + it.charmPoints2 }, { it.difficultyScore })
        ).take(settings.maxResults)
    }

    private fun fillWithDecorations(
        totalSlots: Int,
        armorPoints: Map<String, Int>,
        requiredFamilyPoints: Map<String, Int>,
        decos: List<Decoration>
    ): Triple<Map<String, Int>, List<DecoUsage>, Int> {
        val decoPoints = mutableMapOf<String, Int>()
        val usedDecos = mutableListOf<DecoUsage>()
        var remainSlots = totalSlots

        val sortedDecos = decos.sortedByDescending { deco ->
            deco.skillPoints.entries.sumOf { (family, pts) ->
                val need = (requiredFamilyPoints[family] ?: 0) - (armorPoints[family] ?: 0)
                if (need > 0 && pts > 0) pts else 0
            }
        }

        for (deco in sortedDecos) {
            if (remainSlots < deco.slotSize) continue
            val useful = deco.skillPoints.any { (family, pts) ->
                pts > 0 &&
                ((requiredFamilyPoints[family] ?: 0) - (armorPoints[family] ?: 0) - (decoPoints[family] ?: 0)) > 0
            }
            if (!useful) continue

            val maxCount = remainSlots / deco.slotSize
            if (maxCount <= 0) continue

            usedDecos.add(DecoUsage(deco, maxCount))
            for ((fam, pts) in deco.skillPoints) {
                decoPoints[fam] = (decoPoints[fam] ?: 0) + pts * maxCount
            }
            remainSlots -= deco.slotSize * maxCount
        }

        return Triple(decoPoints, usedDecos, remainSlots)
    }

    private fun getMinRarityForPoints(points: Int): Int? {
        return CHARM_MAX_POINTS_BY_RARITY.entries
            .filter { it.value >= points }
            .minByOrNull { it.key }?.key
    }

    private fun calcExistsProbability(
        family1: String, pts1: Int,
        family2: String, pts2: Int,
        slots: Int,
        oldMode: Boolean
    ): String {
        if (family1.isEmpty() && pts1 == 0 && slots == 0) return "※本当に存在するかは不明"

        // 護石確率の簡易計算（系統別確率テーブルなしの概算）
        val baseProbability = when {
            pts1 >= 10 -> 1000000L
            pts1 >= 8 -> 200000L
            pts1 >= 7 -> 80000L
            pts1 >= 6 -> 30000L
            pts1 >= 5 -> 12000L
            pts1 >= 4 -> 5000L
            pts1 >= 3 -> 2000L
            pts1 >= 2 -> 800L
            pts1 >= 1 -> 300L
            else -> 100L
        }

        val slotDivider = when (slots) {
            3 -> 5L
            2 -> 2L
            1 -> 1L
            else -> 1L
        }

        val family2Multiplier = if (family2.isNotEmpty() && pts2 > 0) {
            when {
                pts2 >= 8 -> 30L
                pts2 >= 6 -> 15L
                pts2 >= 4 -> 8L
                pts2 >= 2 -> 4L
                else -> 2L
            }
        } else 1L

        val prob = baseProbability * family2Multiplier / slotDivider
        val label = if (oldMode) "古" else "風"
        return "$label 1/$prob"
    }

    private fun combineSkillPoints(vararg maps: Map<String, Int>): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        for (map in maps) {
            for ((key, value) in map) {
                result[key] = (result[key] ?: 0) + value
            }
        }
        return result
    }

    private fun calcDifficulty(
        head: Equipment, body: Equipment, arm: Equipment,
        wst: Equipment, leg: Equipment
    ): Double {
        val pieces = listOf(head, body, arm, wst, leg)
        return pieces.sumOf { e ->
            if (e.name == NULL_EQUIP_NAME) 0.0
            else {
                val h = if (e.hallStars == 99) 9999.0 else e.hallStars.toDouble()
                val v = if (e.villageStars == 99) 9999.0 else e.villageStars.toDouble()
                if (e.starsMode == 1) maxOf(h, v) else minOf(h, v)
            }
        }
    }

    private fun getFilteredEquipments(settings: SearchSettings): Map<EquipSlot, List<Equipment>> {
        return CsvLoader.getAllEquipments().mapValues { (_, equipList) ->
            equipList.filter { equip ->
                equip.name !in settings.excludedEquipments &&
                (settings.gender == 0 || equip.gender == 0 || equip.gender == settings.gender) &&
                (settings.weaponType == 0 || equip.type == 0 || equip.type == settings.weaponType)
            }
        }
    }

    private fun getFilteredDecorations(settings: SearchSettings): List<Decoration> {
        return CsvLoader.getDecorations().filter { deco ->
            deco.name !in settings.excludedDecorations
        }
    }

    private fun createNullEquipment() = listOf(Equipment(
        name = NULL_EQUIP_NAME,
        gender = 0, type = 0, rarity = 0, slotCount = 0,
        hallStars = 0, villageStars = 0, starsMode = 0,
        defenseInit = 0, defenseFinal = 0,
        fireRes = 0, waterRes = 0, thunderRes = 0, iceRes = 0, dragonRes = 0,
        skillPoints = emptyMap(), slot = EquipSlot.HEAD, materials = emptyList()
    ))
}
