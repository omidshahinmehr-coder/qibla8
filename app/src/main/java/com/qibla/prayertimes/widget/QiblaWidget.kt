package com.qibla.prayertimes.widget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.qibla.prayertimes.MainActivity
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.WidgetDataStore
import com.qibla.prayertimes.data.WidgetSnapshot
import com.qibla.prayertimes.data.prayerLabels
import com.qibla.prayertimes.util.LocalePrefs
import java.text.SimpleDateFormat
import java.util.*

/* ------------------------- LIGHT MODE COLORS ------------------------- */
private val lightBg = ColorProvider(Color(0xFFF3ECDD))
private val lightCellBorder = ColorProvider(Color(0xFFD9C8A0))
private val lightCellFill = ColorProvider(Color(0xFFFBF6EA))
private val lightGoldText = ColorProvider(Color(0xFF8A6A2E))
private val lightFaintGold = ColorProvider(Color(0xFFAD8F55))

/* ------------------------- DARK MODE COLORS ------------------------- */
private val darkBg = ColorProvider(Color(0xFF1A1A1A))
private val darkCellBorder = ColorProvider(Color(0xFF333333))
private val darkCellFill = ColorProvider(Color(0xFF222222))
private val darkGoldText = ColorProvider(Color(0xFFE6C97A))
private val darkFaintGold = ColorProvider(Color(0xFFBFA86A))

/* ------------------------- PRAYER KEYS ------------------------- */
private val widgetPrayerKeys = listOf("Fajr", "Sunrise", "Dhuhr", "Sunset", "Maghrib", "Midnight")
private val cellWidth = 66.dp

/* ------------------------- WEEKDAY ARRAYS ------------------------- */
private val WEEKDAYS_FA = arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
private val WEEKDAYS_AR = arrayOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")

class QiblaWidget : GlanceAppWidget() {
override suspend fun provideGlance(context: Context, id: GlanceId) {
val localizedContext = LocalePrefs.wrap(context)
val snapshot = WidgetDataStore(context).load()
provideContent {
WidgetContent(localizedContext, snapshot)
}
}
}

@Composable
private fun WidgetContent(langContext: Context, snapshot: WidgetSnapshot?) {

val labels = prayerLabels(langContext)
val language = langContext.resources.configuration.locales[0].language
val isRtl = language == "fa" || language == "ar"

/* ------------------------- DARK MODE DETECTION ------------------------- */
val isDark = langContext.resources.configuration.uiMode and
Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/* ------------------------- SELECT COLORS BASED ON MODE ------------------------- */
val bgColor = if (isDark) darkBg else lightBg
val cellBorderColor = if (isDark) darkCellBorder else lightCellBorder
val cellFillColor = if (isDark) darkCellFill else lightCellFill
val goldText = if (isDark) darkGoldText else lightGoldText
val faintGoldText = if (isDark) darkFaintGold else lightFaintGold

Column(
modifier = GlanceModifier
.fillMaxSize()
.background(bgColor)
.cornerRadius(20.dp)
.padding(12.dp)
.clickable(actionStartActivity<MainActivity>()),
horizontalAlignment = Alignment.CenterHorizontally
) {

if (snapshot != null) {

val countdown = nextPrayerCountdown(snapshot.timings)
val weekdayName = weekdayName(language)
val gregorianText = formatGregorian(langContext, snapshot.gregorianDateKey)
val jalaliWithWeekday = "{snapshot.jalaliText}"

/* ------------------------- CLOCK ------------------------- */
Text(
text = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
style = TextStyle(
color = goldText,
fontSize = 22.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)
)

Spacer(GlanceModifier.height(6.dp))

/* ------------------------- JALALI DATE ------------------------- */
Text(
text = jalaliWithWeekday,
style = TextStyle(
color = goldText,
fontSize = 16.sp,
textAlign = TextAlign.Center
)
)

Spacer(GlanceModifier.height(4.dp))

/* ------------------------- HIJRI + GREGORIAN ------------------------- */
Text(
text = "gregorianText",
style = TextStyle(
color = faintGoldText,
fontSize = 14.sp,
textAlign = TextAlign.Center
)
)

Spacer(GlanceModifier.height(8.dp))

/* ------------------------- COUNTDOWN WITH HORIZON TEXT ------------------------- */
if (countdown != null) {
val nextName = labels[countdown.first] ?: countdown.first
val remaining = staticDuration(countdown.second)

Text(
text = "تا {snapshot.cityName} — $remaining",
style = TextStyle(
color = goldText,
fontSize = 18.sp,
fontWeight = FontWeight.Medium,
textAlign = TextAlign.Center
)
)
}

Spacer(GlanceModifier.height(12.dp))

/* ------------------------- PRAYER TABLE ------------------------- */
Row(modifier = GlanceModifier.fillMaxWidth()) {
widgetPrayerKeys.forEachIndexed { index, key ->
if (index > 0) Spacer(GlanceModifier.width(4.dp))
PrayerCell(
label = labels[key] ?: key,
time = snapshot.timings[key] ?: "--:--",
cellBorderColor = cellBorderColor,
cellFillColor = cellFillColor,
goldText = goldText
)
}
}

} else {
Text(
text = langContext.getString(R.string.widget_updating),
style = TextStyle(color = goldText, fontSize = 13.sp, textAlign = TextAlign.Center)
)
}
}
}

