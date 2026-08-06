package com.chiraggoswami.sduidemo.static

import androidx.activity.compose.ReportDrawn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Hardcoded twin of `home_design.json`, rendered entirely in Kotlin — no schema, no registry,
 * no prop decoding. Same content, same interactions (filters, wishlist, EMI sheet), for a fair
 * `:macrobenchmark` comparison against [com.chiraggoswami.sduidemo.screen.SduiScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaticHomeScreen(modifier: Modifier = Modifier) {
    var topTab by remember { mutableStateOf("all") }
    var carsFilter by remember { mutableStateOf("recent") }
    var emiTenure by remember { mutableStateOf("60") }
    var emiSheetOpen by remember { mutableStateOf(false) }
    val wishlist = remember { mutableStateMapOf<String, Boolean>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun toggleWishlist(carId: String) {
        val next = !(wishlist[carId] ?: false)
        wishlist[carId] = next
        val message = if (next) "Car added to favourite list" else "Car removed from favourite list"
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Box(modifier.fillMaxSize()) {
        StaticScrollableRoot(
            topTab = topTab,
            onTopTabSelect = { topTab = it },
            carsFilter = carsFilter,
            onFilterSelect = { carsFilter = it },
            isWishlisted = { wishlist[it] ?: false },
            onWishlistToggle = ::toggleWishlist,
            onEmiClick = { emiSheetOpen = true },
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (emiSheetOpen) {
        ModalBottomSheet(onDismissRequest = { emiSheetOpen = false }) {
            StaticEmiSheet(emiTenure, onTenureSelect = { emiTenure = it }, onDismiss = { emiSheetOpen = false })
        }
    }
}

/**
 * Reports "fully drawn" via [ReportDrawn] — the same TTFD signal `SduiScreen.kt`'s
 * `ScrollableRoot` reports via `ReportDrawnWhen`, so `StartupBenchmark`'s `timeToFullDisplayMs`
 * is a fair comparison across both variants rather than measuring only one of them (see
 * PERF.md). `ReportDrawn()`, not `ReportDrawnWhen { }`: this screen has no async loading state
 * (no schema fetch/parse — see the class doc comment), so there's no readiness condition to
 * wait for, just "report once this composes," which is exactly `ReportDrawn`'s job. `SduiScreen`
 * needs the predicate variant instead because its first composition is a loading spinner, not
 * real content (see that file's doc comment, and PERF.md's headline finding).
 */
@Composable
private fun StaticScrollableRoot(
    topTab: String,
    onTopTabSelect: (String) -> Unit,
    carsFilter: String,
    onFilterSelect: (String) -> Unit,
    isWishlisted: (String) -> Boolean,
    onWishlistToggle: (String) -> Unit,
    onEmiClick: () -> Unit,
) {
    ReportDrawn()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        StaticHeader()
        Box(Modifier.fillMaxWidth().background(Color(0xFF5B4FE9)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            StaticChipRow(topTabs, topTab, onSelect = onTopTabSelect)
        }
        if (topTab == "all" || topTab == "buy") StaticHeroSection()
        if (topTab == "all" || topTab == "buy") StaticBuySection()
        if (topTab == "all" || topTab == "sell") StaticSellSection()
        if (topTab != "buy" && topTab != "sell") StaticLoanSection()
        StaticChecksSection()
        StaticLoveSection(carsFilter, onFilterSelect, isWishlisted, onWishlistToggle, onEmiClick)
        StaticImageBanner("https://picsum.photos/seed/picsum/200/300")
        StaticTrendingSection(isWishlisted, onWishlistToggle)
        StaticImageBanner("https://placehold.net/2.png")
        StaticImageBanner("https://placehold.net/map-400x400.png", bottomPadding = 24)
        StaticFooter()
    }
}
