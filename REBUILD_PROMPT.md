# بريمبت بناء تطبيق مشتركين MSR WI-FI من الصفر

> ⚠️⚠️⚠️ تحذير مهم جداً: ⚠️⚠️⚠️
> هذا المشروع هو تطبيق أندرويد أصلي (Native Android App) بصيغة APK
> يُبنى باستخدام Kotlin + Jetpack Compose + Material3
> **ليس** موقع ويب (Not a website)
> **ليس** تطبيق ويب (Not a web app)
> **ليس** محاكاة أو تصميم واجهة (Not a UI mockup)
> **ليس** تطبيق React أو Flutter أو أي إطار عمل cross-platform
> **ليس** صفحة HTML أو CSS
>
> المخرج المطلوب: ملف APK يمكن تثبيته على هاتف أندرويد وتشغيله
> البناء يتم عبر: `./gradlew assembleDebug` أو `./gradlew assembleRelease`
>
> إذا كنت تخطط لإنشاء أي شيء غير تطبيق أندرويد أصلي بـ Kotlin — توقف فوراً.

---

## نظرة عامة على المشروع
تطبيق أندرويد أصلي (Native Android) لمشتركي شبكة ISP يعمل كـ APK مثبت على الهاتف، يسمح لهم بـ:
- تسجيل الدخول والاطلاع على باقتهم النشطة (الجيجات المتبقية، الصلاحية)
- شراء باقات جديدة / توليد كروت هوتسبوت
- تجديد باقتهم الحالية أو تغييرها
- شراء باقات إضافية (Addons)
- شحن رصيد المحفظة عبر تحويل كاش
- مراجعة سجل العمليات واستعادة أكواد الكروت
- فحص كارت شحن (بدون تسجيل دخول)

---

## 1. إعداد المشروع

### build.gradle.kts (Root)
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.secrets) apply false
}
```

### app/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aistudio.msr.subwxlq"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
            storeFile = file(keystorePath)
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isCrunchPngs = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    "ksp"(libs.moshi.kotlin.codegen)
}
```

### settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MSR WiFi"
include(":app")
```

---

## 2. AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="MSR WiFi"
        android:supportsRtl="true"
        android:theme="@style/Theme.MSRWiFi">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 3. سيرفر الباك إند

### Base URL
```
http://13.53.130.231:8080
```

### جميع نقاط الاتصال (API Endpoints)

#### 3.1 اكتشاف الشبكة
```
GET /api/subscriber/tenant-info
 Authorization: لا يتطلب
 Response: {
   "id": 1,
   "username": "mmm123",
   "system_name": "شبكة MSR",
   "wallet_phone": "01012345678",
   "currency": "ج.م"
 }
```

#### 3.2 تسجيل الدخول
```
POST /api/subscriber/login
 Body: {
   "mode": "subscriber",
   "tenant_username": "mmm123",
   "username": "12345",
   "password": "12345"
 }
 Response: {
   "token": "sub_abc123...",
   "subscriber": {
     "id": 1,
     "username": "12345",
     "full_name": "أحمد محمد",
     "balance": 50.0,
     "alt_phone": "01012345678",
     "wallet_phone": "01098765432",
     "active_plan": {
       "code": "12345",
       "plan_name": "باقة 30 جيجا",
       "total_gb": 30.0,
       "used_gb": 10.5,
       "remaining_gb": 19.5,
       "basic_quota_gb": 25.0,
       "addon_quota_gb": 5.0,
       "has_addon": true,
       "validity_days": "20 يوم متبقي",
       "plan_id": 5,
       "plan_type": "hotspot",
       "is_expired": false,
       "is_finished": false,
       "has_ended": false
     },
     "wallet": {
       "fullname": "أحمد محمد",
       "username": "12345",
       "balance": 50.0,
       "outstanding_debt": 0.0,
       "wallet_phone": "01098765432"
     },
     "transactions": [
       {
         "id": 1,
         "amount": 25.0,
         "type": "DEBIT",
         "txn_id": "1234567890123456",
         "description": "توليد كارت شحن: باقة 30 جيجا (كود الكارت: 1234567890123456)",
         "created_at": "2026-08-10T14:30:00Z"
       }
     ],
     "outstanding_debt": 0.0,
     "currency": "ج.م",
     "sub_allow_renew": true,
     "sub_allow_change": true,
     "sub_allow_buy_plan": true,
     "sub_allow_buy_addon": true,
     "sub_show_recharge_page": true,
     "sub_show_plans": true,
     "sub_show_addons": true
   }
 }
