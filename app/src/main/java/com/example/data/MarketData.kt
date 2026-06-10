package com.example.data

import com.squareup.moshi.JsonClass

data class Category(
    val id: String,
    val name: String,
    val tamilName: String,
    val iconName: String
)

data class CommodityItem(
    val id: String,
    val name: String,
    val tamilName: String,
    val categoryId: String,
    val unit: String,
    val tamilUnit: String,
    val basePrices: Map<String, Double>, // City ID -> Price
    val isTrendUp: Boolean, // trend comparison
    val description: String,
    val tamilDescription: String
)

data class City(
    val id: String,
    val name: String,
    val tamilName: String,
    val isMetro: Boolean
)

@JsonClass(generateAdapter = true)
data class PurchaseRecord(
    val id: String,
    val itemName: String,
    val cityId: String,
    val quantity: Double,
    val pricePaid: Double,
    val unit: String,
    val date: String,
    val notes: String = ""
)

object MarketRepository {
    val categories = listOf(
        Category("vegetables", "Vegetables", "காய்கறிகள்", "carrot"),
        Category("metals", "Precious Metals", "தங்கம் & வெள்ளி", "gold"),
        Category("grains", "Staples & Grains", "தானியங்கள்", "grain"),
        Category("fuel", "Fuel", "எரிபொருள்", "local_gas_station")
    )

    val cities = listOf(
        City("chennai", "Chennai", "சென்னை", true),
        City("coimbatore", "Coimbatore", "கோயம்புத்தூர்", false),
        City("madurai", "Madurai", "மதுரை", false),
        City("trichy", "Trichy", "திருச்சி", false),
        City("salem", "Salem", "சேலம்", false)
    )

