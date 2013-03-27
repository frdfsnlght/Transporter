/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
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

import com.frdfsnlght.transporter.api.TypeMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class JSON {

    public static String encode(Object o) {
        return encodeValue(o);
    }

    public static Object decode(CharSequence cs) {
        StringBuilder sb = new StringBuilder(cs);
        int[] pos = new int[1];
        pos[0] = 0;
        return decodeValue(sb, pos);
    }

    private static String encodeValue(Object o) {
        if (o == null) return "null";
        if (o instanceof String) return encodeString((String)o);
        if (o instanceof Number) return o.toString();
        if (o instanceof Boolean) return ((Boolean)o) ? "true" : "false";
        if (o instanceof Map) return encodeObject((Map)o);
        if (o instanceof Collection) return encodeArray((Collection)o);
        if (o.getClass().isArray()) return encodeArray((Object[])o);

        throw new IllegalArgumentException("unsupported JSON encodable object " + o);
    }

    private static String encodeString(String s) {
        //s = s.replaceAll("\\", "\\\\");
        s = s.replace("\"", "\\\"");
        s = s.replaceAll("/", "\\/");
        s = s.replaceAll("[\b]", "\\b");
        s = s.replaceAll("\f", "\\f");
        s = s.replaceAll("\n", "\\n");
        s = s.replaceAll("\r", "\\r");
        s = s.replaceAll("\t", "\\t");
        return "\"" + s + "\"";
    }

    private static String encodeObject(Map map) {
        StringBuilder sb = new StringBuilder();
        for (Object key : map.keySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(encodeString(key.toString()));
            sb.append(":");
            sb.append(encodeValue(map.get(key)));
        }
        sb.insert(0, "{");
        sb.append("}");
        return sb.toString();
    }

    private static String encodeArray(Collection col) {
        StringBuilder sb = new StringBuilder();
        for (Object val : col) {
            if (sb.length() > 0) sb.append(",");
            sb.append(encodeValue(val));
        }
        sb.insert(0, "[");
        sb.append("]");
        return sb.toString();
    }

    private static String encodeArray(Object[] arr) {
        StringBuilder sb = new StringBuilder();
        for (Object val : arr) {
            if (sb.length() > 0) sb.append(",");
            sb.append(encodeValue(val));
        }
        sb.insert(0, "[");
        sb.append("]");
        return sb.toString();
    }

    private static Object decodeValue(StringBuilder sb, int[] pos) {
        stripWhitespace(sb, pos);
        requireChars(sb, pos);
        char ch = sb.charAt(0);
        switch (ch) {
            case '{': return decodeObject(sb, pos);
            case '[': return decodeArray(sb, pos);
            case '"': return decodeString(sb, pos, ch);
            case '\'': return decodeString(sb, pos, ch);
            default:
                if (sb.substring(0, 1).matches("[\\-0-9\\.]")) {
                    return decodeNumber(sb, pos);
                }
                if (sb.substring(0, 4).equalsIgnoreCase("true")) {
                    stripChars(sb, pos, 4);
                    return true;
                }
                if (sb.substring(0, 5).equalsIgnoreCase("false")) {
                    stripChars(sb, pos, 5);
                    return false;
                }
                if (sb.substring(0, 4).equalsIgnoreCase("null")) {
                    stripChars(sb, pos, 4);
                    return null;
                }
                throw new IllegalArgumentException("unexpected JSON syntax at " + pos[0] + " (" + sb.substring(0, 200) + ")");
        }
    }

    private static TypeMap decodeObject(StringBuilder sb, int[] pos) {
        TypeMap map = new TypeMap();
        stripChars(sb, pos, 1);
        String key;
        for (;;) {
            stripWhitespace(sb, pos);
            requireChars(sb, pos);
            char ch = sb.charAt(0);
            switch (ch) {
                case '}':
                    stripChars(sb, pos, 1);
                    return map;
                case '"':
                // allow non-compliant string keys with single quotes
                case '\'':
                    key = decodeString(sb, pos, ch);
                    break;
                case ',':
                    stripChars(sb, pos, 1);
                    // this will allow leading, trailing, and extra commas
                    continue;
                default:
                    // allow non-compliant string keys with no quotes
                    key = decodeString(sb, pos, ':');
                    break;
            }
            stripWhitespace(sb, pos);
            requireChars(sb, pos);
            ch = sb.charAt(0);
            if (ch == ':')
                stripChars(sb, pos, 1);
            else
                throw new IllegalArgumentException("expected object key/value separator in JSON at " + pos[0]);
            map.set(key, decodeValue(sb, pos));
        }
    }

    @SuppressWarnings("unchecked")
    private static List decodeArray(StringBuilder sb, int[] pos) {
        List list = new ArrayList();
        stripChars(sb, pos, 1);
        for (;;) {
            stripWhitespace(sb, pos);
            requireChars(sb, pos);
            char ch = sb.charAt(0);
            switch (ch) {
                case ']':
                    stripChars(sb, pos, 1);
                    return list;
                case ',':
                    stripChars(sb, pos, 1);
                    // this will allow leading, trailing, and extra commas
                    continue;
            }
            list.add(decodeValue(sb, pos));
        }
    }

    private static String decodeString(StringBuilder sb, int[] pos, char quote) {
        StringBuilder str = new StringBuilder();
        if (quote != ':') stripChars(sb, pos, 1);
        for (;;) {
            requireChars(sb, pos);
            char ch = sb.charAt(0);
            if (ch == quote) {
                if (quote != ':') stripChars(sb, pos, 1);
                return str.toString();
            }
            if (ch == '\\') {
                stripChars(sb, pos, 1);
                requireChars(sb, pos);
                ch = stripChars(sb, pos, 1).charAt(0);
                switch (ch) {
                    case 'b': str.append("\b"); break;
                    case 'f': str.append("\f"); break;
                    case 'n': str.append("\n"); break;
                    case 'r': str.append("\r"); break;
                    case 't': str.append("\t"); break;
                    case 'u':
                        stripChars(sb, pos, 1);
                        String hex = stripChars(sb, pos, 4).toString();
                        if (! hex.matches("^[a-f0-9]{4}$"))
                            throw new IllegalArgumentException("expected hexidecimal digits in JSON at " + pos[0]);
                        str.append(Character.toChars(Integer.parseInt(hex, 16)));
                        break;
                    default:
                        // any '\' preceded character will be replaced with the character
                        str.append(ch);
                        break;
                }
            } else
                str.append(stripChars(sb, pos, 1));
        }
    }

    private static Pattern numberPattern = Pattern.compile("^(\\-?[0-9]*\\.?[0-9]*[eE]?[\\+\\-]?[0-9]*)");

    private static Number decodeNumber(StringBuilder sb, int[] pos) {
        Matcher matcher = numberPattern.matcher(sb);
        if (matcher.find()) {
            String num = matcher.group(1);
            sb.delete(0, num.length());
            if (num.contains("."))
                return Double.parseDouble(num);
            else
                return Long.parseLong(num);
        }
        throw new IllegalArgumentException("expected number in JSON at " + pos[0]);
    }

    private static void stripWhitespace(StringBuilder sb, int[] pos) {
        while ((sb.length() > 0) && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
            pos[0]++;
        }
    }

    private static void requireChars(StringBuilder sb, int[] pos) {
        if (sb.length() == 0)
            throw new IllegalArgumentException("unexpected end of JSON at " + pos[0]);
    }

    private static String stripChars(StringBuilder sb, int[] pos, int count) {
        if (count > sb.length())
            throw new IllegalArgumentException("expected " + count + " characters at end of JSON at " + pos[0]);
        String ret = sb.substring(0, count);
        pos[0] += count;
        sb.delete(0, count);
        return ret;
    }

    private JSON() {}

}