```

#### 3.3 جلب بيانات المشترك
```
GET /api/subscriber/me
 Headers: Authorization: Bearer <token>
 Response: {
   "id": 1,
   "username": "12345",
   "full_name": "أحمد محمد",
   "balance": 50.0,
   "alt_phone": "01012345678",
   "wallet_phone": "01098765432",
   "cash_phone": "01098765432",
   "phone_number": "01098765432",
   "active_plan": { ... },
   "wallet": { ... },
   "transactions": [ ... ],
   "outstanding_debt": 0.0,
   "currency": "ج.م",
   "sub_allow_renew": true,
   "sub_allow_change": true,
   "sub_allow_buy_plan": true,
   "sub_allow_buy_addon": true,
   "sub_show_recharge_page": true,
   "sub_show_plans": true,
   "sub_show_addons": true
 }
```

#### 3.4 جلب الباقات المتاحة
```
GET /api/subscriber/plans?type=hotspot&mode=voucher&target=hotspot_subscription
 Headers: Authorization: Bearer <token>
 Response: [
   {
     "id": 5,
     "name": "باقة 30 جيجا شهرية",
     "price": 45.0,
     "total_gb": 30.0,
     "validity_days": "30"
   }
 ]
```

#### 3.5 شراء باقة / توليد كارت
```
POST /api/subscriber/buy-plan
 Headers: Authorization: Bearer <token>
 Body: {
   "plan_id": 5,
   "tenant_username": "mmm123",
   "plan_type": "hotspot",
   "mode": "voucher"
 }
 Response: {
   "code": "1234-5678-9012-3456",
   "voucher_code": "1234-5678-9012-3456",
   "message": "تم التوليد بنجاح",
   "success": true,
   "new_balance": 20.0
 }
```

#### 3.6 إدارة الباقة (تجديد/تغيير)
```
POST /api/subscriber/plan
 Headers: Authorization: Bearer <token>
 Body: {
   "action": "renew",
   "plan_id": 5,
   "plan_type": "subscriber"
 }
 Response: {
   "success": true,
   "message": "تم التجديد بنجاح",
   "voucher_code": null,
   "plan_name": "باقة 30 جيجا شهرية",
   "new_balance": 20.0
 }
```

#### 3.7 جلب الباقات الإضافية
```
GET /api/subscriber/addons
 Headers: Authorization: Bearer <token>
 Response: [
   {
     "id": 3,
     "name": "باقة 5 جيجا إضافية",
     "price": 15.0,
     "total_gb": 5.0,
     "validity_days": "30",
     "download_speed": "10M",
     "upload_speed": "5M"
   }
 ]
```

#### 3.8 شراء باقة إضافية
```
POST /api/subscriber/addon
 Headers: Authorization: Bearer <token>
 Body: {
   "id": 3,
   "plan_id": 3
 }
 Response: {
   "success": true,
   "message": "تمت إضافة الباقة بنجاح",
   "new_balance": 5.0
 }
```

#### 3.9 فحص كارت شحن
```
POST /api/subscriber/voucher-info
 Body: {
   "code": "ABCD-EFGH-IJKL",
   "tenant_username": "mmm123"
 }
 Response: {
   "code": "ABCD-EFGH-IJKL",
   "status": "used",
   "validity_days": "منتهي",
   "total_gb": 30.0,
   "used_gb": 25.0,
   "remaining_gb": 5.0
 }
