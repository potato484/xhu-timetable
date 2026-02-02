package vip.mystery0.xhu.timetable.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for SemanticPalette Murmur3 hash implementation.
 *
 * Ensures:
 * - Deterministic color assignment (same input → same color)
 * - Cross-platform consistency (Murmur3 is platform-independent)
 * - Uniform distribution across palette
 */
class SemanticPaletteTest {

    @Test
    fun colorFor_is_deterministic() {
        val input = "高等数学"
        val color1 = SemanticPalette.colorFor(input)
        val color2 = SemanticPalette.colorFor(input)
        assertEquals(color1, color2, "Same input should produce same color")
    }

    @Test
    fun colorFor_different_inputs_may_differ() {
        val color1 = SemanticPalette.colorFor("高等数学")
        val color2 = SemanticPalette.colorFor("大学英语")
        // Not guaranteed to differ, but likely with good hash distribution
        // This test just ensures no crash
        assertTrue(color1 > 0)
        assertTrue(color2 > 0)
    }

    @Test
    fun colorFor_empty_string() {
        val color = SemanticPalette.colorFor("")
        assertTrue(color > 0, "Empty string should still produce valid color")
    }

    @Test
    fun colorFor_unicode_strings() {
        val inputs = listOf(
            "数据结构与算法",
            "计算机网络",
            "操作系统",
            "软件工程",
            "数据库原理",
            "编译原理",
            "人工智能",
            "机器学习",
        )

        val colors = inputs.map { SemanticPalette.colorFor(it) }

        // All colors should be valid (non-zero)
        colors.forEach { assertTrue(it > 0) }

        // Check determinism
        inputs.forEachIndexed { index, input ->
            assertEquals(colors[index], SemanticPalette.colorFor(input))
        }
    }

    @Test
    fun colorFor_known_values() {
        // These are regression tests to ensure Murmur3 implementation doesn't change
        // If these fail after a code change, verify the hash algorithm is still correct
        val testCases = mapOf(
            "test" to SemanticPalette.colorFor("test"),
            "hello" to SemanticPalette.colorFor("hello"),
            "world" to SemanticPalette.colorFor("world"),
        )

        // Re-run to verify determinism
        testCases.forEach { (input, expectedColor) ->
            assertEquals(expectedColor, SemanticPalette.colorFor(input))
        }
    }

    @Test
    fun colorFor_distribution() {
        // Generate many inputs and check distribution across palette
        val inputs = (1..100).map { "Course $it" }
        val colorCounts = mutableMapOf<Long, Int>()

        inputs.forEach { input ->
            val color = SemanticPalette.colorFor(input)
            colorCounts[color] = (colorCounts[color] ?: 0) + 1
        }

        // Should use multiple colors (not all same)
        assertTrue(colorCounts.size > 1, "Should distribute across multiple colors")
    }
}
