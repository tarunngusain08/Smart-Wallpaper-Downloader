Excellent choice — this can be a **very strong, production-grade Android project** if designed well.

Below is a **concise but professional PRD** for your Smart Wallpaper App.

---

# 📄 Product Requirements Document (PRD)

## 1️⃣ Product Overview

### Working Name

**WallShift**

### Vision

An Android app that automatically applies high-quality wallpapers based on user-selected categories and schedules.

### Core Value

Fresh, curated wallpapers automatically applied without manual browsing.

---

# 2️⃣ Goals & Scope

## ✅ Goals (MVP)

* Select wallpaper categories
* Fetch wallpapers from licensed API (Unsplash/Pexels/etc.)
* Apply wallpaper automatically
* Schedule wallpaper changes
* Avoid duplicate wallpapers
* Cache downloaded images
* Minimal, clean UI

## ❌ Out of Scope (v1)

* User login
* Cloud sync
* Custom image editing
* Social sharing
* Manual crawling of websites

---

# 3️⃣ Target Users

* Users who like changing wallpapers regularly
* Aesthetic-focused Android users
* Productivity/minimal UI users
* Tech-savvy users

---

# 4️⃣ Core Features

---

## 4.1 Category Selection

User selects one or more categories:

* Nature
* Cars
* Minimal
* Abstract
* Space
* Anime
* Architecture
* Add more based on popular categories

User can select multiple categories.

---

## 4.2 Wallpaper Change Frequency

User selects frequency:

* Every 30 minutes
* Every 1 hour
* Every 6 hours
* Every 12 hours
* Daily
* Custom (X hours)

Scheduling via:

* WorkManager (≥15 min)
* AlarmManager if shorter intervals needed

---

## 4.3 Image Fetching

Use official image API:

Examples:

* Unsplash API
* Pexels API
* Pixabay API
* And other popular sources.

System Flow:

```
User category → API request → receive image list
→ filter best candidate → download → cache → apply
```

---

## 4.4 Smart Image Selection Logic

Instead of random selection:

Selection criteria:

* Resolution ≥ device resolution
* Aspect ratio close to screen ratio
* High likes/download count
* Not previously used
* Not recently applied
* High image quality score

Maintain local database of:

* Used image IDs
* Last applied timestamps

---

## 4.5 Wallpaper Application

Use:

* WallpaperManager API

Options:

* Home screen only
* Lock screen only
* Both

Handle:

* Scaling
* Cropping
* Memory optimization

---

## 4.6 Caching Strategy

* Store downloaded images locally
* Limit cache size (e.g., 100MB max)
* Auto-delete oldest images
* Avoid re-downloading same image

---

## 4.7 Duplicate Prevention

Maintain local DB table:

| image_id | applied_at | category |

Before applying:

* Check if already used
* Prefer unseen images

---

## 4.8 Settings Screen

### Options:

1. Select categories
2. Change frequency
3. Apply immediately
4. Enable/disable auto-change
5. Clear cache
6. Choose:

   * Home screen
   * Lock screen
   * Both

## 4.9 Save on the device
* Allow the user to save the current wallpaper locally

---

# 5️⃣ Non-Functional Requirements

## Performance

* Image download < 5 seconds (avg)
* Avoid OutOfMemory crashes
* Efficient image scaling
* Images should be high quality and relevant
* Let the user manually adjust the aspect ratio of the current wallpaper

## Reliability

* Survive app kill
* Resume scheduling after reboot
* Respect battery optimization

## Security & Compliance

* Use licensed APIs only
* Comply with API attribution requirements
* No scraping

---

# 6️⃣ Permissions Required

* SET_WALLPAPER
* INTERNET
* RECEIVE_BOOT_COMPLETED (if auto-restart)

No storage permission needed if using internal cache.

---

# 7️⃣ Technical Architecture

## Language

Kotlin

## Architecture

MVVM + Repository pattern

## Components

* Retrofit → API calls
* Room → store image history
* WorkManager → scheduling
* WallpaperManager → apply wallpaper
* Glide/Coil → image loading

---

# 8️⃣ User Flow

### First Launch

1. User selects categories
2. User selects frequency
3. User taps "Start Auto Wallpaper"
4. App fetches and applies first wallpaper

### Scheduled Change

1. Background worker triggers
2. Fetch new image
3. Apply wallpaper
4. Update DB

---

# 9️⃣ Edge Cases

* No internet
* API rate limit reached
* API returns empty result
* Download fails
* User revokes wallpaper permission
* Device in battery saver
* Screen ratio mismatch

---

# 🔟 MVP Definition

MVP includes:

* Category selection
* API integration
* Smart selection logic
* Wallpaper application
* Scheduling
* Duplicate prevention
* Basic settings screen

---

# 1️⃣1️⃣ Future Enhancements (Post-MVP)

* Dark/light mode detection
* Color-based wallpaper matching
* AI mood-based search
* Blur lock screen option
* Widget shortcut
* Analytics dashboard
* Favorite/save wallpaper feature
* Offline-only rotation mode