```

#### 3.10 تحديث رقم الكاش البديل
```
POST /api/subscriber/update-profile
 Headers: Authorization: Bearer <token>
 Body: {
   "alt_phone": "01012345678"
 }
 Response: {
   "message": "تم التحديث بنجاح",
   "success": true
 }
```

#### 3.11 طلب شحن (إرسال رقم الكاش)
```
POST /api/subscriber/payment-request
 Headers: Authorization: Bearer <token>
 Body: {
   "sender_phone": "01012345678"
 }
 Response: {
   "message": "تم استلام الطلب",
   "success": true
 }
```

---

## 4. هيكل ملفات المشروع

```
app/src/main/java/com/example/
├── MainActivity.kt                    # الـ Activity الرئيسي + Navigation
├── config/
│   └── AppConfig.kt                   # إعدادات عامة (BASE_URL، currency، etc.)
├── data/
│   ├── model/
│   │   └── NetworkModels.kt           # جميع Data Models
│   ├── network/
│   │   └── ApiService.kt              # واجهة Retrofit
│   └── repository/
│       └── SubscriberRepository.kt    # مستودع البيانات
├── ui/
│   ├── screens/
│   │   ├── SplashDiscoveryScreen.kt   # شاشة البداية والاكتشاف
│   │   ├── LoginScreen.kt             # شاشة تسجيل الدخول (مشترك + فحص كارت)
│   │   ├── DashboardScreen.kt         # الشاشة الرئيسية
│   │   ├── PlansScreen.kt             # شاشة الباقات + BottomSheet الكروت
│   │   ├── RechargeScreen.kt          # شاشة شحن المحفظة
│   │   ├── VoucherViewScreen.kt       # شاشة تفاصيل الكارت المفحوص
│   │   └── TransactionsScreen.kt      # شاشة سجل العمليات
│   ├── viewmodel/
│   │   └── SubscriberViewModel.kt     # ViewModel الرئيسي
│   └── theme/
│       ├── Color.kt                   # ألوان التطبيق
│       ├── Theme.kt                   # ثيم التطبيق (Dark/Light + RTL)
│       └── Type.kt                    # الخطوط
```

---

## 5. نظام الألوان والثيم

### الألوان الأساسية
```
Dark Theme:
  Background = #09090B (أسود غامق)
  Surface/Card = #18181B (رمادي غامق)
  Primary/Gold = #0EA5E9 (أزرق سماوي)
  Text Primary = #FAFAFA (أبيض)
  Text Secondary = #A1A1AA (رمادي فاتح)
  Success = #10B981 (أخضر)
  Error = #F43F5E (أحمر وردي)
  Warning = #F59E0B (برتقالي)

Light Theme:
  Background = #F4F4F5 (رمادي فاتح جداً)
  Surface/Card = #FFFFFF (أبيض)
  Primary/Blue = #0284C7 (أزرق)
  Text Primary = #09090B (أسود)
  Text Secondary = #52525B (رمادي)
