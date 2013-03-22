/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.transporter;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Base64 {

    /**
     * Forward Base64 translation alphabet as specified in Table 1 of RFC 2045.
     */
    private static final char intToBase64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * Reverse Base64 translation alphabet as specified in Table 1 of RFC 2045.
     */
    private static final byte base64ToInt[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    /**
     * Returns the string encoded as a Base64 string.
     * @param str the string to encode
     * @param charSet the character set of the string
     * @return the bytes array encoded as a Base64 string
     */
    public static String encode(String str, Charset charSet) {
        return encode(str.getBytes(charSet));
    }

    /**
     * Returns the string encoded as a Base64 string.
     * @param str the string to encode
     * @param charSet the name of the character set of the string
     * @return the bytes array encoded as a Base64 string
     */
    public static String encode(String str, String charSet) throws UnsupportedEncodingException {
        return encode(str.getBytes(charSet));
    }

    /**
     * Returns the byte array encoded as a Base64 string.
     * @param bytes the array of bytes to encode
     * @return the bytes array encoded as a Base64 string
     */
    public static String encode(byte[] bytes) {
        int numFullGroups = bytes.length / 3;
        int numBytesInPartialGroup = bytes.length - (3 * numFullGroups);
        StringBuilder result = new StringBuilder();

        // Translate all full groups from byte array elements to Base64
        int inCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int byte0 = bytes[inCursor++] & 0xff;
            int byte1 = bytes[inCursor++] & 0xff;
            int byte2 = bytes[inCursor++] & 0xff;
            result.append(intToBase64[byte0 >> 2]);
            result.append(intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
            result.append(intToBase64[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
            result.append(intToBase64[byte2 & 0x3f]);
        }

        // Translate partial group if present
        if (numBytesInPartialGroup != 0) {
            int byte0 = bytes[inCursor++] & 0xff;
            result.append(intToBase64[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToBase64[(byte0 << 4) & 0x3f]);
                result.append("==");
            } else {
                int byte1 = bytes[inCursor++] & 0xff;
                result.append(intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
                result.append(intToBase64[(byte1 << 2) & 0x3f]);
                result.append('=');
            }
        }
        return result.toString();
    }

    /**
     * Returns the decoded bytes in the Base64 encoded string.
     * @param str the string to decode
     * @return the decoded bytes in the Base64 encoded string
     */
    public static byte[] decode(String str) {
        if ((str.length() % 4) != 0)
            throw new IllegalArgumentException("String length must be a multiple of four.");
        int sLen = str.length();
        int numGroups = sLen / 4;
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (str.charAt(sLen - 1) == '=') {
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if (str.charAt(sLen - 2) == '=')
                missingBytesInLastGroup++;
        }
        byte[] result = new byte[(3 * numGroups) - missingBytesInLastGroup];

        // Translate all full groups from base64 to byte array elements
        int inCursor = 0;
        int outCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int ch0 = decodeChar(str.charAt(inCursor++));
            int ch1 = decodeChar(str.charAt(inCursor++));
            int ch2 = decodeChar(str.charAt(inCursor++));
            int ch3 = decodeChar(str.charAt(inCursor++));
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }

        // Translate partial group, if present
        if (missingBytesInLastGroup != 0) {
            int ch0 = decodeChar(str.charAt(inCursor++));
            int ch1 = decodeChar(str.charAt(inCursor++));
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (missingBytesInLastGroup == 1) {
                int ch2 = decodeChar(str.charAt(inCursor++));
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        return result;
    }

    /**
     * Returns the decoded character.
     */
    private static int decodeChar(char c) {
        int result = base64ToInt[c];
        if (result < 0)
            throw new IllegalArgumentException("Illegal character " + c);
        return result;
    }

    /** Creates a new instance of Base64 */
    private Base64() {}

}
