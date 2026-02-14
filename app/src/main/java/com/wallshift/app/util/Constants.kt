package com.wallshift.app.util

object Constants {
    const val UNSPLASH_BASE_URL = "https://api.unsplash.com/"
    const val PEXELS_BASE_URL = "https://api.pexels.com/"
    const val PIXABAY_BASE_URL = "https://pixabay.com/"
    const val WALLHAVEN_BASE_URL = "https://wallhaven.cc/"

    const val DEFAULT_CACHE_SIZE_MB = 100
    const val DEFAULT_FREQUENCY_MINUTES = 360 // 6 hours
    const val MIN_WORK_INTERVAL_MINUTES = 5L

    const val WALLPAPER_CHANGE_WORK_TAG = "wallpaper_change"
    const val WALLPAPER_CHANGE_CHAIN_TAG = "wallpaper_change_chain"
    const val WALLPAPER_CACHE_DIR = "wallpapers"

    const val IMAGES_PER_PAGE = 30

    /** Prefix used to distinguish user-created custom categories. */
    const val CUSTOM_CATEGORY_PREFIX = "custom:"

    val DEFAULT_CATEGORIES = setOf("nature", "minimal", "abstract")

    // --- 55 built-in categories organized by theme ---

    val AVAILABLE_CATEGORIES = listOf(
        // Devotional & Spiritual
        "radha",
        "krishna",
        "radhakrishna",
        "mahadev",
        "narayan",
        "gauri",
        "vrindavan",
        "braj",
        "nikunj",
        // Nature & Landscapes
        "nature",
        "mountains",
        "ocean",
        "sunset",
        "sunrise",
        "forest",
        "beach",
        "waterfall",
        "desert",
        "lake",
        "sky",
        "flowers",
        "autumn",
        "winter",
        "tropical",
        // Urban & Architecture
        "cityscapes",
        "architecture",
        "street",
        "skyline",
        "neon",
        "urban",
        // Space & Science
        "space",
        "galaxy",
        "planets",
        "nebula",
        "aurora",
        // Art & Design
        "abstract",
        "minimal",
        "geometric",
        "gradient",
        "watercolor",
        "digital art",
        "3d render",
        "typography",
        "pattern",
        // Dark & Moody
        "dark",
        "black",
        "gothic",
        "moody",
        // Lifestyle & Culture
        "vintage",
        "retro",
        "aesthetic",
        "pastel",
        "boho",
        "japanese",
        "cyberpunk",
        "steampunk",
        // Vehicles & Machines
        "cars",
        "motorcycles",
        "aviation",
        // Animals & Wildlife
        "animals",
        "cats",
        "dogs",
        "wildlife",
        "underwater",
        // Pop Culture
        "anime",
        "gaming",
        "superhero",
        "fantasy",
        "sci-fi",
        // Food & Drinks
        "food",
        "coffee",
    )

    /** Sentinel value indicating "Custom" frequency was chosen. */
    const val CUSTOM_FREQUENCY_SENTINEL = -1

    val FREQUENCY_OPTIONS = listOf(
        5 to "5 minutes",
        10 to "10 minutes",
        15 to "15 minutes",
        30 to "30 minutes",
        60 to "1 hour",
        360 to "6 hours",
        720 to "12 hours",
        1440 to "Daily",
        CUSTOM_FREQUENCY_SENTINEL to "Custom",
    )
}
