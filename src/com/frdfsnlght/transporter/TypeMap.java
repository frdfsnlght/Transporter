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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class TypeMap extends HashMap<String,Object> implements Cloneable {

    public static TypeMap decode(String encoded) {
        return (TypeMap)decodeObject(new StringBuilder(encoded));
    }

    public static TypeMap decodeJSON(String encoded) {
        return (TypeMap)JSON.decode(encoded);
    }

    private static String encodeObject(Object v) {
        if (v == null) return "n:0:";
        if (v instanceof String) return encodeString((String)v);
        if (v instanceof Boolean) return encodeBoolean((Boolean)v);
        if (v instanceof Byte) return encodeLong(((Byte)v).longValue());
        if (v instanceof Short) return encodeLong(((Short)v).longValue());
        if (v instanceof Integer) return encodeLong(((Integer)v).longValue());
        if (v instanceof Long) return encodeLong((Long)v);
        if (v instanceof Float) return encodeDouble(((Float)v).doubleValue());
        if (v instanceof Double) return encodeDouble((Double)v);
        if (v instanceof TypeMap) return encodeMap((TypeMap)v);
        if (v instanceof Collection) return encodeList((Collection)v);
        throw new IllegalArgumentException("unable to encode '" + v.getClass().getName() + "'");
    }

    private static Object decodeObject(StringBuilder b) {
        //if (b.length() == 0) return null;   // to fix StringIndexOutOfBoundsException?
        char type = b.charAt(0);
        b.delete(0, 2);
        int pos = b.indexOf(":");
        int len = Integer.parseInt(b.substring(0, pos));
        b.delete(0, pos + 1);
        switch (type) {
            case 'n':
//System.out.println("decoded null");
                return null;
            case 's': return decodeString(b, len);
            case 'b': return decodeBoolean(b, len);
            case 'l': return decodeLong(b, len);
            case 'd': return decodeDouble(b, len);
            case 'm': return decodeMap(b, len);
            case 'v': return decodeList(b, len);
            default:
                throw new IllegalArgumentException("unable to decode '" + type + "'");
        }
    }

    private static String stringifyObject(Object v) {
        if (v == null) return "null";
        if (v instanceof String) return stringifyString((String)v);
        if (v instanceof Boolean) return stringifyBoolean((Boolean)v);
        if (v instanceof Byte) return stringifyLong(((Byte)v).longValue());
        if (v instanceof Short) return stringifyLong(((Short)v).longValue());
        if (v instanceof Integer) return stringifyLong(((Integer)v).longValue());
        if (v instanceof Long) return stringifyLong((Long)v);
        if (v instanceof Float) return stringifyDouble(((Float)v).doubleValue());
        if (v instanceof Double) return stringifyDouble((Double)v);
        if (v instanceof TypeMap) return stringifyMap((TypeMap)v);
        if (v instanceof Collection) return stringifyList((Collection)v);
        throw new IllegalArgumentException("unable to stringify '" + v.getClass().getName() + "'");
    }

    private static String encodeString(String v) {
        try {
            v = URLEncoder.encode(v, "UTF-8");
        } catch (UnsupportedEncodingException e) {}
        return "s:" + v.length() + ":" + v;
    }

    private static String decodeString(StringBuilder b, int len) {
//System.out.print("decode string (" + len + "): ");
        String str = b.substring(0, len);
        b.delete(0, len);
        try {
            String s = URLDecoder.decode(str, "UTF-8");
//System.out.println(s);
            return s;
        } catch (UnsupportedEncodingException e) {
//System.out.println("unsupported encoding!!!!!");
            return null;
        }
    }

    private static String stringifyString(String v) {
        return "\"" + v + "\"";
    }

    private static String encodeBoolean(Boolean v) {
        String s = v.toString();
        return "b:" + s.length() + ":" + s;
    }

    private static Boolean decodeBoolean(StringBuilder b, int len) {
        String str = b.substring(0, len);
        b.delete(0, len);
        Boolean bool = Boolean.parseBoolean(str);
//System.out.println("decode boolean: " + bool);
        return bool;
    }

    private static String stringifyBoolean(Boolean v) {
        return v.toString();
    }

    private static String encodeLong(Long v) {
        String s = v.toString();
        return "l:" + s.length() + ":" + s;
    }

    private static Long decodeLong(StringBuilder b, int len) {
        String str = b.substring(0, len);
        b.delete(0, len);
        Long l = Long.parseLong(str);
//System.out.println("decode long: " + l);
        return l;
    }

    private static String stringifyLong(Long v) {
        return v.toString();
    }

    private static String encodeDouble(Double v) {
        String s = v.toString();
        return "d:" + s.length() + ":" + s;
    }

    private static Double decodeDouble(StringBuilder b, int len) {
        String str = b.substring(0, len);
        b.delete(0, len);
        Double d = Double.parseDouble(str);
//System.out.println("decode double: " + d);
        return d;
    }

    private static String stringifyDouble(Double v) {
        return v.toString();
    }

    private static String encodeMap(TypeMap v) {
        StringBuilder buf = new StringBuilder("m:");
        buf.append(v.size()).append(":");
        for (String key : v.keySet()) {
            buf.append(encodeString(key));
            buf.append(encodeObject(v.get(key)));
        }
        return buf.toString();
    }

    private static TypeMap decodeMap(StringBuilder b, int len) {
//System.out.println("decode message (" + len + ")");
        TypeMap m = new TypeMap();
        for (int i = 0; i < len; i++) {
//System.out.print(" message item " + i + ": ");
            String key = (String)decodeObject(b);
            Object value = decodeObject(b);
            m.put(key, value);
        }
        return m;
    }

    private static String stringifyMap(TypeMap v) {
        StringBuilder buf = new StringBuilder();
        for (String key : v.keySet())
            buf.append(key).append(": ").append(stringifyObject(v.get(key))).append("\n");
        if (buf.length() > 0)
            buf.deleteCharAt(buf.length() - 1);
        return "{\n" + pad(buf.toString()) + "\n}";
    }

    private static String encodeList(Collection v) {
        StringBuilder buf = new StringBuilder("v:");
        buf.append(v.size()).append(":");
        for (Object o : v)
            buf.append(encodeObject(o));
        return buf.toString();
    }

    private static List<Object> decodeList(StringBuilder b, int len) {
//System.out.println("decode list (" + len + ")");
        List<Object> l = new ArrayList<Object>();
        for (int i = 0; i < len; i++) {
//System.out.print(" list item " + i + ": ");
            l.add(decodeObject(b));
        }
        return l;
    }

    private static String stringifyList(Collection v) {
        StringBuilder buf = new StringBuilder();
        for (Object o : v)
            buf.append(stringifyObject(o)).append("\n");
        if (buf.length() > 0)
            buf.deleteCharAt(buf.length() - 1);
        return "[\n" + pad(buf.toString()) + "\n]";
    }

    private static String pad(String str) {
        StringBuilder buf = new StringBuilder();
        for (String line : str.split("\n"))
            buf.append("  ").append(line).append("\n");
        if (buf.length() > 0)
            buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(Object val) {
        if (val == null) return null;
        if (val instanceof TypeMap) {
            for (String k : ((TypeMap)val).keySet())
                ((TypeMap)val).put(k, convertValue(((TypeMap)val).get(k)));
            return val;
        }
        if (val instanceof Map) {
            TypeMap child = new TypeMap();
            for (Object k : ((Map)val).keySet())
                child.put(k.toString(), convertValue(((Map)val).get(k)));
            return child;
        }
        if (val instanceof List) {
            for (int i = 0; i < ((List)val).size(); i++)
                ((List)val).set(i, convertValue(((List)val).get(i)));
            return val;
        }
        if (val instanceof Collection) {
            Object[] vals = ((Collection)val).toArray();
            ((Collection)val).clear();
            for (Object v : vals)
                ((Collection)val).add(convertValue(v));
            return val;
        }
        return val;
    }

    @SuppressWarnings("unchecked")
    private static Object cloneValue(Object val) {
        if (val == null) return null;
        if (val instanceof TypeMap) {
            TypeMap map = new TypeMap();
            for (String k : ((TypeMap)val).keySet())
                map.put(k, cloneValue(((TypeMap)val).get(k)));
            return val;
        }
        if (val instanceof Map) {
            TypeMap child = new TypeMap();
            for (Object k : ((Map)val).keySet())
                child.put(k.toString(), cloneValue(((Map)val).get(k)));
            return child;
        }
        if (val instanceof List) {
            for (int i = 0; i < ((List)val).size(); i++)
                ((List)val).set(i, cloneValue(((List)val).get(i)));
            return val;
        }
        if (val instanceof Collection) {
            Object[] vals = ((Collection)val).toArray();
            ((Collection)val).clear();
            for (Object v : vals)
                ((Collection)val).add(cloneValue(v));
            return val;
        }
        return val;
    }

    private File file = null;

    public TypeMap() {}

    public TypeMap(Map map) {
        putAll(map);
    }

    public TypeMap(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public TypeMap clone() {
        return new TypeMap(this);
    }

    @Override
    public void putAll(Map map) {
        for (Object key : map.keySet()) {
            Object value = cloneValue(map.get(key));
            put(key.toString(), value);
        }
    }

    public void load() {
        if (file == null)
            throw new IllegalStateException("no file defined");
        clear();
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            Yaml yaml = new Yaml();
            Object o = yaml.load(input);
            if (! (o instanceof Map)) return;
            for (Object k : ((Map)o).keySet())
                set(k.toString(), ((Map)o).get(k));
        } catch (IOException e) {
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException e) {}
        }
    }

    public void save(File file) {
        this.file = file;
        save();
    }

    public void save() {
        if (file == null)
            throw new IllegalStateException("no file defined");
        DumperOptions options = new DumperOptions();
        //options.setAllowUnicode(true);
        options.setIndent(4);
        Yaml yaml = new Yaml(options);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            yaml.dump(this, writer);
        } catch (IOException e) {
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {}
        }
    }

    public String encode() {
        return encodeMap(this);
    }

    public String encodeJSON() {
        return JSON.encode(this);
    }

    public void set(String key, Object val) {
        String[] keyParts = splitKey(key);
        if (keyParts.length == 1) {
            put(key, convertValue(val));
            return;
        }
        TypeMap child = getMap(keyParts[0]);
        if (child == null) {
            child = new TypeMap();
            put(keyParts[0], child);
        }
        child.set(keyParts[1], val);
    }

    public void remove(String key) {
        String[] keyParts = splitKey(key);
        if (keyParts.length == 1) {
            super.remove(key);
            return;
        }
        TypeMap child = getMap(keyParts[0]);
        if (child == null) return;
        child.remove(keyParts[1]);
    }

    public List<String> getKeys() {
        return new ArrayList<String>(keySet());
    }

    public List<String> getKeys(String key) {
        String[] keyParts = splitKey(key);
        TypeMap child = getMap(keyParts[0]);
        if (child == null) return new ArrayList<String>();
        if (keyParts.length == 1)
            return child.getKeys();
        return child.getKeys(keyParts[1]);
    }

    public Object get(String key) {
        return get(key, null);
    }

    public Object get(String key, Object def) {
        String[] keyParts = splitKey(key);
        if (keyParts.length == 1) {
            if (containsKey(key))
                return super.get(key);
            else
                return def;
        }
        TypeMap child = getMap(keyParts[0]);
        if (child == null) return def;
        return child.get(keyParts[1], def);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String def) {
        Object o = get(key, def);
        if (o == null) return null;
        return o.toString();
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean def) {
        Object o = get(key);
        if (o == null) return def;
        try {
            return Boolean.parseBoolean(o.toString());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public byte getByte(String key) {
        return (byte)getLong(key, 0);
    }

    public byte getByte(String key, byte def) {
        return (byte)getLong(key, def);
    }

    public int getInt(String key) {
        return (int)getLong(key, 0);
    }

    public int getInt(String key, int def) {
        return (int)getLong(key, def);
    }

    public short getShort(String key) {
        return (short)getLong(key, 0);
    }

    public short getShort(String key, short def) {
        return (short)getLong(key, def);
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long def) {
        Object o = get(key);
        if (o == null) return def;
        try {
            return Long.parseLong(o.toString());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public Date getDate(String key) {
        return getDate(key, null);
    }

    public Date getDate(String key, Date def) {
        Object o = get(key);
        if (o == null) return def;
        if (o instanceof Date) return (Date)o;
        if (o instanceof Calendar) return new Date(((Calendar)o).getTimeInMillis());
        try {
            return new Date(Long.parseLong(o.toString()));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public float getFloat(String key) {
        return (float)getDouble(key, 0);
    }

    public float getFloat(String key, float def) {
        return (float)getDouble(key, def);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double def) {
        Object o = get(key);
        if (o == null) return def;
        try {
            return Double.parseDouble(o.toString());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public TypeMap getMap(String key) {
        return getMap(key, null);
    }

    public TypeMap getMap(String key, TypeMap def) {
        Object o = get(key);
        if (o == null) return def;
        if (o instanceof TypeMap) return (TypeMap)o;
        return def;
    }

    @SuppressWarnings("unchecked")
    public List<Object> getList(String key) {
        Object o = get(key);
        if (o == null) return new ArrayList<Object>();
        if (! (o instanceof Collection)) return null;
        return new ArrayList<Object>((Collection)o);
    }

    public List<String> getStringList(String key) {
        return getStringList(key, null);
    }

    public List<String> getStringList(String key, List<String> def) {
        Object o = get(key);
        if (o == null) return def;
        List<String> c = new ArrayList<String>();
        if (o instanceof Collection) {
            for (Object obj : (Collection)o) {
                if ((obj instanceof String) || (obj == null))
                    c.add((String)obj);
            }
        }
        return c;
    }

    public List<TypeMap> getMapList(String key) {
        Object o = get(key);
        if (o == null) return null;
        List<TypeMap> c = new ArrayList<TypeMap>();
        if (o instanceof Collection) {
            for (Object obj : (Collection)o) {
                if ((obj instanceof TypeMap) || (obj == null))
                    c.add((TypeMap)obj);
            }
        }
        return c;
    }

    @Override
    public String toString() {
        return stringifyMap(this);
    }

    private String[] splitKey(String key) {
        int pos = key.indexOf(".");
        if (pos == -1) return new String[] { key };
        return new String[] { key.substring(0, pos), key.substring(pos + 1) };
    }

}
