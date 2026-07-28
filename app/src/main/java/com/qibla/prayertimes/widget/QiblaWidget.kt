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

private val widgetPrayerKeys = listOf("Fajr", "Sunrise", "Dhuhr", "Sunset", "Maghrib", "Midnight")
private val cellWidth = 66.dp

private val WEEKDAYS_FA = arrayOf("یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه","شنبه")
private val WEEKDAYS_AR = arrayOf("الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت")

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

val isDarkMode =
(langContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
Configuration.UI_MODE_NIGHT_YES

val bgColor = ColorProvider(if (isDarkMode) Color(0xFF3E2F1C) else Color(0xFFF3ECDD))
val cellBorderColor = ColorProvider(if (isDarkMode) Color(0xFF5A4730) else Color(0xFFD9C8A0))
val cellFillColor = ColorProvider(if (isDarkMode) Color(0xFF4A3A24) else Color(0xFFFBF6EA))
val goldText = ColorProvider(if (isDarkMode) Color(0xFFE8C97A) else Color(0xFF8A6A2E))
val faintGoldText = ColorProvider(if (isDarkMode) Color(0xFFDABF7A) else Color(0xFFAD8F55))

Column(
modifier = GlanceModifier
.fillMaxSize()
.background(bgColor)
.cornerRadius(20.dp)
.padding(12.dp)
.clickable(actionStartActivity<MainActivity>())
) {

if (snapshot != null) {

val countdown = nextPrayerCountdown(snapshot.timings)
val weekdayName = weekdayName(language)
val gregorianText = formatGregorian(langContext, snapshot.gregorianDateKey)

AndroidRemoteViews(
RemoteViews(langContext.packageName, R.layout.widget_clock)
)

Spacer(modifier = GlanceModifier.height(6.dp))

Text(
text = "{snapshot.jalaliText}",
style = TextStyle(
color = goldText,
fontSize = 14.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
),
modifier = GlanceModifier.fillMaxWidth()
)

Text(
text = "gregorianText",
style = TextStyle(
color = faintGoldText,
fontSize = 12.sp,
textAlign = TextAlign.Center
),
modifier = GlanceModifier.fillMaxWidth()
)

Spacer(modifier = GlanceModifier.height(6.dp))

if (countdown != null) {

Text(
text = langContext.getString(
R.string.widget_countdown_label,
labels[countdown.first] ?: countdown.first,
snapshot.cityName
),
style = TextStyle(
color = faintGoldText,
fontSize = 12.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
),
modifier = GlanceModifier.fillMaxWidth()
)

Spacer(modifier = GlanceModifier.height(4.dp))

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
val nowElapsed = SystemClock.elapsedRealtime()
val nowWall = System.currentTimeMillis()
val base = nowElapsed + (countdown.second - nowWall)

val rv = RemoteViews(langContext.packageName, R.layout.widget_countdown)
rv.setChronometer(R.id.widget_countdown_view, base, null, true)

AndroidRemoteViews(rv)

} else {
Text(
text = staticDuration(countdown.second),
style = TextStyle(
color = goldText,
fontSize = 20.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
),
modifier = GlanceModifier.fillMaxWidth()
)
}
}

Spacer(modifier = GlanceModifier.height(10.dp))

Row(
modifier = GlanceModifier.fillMaxWidth(),
horizontalAlignment = Alignment.Horizontal.CenterHorizontally
) {
val keys = if (isRtl) widgetPrayerKeys.reversed() else widgetPrayerKeys

keys.forEachIndexed { index, key ->
if (index > 0) Spacer(modifier = GlanceModifier.width(4.dp))

PrayerCell(
label = labels[key] ?: key,
time = snapshot.timings[key] ?: "--:--",
cellBorderColor = cellBorderColor,
cellFillColor = cellFillColor,
goldText = goldText
)
}
}
}
}
}

@Composable
private fun PrayerCell(
label: String,
time: String,
cellBorderColor: ColorProvider,
cellFillColor: ColorProvider,
goldText: ColorProvider
) {
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
horizontalAlignment = Alignment.Horizontal.CenterHorizontally
) {
Text(
text = label,
style = TextStyle(
color = goldText,
fontSize = 10.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)
)
Spacer(modifier = GlanceModifier.height(2.dp))
Text(
text = time,
style = TextStyle(
color = goldText,
fontSize = 14.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)
)
}
}
}

private fun weekdayName(language: String): String {
val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
return when (language) {
"fa" -> WEEKDAYS_FA[dow - 1]
"ar" -> WEEKDAYS_AR[dow - 1]
else -> SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
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
val diff = (targetMillis - System.currentTimeMillis()).coerceAtLeast(0) / 1000
val h = diff / 3600
val m = (diff % 3600) / 60
val s = diff % 60
return "%02d:%02d:%02d".format(h, m, s)
}

private fun nextPrayerCountdown(timings: Map<String, String>): Pair<String, Long>? {
val order = listOf("Fajr","Dhuhr","Asr","Maghrib","Isha")
val now = Calendar.getInstance()
val sdf = SimpleDateFormat("HH:mm", Locale.US)

fun toCalendar(hhmm: String): Calendar? {
val parsed = try { sdf.parse(hhmm) } catch (_: Exception) { null } ?: return null
val parsedCal = Calendar.getInstance().apply { time = parsed }
return Calendar.getInstance().apply {
set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
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

return bestKey to bestCal!!.timeInMillis
}
