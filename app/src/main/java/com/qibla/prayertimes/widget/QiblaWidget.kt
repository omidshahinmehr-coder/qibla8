@Composable
private fun UnifiedPrayerWidget(langContext: Context, snapshot: WidgetSnapshot?) {
val labels = prayerLabels(langContext)
val language = langContext.resources.configuration.locales[0].language
val isRtl = language == "fa" || language == "ar"

// حالت سیستم (روشن/تاریک)
val uiModeManager = langContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
val isDarkMode = uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES

// رنگ‌ها بر اساس حالت سیستم
val bgColor = if (isDarkMode) Color(0xFF3E2F1C) else Color(0xFFF8F3E7)
val goldText = if (isDarkMode) Color(0xFFE8C97A) else Color(0xFF8A6A2E)
val faintGoldText = if (isDarkMode) Color(0xFFDABF7A) else Color(0xFFAD8F55)
val cellBg = if (isDarkMode) Color(0xFF4A3A24) else Color(0xFFFBF6EA)

Column(
modifier = GlanceModifier
.fillMaxSize()
.background(bgColor)
.cornerRadius(20.dp)
.padding(14.dp)
.clickable(actionStartActivity<MainActivity>())
) {
if (snapshot != null) {
val countdown = nextPrayerCountdown(snapshot.timings)
val weekdayName = weekdayName(language)
val gregorianText = formatGregorian(langContext, snapshot.gregorianDateKey)

// ساعت بالا
val clockBlock: @Composable () -> Unit = {
val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
val currentTime = sdf.format(Calendar.getInstance().time)
Text(
text = currentTime,
style = TextStyle(
color = goldText,
fontSize = 24.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
),
modifier = GlanceModifier.fillMaxWidth()
)
}

// شمارش معکوس
val countdownBlock: @Composable () -> Unit = {
if (countdown != null) {
Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
Text(
text = langContext.getString(
R.string.widget_countdown_label,
labels[countdown.first] ?: countdown.first,
snapshot.cityName
),
style = TextStyle(
color = faintGoldText,
fontSize = 14.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)
)
Text(
text = staticDuration(countdown.second),
style = TextStyle(
color = goldText,
fontSize = 22.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)
)
}
}
}

// تاریخ‌ها
val dateBlock: @Composable () -> Unit = {
Text(
text = "{snapshot.jalaliText}",
style = TextStyle(color = goldText, fontSize = 14.sp, fontWeight = FontWeight.Bold),
textAlign = TextAlign.Center,
modifier = GlanceModifier.fillMaxWidth()
)
Text(
text = "gregorianText",
style = TextStyle(color = faintGoldText, fontSize = 12.sp),
textAlign = TextAlign.Center,
modifier = GlanceModifier.fillMaxWidth()
)
}

// ترتیب نمایش
clockBlock()
Spacer(modifier = GlanceModifier.height(6.dp))
dateBlock()
Spacer(modifier = GlanceModifier.height(6.dp))
countdownBlock()
Spacer(modifier = GlanceModifier.height(10.dp))

// جدول پایین
Row(modifier = GlanceModifier.fillMaxWidth()) {
val keys = if (isRtl) widgetPrayerKeys.reversed() else widgetPrayerKeys
keys.forEachIndexed { index, key ->
if (index > 0) Spacer(modifier = GlanceModifier.width(6.dp))
PrayerCell(
label = labels[key] ?: key,
time = snapshot.timings[key] ?: "--:--",
backgroundColor = cellBg,
textColor = goldText
)
}
}
}
}
}

// شمارش معکوس کامل
private fun staticDuration(targetMillis: Long): String {
val diff = (targetMillis - System.currentTimeMillis()).coerceAtLeast(0) / 1000
val h = diff / 3600
val m = (diff % 3600) / 60
val s = diff % 60
return "%02d:%02d:%02d".format(h, m, s)
}