@Composable
private fun PrayerCell(label: String, time: String, cellBorderColor: ColorProvider, cellFillColor: ColorProvider, goldText: ColorProvider) {
Column(
modifier = GlanceModifier
.width(cellWidth)
.background(cellBorderColor)
.cornerRadius(16.dp)
.padding(1.2.dp)
) {
Column(
modifier = GlanceModifier
.fillMaxWidth()
.background(cellFillColor)
.cornerRadius(15.dp)
.padding(horizontal = 4.dp, vertical = 6.dp),
horizontalAlignment = Alignment.CenterHorizontally
) {
Text(text = label, style = TextStyle(color = goldText, fontSize = 10.sp, fontWeight = FontWeight.Bold))
Spacer(GlanceModifier.height(2.dp))
Text(text = time, style = TextStyle(color = goldText, fontSize = 14.sp, fontWeight = FontWeight.Bold))
}
}
}

/* ------------------------- DATE & COUNTDOWN HELPERS ------------------------- */

private fun weekdayName(language: String): String {
val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
return when (language) {
"fa" -> WEEKDAYS_FA[dow - 1]
"ar" -> WEEKDAYS_AR[dow - 1]
else -> SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
}
}

private fun formatGregorian(context: Context, dateKey: String): String {
return try {
val parts = dateKey.split("-").map { it.toInt() }
val cal = Calendar.getInstance().apply { set(parts[0], parts[1] - 1, parts[2]) }
SimpleDateFormat("d MMMM yyyy", context.resources.configuration.locales[0]).format(cal.time)
} catch (e: Exception) {
dateKey
}
}

private fun staticDuration(targetMillis: Long): String {
val diff = (targetMillis - System.currentMillis()).coerceAtLeast(0) / 1000
val h = diff / 3600
val m = (diff % 3600) / 60
val s = diff % 60
return "%02d:%02d:%02d".format(h, m, s)
}

private fun nextPrayerCountdown(timings: Map<String, String>): Pair<String, Long>? {
val order = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
val now = Calendar.getInstance()
val sdf = SimpleDateFormat("HH:mm", Locale.US)

fun toCalendar(hhmm: String): Calendar? {
val parsed = sdf.parse(hhmm) ?: return null
return Calendar.getInstance().apply {
time = parsed
set(Calendar.SECOND, 0)
set(Calendar.MILLISECOND, 0)
}
}

var bestKey: String? = null
var bestCal: Calendar? = null

for (key in order) {
val timeStr = timings[key] ?: continue
val cal = toCalendar(timeStr) ?: continue
if (cal.after(now) && (bestCal == null || cal.before(bestCal))) {
bestKey = key
bestCal = cal
}
}

if (bestKey == null) {
val fajrStr = timings["Fajr"] ?: return null
val cal = toCalendar(fajrStr) ?: return null
cal.add(Calendar.DAY_OF_YEAR, 1)
bestKey = "Fajr"
bestCal = cal
}

return bestKey!! to bestCal!!.timeInMillis
}
