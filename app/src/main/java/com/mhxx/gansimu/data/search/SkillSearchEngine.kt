package com.mhxx.gansimu.data.search

import com.mhxx.gansimu.data.loader.CsvLoader
import com.mhxx.gansimu.data.models.*
import kotlin.math.min

/**
 * スキルシミュレーター 検索エンジン
 * 指定したスキルを発動できる防具の組み合わせを検索する
 */
class SkillSearchEngine {

    companion object {
        private const val MAX_RESULTS = 100
        private const val NULL_EQUIP_NAME = "装備なし"
    }

    /**
     * スキル検索メイン関数
     * @param desiredSkills 欲しいスキルのリスト
     * @param settings 検索設定
     * @return 検索結果リスト
     */
    fun search(
        desiredSkills: List<DesiredSkill>,
        settings: SearchSettings
    ): List<SearchResult> {
        if (desiredSkills.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResult>()

        // 必要なスキル系統とポイントをMapに変換
        val requiredFamilyPoints = desiredSkills.associate { it.familyName to it.requiredPoints }

        // 各スロット用の防具リストを作成（フィルタ適用済み）
        val filteredEquip = getFilteredEquipments(settings)
        val filteredDecos = getFilteredDecorations(settings)

        // ヌル防具（装備なし）を追加
        val nullEquip = createNullEquipment()
        val heads = (filteredEquip[EquipSlot.HEAD] ?: emptyList()) + nullEquip
        val bodies = (filteredEquip[EquipSlot.BODY] ?: emptyList()) + nullEquip
        val arms = (filteredEquip[EquipSlot.ARM] ?: emptyList()) + nullEquip
        val wsts = (filteredEquip[EquipSlot.WST] ?: emptyList()) + nullEquip
        val legs = (filteredEquip[EquipSlot.LEG] ?: emptyList()) + nullEquip

        // 固定防具があれば適用
        val fixedHead = settings.fixedEquipments[EquipSlot.HEAD]?.let {
            heads.find { e -> e.name == it }
        }
        val fixedBody = settings.fixedEquipments[EquipSlot.BODY]?.let {
            bodies.find { e -> e.name == it }
        }
        val fixedArm = settings.fixedEquipments[EquipSlot.ARM]?.let {
            arms.find { e -> e.name == it }
        }
        val fixedWst = settings.fixedEquipments[EquipSlot.WST]?.let {
            wsts.find { e -> e.name == it }
        }
        val fixedLeg = settings.fixedEquipments[EquipSlot.LEG]?.let {
            legs.find { e -> e.name == it }
        }

        val searchHeads = if (fixedHead != null) listOf(fixedHead) else heads
        val searchBodies = if (fixedBody != null) listOf(fixedBody) else bodies
        val searchArms = if (fixedArm != null) listOf(fixedArm) else arms
        val searchWsts = if (fixedWst != null) listOf(fixedWst) else wsts
        val searchLegs = if (fixedLeg != null) listOf(fixedLeg) else legs

        // 枝刈り用：各スロットの最大スキルポイントを計算
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

                            // 5部位のスキルポイント合計
                            val armorPoints = combineSkillPoints(
                                head.skillPoints,
                                body.skillPoints,
                                arm.skillPoints,
                                wst.skillPoints,
                                leg.skillPoints
                            )

                            // 総スロット数
                            val totalSlots = head.slotCount + body.slotCount + arm.slotCount +
                                    wst.slotCount + leg.slotCount

                            // 装飾品+護石で補えるか試算
                            val result = tryCompleteWithDecoAndCharm(
                                head, body, arm, wst, leg,
                                armorPoints, totalSlots,
                                requiredFamilyPoints,
                                filteredDecos,
                                settings
                            )

                            if (result != null) {
                                results.add(result)
                            }
                        }
                    }
                }
            }
        }

        // 入手難度でソート
        return results.sortedBy { it.difficultyScore }.take(settings.maxResults)
    }

    /**
     * 装飾品と護石で不足ポイントを補完し、発動可能か判定する
     */
    private fun tryCompleteWithDecoAndCharm(
        head: Equipment, body: Equipment, arm: Equipment, wst: Equipment, leg: Equipment,
        armorPoints: Map<String, Int>,
        totalSlots: Int,
        requiredFamilyPoints: Map<String, Int>,
        filteredDecos: List<Decoration>,
        settings: SearchSettings
    ): SearchResult? {
        // 不足ポイントを計算
        val shortage = mutableMapOf<String, Int>()
        for ((family, required) in requiredFamilyPoints) {
            val have = armorPoints[family] ?: 0
            if (have < required) {
                shortage[family] = required - have
            }
        }

        // 使用装飾品を選択
        val usedDecos = mutableListOf<DecoUsage>()
        val decoPoints = mutableMapOf<String, Int>()
        var remainSlots = totalSlots

        // 装飾品で補填（グリーディー）
        val sortedDecos = filteredDecos.sortedByDescending { deco ->
            deco.skillPoints.entries.sumOf { (family, pts) ->
                if (shortage.containsKey(family)) pts else 0
            }
        }

        for (deco in sortedDecos) {
            if (remainSlots <= 0) break
            if (deco.slotSize > remainSlots) continue

            val useful = deco.skillPoints.any { (family, pts) ->
                pts > 0 && (shortage[family] ?: 0) > 0
            }
            if (!useful) continue

            val maxCount = remainSlots / deco.slotSize
            var useCount = 0
            for (i in 1..maxCount) {
                val stillNeeded = deco.skillPoints.any { (family, pts) ->
                    pts > 0 && ((shortage[family] ?: 0) - (decoPoints[family] ?: 0) - pts * i + (armorPoints[family] ?: 0)) < requiredFamilyPoints.getOrDefault(family, Int.MAX_VALUE)
                }
                if (stillNeeded || useCount == 0) useCount = i else break
            }
            if (useCount > 0) {
                usedDecos.add(DecoUsage(deco, useCount))
                remainSlots -= deco.slotSize * useCount
                for ((family, pts) in deco.skillPoints) {
                    decoPoints[family] = (decoPoints[family] ?: 0) + pts * useCount
                }
            }
        }

        // 装飾品+防具で発動するか
        val totalWithDeco = combineSkillPoints(armorPoints, decoPoints)

        // まだ不足している分を護石で補うべきか計算
        val charmShortage = mutableMapOf<String, Int>()
        for ((family, required) in requiredFamilyPoints) {
            val have = totalWithDeco[family] ?: 0
            if (have < required) {
                charmShortage[family] = required - have
            }
        }

        // 護石なしで全スキル発動できる場合
        if (charmShortage.isEmpty()) {
            return buildResult(head, body, arm, wst, leg, usedDecos, totalWithDeco, settings, remainSlots)
        }

        // 護石スロットで補填を試みる
        if (settings.availableCharm != null) {
            val charm = settings.availableCharm
            val charmPoints = charm.skillPoints.toMutableMap()
            val charmSlotDecos = tryFillSlotsWithDecos(charm.slots, charmShortage, filteredDecos, decoPoints, armorPoints, totalWithDeco)
            val totalWithCharm = combineSkillPoints(totalWithDeco, charmPoints,
                charmSlotDecos.second.associate { (deco, cnt) -> deco.skillPoints.entries.firstOrNull()?.key to ((decoPoints[deco.skillPoints.entries.firstOrNull()?.key] ?: 0)) }.filterKeys { it != null } as Map<String, Int>)

            val stillShort = requiredFamilyPoints.any { (family, required) ->
                (totalWithCharm[family] ?: 0) < required
            }
            if (!stillShort) {
                return buildResult(head, body, arm, wst, leg,
                    usedDecos + charmSlotDecos.second, totalWithCharm, settings, remainSlots,
                    charm = charm, charmPoints = charmPoints)
            }
        }

        // 護石なし・固定護石ありでも補えない場合は、要求護石を返す
        // (charm search modeではここで護石要件を返すが、skill search modeではnullを返す)
        if (settings.availableCharm == null && charmShortage.isNotEmpty()) {
            // 護石で全て補えるケースのみ保留
            val charmFamilies = charmShortage.entries.take(2)
            if (charmFamilies.size <= 2) {
                val f1 = charmFamilies.getOrNull(0)
                val f2 = charmFamilies.getOrNull(1)
                return SearchResult(
                    head = head.takeIf { it.name != NULL_EQUIP_NAME },
                    body = body.takeIf { it.name != NULL_EQUIP_NAME },
                    arm = arm.takeIf { it.name != NULL_EQUIP_NAME },
                    wst = wst.takeIf { it.name != NULL_EQUIP_NAME },
                    leg = leg.takeIf { it.name != NULL_EQUIP_NAME },
                    charmSlot1Family = f1?.key ?: "",
                    charmSlot1Points = f1?.value ?: 0,
                    charmSlot2Family = f2?.key ?: "",
                    charmSlot2Points = f2?.value ?: 0,
                    charmSlots = min(remainSlots, 3),
                    decorations = usedDecos,
                    activeSkills = getActiveSkills(totalWithDeco, requiredFamilyPoints),
                    difficultyScore = calcDifficulty(head, body, arm, wst, leg),
                    existsProbability = calcExistsProbability(f1?.key ?: "", f1?.value ?: 0, f2?.key ?: "", f2?.value ?: 0, min(remainSlots, 3), false),
                    existsProbabilityOld = calcExistsProbability(f1?.key ?: "", f1?.value ?: 0, f2?.key ?: "", f2?.value ?: 0, min(remainSlots, 3), true)
                )
            }
        }

        return null
    }

    private fun tryFillSlotsWithDecos(
        slots: Int, shortage: Map<String, Int>, decos: List<Decoration>,
        existingDecoPoints: Map<String, Int>, armorPoints: Map<String, Int>,
        currentTotal: Map<String, Int>
    ): Pair<Int, List<DecoUsage>> {
        if (slots <= 0) return Pair(0, emptyList())
        val usedDecos = mutableListOf<DecoUsage>()
        var remainSlots = slots
        val addedPoints = mutableMapOf<String, Int>()

        for (deco in decos.filter { it.slotSize <= slots }) {
            if (remainSlots < deco.slotSize) continue
            val useful = deco.skillPoints.any { (family, pts) ->
                pts > 0 && (shortage[family] ?: 0) > ((addedPoints[family] ?: 0))
            }
            if (!useful) continue
            val count = remainSlots / deco.slotSize
            usedDecos.add(DecoUsage(deco, count))
            for ((fam, pts) in deco.skillPoints) {
                addedPoints[fam] = (addedPoints[fam] ?: 0) + pts * count
            }
            remainSlots -= deco.slotSize * count
        }
        return Pair(remainSlots, usedDecos)
    }

    private fun buildResult(
        head: Equipment, body: Equipment, arm: Equipment, wst: Equipment, leg: Equipment,
        decos: List<DecoUsage>,
        totalPoints: Map<String, Int>,
        settings: SearchSettings,
        remainSlots: Int,
        charm: Charm? = null,
        charmPoints: Map<String, Int> = emptyMap()
    ): SearchResult {
        val activeSkills = getActiveSkillNames(totalPoints)
        return SearchResult(
            head = head.takeIf { it.name != NULL_EQUIP_NAME },
            body = body.takeIf { it.name != NULL_EQUIP_NAME },
            arm = arm.takeIf { it.name != NULL_EQUIP_NAME },
            wst = wst.takeIf { it.name != NULL_EQUIP_NAME },
            leg = leg.takeIf { it.name != NULL_EQUIP_NAME },
            charmSlot1Family = charmPoints.entries.toList().getOrNull(0)?.key ?: "",
            charmSlot1Points = charmPoints.entries.toList().getOrNull(0)?.value ?: 0,
            charmSlot2Family = charmPoints.entries.toList().getOrNull(1)?.key ?: "",
            charmSlot2Points = charmPoints.entries.toList().getOrNull(1)?.value ?: 0,
            charmSlots = charm?.slots ?: 0,
            decorations = decos,
            activeSkills = activeSkills,
            difficultyScore = calcDifficulty(head, body, arm, wst, leg),
            existsProbability = calcExistsProbability(
                charmPoints.entries.toList().getOrNull(0)?.key ?: "",
                charmPoints.entries.toList().getOrNull(0)?.value ?: 0,
                charmPoints.entries.toList().getOrNull(1)?.key ?: "",
                charmPoints.entries.toList().getOrNull(1)?.value ?: 0,
                charm?.slots ?: 0,
                false
            ),
            existsProbabilityOld = calcExistsProbability(
                charmPoints.entries.toList().getOrNull(0)?.key ?: "",
                charmPoints.entries.toList().getOrNull(0)?.value ?: 0,
                charmPoints.entries.toList().getOrNull(1)?.key ?: "",
                charmPoints.entries.toList().getOrNull(1)?.value ?: 0,
                charm?.slots ?: 0,
                true
            )
        )
    }

    private fun getFilteredEquipments(settings: SearchSettings): Map<EquipSlot, List<Equipment>> {
        return CsvLoader.getAllEquipments().mapValues { (_, equipList) ->
            equipList.filter { equip ->
                // 除外装備除外
                equip.name !in settings.excludedEquipments &&
                // 性別フィルタ
                (settings.gender == 0 || equip.gender == 0 || equip.gender == settings.gender) &&
                // タイプフィルタ
                (settings.weaponType == 0 || equip.type == 0 || equip.type == settings.weaponType)
            }
        }
    }

    private fun getFilteredDecorations(settings: SearchSettings): List<Decoration> {
        return CsvLoader.getDecorations().filter { deco ->
            deco.name !in settings.excludedDecorations
        }
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

    private fun getActiveSkills(points: Map<String, Int>, required: Map<String, Int>): List<String> {
        return required.keys.filter { family ->
            (points[family] ?: 0) >= (required[family] ?: Int.MAX_VALUE)
        }.mapNotNull { family ->
            CsvLoader.getSkillsByFamily(family)
                .filter { it.points > 0 }
                .firstOrNull { skill ->
                    (points[family] ?: 0) >= skill.points
                }?.name
        }
    }

    private fun getActiveSkillNames(points: Map<String, Int>): List<String> {
        val active = mutableListOf<String>()
        val skills = CsvLoader.getSkills()
        for ((family, pts) in points) {
            val familySkills = skills.filter { it.family == family && it.points > 0 }
            val activated = familySkills.filter { pts >= it.points }
            if (activated.isNotEmpty()) {
                activated.maxByOrNull { it.points }?.let { active.add(it.name) }
            }
        }
        return active
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

    /**
     * 護石の存在確率計算
     * MH4G方式・古参方式の切り替えに対応
     */
    fun calcExistsProbability(
        family1: String, pts1: Int,
        family2: String, pts2: Int,
        slots: Int,
        oldMode: Boolean
    ): String {
        if (family1.isEmpty() && pts1 == 0) return ""

        // 簡易計算（実際には系統別の確率テーブルが必要）
        // ここでは概算値を返す
        val baseProb = when {
            pts1 >= 8 -> 50000.0
            pts1 >= 7 -> 20000.0
            pts1 >= 6 -> 10000.0
            pts1 >= 5 -> 5000.0
            pts1 >= 4 -> 2000.0
            pts1 >= 3 -> 800.0
            pts1 >= 2 -> 300.0
            else -> 100.0
        }

        val slotMult = when (slots) {
            3 -> 5.0
            2 -> 2.0
            1 -> 1.5
            else -> 1.0
        }

        val family2Mult = if (family2.isNotEmpty() && pts2 > 0) {
            when {
                pts2 >= 6 -> 8.0
                pts2 >= 4 -> 4.0
                pts2 >= 2 -> 2.0
                else -> 1.5
            }
        } else 1.0

        val total = (baseProb * slotMult * family2Mult).toLong()
        return "1/$total"
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