```

### RTL Layout
```kotlin
// في Theme.kt، يجب فرض الاتجاه RTL:
CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
```

---

## 6. شاشات التطبيق بالتفصيل

### 6.1 SplashDiscoveryScreen
- شاشة البداية عند فتح التطبيق
- تعرض شعار "MSR WI-FI" بلون ذهبي
- نص فرعي: "نظام إدارة واشتراك المشتركين الذكي"
- أثناء الاكتشاف: Animated spinner + "جاري اكتشاف الشبكة تلقائياً عبر DNS..."
- بعد الاكتشاف: زر "دخول التطبيق"
- في الأسفل: "الإصدار 1.0.0 © MSR Systems"
- عند الفتح: يستدعي `GET /api/subscriber/tenant-info` لاكتشاف الشبكة
- إذا نجح → يحفظ بيانات الشبكة ويرسّل للـ Login أو Dashboard
- إذا فشل → يستخدم البيانات المحفوظة أو الافتراضية

### 6.2 LoginScreen
- شاشتان في تبويب واحد (Segmented Tab):
  - **حساب مشترك**: حقل اسم المستخدم + كلمة المرور + زر "تسجيل الدخول"
  - **فحص كارت**: حقل كود الكارت + زر "فحص الكارت" (لا يتطلب تسجيل دخول)
- في الزاوية العلوية: زر تبديل المظهر (Sun/Moon)
- في الأعلى: "مرحباً بكم في شبكة" + اسم الشبكة (ديناميكي)
- تاب "حساب مشترك" → `POST /api/subscriber/login` → Dashboard
- تاب "فحص كارت" → `POST /api/subscriber/voucher-info` → VoucherViewScreen

### 6.3 DashboardScreen
- **TopBar**: "MSR Online" + مؤشر أخضر/أحمر + أزرار (تحديث، خروج، تبديل مظهر)
- **بطاقة الرصيد**: Glassmorphism card تعرض:
  - "مرحباً يا [الاسم]"
  - الرصيد المتوفر (بالعملية المحلية)
  - تحذير إذا الرصيد سالب
- **بطاقة المديونية** (إذا وجدت): مبلغ المديونية المستحقة
- **بطاقة الباقة الأساسية** (PremiumPlanCard):
  - badgeText: "الباقة الشهرية"
  - اسم الباقة
  - الجيجات المتبقية من أصل الإجمالي
  - شريط تقدم (Progress Bar)
  - النسبة المئوية
  - النص: "X يوم متبقي" أو "منتهي"
- **بطاقة الباقة الإضافية** (إذا وجدت): نفس التصميم بلون مختلف
- **أزرار الإجراءات** (داخل Card):
  - **باقة إضافية** (إذا `showAddons = true`): يفتح BottomSheet الباقات الإضافية
  - **تجديد الباقة** (إذا `allowRenew = true`): يطلب تأكيد ثم `POST /api/subscriber/plan` بـ action=renew
  - **تغيير الباقة** (إذا `allowChange = true` و `showPlans = true`): يفتح شاشة الباقات بـ action=change
- **خدمات متاحة**:
  - **إنشاء كروت** (إذا `allowBuyPlan = true` و `showPlans = true`): يفتح BottomSheet الكروت
  - **شحن رصيد المحفظة** (إذا `showRechargePage = true`): يفتح شاشة الشحن
  - **سجل العمليات**: يفتح شاشة السجل
- **BottomSheet الباقات الإضافية**: عرض الباقات مع أزرار الشراء + حوار تأكيد
- **Dialog تأكيد التجديد**: يوضح اسم الباقة والمبلغ
- **Dialog نجاح العملية**: أيقونة ✅ + رسالة + زر موافق

### 6.4 PlansScreen
- **عند type=hotspot**: يعرض `VoucherBottomSheetScreen` (BottomSheet يتكرر من الأسفل)
  - شريط سحب
  - عنوان "إنشاء كروت هوتسبوت" أو "تم التوليد بنجاح!"
  - بطاقة الرصيد
  - قائمة الباقات مع (السعر، اسم الباقة، الصلاحية، السعة)
  - بعد التوليد: كود الكارت + أزرار نسخ/مشاركة + العودة للرئيسية
- **عند type=subscriber**: شاشة عادية
  - عنوان "الباقات المتاحة للشبكة"
  - بطاقة الرصيد في الأعلى
  - قائمة الباقات مع (السعر، اسم، الصلاحية، زر الاشتراك)
  - حوار تأكيد قبل الشراء
  - Dialog نجاح بعد الاشتراك

### 6.5 RechargeScreen
- عنوان "شحن رصيد المحفظة"
- بطاقة تحتوي:
  - "شحن رصيد المحفظة عبر تحويل كاش"
  - رقم المحفظة (قابل للنسخ)
  - زر نسخ الرقم
- قسم "هل ستحول من رقم آخر؟"
  - حقل إدخال رقم الكاش البديل
  - زر "تأكيد الرقم قبل التحويل"
  - `POST /api/subscriber/payment-request`

### 6.6 VoucherViewScreen
- تعرض نتائج فحص الكارت (بدون تسجيل دخول)
- أيقونة حالة (✅ نشط / ❌ منتهي)
- بطاقة معلومات الكارت:
  - رمز الكود
  - شريط استهلاك البيانات
  - الصلاحية / الإجمالي / المستخدم / المتبقي
- زر "العودة لشاشة الدخول"

### 6.7 TransactionsScreen
- عنوان "سجل العمليات"
- قائمة العمليات (LazyColumn)
- كل عملية تعرض:
  - التاريخ والوقت
  - المبلغ (سالب/موجب) + العملة
  - الوصف
  - إذا كانت عملية كارت: عرض الكود + زر نسخ

---

## 7. نموذج البيانات (Data Models)

```kotlin
// LoginRequest
data class LoginRequest(
    val mode: String = "subscriber",
    val tenant_username: String,
    val username: String,
    val password: String
)

