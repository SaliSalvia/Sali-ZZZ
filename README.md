# Sali-ZZai — Native Android App

<div dir="rtl">

اپ نیتیو اندروید (Kotlin + Jetpack Compose) برای Z.ai — بدون نیاز به سرور!

## ✨ ویژگی‌ها

- 🔐 **بدون سرور** — تمام داده‌ها روی گوشی ذخیره می‌شوند (Room Database)
- 🎨 **Material 3 + Jetpack Compose** — رابط کاربری مدرن اندروید
- 📦 **استخراج ZIP روی دستگاه** — با `java.util.zip` (بدون کتابخانه خارجی)
- 💬 **چت استریم زنده** با Z.ai API (OkHttp + SSE)
- 🗂️ **نمایشگر درختی تعاملی** ساختار پوشه ZIP
- 🖼️ **تولید تصویر** با Z.ai
- 🔍 **جستجوی وب** با Z.ai
- 🔒 **رمزنگاری** API Key با EncryptedSharedPreferences
- 🎨 **لوگوی Z بنفش** (#7C1FA8) با آیکون Adaptive

## 🛠 تکنولوژی‌ها

- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM (ViewModel + StateFlow)
- **Networking**: OkHttp + Kotlin Serialization + SSE
- **Database**: Room (SQLite)
- **Preferences**: DataStore (Encrypted)
- **Image Loading**: Coil
- **Markdown**: Markwon
- **Build**: Gradle 8.9 (Kotlin DSL)

## 🏗️ ساخت APK

### روش ۱: GitHub Actions (خودکار)
این ریپو شامل workflow کاملی است که APK را خودکار می‌سازد:

۱. پروژه را به GitHub پوش کنید
۲. به تب **Actions** بروید
۳. روی **Build Android APK** کلیک کنید
۴. روی **Run workflow** بزنید
۵. پس از ~۱۰ دقیقه، APK را از **Artifacts** دانلود کنید

### روش ۲: Android Studio
۱. این پروژه را در Android Studio باز کنید
۲. روی **Run** بزنید (Shift+F10)
۳. APK در `app/build/outputs/apk/debug/` ساخته می‌شود

### روش ۳: خط فرمان
```bash
./gradlew assembleRelease
# APK در: app/build/outputs/apk/release/app-release.apk
```

## 📱 نصب APK

پس از دانلود APK:
- در مرورگر اندروید، روی APK بزنید
- اجازه "نصب از منابع ناشناس" بدهید
- برنامه را باز کنید

## 🔑 تنظیم API Key

برنامه را باز کنید → به **تنظیمات** بروید → **Z.ai API Key** را وارد کنید.

برای دریافت API Key:
۱. به https://chat.z.ai بروید
۲. وارد شوید
۳. به **Settings → API Keys** بروید

## 📁 ساختار پروژه

```
native-android/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/salizzai/app/
│       │   ├── SaliZZaiApp.kt          # Application class
│       │   ├── MainActivity.kt         # Single activity
│       │   ├── data/
│       │   │   ├── api/ZaiApiClient.kt # Direct Z.ai calls (no server)
│       │   │   ├── db/                 # Room database + DAOs
│       │   │   ├── model/              # Data classes
│       │   │   └── prefs/              # UserPreferences
│       │   ├── feature/
│       │   │   ├── chat/               # Chat screen + ViewModel
│       │   │   ├── history/            # History list
│       │   │   ├── settings/           # Settings screen
│       │   │   ├── imagegen/           # Image generation
│       │   │   ├── discover/           # Web search
│       │   │   └── ziptree/            # ZIP file tree viewer
│       │   ├── ui/
│       │   │   ├── theme/              # Color, Theme, Typography
│       │   │   └── components/         # Markdown, ThinkingIndicator
│       │   └── util/
│       │       ├── PasswordHasher.kt   # PBKDF2 hashing
│       │       └── ZipExtractor.kt     # ZIP extraction
│       └── res/
│           ├── drawable/               # Logo (Z with purple)
│           ├── values/                 # Strings, colors, themes
│           └── xml/                    # Backup + network config
├── .github/workflows/build-apk.yml    # GitHub Actions
├── build.gradle.kts                   # Project-level
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## 📜 لایسنس

MIT

</div>
