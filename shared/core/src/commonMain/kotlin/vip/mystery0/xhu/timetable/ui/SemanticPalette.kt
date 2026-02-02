package vip.mystery0.xhu.timetable.ui

/**
 * Semantic color palette for course items.
 *
 * Uses Murmur3 hash for deterministic, cross-platform color assignment.
 * The same course name will always get the same color, regardless of platform.
 *
 * IMPLEMENTATION NOTE: Manual Murmur3 implementation is used because:
 * - Kotlin stdlib hashCode() is not guaranteed to be consistent across platforms
 * - No common KMP library provides Murmur3 in commonMain
 * - The implementation is well-tested and follows the reference spec
 *
 * @see SemanticPaletteTest for verification tests
 */
object SemanticPalette {
    private val COLORS = listOf(
        0xFF5C6BC0, // Indigo
        0xFF26A69A, // Teal
        0xFFEF5350, // Red
        0xFFAB47BC, // Purple
        0xFF42A5F5, // Blue
        0xFFFF7043, // Deep Orange
        0xFF66BB6A, // Green
        0xFFFFA726, // Orange
    )

    fun colorFor(id: String): Long = COLORS[(murmur3(id).toUInt() % COLORS.size.toUInt()).toInt()]

    private fun murmur3(key: String): Int {
        val data = key.encodeToByteArray()
        val len = data.size
        val c1 = 0xcc9e2d51.toInt()
        val c2 = 0x1b873593
        var h1 = 0
        val nblocks = len / 4

        for (i in 0 until nblocks) {
            val i4 = i * 4
            var k1 = (data[i4].toInt() and 0xff) or
                    ((data[i4 + 1].toInt() and 0xff) shl 8) or
                    ((data[i4 + 2].toInt() and 0xff) shl 16) or
                    ((data[i4 + 3].toInt() and 0xff) shl 24)
            k1 *= c1
            k1 = k1.rotateLeft(15)
            k1 *= c2
            h1 = h1 xor k1
            h1 = h1.rotateLeft(13)
            h1 = h1 * 5 + 0xe6546b64.toInt()
        }

        var k1 = 0
        val tail = nblocks * 4
        when (len and 3) {
            3 -> {
                k1 = k1 xor ((data[tail + 2].toInt() and 0xff) shl 16)
                k1 = k1 xor ((data[tail + 1].toInt() and 0xff) shl 8)
                k1 = k1 xor (data[tail].toInt() and 0xff)
                k1 *= c1
                k1 = k1.rotateLeft(15)
                k1 *= c2
                h1 = h1 xor k1
            }
            2 -> {
                k1 = k1 xor ((data[tail + 1].toInt() and 0xff) shl 8)
                k1 = k1 xor (data[tail].toInt() and 0xff)
                k1 *= c1
                k1 = k1.rotateLeft(15)
                k1 *= c2
                h1 = h1 xor k1
            }
            1 -> {
                k1 = k1 xor (data[tail].toInt() and 0xff)
                k1 *= c1
                k1 = k1.rotateLeft(15)
                k1 *= c2
                h1 = h1 xor k1
            }
        }

        h1 = h1 xor len
        h1 = h1 xor (h1 ushr 16)
        h1 *= 0x85ebca6b.toInt()
        h1 = h1 xor (h1 ushr 13)
        h1 *= 0xc2b2ae35.toInt()
        h1 = h1 xor (h1 ushr 16)

        return h1
    }

    private fun Int.rotateLeft(bits: Int): Int = (this shl bits) or (this ushr (32 - bits))
}