// LoginResponse
data class LoginResponse(
    val token: String?,
    val subscriber: SubscriberInfo?
)

// SubscriberInfo - النموذج الرئيسي
data class SubscriberInfo(
    val id: Int? = null,
    val username: String? = null,
    val full_name: String? = null,          // ← لاحظ: full_name وليس fullname
    val balance: Double? = null,
    val alt_phone: String? = null,
    val wallet_phone: String? = null,
    val cash_phone: String? = null,
    val phone_number: String? = null,
    val vf_phone1: String? = null,
    val recharge_phone: String? = null,
    val wallet_number: String? = null,
    val active_plan: ActivePlanInfo? = null,
    val wallet: WalletInfo? = null,
    val transactions: List<Transaction>? = null,
    val outstanding_debt: Double? = null,
    val currency: String? = null,
    val sub_allow_renew: Boolean? = true,
    val sub_allow_change: Boolean? = true,
    val sub_allow_buy_plan: Boolean? = true,
    val sub_allow_buy_addon: Boolean? = true,
    val sub_show_recharge_page: Boolean? = true,
    val sub_show_plans: Boolean? = true,
    val sub_show_addons: Boolean? = true
) {
    val allowRenew: Boolean get() = sub_allow_renew ?: true
    val allowChange: Boolean get() = sub_allow_change ?: true
    val allowBuyPlan: Boolean get() = sub_allow_buy_plan ?: true
    val allowBuyAddon: Boolean get() = sub_allow_buy_addon ?: true
    val showRechargePage: Boolean get() = sub_show_recharge_page ?: true
    val showPlans: Boolean get() = sub_show_plans ?: true
    val showAddons: Boolean get() = sub_show_addons ?: true

    val displayFullName: String get() = wallet?.fullName ?: full_name ?: ""
    val displayUsername: String get() = wallet?.username ?: username ?: ""
    val displayBalance: Double get() = wallet?.balance ?: balance ?: 0.0
    val displayOutstandingDebt: Double get() = wallet?.outstandingDebt ?: outstanding_debt ?: 0.0
}

data class ActivePlanInfo(
    val plan_id: Int? = null,
    val plan_type: String? = null,
    val name: String? = null,
    val plan_name: String? = null,
    val total_gb: Double? = null,
    val used_gb: Double? = null,
    val remaining_gb: Double? = null,
    val validity_days: String? = null,
    val basic_quota_gb: Double? = null,
    val addon_quota_gb: Double? = null,
    val has_addon: Boolean? = null
)

