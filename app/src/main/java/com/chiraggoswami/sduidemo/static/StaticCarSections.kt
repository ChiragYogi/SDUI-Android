package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** "Used cars you'll love": chip filter (recent/hot) + a car_card rail — same shape as sec_love in home_design.json. */
@Composable
fun StaticLoveSection(
    filter: String,
    onFilterSelect: (String) -> Unit,
    isWishlisted: (String) -> Boolean,
    onWishlistToggle: (String) -> Unit,
    onEmiClick: () -> Unit,
) {
    StaticSection(title = "Used cars you'll love", trailingLabel = "View all") {
        Box(Modifier.fillMaxWidth().padding(start = 20.dp, top = 12.dp, end = 20.dp)) {
            StaticChipRow(carsFilterChips, filter, baseColor = Color(0xFF1A1A1A), onSelect = onFilterSelect)
        }
        val cars = if (filter == "hot") hotCars else recentCars
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cars, key = { it.carId }) { car ->
                StaticCarCard(
                    item = car,
                    isWishlisted = isWishlisted(car.carId),
                    onWishlistToggle = { onWishlistToggle(car.carId) },
                    onEmiClick = onEmiClick,
                )
            }
        }
    }
}

/** "Trending new cars": a car_card rail with rank badges instead of price/EMI — sec_trending in home_design.json. */
@Composable
fun StaticTrendingSection(isWishlisted: (String) -> Boolean, onWishlistToggle: (String) -> Unit) {
    StaticSection(title = "Trending new cars", trailingLabel = "View all") {
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(trendingCars, key = { it.carId }) { car ->
                StaticCarCard(
                    item = car,
                    isWishlisted = isWishlisted(car.carId),
                    onWishlistToggle = { onWishlistToggle(car.carId) },
                )
            }
        }
    }
}
