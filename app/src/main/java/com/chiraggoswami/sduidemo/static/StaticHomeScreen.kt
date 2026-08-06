package com.chiraggoswami.sduidemo.static

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
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            StaticHeader()
            Box(Modifier.fillMaxWidth().background(Color(0xFF5B4FE9)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                StaticChipRow(topTabs, topTab, onSelect = { topTab = it })
            }
            if (topTab == "all" || topTab == "buy") StaticHeroSection()
            if (topTab == "all" || topTab == "buy") StaticBuySection()
            if (topTab == "all" || topTab == "sell") StaticSellSection()
            if (topTab != "buy" && topTab != "sell") StaticLoanSection()
            StaticChecksSection()
            StaticLoveSection(
                filter = carsFilter,
                onFilterSelect = { carsFilter = it },
                isWishlisted = { wishlist[it] ?: false },
                onWishlistToggle = ::toggleWishlist,
                onEmiClick = { emiSheetOpen = true },
            )
            StaticImageBanner("https://picsum.photos/seed/picsum/200/300")
            StaticTrendingSection(isWishlisted = { wishlist[it] ?: false }, onWishlistToggle = ::toggleWishlist)
            StaticImageBanner("https://placehold.net/2.png")
            StaticImageBanner("https://placehold.net/map-400x400.png", bottomPadding = 24)
            StaticFooter()
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (emiSheetOpen) {
        ModalBottomSheet(onDismissRequest = { emiSheetOpen = false }) {
            StaticEmiSheet(emiTenure, onTenureSelect = { emiTenure = it }, onDismiss = { emiSheetOpen = false })
        }
    }
}
