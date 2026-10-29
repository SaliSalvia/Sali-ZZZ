# Sali-ZZai Native Android — Quick Start (v1.0.0 — Reviewed)

<div dir="rtl">

## ✅ بازبینی شده و بدون باگ

این نسخه پس از بازبینی کامل، تمام باگ‌های زیر اصلاح شده‌اند:

- ✅ **C1**: `TypeConverters` حذف شد (لازم نبود)
- ✅ **C2**: `String.copy()` در ChatViewModel
- ✅ **C3**: `@Composable` در try/catch در ImageGenScreen
- ✅ **C4**: `MaterialTheme.colorScheme` در AndroidView factory
- ✅ **L1**: اضافه‌شدن observerهای thinkingEnabled/temperature/systemPrompt
- ✅ **L2**: پاک‌کردن imageBase64 قبل از درخواست جدید
- ✅ **L3**: layout مشکل در DiscoverScreen
- ✅ **L4**: خواندن currentState داخل coroutine
- ✅ **L5**: minSdk به 26 (برای PBKDF2WithHmacSHA256)
- ✅ **L6**: آپدیت رنگ MarkdownText در recomposition
- ✅ تم activity (نه splash) برای جلوگیری از flash
- ✅ imports مرده پاک شدند

## 🚀 شروع سریع — ۳ مرحله

### ۱. دانلود ZIP

```bash
curl -fsSL -o ~/storage/downloads/Sali-ZZai-native.zip \
  https://preview-bot-7v60rw8c.space-z.ai/Sali-ZZai-native.zip
```

> ⚠️ آدرس `preview-bot-7v60rw8c.space-z.ai` را با Preview URL واقعی خودتان از پنل سمت راست عوض کنید.

### ۲. پوش به GitHub

```bash
cd ~ && unzip ~/storage/downloads/Sali-ZZai-native.zip -d Sali-ZZai-native
cd Sali-ZZai-native

git init
git add .
git commit -m "feat: Sali-ZZai v1.0.0 — native Android app (reviewed)"

gh repo create Sali-ZZai --public \
  --source=. \
  --remote=origin \
  --push \
  --description "Sali-ZZai — Native Android AI Assistant (no server)"
```

### ۳. ساخت APK خودکار

۱. به ریپو در GitHub بروید
۲. تب **Actions** → در سمت چپ **Build Native Android APK** را کلیک کنید
۳. روی **Run workflow** بزنید
۴. صبر کنید ~۱۰-۱۵ دقیقه
۵. در صفحه run، به پایین اسکرول کنید
۶. در بخش **Artifacts**، روی `sali-zzai-apk` کلیک کنید
۷. فایل `Sali-ZZai.apk` دانلود می‌شود

## 🔑 نصب و راه‌اندازی APK

۱. APK را به گوشی منتقل کنید (یا اگر در Downloads است، بگذارید)
۲. در File Manager روی APK بزنید
۳. اجازه "نصب از منابع ناشناس" بدهید
۴. برنامه را باز کنید 🎉
۵. به **تنظیمات** (آیکون ⚙️ پایین صفحه) بروید
۶. در بخش **حساب Z.ai**، **Z.ai API Key** را وارد کنید
۷. روی **ذخیره** بزنید

برای دریافت API Key:
۱. به https://chat.z.ai بروید
۲. وارد شوید
۳. به **Settings → API Keys** بروید
۴. یک key بسازید و کپی کنید

## 📱 ویژگی‌ها

- 💬 **چت با استریم زنده** (SSE از Z.ai)
- 📦 **تحلیل ZIP** (با java.util.zip، بدون کتابخانه خارجی)
- 🗂️ **نمایشگر درختی** تعاملی ساختار پوشه‌ها
- 🖼️ **تولید تصویر** در ۵ اندازه
- 🔍 **جستجوی وب**
- 📚 **تاریخچه گفتگوها**
- 🎨 **Material 3** + Jetpack Compose
- 🔒 **رمزنگاری** API Key با EncryptedSharedPreferences
- 🎨 **لوگوی Z بنفش** (#7C1FA8)
- 🌐 **بدون سرور** — همه چیز روی دستگاه

## 🛠 بدون سرور — چطور کار می‌کند؟

این اپ **کاملاً آفلاین** کار می‌کند:

| ویژگی | محل ذخیره |
|------|----------|
| گفتگوها | Room Database (SQLite) روی گوشی |
| تنظیمات | DataStore روی گوشی |
| API Key | EncryptedSharedPreferences |
| ZIP extraction | با `java.util.zip` روی دستگاه |
| API calls | مستقیم به `https://api.z.ai` (OkHttp + SSE) |
| Password hashing | PBKDF2WithHmacSHA256 |

هیچ Vercel، Netlify، Backend، یا سرور لازم نیست!

## 🆘 رفع اشکال

### Workflow fail — "Could not find gradle-wrapper.jar"
اسکریپت workflow خودش wrapper را تولید می‌کند. اگر خطا داد، دستی اجرا کنید:
```bash
gradle wrapper --gradle-version 8.9
git add gradle/wrapper/gradle-wrapper.jar
git commit -m "chore: add gradle wrapper jar"
git push
```

### APK نصب نمی‌شود
اجازه "نصب از منابع ناشناس" بدهید:
**Settings → Apps → Special Access → Install Unknown Apps**

### برنامه باز می‌شود ولی چت کار نمی‌کند
به **تنظیمات** بروید و **Z.ai API Key** را وارد کنید.

### خطای "Network error"
- مطمئن شوید اینترنت وصل است
- مطمئن شوید API Key صحیح است
- مطمئن شوید Base URL: `https://api.z.ai/api/v1` است

### خطای build در GitHub Actions
به تب Actions بروید، روی workflow run failed کلیک کنید، لاگ را ببینید. اگر کاری از دستتان برنمی‌آید، در Issues بنویسید.

## 📜 لایسنس

MIT — آزاد برای استفاده، تغییر، و توزیع.

</div>