data class WalletInfo(
    val fullname: String? = null,
    val username: String? = null,
    val balance: Double? = null,
    val outstanding_debt: Double? = null
)

data class Transaction(
    val id: Int?,
    val amount: Double?,
    val type: String?,
    val txn_id: String?,
    val description: String?,
    val created_at: String?
)

data class Plan(
    val id: Int,
    val name: String?,
    val price: Double?,
    val total_gb: Double?,
    val validity_days: String?
)

data class AddonPlan(
    val id: Int,
    val name: String? = null,
    val price: Double? = null,
    val total_gb: Double? = null,
    val validity_days: String? = null,
    val download_speed: String? = null,
    val upload_speed: String? = null
)

data class VoucherInfoResponse(
    val code: String?,
    val status: String?,
    val validity_days: String?,
    val total_gb: Double?,
    val used_gb: Double?,
    val remaining_gb: Double?
)

//_buyPlan Response
data class BuyPlanResponse(
    val code: String? = null,
    val voucher_code: String? = null,
    val message: String? = null,
    val success: Boolean? = null,
    val new_balance: Double? = null
) {
    val displayVoucherCode: String? get() = voucher_code ?: code
}

data class ManagePlanResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val voucher_code: String? = null,
    val plan_name: String? = null,
    val new_balance: Double? = null
)

data class BuyAddonResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val new_balance: Double? = null
)
```

---

## 8. واجهة API (Retrofit)

```kotlin
interface ApiService {
    @GET("/api/subscriber/tenant-info")
    suspend fun getTenantInfo(): Response<TenantInfoResponse>

