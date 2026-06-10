package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import com.example.data.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.City
import com.example.data.CommodityItem
import com.example.data.PurchaseRecord
import com.example.ui.theme.TrendUpColor
import com.example.ui.theme.TrendDownColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketHomeScreen(viewModel: MarketViewModel) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val marketItems by viewModel.marketItems.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val purchaseRecords by viewModel.purchaseRecords.collectAsState()
    val aiCommentary by viewModel.aiCommentary.collectAsState()
    val isSyncingAI by viewModel.isSyncingAI.collectAsState()

    var activeTab by remember { mutableStateOf("explorer") } // explorer, ledger, calculator, guru

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "explorer",
                    onClick = { activeTab = "explorer" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Explorer") },
                    label = { Text("Prices") },
                    modifier = Modifier.testTag("tab_explorer")
                )
                NavigationBarItem(
                    selected = activeTab == "ledger",
                    onClick = { activeTab = "ledger" },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Ledger") },
                    label = { Text("Spend") },
                    modifier = Modifier.testTag("tab_ledger")
                )
                NavigationBarItem(
                    selected = activeTab == "calculator",
                    onClick = { activeTab = "calculator" },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Calculator") },
                    label = { Text("Basket") },
                    modifier = Modifier.testTag("tab_calculator")
                )
                NavigationBarItem(
                    selected = activeTab == "guru",
                    onClick = { activeTab = "guru" },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Guru Assistant") },
                    label = { Text("Guru AI") },
                    modifier = Modifier.testTag("tab_guru")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // --- Elegant Top App Bar Header ---
            HeaderBlock(
                selectedCity = selectedCity,
                cities = viewModel.cities.value,
                onCitySelected = { viewModel.selectCity(it) },
                onAiSync = { viewModel.triggerAIMarketSync() },
                isSyncing = isSyncingAI
            )

            // --- Collapsible AI Daily Bulletin ---
            aiCommentary?.let { report ->
                AiReportCard(
                    reportText = report,
                    isSyncing = isSyncingAI,
                    onClear = { viewModel.triggerAIMarketSync() }
                )
            }

            // --- Filter Tab Content Viewport ---
            Spacer(modifier = Modifier.height(6.dp))

            when (activeTab) {
                "explorer" -> PriceExplorerTab(
                    viewModel = viewModel,
                    marketItems = marketItems,
                    watchlist = watchlist,
                    selectedCity = selectedCity,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery
                )
                "ledger" -> SpendLedgerTab(
                    viewModel = viewModel,
                    purchaseRecords = purchaseRecords,
                    marketItems = marketItems,
                    selectedCity = selectedCity
                )
                "calculator" -> PriceCalculatorTab(
                    viewModel = viewModel,
                    marketItems = marketItems,
                    selectedCity = selectedCity
                )
                "guru" -> GuruAssistantTab(
                    viewModel = viewModel
                )
            }
        }
    }
}

// --- Header Component ---
@Composable
fun HeaderBlock(
    selectedCity: City,
    cities: List<City>,
    onCitySelected: (City) -> Unit,
    onAiSync: () -> Unit,
    isSyncing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Clean Brand Label & Icon Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Trend Badge Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "TN Price Watch",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "தமிழக சந்தை நிலவர விலை",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Right side: Active City Pill and AI Sync
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Active City Icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = selectedCity.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                val rotation by animateFloatAsState(targetValue = if (isSyncing) 360f else 0f)
                IconButton(
                    onClick = onAiSync,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                        .testTag("ai_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync rates with AI model",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.rotate(rotation).size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cities horizontal sliding slider
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(cities) { city ->
                val isSelected = city.id == selectedCity.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                        .clickable { onCitySelected(city) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("city_pill_${city.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${city.name} | ${city.tamilName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White 
                                    else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}

// --- AI Daily Bulletin Card ---
@Composable
fun AiReportCard(
    reportText: String,
    isSyncing: Boolean,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Briefing info logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "GURU'S DAILY INSIGHTS (இன்றைய நிலவரம்)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            letterSpacing = 1.sp
                        )
                    )
                }
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reportText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "*AI trends are generated realistically based on regional regional retail flows.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            )
        }
    }
}

