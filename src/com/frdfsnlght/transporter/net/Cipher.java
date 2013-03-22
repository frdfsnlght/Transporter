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

package com.frdfsnlght.transporter.net;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Cipher {

    /**
     * used to indicate encryption mode
     */
    public static final int Encrypt = 1;
    /**
     * used to indicate decryption mode
     */
    public static final int Decrypt = 2;

    private static final int None = 0;
    private static final long randomP1 = 16807;
    private static final long randomP2 = 0;
    private static final long randomN = Integer.MAX_VALUE;

    // The randomSeed value was arbitrarily choosen, but shouldn't be
    // changed if you ever expect to decrypt something you've already
    // encrypted before the value was changed!
    private static long randomSeed = 4587243876L;
    private static final List<Byte> scramble;

    static {
        List<Byte> seed = new ArrayList<Byte>(256);
        scramble = new ArrayList<Byte>(256);
        for (int i = 0; i < 256; i++)
            seed.add((byte)(i + Byte.MIN_VALUE));
        while (seed.size() > 0)
            //scramble.add(seed.remove(random(seed.size())));
            scramble.add(seed.remove(0));
    }

    // Use a custom random number generator because we can't rely on the
    // JRE generator producing the same result given the same seed under
    // different implementations of the runtime.
    // I don't think we need a cryptographically strong generator since we're
    // just randomizing an array of of lookup values.
    private static int random(int range) {
        randomSeed = ((randomP1 * randomSeed) + randomP2) % randomN;
        return (int)(((double)randomSeed / (double)randomN) * (double)range);
    }

    private ByteArrayOutputStream buffer;
    private int padSize;
    private int mode;
    private byte[] key;
    private int keyIndex;
    private int factor1;
    private int factor2;

    /**
     * Creates a new instance of Cipher.
     */
    public Cipher() {
        this(0);
    }

    /**
     * Creates a new instance of Cipher with the specified pad size.
     */
    public Cipher(int padSize) {
        mode = None;
        setPadSize(padSize);
        reset();
    }

    /**
     * Returns the current pad size.
     * @return the current pad size
     */
    public int getPadSize() {
        return padSize;
    }

    /**
     * Sets the current pad size.
     * <p>
     * The specified pad size must be greater than or equal to 0.
     * </p>
     * <p>
     * If the pad size is zero and you encrypt some data, the resulting cipher
     * data will be exactly the same length as the input data. This can make
     * cracking the encryption easier. Setting the pad size to any value
     * larger than zero will mean the cipher data length will be a multiple
     * of the pad size and least as long as the pad size.
     * This makes cracking the encryption a bit more difficult
     * because of the extra "garbage" in the cipher data and the fact the cracker
     * now doesn't know the length of the original data. Pad sizes greater than 1
     * are recommended and larger values offer more protection (e.g., 1024, 8096,
     * or even larger).
     * </p>
     * @param padSize the new pad size
     */
    public void setPadSize(int padSize) {
        if (padSize < 0)
            throw new IllegalArgumentException("padSize must be >= 0");
        this.padSize = padSize;
    }

    /**
     * Initializes the cipher in the specified mode with the specified key data.
     * <p>
     * The key data is used to de/encrypt the data. The longer and more random
     * the key is, the better the encryption. To decrypt the data later, you
     * must use the same key.
     * </p>
     * <p>
     * Usually, the key data is obtained from the
     * {@link java.security.Key#getEncoded} method.
     * </p>
     * @param mode the mode the cipher will be used in
     * @param key the key data used during de/encryption
     */
    public void init(int mode, byte[] key) {
        if ((mode != Encrypt) && (mode != Decrypt))
            throw new IllegalArgumentException("mode is invalid");
        reset();
        this.mode = mode;
        this.key = key;
    }

    /**
     * Initializes the cipher in the specified mode with the specified key.
     * <p>
     * This method is the equivalent of:
     * <pre>
     *    cipher.init(mode, Key.getEncoded());
     * </pre>
     * </p>
     * @param mode the mode the cipher will be used in
     * @param key the key used during de/encryption
     */
    public void init(int mode, Key key) {
        init(mode, key.getEncoded());
    }

    /**
     * Initializes the cipher for encryption with the specified key data.
     * @param key the key data used during encryption
     */
    public void initEncrypt(byte[] key) {
        init(Encrypt, key);
    }

    /**
     * Initializes the cipher for encryption with the specified key.
     * @param key the key used during encryption
     */
    public void initEncrypt(Key key) {
        init(Encrypt, key);
    }

    /**
     * Initializes the cipher for decryption with the specified key data.
     * @param key the key data used during decryption
     */
    public void initDecrypt(byte[] key) {
        init(Decrypt, key);
    }

    /**
     * Initializes the cipher for decryption with the specified key.
     * @param key the key used during decryption
     */
    public void initDecrypt(Key key) {
        init(Decrypt, key);
    }

    /**
     * Resets the cipher, canceling any de/encryption currently in progress.
     */
    public void reset() {
        buffer = new ByteArrayOutputStream();
        keyIndex = 0;
        factor1 = factor2 = 0;
        mode = None;
    }

    /**
     * Returns the specified plain text data encrypted with the specified key.
     * @param key the key data used during encryption
     * @param plainText the plaintext data to encrypt
     * @return the encrypted, or cipher text, data
     */
    public byte[] encrypt(byte[] key, byte[] plainText) {
        init(Encrypt, key);
        return doFinal(plainText);
    }

    /**
     * Returns the specified cipher text data decrypted with the specified key.
     * @param key the key data used during decryption
     * @param cipherText the encrypted data to decrypt
     * @return the decrypted, or plain text, data
     */
    public byte[] decrypt(byte[] key, byte[] cipherText) {
        init(Decrypt, key);
        return doFinal(cipherText);
    }

    /**
     * Updates the cipher stream with a single byte of data.
     * @param data the byte of data
     */
    public void update(byte data) {
        if (mode == None)
            throw new IllegalStateException("encrypt/decrypt mode not set");

        int posIn = scramble.indexOf(data);
        int adj = scramble.indexOf(key[keyIndex++]);
        if (keyIndex >= key.length) keyIndex = 0;

        factor1 = factor2 + adj;

        int posOut;
        if (mode == Encrypt)
            posOut = posIn + factor1;
        else
            posOut = posIn - factor1;

        posOut = (posOut % scramble.size());
        if (posOut < 0) posOut += scramble.size();

        if (mode == Encrypt)
            factor2 = factor1 + posOut;
        else
            factor2 = factor1 + posIn;

        buffer.write(scramble.get(posOut));
    }

    /**
     * Updates the cipher stream with an array of data.
     * @param data an array of data
     */
    public void update(byte[] data) {
        if (data == null) return;
        for (byte b : data)
            update(b);
    }

    /**
     * Updates the cipher stream with a portion of the data in an array.
     * @param data the array containing the data
     * @param offset the offset within the array where the data to read is located
     * @param length the length of the data in the array to read
     */
    public void update(byte[] data, int offset, int length) {
        if (data == null) return;
        for (int i = offset; i < (i + length); i++)
            update(data[i]);
    }

    /**
     * Completes the de/encryption cycle, resets the cipher instance,
     * and returns the de/encrypted (cipher) data.
     * @return the de/encrypted (cipher) data
     */
    public byte[] doFinal() {
        if (mode == None)
            throw new IllegalStateException("encrypt/decrypt mode not set");
        try {
            if (padSize > 0) {
                if (mode == Encrypt) {
                    int extraBytes = padSize - ((buffer.size() + 4) % padSize);
                    if (extraBytes == padSize) extraBytes = 0;
                    Random r = new Random();
                    for (int i = 0; i < extraBytes; i++)
                        update((byte)(r.nextInt(256) + Byte.MIN_VALUE));
                    update((byte)((extraBytes >> 24) & 0x000000ff));
                    update((byte)((extraBytes >> 16) & 0x000000ff));
                    update((byte)((extraBytes >> 8) & 0x000000ff));
                    update((byte)(extraBytes & 0x000000ff));
                    return buffer.toByteArray();
                } else {
                    byte[] tmp = buffer.toByteArray();
                    if ((tmp.length % padSize) != 0) {
                        // decryption failed
                        return new byte[0];
                    }
                    int extraBytes =
                            ((int)tmp[tmp.length - 1] & 0x000000ff) |
                            (((int)tmp[tmp.length - 2] << 8) & 0x0000ff00) |
                            (((int)tmp[tmp.length - 3] << 16) & 0x00ff0000) |
                            (((int)tmp[tmp.length - 4] << 24) & 0xff000000);
                    if ((extraBytes >= padSize) || (extraBytes < 0)) {
                        // something went wrong
                        return new byte[0];
                    }

                    byte[] data = new byte[tmp.length - 4 - extraBytes];
                    System.arraycopy(tmp, 0, data, 0, data.length);
                    return data;
                }
            } else
                return buffer.toByteArray();
        } finally {
            reset();
        }
    }

    /**
     * Completes the de/encryption cycle, resets the cipher instance,
     * and returns the de/encrypted (cipher) data.
     * <p>
     * This method is the equivalent of:
     * <pre>
     *    cipher.update(data);
     *    cipher.doFinal();
     * </pre>
     * </p>
     * @param data the final array of data update the cipher stream with
     * @return the de/encrypted (cipher) data
     */
    public byte[] doFinal(byte[] data) {
        update(data);
        return doFinal();
    }

}
