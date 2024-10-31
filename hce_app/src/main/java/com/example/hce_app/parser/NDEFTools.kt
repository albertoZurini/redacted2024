package com.example.hce_app.parser

class NDEFTools {
    companion object {
        fun ExtractTextFromNDEF(rawData: ByteArray): String {
            /* Example; TODO: figoure out how it works

        0 = 0x0
        1 = 0xE     record header
        2 = 0xD9    payload length
        3 = 0x1
        4 = 0x7
        5 = 0x2
        6 = 0x54
        7 = 0xE1
        8 = 0x4
        9 = 0x2
        10 = 0x65
        11 = 0x6E
        12 = 0x63
        13 = 0x69
        14 = 0x61
        15 = 0x6F
        16 = 0x90
        17 = 0x0

         */
            // Check that the data is long enough to contain a meaningful NDEF record
            if (rawData.size < 3) {
                throw IllegalArgumentException("The NDEF data is too short.")
            }

            // The NDEF message starts with byte 0xD9 (indicating a message)
            if (rawData[2] != 0xD9.toByte()) {
                throw IllegalArgumentException("Invalid NDEF message start.")
            }

            // The TNF (Type Name Format) is stored in byte 6 and should be '0x54' for text
            if (rawData[6] != 0x54.toByte()) {
                throw IllegalArgumentException("Invalid NDEF record type.")
            }

            // The language code length is stored in byte 9, for example 0x02 for "en"
            val languageCodeLength = rawData[9].toInt()

            // The text starts after the language code (position 10 + language code length)
            val textStartIndex = 10 + languageCodeLength
            val textLength =
                rawData.size - textStartIndex - 2 // Remove the last padding byte (0x90)

            // Extract and return the text portion
            return rawData.copyOfRange(textStartIndex, textStartIndex + textLength)
                .toString(Charsets.UTF_8)
        }
    }

}