    @POST("/api/subscriber/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("/api/subscriber/voucher-info")
    suspend fun getVoucherInfo(@Body body: VoucherInfoRequest): Response<VoucherInfoResponse>

    @GET("/api/subscriber/me")
    suspend fun getMe(@Header("Authorization") authHeader: String): Response<SubscriberInfo>

    @GET("/api/subscriber/plans")
    suspend fun getPlans(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("mode") mode: String? = null,
        @Query("target") target: String? = null
    ): Response<List<Plan>>

    @POST("/api/subscriber/buy-plan")
    suspend fun buyPlan(
        @Header("Authorization") authHeader: String,
        @Body body: BuyPlanRequest
    ): Response<BuyPlanResponse>

    @POST("/api/subscriber/plan")
    suspend fun managePlan(
        @Header("Authorization") authHeader: String,
        @Body body: ManagePlanRequest
    ): Response<ManagePlanResponse>

    @POST("/api/subscriber/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Body body: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @POST("/api/subscriber/payment-request")
    suspend fun sendPaymentRequest(
        @Header("Authorization") authHeader: String,
        @Body body: PaymentRequest
    ): Response<PaymentRequestResponse>

    @GET("/api/subscriber/addons")
    suspend fun getAddons(@Header("Authorization") authHeader: String): Response<List<AddonPlan>>

    @POST("/api/subscriber/addon")
    suspend fun buyAddon(
        @Header("Authorization") authHeader: String,
        @Body body: BuyAddonRequest
    ): Response<BuyAddonResponse>
}
```

---

## 9. SharedPreferences Keys

```kotlin
//(msr_prefs)
KEY_TOKEN = "token"
KEY_TENANT_USERNAME = "tenant_username"
KEY_TENANT_SYSTEM_NAME = "tenant_system_name"
KEY_TENANT_ID = "tenant_id"
KEY_WALLET_PHONE = "wallet_phone"
KEY_SUB_USERNAME = "sub_username"
KEY_SUB_FULLNAME = "sub_fullname"
KEY_SUB_BALANCE = "sub_balance"
KEY_ALT_PHONE = "alt_phone"
KEY_ACTIVE_PLAN_NAME = "active_plan_name"
KEY_ACTIVE_PLAN_TOTAL = "active_plan_total"
KEY_ACTIVE_PLAN_USED = "active_plan_used"
KEY_ACTIVE_PLAN_REMAINING = "active_plan_remaining"
KEY_ACTIVE_PLAN_VALIDITY = "active_plan_validity"
KEY_TRANSACTIONS = "transactions_json"
"currency" = "ج.م"
"sub_allow_renew" = true
"sub_allow_change" = true
"sub_allow_buy_plan" = true
"sub_allow_buy_addon" = true
"sub_show_recharge_page" = true
"sub_show_plans" = true
"sub_show_addons" = true
"local_vouchers" = "[]"
"outstanding_debt" = 0.0

// (app_settings)
"theme_mode" = 0  // 0=System, 1=Light, 2=Dark
```

---

## 10. سلوك التطبيق

### عند التشغيل:
1. يفتح SplashDiscoveryScreen
2. يستدعي `GET /api/subscriber/tenant-info` (لا يتطلب auth)
3. يحدد الشبكة بناءً على IP العام (أو يُرجع خطأ)
4. إذا وجد توكن محفوظ → Dashboard مباشرة
5. إذا لم يجد → LoginScreen

### تسجيل الدخول:
1. يرسل `POST /api/subscriber/login` مع `{mode: "subscriber", tenant_username, username, password}`
2. السيرفر يبحث في: subscriber_wallets → vouchers → pppoe_users → MikroTik (4 مراحل)
3. يُرجع `{token, subscriber}` (subscriber يحتوي على 7 صلاحيات)
4. يحفظ التوكن + بيانات المشترك في SharedPreferences
5. ينتقل إلى Dashboard

### Dashboard:
1. يستدعي `GET /api/subscriber/me` بـ Bearer token
2. يعرض البيانات: الباقة النشطة، الرصيد، المديونية
3. يعرض الأزرار حسب الصلاحيات (7 flags)
4. يعرض باقات إضافية إذا `showAddons = true`

### شراء باقة/كارت:
1. يعرض الباقات من `GET /api/subscriber/plans`
2. المستخدم يختار باقة
3. يعرض حوار تأكيد
4. يرسل `POST /api/subscriber/buy-plan` مع plan_id
5. يعرض كود الكارت + أزرار نسخ/مشاركة

### تجديد/تغيير الباقة:
1. يعرض الباقات من `GET /api/subscriber/plans?type=subscriber`
2. المستخدم يختار باقة
3. يرسل `POST /api/subscriber/plan` مع action=renew أو action=change
4. يعرض رسالة نجاح + يعيد تحميل البيانات

### فحص كارت (بدون تسجيل دخول):
1. من شاشة LoginScreen، تاب "فحص كارت"
2. يرسل `POST /api/subscriber/voucher-info` مع الكود
3. يعرض تفاصيل الكارت (الحالة، الصلاحية، الاستهلاك)

### شحن المحفظة:
1. يعرض رقم محفظة الشبكة (قابل للنسخ)
2. المستخدم يدخل رقم الكاش البديل
3. يرسل `POST /api/subscriber/payment-request` مع sender_phone
4. يعرض رسالة "بانتظار التحويل"

---

## 11. ملاحظات مهمة جداً

### حقل `full_name` وليس `fullname`
```kotlin
// الصواب:
@Json(name = "full_name") val fullName: String? = null

// الخاطئ:
@Json(name = "fullname") val fullName: String? = null
```
الباك إند يُرجع `"full_name"` (بشرطة سفلية) في كلا الـ endpoints (login و me).

### شكل استجابة Login
```kotlin
// الباك إند يُرجع:
{
  "token": "sub_abc...",
  "subscriber": { "full_name": "...", "balance": 50.0, ... }
}

// وليس:
{
  "token": "sub_abc...",
  "full_name": "...",
  "balance": 50.0
}
```

### الـ 7 صلاحيات (Feature Flags)
```kotlin
sub_allow_renew        → تجديد الباقة
sub_allow_change       → تغيير الباقة
sub_allow_buy_plan     → شراء باقة / إنشاء كروت
sub_allow_buy_addon    → شراء باقة إضافية
sub_show_recharge_page → عرض صفحة الشحن
sub_show_plans         → عرض الباقات
sub_show_addons        → عرض الباقات الإضافية
```

### التحقق من الصلاحيات في Dashboard
```kotlin
// زر إنشاء كروت:
if (currentSub.allowBuyPlan && currentSub.showPlans) { ... }

// زر شحن رصيد:
if (currentSub.showRechargePage) { ... }

// زر تجديد:
val renewEnabled = currentSub.allowRenew

// زر تغيير:
val changeEnabled = currentSub.allowChange && currentSub.showPlans

// زر باقة إضافية:
if (currentSub.showAddons) { ... }
```

### الاتصال بالميكروتك
- السيرفر يتصل بالميكروتك مباشرة عبر RouterOS API (منفذ 8728)
- لا يوجد خادم Access منفصل
- السيرفر Go هو نفسه خادم RADIUS (UDP 1812/1813)
- التعرف على صاحب الشبكة يتم عبر IP العام أو SSTP VPN

---

## 12. قائمة الملفات المطلوب إنشاؤها

1. `app/src/main/AndroidManifest.xml`
2. `app/src/main/java/com/example/MainActivity.kt`
3. `app/src/main/java/com/example/config/AppConfig.kt`
4. `app/src/main/java/com/example/data/model/NetworkModels.kt`
5. `app/src/main/java/com/example/data/network/ApiService.kt`
6. `app/src/main/java/com/example/data/repository/SubscriberRepository.kt`
7. `app/src/main/java/com/example/ui/screens/SplashDiscoveryScreen.kt`
8. `app/src/main/java/com/example/ui/screens/LoginScreen.kt`
9. `app/src/main/java/com/example/ui/screens/DashboardScreen.kt`
10. `app/src/main/java/com/example/ui/screens/PlansScreen.kt`
11. `app/src/main/java/com/example/ui/screens/RechargeScreen.kt`
12. `app/src/main/java/com/example/ui/screens/VoucherViewScreen.kt`
13. `app/src/main/java/com/example/ui/screens/TransactionsScreen.kt`
14. `app/src/main/java/com/example/ui/viewmodel/SubscriberViewModel.kt`
15. `app/src/main/java/com/example/ui/theme/Color.kt`
16. `app/src/main/java/com/example/ui/theme/Theme.kt`
17. `app/src/main/java/com/example/ui/theme/Type.kt`
18. `build.gradle.kts` (Root)
19. `app/build.gradle.kts`
20. `settings.gradle.kts`
21. `gradle/libs.versions.toml` (version catalog)

---

## ⚠️ تذكير أخير ونهائي ⚠️

**هذا هو التطبيق الأصلي:**
- 📁 هيكل الملفات: `app/src/main/java/com/example/` (مسار أندرويد)
- 🛠️ لغة البناء: **Kotlin** (ليس JavaScript/TypeScript/HTML/CSS)
- 🎨 واجهة المستخدم: **Jetpack Compose** (ليس React/Vue/Angular)
- 📦 المخرج: **ملف APK** (ليس ملف HTML أو ZIP لموقع ويب)
- ⚙️ نظام البناء: **Gradle** (ليس npm/yarn/pip)

**التحقق النهائي:**
1. يجب أن يكون المشروع قادراً على البناء بـ `./gradlew assembleDebug`
2. يجب أن يُنتج ملف APK في `app/build/outputs/apk/debug/`
3. يجب أن يعمل على هاتف أندرويد حقيقي أو محاكي أندرويد
4. لا يُقبل أي شكل من أشكال موقع الويب أو التطبيق الوبي كبديل
