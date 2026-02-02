package vip.mystery0.xhu.timetable.shared.ui.timetable

import kotlinx.datetime.LocalDate

internal data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean,
)

/**
 * 轻量农历转换（1900-2100）。
 *
 * 说明：
 * - 基准：1900-01-31 对应农历 1900-01-01
 * - 仅用于月历 UI 展示（农历日期 + 常见节日），不承担“国务院调休”一类的精确法定节假日安排。
 */
internal object ChineseLunarCalendar {
    private const val MIN_YEAR = 1900
    private const val MAX_YEAR = 2100
    private val BASE_DATE = LocalDate(1900, 1, 31)

    /**
     * 农历1900-2100的润大小信息表（hex）。
     * 参考来源广泛，常用于前端/后端农历换算实现。
     */
    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, //1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, //1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, //1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, //1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, //1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, //1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, //1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, //1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, //1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0, //1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, //2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, //2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, //2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, //2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, //2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, //2050-2059
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, //2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, //2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, //2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, //2090-2099
        0x0d520, //2100
    )

    fun solarToLunar(date: LocalDate): LunarDate? {
        if (date.year !in MIN_YEAR..MAX_YEAR) return null

        var offsetDays = date.toEpochDays() - BASE_DATE.toEpochDays()
        if (offsetDays < 0) return null

        var year = MIN_YEAR
        var yearDays = lunarYearDays(year)
        while (year < MAX_YEAR && offsetDays >= yearDays) {
            offsetDays -= yearDays
            year++
            yearDays = lunarYearDays(year)
        }

        val leapMonth = leapMonth(year)
        var month = 1
        var isLeap = false
        while (month <= 12) {
            val monthDays = if (isLeap) leapDays(year) else monthDays(year, month)
            if (offsetDays < monthDays) break
            offsetDays -= monthDays

            if (leapMonth != 0 && month == leapMonth && !isLeap) {
                isLeap = true
            } else {
                if (isLeap) isLeap = false
                month++
            }
        }

        val day = (offsetDays + 1).toInt()
        return LunarDate(
            year = year,
            month = month,
            day = day,
            isLeapMonth = isLeap,
        )
    }

    fun displayLunar(date: LocalDate): String? {
        val lunar = solarToLunar(date) ?: return null
        val monthText = lunarMonthText(lunar.month, lunar.isLeapMonth)
        val dayText = lunarDayText(lunar.day)
        return if (lunar.day == 1) monthText else dayText
    }

    fun festival(date: LocalDate): String? {
        // 常见阳历节日（固定日期）
        val solar = when (date.monthNumber to date.dayOfMonth) {
            1 to 1 -> "元旦"
            5 to 1 -> "劳动节"
            10 to 1 -> "国庆"
            else -> null
        }
        if (solar != null) return solar

        val lunar = solarToLunar(date) ?: return null

        // 常见农历节日（固定日期；除夕按腊月最后一天判断）
        val lunarKey = lunar.month to lunar.day
        val fixed = when (lunarKey) {
            1 to 1 -> "春节"
            1 to 15 -> "元宵"
            5 to 5 -> "端午"
            7 to 7 -> "七夕"
            8 to 15 -> "中秋"
            9 to 9 -> "重阳"
            12 to 8 -> "腊八"
            12 to 23 -> "小年"
            else -> null
        }
        if (fixed != null) return fixed

        if (!lunar.isLeapMonth && lunar.month == 12) {
            val last = monthDays(lunar.year, 12)
            if (lunar.day == last) return "除夕"
        }

        return null
    }

    private fun lunarYearDays(year: Int): Int {
        var sum = 348
        val info = LUNAR_INFO[year - MIN_YEAR]
        // 12个月，每个bit表示该月是否为大月(30天)
        for (i in 0 until 12) {
            if ((info and (0x8000 shr i)) != 0) sum += 1
        }
        return sum + leapDays(year)
    }

    private fun leapMonth(year: Int): Int = LUNAR_INFO[year - MIN_YEAR] and 0xF

    private fun leapDays(year: Int): Int {
        val leap = leapMonth(year)
        if (leap == 0) return 0
        val info = LUNAR_INFO[year - MIN_YEAR]
        return if ((info and 0x10000) != 0) 30 else 29
    }

    private fun monthDays(year: Int, month: Int): Int {
        val info = LUNAR_INFO[year - MIN_YEAR]
        return if ((info and (0x10000 shr month)) != 0) 30 else 29
    }

    private fun lunarMonthText(month: Int, isLeap: Boolean): String {
        val monthNames = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
        val m = month.coerceIn(1, 12)
        val prefix = if (isLeap) "闰" else ""
        return prefix + monthNames[m - 1] + "月"
    }

    private fun lunarDayText(day: Int): String {
        val d = day.coerceIn(1, 30)
        val tens = arrayOf("初", "十", "廿", "三")
        val units = arrayOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
        return when (d) {
            10 -> "初十"
            20 -> "二十"
            30 -> "三十"
            else -> tens[d / 10] + units[(d - 1) % 10]
        }
    }
}