    val defaultItems = listOf(
        // Vegetables
        CommodityItem(
            id = "veg_tomato",
            name = "Tomato",
            tamilName = "தக்காளி",
            categoryId = "vegetables",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 42.0,
                "coimbatore" to 38.0,
                "madurai" to 35.0,
                "trichy" to 36.0,
                "salem" to 34.0
            ),
            isTrendUp = true,
            description = "Fresh country and hybrid tomatoes from local farms.",
            tamilDescription = "உள்ளூர் பண்ணைகளில் இருந்து புதிய நாட்டு மற்றும் ஹைப்ரிட் தக்காளி."
        ),
        CommodityItem(
            id = "veg_onion",
            name = "Onion (Bellary)",
            tamilName = "வெங்காயம் (பெல்லாரி)",
            categoryId = "vegetables",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 32.0,
                "coimbatore" to 29.0,
                "madurai" to 28.0,
                "trichy" to 30.0,
                "salem" to 28.0
            ),
            isTrendUp = false,
            description = "Bellary medium onions from Tamil Nadu and Maharashtra markets.",
            tamilDescription = "தமிழ்நாடு மற்றும் மகாராஷ்டிரா சந்தைகளில் இருந்து நடுத்தர பெல்லாரி வெங்காயம்."
        ),
        CommodityItem(
            id = "veg_onion_small",
            name = "Small Onion (Sambar)",
            tamilName = "சின்ன வெங்காயம்",
            categoryId = "vegetables",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 65.0,
                "coimbatore" to 58.0,
                "madurai" to 54.0,
                "trichy" to 56.0,
                "salem" to 55.0
            ),
            isTrendUp = true,
            description = "Premium small onions, highly popular for Sambar.",
            tamilDescription = "சாம்பாருக்கு மிகவும் பிரபலமான பிரீமியம் ரக சின்ன வெங்காயம்."
        ),
        CommodityItem(
            id = "veg_potato",
            name = "Potato",
            tamilName = "உருளைக்கிழங்கு",
            categoryId = "vegetables",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 35.0,
                "coimbatore" to 31.0,
                "madurai" to 32.0,
                "trichy" to 33.0,
                "salem" to 30.0
            ),
            isTrendUp = false,
            description = "Ooty and Mettupalayam quality potatoes.",
            tamilDescription = "ஊட்டி மற்றும் மேட்டுப்பாளையம் தரமான உருளைக்கிழங்கு."
        ),
        CommodityItem(
            id = "veg_drumstick",
            name = "Drumstick",
            tamilName = "முருங்கைக்காய்",
            categoryId = "vegetables",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 50.0,
                "coimbatore" to 44.0,
                "madurai" to 40.0,
                "trichy" to 42.0,
                "salem" to 45.0
            ),
            isTrendUp = true,
            description = "Fresh green drumsticks from southern farming hubs.",
            tamilDescription = "தென்னக விவசாய மையங்களில் இருந்து புதிய முருங்கைக்காய்."
        ),
        CommodityItem(
            id = "veg_chili",
            name = "Green Chili",
            tamilName = "பச்சை மிளகாய்",
            categoryId = "vegetables",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 48.0,
                "coimbatore" to 42.0,
                "madurai" to 45.0,
                "trichy" to 40.0,
                "salem" to 42.0
            ),
            isTrendUp = false,
            description = "Spicy local green chilies.",
            tamilDescription = "காரமான உள்ளூர் பச்சை மிளகாய்."
        ),

        // Metals
        CommodityItem(
            id = "metal_gold_22k",
            name = "Gold 22K",
            tamilName = "22K ஆபரணத் தங்கம்",
            categoryId = "metals",
            unit = "gram",
            tamilUnit = "கிராம்",
            basePrices = mapOf(
                "chennai" to 7150.0,
                "coimbatore" to 7145.0,
                "madurai" to 7140.0,
                "trichy" to 7145.0,
                "salem" to 7135.0
            ),
            isTrendUp = true,
            description = "Ornament Gold (22 Karat) per gram rate in leading jewelers.",
            tamilDescription = "முன்னணி நகைக்கடைகளில் ஒரு கிராமிற்கு 22 கேரட் ஆபரணத் தங்க விலை."
        ),
        CommodityItem(
            id = "metal_gold_24k",
            name = "Gold 24K",
            tamilName = "24K தூய தங்கம்",
            categoryId = "metals",
            unit = "gram",
            tamilUnit = "கிராம்",
            basePrices = mapOf(
                "chennai" to 7800.0,
                "coimbatore" to 7795.0,
                "madurai" to 7790.0,
                "trichy" to 7795.0,
                "salem" to 7785.0
            ),
            isTrendUp = true,
            description = "Pure Gold (24 Karat / Bullet Gold) per gram rate.",
            tamilDescription = "ஒரு கிராமிற்கு 24 கேரட் தூய தங்கத்தின் தற்போதைய சந்தை விலை."
        ),
        CommodityItem(
            id = "metal_silver",
            name = "Silver",
            tamilName = "வெள்ளி",
            categoryId = "metals",
            unit = "gram",
            tamilUnit = "கிராம்",
            basePrices = mapOf(
                "chennai" to 94.50,
                "coimbatore" to 94.20,
                "madurai" to 94.00,
                "trichy" to 94.30,
                "salem" to 94.10
            ),
            isTrendUp = false,
            description = "Retail silver per gram rate across major cities.",
            tamilDescription = "முக்கிய நகரங்களில் ஒரு கிராம் வெள்ளியின் சில்லறை வர்த்தக விலை."
        ),

        // Staples & Grains
        CommodityItem(
            id = "grain_rice_ponni",
            name = "Ponni Rice (Premium)",
            tamilName = "பொன்னி அரிசி (பிரீமியம்)",
            categoryId = "grains",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 58.0,
                "coimbatore" to 55.0,
                "madurai" to 54.0,
                "trichy" to 52.0,
                "salem" to 53.0
            ),
            isTrendUp = false,
            description = "Double boiled premium old Ponni rice.",
            tamilDescription = "புழுங்கல் முறையில் வேகவைக்கப்பட்டு பதப்படுத்தப்பட்ட பழைய பொன்னி அரிசி."
        ),
        CommodityItem(
            id = "grain_toor_dhal",
            name = "Toor Dhal",
            tamilName = "துவரம் பருப்பு",
            categoryId = "grains",
            unit = "kg",
            tamilUnit = "கிலோ",
            basePrices = mapOf(
                "chennai" to 160.0,
                "coimbatore" to 155.0,
                "madurai" to 152.0,
                "trichy" to 154.0,
                "salem" to 153.0
            ),
            isTrendUp = true,
            description = "Polished grade-A split pigeon peas.",
            tamilDescription = "பளபளப்பான முதல் தர துவரம் பருப்பு சில்லறை விலை."
        ),
        CommodityItem(
            id = "grain_sunflower_oil",
            name = "Sunflower Oil",
            tamilName = "சூரியகாந்தி எண்ணெய்",
            categoryId = "grains",
            unit = "liter",
            tamilUnit = "லிட்டர்",
            basePrices = mapOf(
                "chennai" to 125.0,
                "coimbatore" to 122.0,
                "madurai" to 120.0,
                "trichy" to 121.0,
                "salem" to 119.0
            ),
            isTrendUp = false,
            description = "Double-refined edible sunflower oil (per standard Pouch).",
            tamilDescription = "இருமுறை சுத்திகரிக்கப்பட்ட சூரியகாந்தி சமையல் எண்ணெய் சில்லறை விலை."
        ),

        // Fuel
        CommodityItem(
            id = "fuel_petrol",
            name = "Petrol",
            tamilName = "பெட்ரோல்",
            categoryId = "fuel",
            unit = "liter",
            tamilUnit = "லிட்டர்",
            basePrices = mapOf(
                "chennai" to 100.75,
                "coimbatore" to 101.40,
                "madurai" to 101.65,
                "trichy" to 100.95,
                "salem" to 101.50
            ),
            isTrendUp = false,
            description = "Regular unleaded petrol rate per liter.",
            tamilDescription = "சாதாரண பெட்ரோல் லிட்டருக்கு தற்போதைய சந்தை விலை."
        ),
        CommodityItem(
            id = "fuel_diesel",
            name = "Diesel",
            tamilName = "டீசல்",
            categoryId = "fuel",
            unit = "liter",
            tamilUnit = "லிட்டர்",
            basePrices = mapOf(
                "chennai" to 92.34,
                "coimbatore" to 93.00,
                "madurai" to 93.25,
                "trichy" to 92.55,
                "salem" to 93.10
            ),
            isTrendUp = false,
            description = "Regular retail diesel rate per liter.",
            tamilDescription = "டீசல் லிட்டருக்கு தற்போதைய சில்லறை விற்பனை விலை."
        )
    )
}
