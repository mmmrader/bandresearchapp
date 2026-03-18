package com.tkachukmo.bandresearchapp.feature.discover.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val searchHistory = listOf(
    "Океан Ельзи",
    "Рок Україна",
    "DakhaBrakha",
    "Kazka"
)

val trendingSearches = listOf(
    "🔥 Brutto",
    "🎸 Українські гурти",
    "🎹 Синтпоп",
    "⚡ Метал",
    "🎻 Фолк",
    "🎷 Джаз"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onBandClick: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val history = remember { mutableStateListOf(*searchHistory.toTypedArray()) }

    val searchResults = if (query.isNotBlank()) {
        sampleBands.filter { band ->
            band.name.contains(query, ignoreCase = true) ||
                    band.genre.contains(query, ignoreCase = true) ||
                    band.country.contains(query, ignoreCase = true) ||
                    band.tags.any { it.contains(query, ignoreCase = true) }
        }
    } else emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Пошукове поле
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Пошук гуртів, жанрів...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = {
                        query = ""
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистити")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (query.isNotBlank() && !history.contains(query)) {
                        history.add(0, query)
                        if (history.size > 6) history.removeAt(history.size - 1)
                    }
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (query.isBlank()) {
            // Дефолтний стан — показуємо історію і трендові
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // Історія пошуку
                if (history.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Недавні пошуки",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Очистити",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { history.clear() }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(history) { item ->
                                InputChip(
                                    selected = false,
                                    onClick = { query = item },
                                    label = { Text(item) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Видалити",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { history.remove(item) }
                                        )
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Трендові пошуки
                item {
                    Text(
                        text = "Популярні запити",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trendingSearches) { trend ->
                            SuggestionChip(
                                onClick = {
                                    query = trend.substring(2)
                                },
                                label = { Text(trend) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Категорії
                item {
                    Text(
                        text = "Категорії",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val categories = listOf(
                    Triple("🎸", "Рок", "1,240 гуртів"),
                    Triple("🎹", "Поп", "890 гуртів"),
                    Triple("⚡", "Метал", "560 гуртів"),
                    Triple("🎻", "Фолк", "430 гуртів"),
                    Triple("🎧", "Електроніка", "720 гуртів"),
                    Triple("🎷", "Джаз", "340 гуртів"),
                )

                items(categories) { (emoji, name, count) ->
                    SearchCategoryRow(
                        emoji = emoji,
                        name = name,
                        count = count,
                        onClick = { query = name }
                    )
                }
            }
        } else {
            // Результати пошуку
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Нічого не знайдено",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Спробуй інший запит",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Результати: ${searchResults.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(searchResults) { band ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBandClick(band.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.size(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = band.color
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = band.emoji, fontSize = 24.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = band.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${band.genre} · ${band.country}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = "Гурт",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchCategoryRow(
    emoji: String,
    name: String,
    count: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = count,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}