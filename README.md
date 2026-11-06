# TraderDesk Native

إعادة كتابة Android Native لتطبيق TraderDesk باستخدام Kotlin وJetpack Compose، بدون Flutter Runtime وبدون WebView.

## المتطلبات

- JDK 17 أو 21
- Android SDK Platform 35
- Android Build Tools 35.0.0
- Android Studio حديث أو Gradle Wrapper

## البناء

```bash
./gradlew clean assembleDebug
```

الناتج يكون داخل:

```text
app/build/outputs/apk/debug/app-debug.apk
```

يمكن تشغيل الاختبارات والتحقق من الجودة:

```bash
./gradlew testDebugUnitTest lintDebug
```

## ما يتضمنه المشروع

- Kotlin + Jetpack Compose + Material 3.
- دعم RTL بالعربية وخط IBM Plex Sans Arabic المرفق.
- الصفحة الرئيسية ومنحنى رأس المال وبطاقات المؤشرات.
- الحسابات التجريبية والحقيقية واختيار الحساب.
- سجل الصفقات مع الإضافة والتعديل والحذف والبحث.
- نموذج الصفقة: السوق، السبب، الاتجاه، النتيجة، الخسارة المتوقعة، المخاطرة، الفريمات، التأكيدات، المزاج، التسلسل، الالتزام بالخطة، سجل التنفيذ، أسباب عدم الدخول، الصور والملاحظات.
- تقويم الأيام مع صافي نتائج كل يوم وشهر.
- شاشة الأداء بفترات الأسبوع والشهر وآخر 30 يومًا والكل.
- مختبر السلوك المسجل حسب الالتزام والمزاج وسجل التنفيذ.
- حاسبة حجم الصفقة والعائد إلى المخاطرة.
- تخزين محلي JSON وتوافق أسماء الحقول مع نموذج Flutter.
- نسخ واستعادة JSON، ونسخ سجل الصفقات بصيغة CSV.
- جميع GIF وWebP الخاصة بالإيموجيات الأصلية داخل موارد Android.

## ملاحظات البناء

لا تُضمّن `local.properties` عند نقل المصدر إلى جهاز آخر؛ أنشئه تلقائيًا من Android Studio أو اكتب مسار SDK المحلي لديك:

```properties
sdk.dir=/path/to/Android/Sdk
```
