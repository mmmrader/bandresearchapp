package com.tkachukmo.bandresearchapp.feature.profile.ui

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ----------------------------------------------------------------
// ОНОВЛЕНІ Пресети еквалайзера на 9 смуг (як у Wavelet)
// ----------------------------------------------------------------
private val EQ_PRESETS: Map<String, List<Int>> = linkedMapOf(
    "Нормальний"     to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
    "Поп"            to listOf(-200, -100, 200, 400, 500, 400, 200, -100, -200),
    "Рок"            to listOf(500, 400, 200, -100, -200, -100, 200, 400, 500),
    "Акустика"       to listOf(300, 200, 100, 0, 0, 0, 100, 200, 300),
    "Електроніка"    to listOf(500, 400, 100, 0, -200, 0, 100, 400, 500),
    "Користувацький" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(onNavigateBack: () -> Unit) {
    var isEqEnabled by remember { mutableStateOf(EqController.isEqualizerEnabled()) }

    // Отримуємо мін/макс (зазвичай від -1500 до 1500)
    val minMb = EqController.getMinBandLevel()
    val maxMb = EqController.getMaxBandLevel()

    // 9 смуг частот
    val uiLabels = listOf("62.5", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    var uiLevels by remember { mutableStateOf(EQ_PRESETS["Нормальний"]!!) }
    var selectedPreset by remember { mutableStateOf("Нормальний") }
    var expanded by remember { mutableStateOf(false) }

    // При старті зчитуємо реальні 5 смуг з Android і мапимо їх на наш красивий 9-смуговий UI
    LaunchedEffect(Unit) {
        val bandsCount = EqController.getNumberOfBands()
        if (bandsCount == 5) {
            val hw0 = EqController.getBandLevel(0).toInt()
            val hw1 = EqController.getBandLevel(1).toInt()
            val hw2 = EqController.getBandLevel(2).toInt()
            val hw3 = EqController.getBandLevel(3).toInt()
            val hw4 = EqController.getBandLevel(4).toInt()
            uiLevels = listOf(hw0, hw0, hw1, hw1, hw2, hw2, hw3, hw3, hw4)
            selectedPreset = "Користувацький"
        }
    }

    // Зворотнє перетворення наших 9 смуг у реальні 5 смуг Android Equalizer
    fun updateHardwareEq(levels: List<Int>) {
        val bandsCount = EqController.getNumberOfBands()
        if (bandsCount == 5) {
            EqController.setBandLevel(0, levels[0].toShort())
            EqController.setBandLevel(1, ((levels[2] + levels[3]) / 2).toShort())
            EqController.setBandLevel(2, levels[4].toShort())
            EqController.setBandLevel(3, ((levels[6] + levels[7]) / 2).toShort())
            EqController.setBandLevel(4, levels[8].toShort())
        } else if (bandsCount > 0) {
            for (i in 0 until bandsCount) {
                val mappedIdx = (i * 9) / bandsCount
                EqController.setBandLevel(i.toShort(), levels[mappedIdx].toShort())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Еквалайзер") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ----------------------------------------------------------------
            // ЕКВАЛАЙЗЕР (9 Смуг + Лінія)
            // ----------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Смуговий еквалайзер", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = isEqEnabled,
                            onCheckedChange = {
                                isEqEnabled = it
                                EqController.setEqualizerEnabled(it)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Блок з лінією (Canvas) та збільшеними повзунками
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 1. МАЛЮЄМО ЛІНІЮ ТА ГРАДІЄНТ ПІД ПОВЗУНКАМИ
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 22.dp, vertical = 36.dp)
                        ) {
                            if (uiLevels.isEmpty()) return@Canvas

                            val stepX = size.width / (uiLevels.size - 1).coerceAtLeast(1)
                            val path = Path()

                            uiLevels.forEachIndexed { index, level ->
                                val x = index * stepX
                                val normalizedY = 1f - ((level - minMb).toFloat() / (maxMb - minMb).toFloat())
                                val y = normalizedY * size.height

                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    val prevX = (index - 1) * stepX
                                    val prevLevel = uiLevels[index - 1]
                                    val prevNormY = 1f - ((prevLevel - minMb).toFloat() / (maxMb - minMb).toFloat())
                                    val prevY = prevNormY * size.height

                                    // Робимо лінію плавною
                                    val cpX = (prevX + x) / 2
                                    path.cubicTo(cpX, prevY, cpX, y, x, y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFB288FF).copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )

                            drawPath(
                                path = path,
                                color = Color(0xFFB288FF),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // 2. ДОДАЄМО 9 ПОВЗУНКІВ ПОВЕРХ ЛІНІЇ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiLevels.forEachIndexed { index, level ->
                                EqBandSlider(
                                    modifier = Modifier.weight(1f), // ТЕПЕР ВАГА ПРАЦЮЄ ПРАВИЛЬНО (В ROW)
                                    frequency = uiLabels[index],
                                    levelMb = level,
                                    minMb = minMb,
                                    maxMb = maxMb,
                                    enabled = isEqEnabled,
                                    onLevelChange = { newLevel ->
                                        val newLevels = uiLevels.toMutableList()
                                        newLevels[index] = newLevel
                                        uiLevels = newLevels
                                        updateHardwareEq(newLevels)
                                        selectedPreset = "Користувацький"
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    @Suppress("DEPRECATION")
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedPreset,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Пресет") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            EQ_PRESETS.forEach { (name, levels) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedPreset = name
                                        uiLevels = levels
                                        updateHardwareEq(levels)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // BASS BOOST (Без змін)
            // ----------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                var isBassBoostEnabled by remember { mutableStateOf(EqController.isBassBoostEnabled()) }
                var bassStrength by remember { mutableFloatStateOf(EqController.getBassBoostStrength().toFloat()) }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bass Boost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isBassBoostEnabled,
                            onCheckedChange = {
                                isBassBoostEnabled = it
                                EqController.setBassBoostEnabled(it)
                            }
                        )
                    }
                    Slider(
                        value = bassStrength,
                        onValueChange = {
                            bassStrength = it
                            EqController.setBassBoostStrength(it.toInt().toShort())
                        },
                        valueRange = 0f..1000f,
                        enabled = isBassBoostEnabled
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// Вертикальний слайдер однієї смуги EQ
// ----------------------------------------------------------------
@Composable
private fun EqBandSlider(
    modifier: Modifier = Modifier,
    frequency: String,
    levelMb: Int,
    minMb: Int,
    maxMb: Int,
    enabled: Boolean,
    onLevelChange: (Int) -> Unit
) {
    val dbValue = levelMb / 100

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "${if (dbValue > 0) "+" else ""}$dbValue",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value        = levelMb.toFloat(),
                onValueChange = { onLevelChange(it.toInt()) },
                valueRange   = minMb.toFloat()..maxMb.toFloat(),
                enabled      = enabled,
                colors       = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    thumbColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
                ),
                modifier     = Modifier
                    .width(200.dp)
                    .rotate(270f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text  = frequency,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----------------------------------------------------------------
// EqController: Керує системним аудіо (Android Equalizer)
// ----------------------------------------------------------------
object EqController {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    init {
        try {
            // "0" означає загальний мікс системи (працюватиме для всіх звуків)
            equalizer = Equalizer(0, 0)
        } catch (e: Exception) { e.printStackTrace() }

        try {
            bassBoost = BassBoost(0, 0)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun isEqualizerEnabled() = equalizer?.enabled ?: false
    fun setEqualizerEnabled(enabled: Boolean) { equalizer?.enabled = enabled }

    fun getMinBandLevel(): Int = equalizer?.bandLevelRange?.get(0)?.toInt() ?: -1500
    fun getMaxBandLevel(): Int = equalizer?.bandLevelRange?.get(1)?.toInt() ?: 1500

    fun getNumberOfBands(): Int = equalizer?.numberOfBands?.toInt() ?: 0

    fun getBandLevel(band: Short): Short = equalizer?.getBandLevel(band) ?: 0
    fun setBandLevel(band: Short, level: Short) {
        try { equalizer?.setBandLevel(band, level) } catch (e: Exception) {}
    }

    fun isBassBoostEnabled() = bassBoost?.enabled ?: false
    fun setBassBoostEnabled(enabled: Boolean) { bassBoost?.enabled = enabled }

    fun getBassBoostStrength(): Short = bassBoost?.roundedStrength ?: 0
    fun setBassBoostStrength(strength: Short) {
        try { bassBoost?.setStrength(strength) } catch (e: Exception) {}
    }
}