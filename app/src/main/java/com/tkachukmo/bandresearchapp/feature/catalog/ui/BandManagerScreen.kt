package com.tkachukmo.bandresearchapp.feature.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class ManagerAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandManagerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val actions = listOf(
        ManagerAction(
            icon = Icons.Default.MusicNote,
            title = "Керування треками",
            subtitle = "Додати, редагувати, видалити треки",
            color = Color(0xFF6750A4)
        ),
        ManagerAction(
            icon = Icons.Default.VideoLibrary,
            title = "Керування відео",
            subtitle = "Додати YouTube відео та кліпи",
            color = Color(0xFF006064)
        ),
        ManagerAction(
            icon = Icons.Default.Newspaper,
            title = "Публікація новин",
            subtitle = "Написати новину для підписників",
            color = Color(0xFF880E4F)
        ),
        ManagerAction(
            icon = Icons.Default.Event,
            title = "Керування подіями",
            subtitle = "Додати концерти та виступи",
            color = Color(0xFFE65100)
        ),
        ManagerAction(
            icon = Icons.Default.Edit,
            title = "Редагувати профіль",
            subtitle = "Змінити опис, фото, жанри",
            color = Color(0xFF1B5E20)
        ),
        ManagerAction(
            icon = Icons.Default.BarChart,
            title = "Статистика",
            subtitle = "Перегляд аналітики гурту",
            color = Color(0xFF1565C0)
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кабінет гурту") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // Шапка гурту
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFF4A0080)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎸", fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Мій гурт",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Рок · Україна",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Кнопки швидких дій
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Редагування профілю")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Редагувати")
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Перегляд профілю")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Переглянути")
                    }
                }
            }

            // Статистика
            item {
                Text(
                    text = "Статистика",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ManagerStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.People,
                        value = "1,240",
                        label = "Підписників",
                        trend = "+12%",
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    ManagerStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MusicNote,
                        value = "8,450",
                        label = "Прослухань",
                        trend = "+8%",
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ManagerStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VideoLibrary,
                        value = "3,210",
                        label = "Переглядів",
                        trend = "+15%",
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    ManagerStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        value = "92%",
                        label = "Рейтинг",
                        trend = "+3%",
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Дії
            item {
                Text(
                    text = "Керування контентом",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )
            }

            // Грід дій
            items(actions.size) { index ->
                if (index % 2 == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ManagerActionCard(
                            action = actions[index],
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(actions[index].title)
                                }
                            }
                        )
                        if (index + 1 < actions.size) {
                            ManagerActionCard(
                                action = actions[index + 1],
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(actions[index + 1].title)
                                    }
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Швидке додавання
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Швидке додавання",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAddRow(
                        icon = Icons.Default.MusicNote,
                        title = "Додати трек",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Завантаження треку...")
                            }
                        }
                    )
                    QuickAddRow(
                        icon = Icons.Default.Event,
                        title = "Додати подію",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Створення події...")
                            }
                        }
                    )
                    QuickAddRow(
                        icon = Icons.Default.Newspaper,
                        title = "Написати новину",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Редактор новин...")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManagerStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    trend: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ManagerActionCard(
    action: ManagerAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(action.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    action.icon,
                    contentDescription = null,
                    tint = action.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickAddRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}