// ==================== TAB 1: PRICE EXPLORER ====================
@Composable
fun PriceExplorerTab(
    viewModel: MarketViewModel,
    marketItems: List<CommodityItem>,
    watchlist: Set<String>,
    selectedCity: City,
    selectedCategory: String,
    searchQuery: String
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search text field styled as a modern minimal pill container
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("search_field"),
            placeholder = { Text("Search items (e.g. Tomato, Gold)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Log") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        // Categories List (Row with clean pills)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryPill(
                    label = "All List",
                    tamilLabel = "எல்லாம்",
                    isSelected = selectedCategory == "all",
                    onClick = { viewModel.selectCategory("all") },
                    tag = "cat_all"
                )
            }
            items(MarketRepository.categories) { cat ->
                CategoryPill(
                    label = cat.name,
                    tamilLabel = cat.tamilName,
                    isSelected = selectedCategory == cat.id,
                    onClick = { viewModel.selectCategory(cat.id) },
                    tag = "cat_${cat.id}"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid List of Commodity Pricing items
        val filteredItems = remember(marketItems, selectedCategory, searchQuery) {
            marketItems.filter { item ->
                val categoryMatch = selectedCategory == "all" || item.categoryId == selectedCategory
                val searchMatch = searchQuery.isEmpty() || 
                        item.name.contains(searchQuery, ignoreCase = true) || 
                        item.tamilName.contains(searchQuery, ignoreCase = true)
                categoryMatch && searchMatch
            }
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Empty search logo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No commodities matched search.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Try clearing queries or trying different category selections.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                state = rememberLazyListState()
            ) {
                items(filteredItems) { item ->
                    val isFavorite = watchlist.contains(item.id)
                    val price = item.basePrices[selectedCity.id] ?: item.basePrices.values.first()
                    CommodityCard(
                        item = item,
                        price = price,
                        isFavorite = isFavorite,
                        onFavoriteToggle = { viewModel.toggleWatchlist(item.id) },
                        selectedCity = selectedCity
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPill(
    label: String,
    tamilLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
            Text(
                text = "• $tamilLabel",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun CommodityCard(
    item: CommodityItem,
    price: Double,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    selectedCity: City
) {
    val categoryColor = when (item.categoryId) {
        "vegetables" -> Color(0xFFFFD8E4) // Soft light pink
        "metals" -> Color(0xFFD3E3FD)      // Soft light blue
        "grains" -> Color(0xFFE8DEF8)      // Soft light lavender
        else -> Color(0xFFFFECB3)          // Soft warm light yellow
    }

    val categoryEmoji = when (item.categoryId) {
        "vegetables" -> "🧅"
        "metals" -> "🪙"
        "grains" -> "🍚"
        else -> "⛽"
    }

    val isUp = item.isTrendUp
    val trendColor = if (isUp) TrendUpColor else TrendDownColor
    val trendSymbol = if (isUp) "▲" else "▼"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("commodity_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category circular badge and Text details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Circle Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(categoryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = categoryEmoji, fontSize = 20.sp)
                    }

                    // Commodity details
                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = item.tamilName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Retail Price • Per ${item.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Price rate on the right
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    val formattedPrice = if (item.categoryId == "metals" && item.id != "metal_silver") {
                        "₹${String.format("%,.0f", price)}"
                    } else {
                        "₹${String.format("%,.2f", price)}"
                    }

                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Compact Trend Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = trendSymbol,
                            fontSize = 11.sp,
                            color = trendColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isUp) "+Trend" else "Stable/-",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = trendColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Lower Bar containing descriptive note text and favorite icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.tamilDescription,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .testTag("favorite_button_${item.id}")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite status",
                        tint = if (isFavorite) Color.Red else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


// ==================== TAB 2: SPEND LEDGER ====================
@Composable
fun SpendLedgerTab(
    viewModel: MarketViewModel,
    purchaseRecords: List<PurchaseRecord>,
    marketItems: List<CommodityItem>,
    selectedCity: City
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Stats Banner card
        LedgerStatsCard(purchaseRecords = purchaseRecords, marketItems = marketItems, selectedCity = selectedCity)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recorded Purchases (கொள்முதல் விவரம்)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_purchase_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add buy invoice logs", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Buy")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (purchaseRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Empty ledger logo",
                        tint = Color.LightGray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No purchases logged yet.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Click 'Log Buy' above to record prices you actually paid and see savings insights!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(purchaseRecords) { record ->
                    // Find matching default item to do comparisons
                    val marketItem = marketItems.firstOrNull { it.name.contains(record.itemName, ignoreCase = true) }
                    val currentAvgRate = marketItem?.basePrices?.get(record.cityId)

                    PurchaseRecordCard(
                        record = record,
                        avgPriceRate = currentAvgRate,
                        onDelete = { viewModel.deletePurchaseRecord(record.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddPurchaseDialog(
            marketItems = marketItems,
            cities = MarketRepository.cities,
            activeCity = selectedCity,
            onDismiss = { showAddDialog = false },
            onSave = { itemName, cityId, qty, price, unit, notes ->
                viewModel.addPurchaseRecord(itemName, cityId, qty, price, unit, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LedgerStatsCard(
    purchaseRecords: List<PurchaseRecord>,
    marketItems: List<CommodityItem>,
    selectedCity: City
) {
    val totalExpense = purchaseRecords.sumOf { it.pricePaid * it.quantity }
    
    // Calculate total compared average market cost for matching entries
    var totalMarketAverageValue = 0.0
    purchaseRecords.forEach { rec ->
        val item = marketItems.firstOrNull { it.name.contains(rec.itemName, ignoreCase = true) }
        val price = item?.basePrices?.get(rec.cityId) ?: currentFallbackPriceForName(rec.itemName, marketItems)
        totalMarketAverageValue += (price * rec.quantity)
    }

    val netSavings = totalMarketAverageValue - totalExpense
    val isProfit = netSavings >= 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPEND LEDGER INSIGHTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isProfit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isProfit) "SAVING DEAL" else "PREMIUM RATE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isProfit) Color(0xFF2E7D32) else Color(0xFFC62828),
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Logged Outlay",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "₹${String.format("%,.2f", totalExpense)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Pocket Savings",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Text(
                        text = (if (isProfit) "+" else "") + "₹${String.format("%,.2f", netSavings)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isProfit) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                        )
                    )
                }
            }
        }
    }
}

private fun currentFallbackPriceForName(name: String, items: List<CommodityItem>): Double {
    return items.firstOrNull { it.name.equals(name, ignoreCase = true) }
        ?.basePrices?.values?.firstOrNull() ?: 1.0
}

@Composable
fun PurchaseRecordCard(
    record: PurchaseRecord,
    avgPriceRate: Double?,
    onDelete: () -> Unit
) {
    val categoryEmoji = when {
        record.itemName.contains("tomato", ignoreCase = true) || record.itemName.contains("onion", ignoreCase = true) || record.itemName.contains("vegetable", ignoreCase = true) || record.itemName.contains("carrot", ignoreCase = true) -> "🧅"
        record.itemName.contains("gold", ignoreCase = true) || record.itemName.contains("silver", ignoreCase = true) || record.itemName.contains("metal", ignoreCase = true) -> "🪙"
        record.itemName.contains("rice", ignoreCase = true) || record.itemName.contains("turmeric", ignoreCase = true) || record.itemName.contains("grain", ignoreCase = true) -> "🍚"
        else -> "⛽"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = categoryEmoji, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Mid detail column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.itemName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "${record.quantity} ${record.unit} paid ₹${record.pricePaid}/${record.unit}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }

                // Total display on target right
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    val totalPaid = record.pricePaid * record.quantity
                    Text(
                        text = "₹${String.format("%,.2f", totalPaid)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (avgPriceRate != null) {
                        val saving = (avgPriceRate - record.pricePaid) * record.quantity
                        val goodBuy = saving >= 0
                        val text = if (goodBuy) "+ Saved ₹${String.format("%.1f", saving)}" 
                                   else "- Premium ₹${String.format("%.1f", -saving)}"
                        val col = if (goodBuy) TrendUpColor else TrendDownColor
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = col,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            if (record.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Notes: ${record.notes}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSecondaryContainer),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.date} • ${record.cityId.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_purchase_btn_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete buying history line",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Dialog for adding Purchase Records
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseDialog(
    marketItems: List<CommodityItem>,
    cities: List<City>,
    activeCity: City,
    onDismiss: () -> Unit,
    onSave: (itemName: String, cityId: String, qty: Double, price: Double, unit: String, notes: String) -> Unit
) {
    var selectedItem by remember { mutableStateOf(marketItems.firstOrNull() ?: MarketRepository.defaultItems.first()) }
    var chooseCity by remember { mutableStateOf(activeCity) }
    var qtyText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    var expandedItemDropdown by remember { mutableStateOf(false) }
    var expandedCityDropdown by remember { mutableStateOf(false) }

    // Update default price when selection swaps
    LaunchedEffect(selectedItem, chooseCity) {
        val p = selectedItem.basePrices[chooseCity.id] ?: selectedItem.basePrices.values.first()
        priceText = p.toString()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Log Paid Invoice Rate",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Record actual price paid on a certain purchase to compare with local average rates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // Select Item Box
                Column {
                    Text("Select Commodity (பொருள்)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                            .clickable { expandedItemDropdown = true }
                            .padding(14.dp)
                    ) {
                        Text("${selectedItem.name} (${selectedItem.tamilName})")
                    }
                    DropdownMenu(
                        expanded = expandedItemDropdown,
                        onDismissRequest = { expandedItemDropdown = false }
                    ) {
                        marketItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.name} (${item.tamilName})") },
                                onClick = {
                                    selectedItem = item
                                    expandedItemDropdown = false
                                }
                            )
                        }
                    }
                }

                // Select City Box
                Column {
                    Text("Select Purchase City (நகரம்)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                            .clickable { expandedCityDropdown = true }
                            .padding(14.dp)
                    ) {
                        Text("${chooseCity.name} (${chooseCity.tamilName})")
                    }
                    DropdownMenu(
                        expanded = expandedCityDropdown,
                        onDismissRequest = { expandedCityDropdown = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text("${city.name} (${city.tamilName})") },
                                onClick = {
                                    chooseCity = city
                                    expandedCityDropdown = false
                                }
                            )
                        }
                    }
                }

                // Quantity and Price Text Fields Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        modifier = Modifier.weight(1f).testTag("qty_input"),
                        label = { Text("Qty (${selectedItem.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        modifier = Modifier.weight(1.2f).testTag("price_input"),
                        label = { Text("Paid Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Purchase Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    modifier = Modifier.fillMaxWidth().testTag("notes_input"),
                    label = { Text("Purchase Notes / Vendor (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("save_dialog_btn"),
                        onClick = {
                            val qty = qtyText.toDoubleOrNull() ?: 1.0
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            onSave(
                                selectedItem.name,
                                chooseCity.id,
                                qty,
                                price,
                                selectedItem.unit,
                                notesText
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Log")
                    }
                }
            }
        }
    }
}


// ==================== TAB 3: PRICE CALCULATOR ====================
@Composable
fun PriceCalculatorTab(
    viewModel: MarketViewModel,
    marketItems: List<CommodityItem>,
    selectedCity: City
) {
    val basket by viewModel.basket.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Calculator Banner/Header styled like visual briefing daily card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "BASKET CALCULATION SLIP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Increment count tags of crops, silver, or fuel below to estimate a combined purchase slip instantly in ${selectedCity.name}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spreadsheet like list of items to adjust quantity
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(marketItems) { item ->
                val qty = basket[item.id] ?: 0.0
                val unitPrice = item.basePrices[selectedCity.id] ?: item.basePrices.values.first()
                CalculatorItemRow(
                    item = item,
                    qty = qty,
                    unitPrice = unitPrice,
                    onQtyChange = { viewModel.updateBasketQuantity(item.id, it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sticky bill summary
        val grandTotal = viewModel.getBasketTotal(selectedCity.id)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estimated Purchase Slip",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Text(
                            text = "Based on averages in ${selectedCity.name}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${String.format("%,.2f", grandTotal)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("basket_grand_total")
                        )
                    }
                }

                if (grandTotal > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.clearBasket() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_basket_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear Grocery Basket", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
fun CalculatorItemRow(
    item: CommodityItem,
    qty: Double,
    unitPrice: Double,
    onQtyChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "₹${String.format("%.2f", unitPrice)} / ${item.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Price calculated component
            val itemSum = unitPrice * qty
            if (qty > 0.0) {
                Text(
                    text = "₹${String.format("%.1f", itemSum)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            // Math counter increment indicators + -
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { onQtyChange(Math.max(0.0, qty - 1.0)) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        .testTag("dec_qty_${item.id}")
                ) {
                    Text("-", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }

                Box(
                    modifier = Modifier
                        .widthIn(min = 32.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (qty == qty.toInt().toDouble()) qty.toInt().toString() else String.format("%.1f", qty),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black)
                    )
                }

                IconButton(
                    onClick = { onQtyChange(qty + 1.0) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        .testTag("inc_qty_${item.id}")
                ) {
                    Text("+", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}


// ==================== TAB 4: GURU AI ASSISTANT ====================
@Composable
fun GuruAssistantTab(
    viewModel: MarketViewModel
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAssistantResponding by viewModel.isAssistantResponding.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val suggestionChips = listOf(
        "Suggest cheap lunch recipe under ₹100?",
        "Tips for investing in gold in Chennai?",
        "Are tomato prices rising or falling?",
        "Give me today's Erode turmeric news."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Chat screen header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "TN price advisor avatar logo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Price Guru / விலை குரு",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Expert Tamil Nadu Market pricing advisor",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Message board
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(chatMessages) { msg ->
                ChatBubble(msg = msg)
            }

            if (isAssistantResponding) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Guru is researching wholesale catalogs...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Quick suggestion tags row
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Guru's Hot Topics (கேளுங்கள்):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.DarkGray,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(suggestionChips) { chip ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable {
                                    viewModel.sendAssistantMessage(chip)
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Send Text input row bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = { Text("Ask TN Price Guru...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.trim().isNotEmpty()) {
                        viewModel.sendAssistantMessage(textInput)
                        textInput = ""
                    }
                    focusManager.clearFocus()
                }),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                )
            )

            IconButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        viewModel.sendAssistantMessage(textInput)
                        textInput = ""
                    }
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag("chat_send_button")
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isAi = msg.sender == "ai"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isAi) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Column(
            horizontalAlignment = if (isAi) Alignment.Start else Alignment.End,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isAi) 2.dp else 16.dp,
                    bottomEnd = if (isAi) 16.dp else 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAi) MaterialTheme.colorScheme.primaryContainer 
                                     else MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isAi) MaterialTheme.colorScheme.onPrimaryContainer 
                                    else Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg.timestamp,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.Gray
            )
        }
    }
}
