# NFC Demo Application

This app is a working version of [https://github.com/underwindfall/NFCAndroid](https://github.com/underwindfall/NFCAndroid), as I was unable to run it. I encountered the issue described here: [https://github.com/underwindfall/NFCAndroid/issues/11](https://github.com/underwindfall/NFCAndroid/issues/11).

After some research and a bit of headache, I managed to get this app working. I still need to read all the NFC documentation to determine if my code follows ISO14443 or if I'm doing something wrong, but the PoC I was able to test works.

# How does this work

Basically, [here](https://developer.android.com/develop/connectivity/nfc/hce) it states that you need to define an AID to enable an HCE-enabled app to emulate a tag that a reader app has to read. The problem is that normal readers (such as the Flipper Zero or NFC Tools) will not work with HCE because you first need to send this custom AID. I tested it with `scriptor` by doing the following:

1. Placing the ACR122U (I believe other readers should be fine too, but I only have this one) on the Android phone.
2. Running `scriptor`.
3. Entering this string `00A4040007F0394148148100` (where `F0394148148100` is the custom AID for my app) [https://github.com/albertoZurini/NFC-Demo/blob/158855bf0cd72ad3adbd54e707e7c4b9215fc99c/hce_app/src/main/res/xml/apduservice.xml#L10](https://github.com/albertoZurini/NFC-Demo/blob/158855bf0cd72ad3adbd54e707e7c4b9215fc99c/hce_app/src/main/res/xml/apduservice.xml#L10).
4. Entering `00B00000FF` to initiate the binary reading.

My reader app is essentially doing the same thing here: [https://github.com/albertoZurini/NFC-Demo/blob/158855bf0cd72ad3adbd54e707e7c4b9215fc99c/reader_app/src/main/java/com/codetogether/nfcdemo/MainActivity.kt#L80-L230](https://github.com/albertoZurini/NFC-Demo/blob/158855bf0cd72ad3adbd54e707e7c4b9215fc99c/reader_app/src/main/java/com/codetogether/nfcdemo/MainActivity.kt#L80-L230).

The only issue I encountered is that sending `0xFF` for the length doesn't work, so I worked around it by sending `0xFE` [https://github.com/albertoZurini/NFC-Demo/blob/158855bf0cd72ad3adbd54e707e7c4b9215fc99c/reader_app/src/main/java/com/codetogether/nfcdemo/MainActivity.kt#L98](https://github.com/albertoZurini/NFC-Demo/blob/158855bf0cd72ad3adbd54e707e7c4b9215fc99c/reader_app/src/main/java/com/codetogether/nfcdemo/MainActivity.kt#L98).

# Demo

On the left is the HCE device, and on the right is the reader.
![Demo](./assets/demo.gif "Demo")
