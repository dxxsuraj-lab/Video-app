# Video Gallery Pro — Android Studio Java

## Features
- फोन के MediaStore में मौजूद वीडियो और थंबनेल दिखाता है
- वीडियो चलाने के लिए इन-ऐप VideoView + playback controls
- Like/Favourite सूची SharedPreferences में सेव
- Android Sharesheet से वीडियो शेयर
- Android 11+ पर सिस्टम delete confirmation; पुराने Android पर उपलब्ध delete permission
- Trim/Edit के लिए इंस्टॉल किए हुए ऐप को Android `ACTION_EDIT` intent से खोलने की कोशिश

## खोलने का तरीका
1. ZIP को extract करें।
2. Android Studio में **Open** चुनें और `VideoGalleryPro` फोल्डर खोलें।
3. Gradle sync पूरा होने दें (Android Studio/Gradle plugin के लिए इंटरनेट लग सकता है)।
4. फोन में Developer options और USB debugging चालू करके फोन कनेक्ट करें, या Android Emulator चुनें।
5. Run दबाएँ। APK बनाने के लिए **Build > Build APK(s)** चुनें।

## जरूरी बातें
- यह Java Android Studio प्रोजेक्ट है, Jvdroid का single-file Java प्रोग्राम नहीं।
- Android 13+ पर `READ_MEDIA_VIDEO`, Android 12 और उससे पुराने पर `READ_EXTERNAL_STORAGE` permission माँगी जाती है।
- Android 14+ पर यूज़र कुछ फोटो/वीडियो तक सीमित access दे सकता है; यदि कुछ वीडियो न दिखें, App settings में video access जाँचें।
- `ACTION_EDIT` हर फोन पर उपलब्ध नहीं होता। Android में कोई सार्वभौमिक built-in video editor intent नहीं है; इसलिए Trim/Edit के लिए compatible editor इंस्टॉल होना चाहिए।
- “Edit” बटन वीडियो को अपने-आप trim नहीं करता और इस नमूने में crop, filters, transitions या export engine शामिल नहीं है।
- Delete करने पर Android सिस्टम की अनुमति/confirmation दिखाई दे सकती है।
