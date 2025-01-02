package com.imoonday.advskills_re.util

import kotlin.random.*

class DynamicDrawPool<P : Any, S : Any>(
    private val primaryItems: List<P>,
    private val secondaryItems: List<S>,
    private val getPrimaryWeight: (P) -> Int,
    private val getSecondaryWeight: (S) -> Int,
    private val primaryRatio: Double,
    private val secondaryRatio: Double,
    private val primarySkipCondition: (P) -> Boolean = { false },
    private val secondarySkipCondition: (S) -> Boolean = { false }
) {

    private val random = Random(System.currentTimeMillis())

    private data class Entry(val item: Any, val cumulativeWeight: Double)

    private fun buildCumulativePool(): List<Entry> {
        val pool = mutableListOf<Entry>()
        var cumulativeWeight = 0.0

        // 过滤掉跳过的项目，重新计算有效的权重总和
        val filteredPrimary = primaryItems.filterNot(primarySkipCondition)
        val filteredSecondary = secondaryItems.filterNot(secondarySkipCondition)

        val primaryTotalWeight = filteredPrimary.sumOf { getPrimaryWeight(it).coerceAtLeast(0) }
        val secondaryTotalWeight = filteredSecondary.sumOf { getSecondaryWeight(it).coerceAtLeast(0) }

        // 防止系数过小的动态调整
        val primaryCoefficient = primaryRatio / primaryTotalWeight.coerceAtLeast(1)
        val secondaryCoefficient = secondaryRatio / secondaryTotalWeight.coerceAtLeast(1)

        // 构建主条目权重池
        for (item in filteredPrimary) {
            cumulativeWeight += getPrimaryWeight(item) * primaryCoefficient
            pool.add(Entry(item, cumulativeWeight))
        }

        // 构建次条目权重池
        for (item in filteredSecondary) {
            cumulativeWeight += getSecondaryWeight(item) * secondaryCoefficient
            pool.add(Entry(item, cumulativeWeight))
        }

        return pool
    }

    fun drawSingle(): Any? = randomDraw(buildCumulativePool())

    // 抽取单个条目
    private fun randomDraw(cumulativePool: List<Entry>): Any? {
        if (cumulativePool.isEmpty()) return null
        val totalWeight = cumulativePool.last().cumulativeWeight
        val randomValue = random.nextDouble() * totalWeight // 使用 Double 随机数

        return cumulativePool.firstOrNull { it.cumulativeWeight >= randomValue }?.item
    }

    // 抽取多个条目
    fun drawMultiple(count: Int, default: Any): List<Any> {
        val cumulativePool = buildCumulativePool()
        val results = mutableListOf<Any>() // 用集合避免重复

        if (cumulativePool.size < count) {
            return List(count) { cumulativePool.elementAtOrNull(it)?.item ?: default }
        }

        repeat(count) {
            while (true) {
                val drawn = randomDraw(cumulativePool)

                if (drawn == null) {
                    results.add(default)
                    break
                }

                if (!results.contains(drawn)) {
                    results.add(drawn)
                    break
                }
            }
        }

        return List(count) { results.elementAtOrNull(it) ?: default }
    }
}
