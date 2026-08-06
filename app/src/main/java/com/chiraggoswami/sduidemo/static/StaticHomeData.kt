package com.chiraggoswami.sduidemo.static

/** Hardcoded twin of home_design.json's `items[]` arrays — literal Kotlin, not decoded JSON. */
data class StaticCarItem(
    val carId: String,
    val imageUrl: String,
    val assuredLabel: String = "",
    val name: String,
    val variant: String = "",
    val specs: List<String> = emptyList(),
    val price: String = "",
    val emi: String = "",
    val footnote: String = "",
    val badges: List<String> = emptyList(),
    val rankLabel: String = "",
)

data class StaticTileItem(val id: String, val title: String, val imageUrl: String)

val recentCars = listOf(
    StaticCarItem(
        carId = "creta2016",
        imageUrl = "https://images.pexels.com/photos/25637367/pexels-photo-25637367.jpeg",
        assuredLabel = "Cars24 Owned stock",
        name = "2016 Hyundai Creta",
        variant = "SX PLUS 1.6 PETROL",
        specs = listOf("72,971 km", "Petrol", "Manual", "GJ01"),
        price = "₹5.21 lakh",
        emi = "EMI ₹11,598/m*",
        footnote = "+other charges",
        badges = listOf("Zero Worry Max", "Lifetime warranty"),
    ),
    StaticCarItem(
        carId = "baleno2021",
        imageUrl = "https://images.pexels.com/photos/26834313/pexels-photo-26834313.jpeg",
        assuredLabel = "Cars24 Owned stock",
        name = "2021 Maruti Baleno",
        variant = "ZETA PETROL 1.2",
        specs = listOf("20,518 km", "Petrol", "Manual", "GJ05"),
        price = "₹4.76 lakh",
        emi = "EMI ₹10,602/m*",
        footnote = "+other charges",
        badges = listOf("Zero Worry Max", "Lifetime warranty"),
    ),
)

val hotCars = listOf(
    StaticCarItem(
        carId = "swift2019",
        imageUrl = "https://images.pexels.com/photos/34712982/pexels-photo-34712982.jpeg",
        assuredLabel = "Cars24 Owned stock",
        name = "2019 Maruti Swift",
        variant = "VXI 1.2",
        specs = listOf("41,204 km", "Petrol", "Manual", "GJ01"),
        price = "₹4.15 lakh",
        emi = "EMI ₹9,240/m*",
        footnote = "+other charges",
        badges = listOf("Hot deal", "Lifetime warranty"),
    ),
)

private const val TRENDING_IMAGE = "https://picsum.photos/200/300?grayscale"

val trendingCars = listOf(
    StaticCarItem(carId = "seltos", imageUrl = TRENDING_IMAGE, name = "Seltos", variant = "Kia", rankLabel = "1"),
    StaticCarItem(carId = "sonet", imageUrl = TRENDING_IMAGE, name = "Sonet", variant = "Kia", rankLabel = "2"),
    StaticCarItem(carId = "syros", imageUrl = TRENDING_IMAGE, name = "Syros", variant = "Kia", rankLabel = "3"),
)

val buyTiles = listOf(
    StaticTileItem("t_all", "All used cars", "https://placehold.net/8.png"),
    StaticTileItem("t_budget", "Budget used cars", "https://placehold.net/8.png"),
    StaticTileItem("t_premium", "Premium used cars", "https://placehold.net/8.png"),
    StaticTileItem("t_new", "New cars", "https://placehold.net/8.png"),
)

val sellTiles = listOf(
    StaticTileItem("s_sell", "Sell your car", "https://placehold.net/5.png"),
    StaticTileItem("s_valuation", "Check car valuation", "https://placehold.net/5.png"),
    StaticTileItem("s_scrap", "Scrap your car", "https://placehold.net/5.png"),
)

val loanTiles = listOf(
    StaticTileItem("l_used", "Used car loan", "https://placehold.net/7.png"),
    StaticTileItem("l_against", "Loan against car", "https://placehold.net/7.png"),
    StaticTileItem("l_personal", "Personal loan", "https://placehold.net/7.png"),
)

val checkServices = listOf(
    StaticTileItem("g_pdi", "New car PDI", "file:///android_asset/images/ic_pdi.png"),
    StaticTileItem("g_check", "Used car check", "file:///android_asset/images/ic_check.png"),
    StaticTileItem("g_history", "Vehicle history", "file:///android_asset/images/ic_history.png"),
    StaticTileItem("g_challan", "Check challan", "file:///android_asset/images/ic_challan.png"),
    StaticTileItem("g_ins", "Check car insurance", "file:///android_asset/images/ic_insurance.png"),
    StaticTileItem("g_odo", "Odometer tampering", "file:///android_asset/images/ic_odometer.png"),
)

/** tenure -> (monthly EMI, breakdown line). Same figures as the `emi_breakup` sheet in home_design.json. */
val emiByTenure = mapOf(
    "12" to ("₹37,220 / month" to "Down payment ₹1,04,200 · 12 months · 12.5% p.a."),
    "24" to ("₹19,540 / month" to "Down payment ₹1,04,200 · 24 months · 12.5% p.a."),
    "36" to ("₹13,780 / month" to "Down payment ₹1,04,200 · 36 months · 12.5% p.a."),
    "48" to ("₹12,150 / month" to "Down payment ₹1,04,200 · 48 months · 12.5% p.a."),
    "60" to ("₹11,598 / month" to "Down payment ₹1,04,200 · 60 months · 12.5% p.a."),
)

val emiTenures = listOf("12" to "12 mo", "24" to "24 mo", "36" to "36 mo", "48" to "48 mo", "60" to "60 mo")
val topTabs = listOf("all" to "All", "buy" to "Buy used car", "sell" to "Sell car", "loan" to "Loans")
val carsFilterChips = listOf("recent" to "Recently viewed", "hot" to "Hot deals")
