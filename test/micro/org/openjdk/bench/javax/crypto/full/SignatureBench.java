/*
 * Copyright (c) 2015, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.bench.javax.crypto.full;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Random;

public class SignatureBench extends CryptoBase {

    public static final int SET_SIZE = 128;

    @Param({"SHA256withDSA"})
    private String algorithm;

    @Param({"1024", "16384"})
    int dataSize;

    @Param({"1024"})
    private int keyLength;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[][] data;
    private byte[][] signedData;
    int index;


    private String getKeyPairGeneratorName() {
        int withIndex = algorithm.lastIndexOf("with");
        if (withIndex < 0) {
            return algorithm;
        }
        String tail = algorithm.substring(withIndex + 4);
        return "ECDSA".equals(tail) ? "EC" : tail;
    }

    @Setup()
    public void setup() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        setupProvider();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(getKeyPairGeneratorName());
        if (keyLength > 0) { // not all key pair generators allow the use of key length
            kpg.initialize(keyLength);
        }
        KeyPair keys = kpg.generateKeyPair();
        this.privateKey = keys.getPrivate();
        this.publicKey = keys.getPublic();
        data = fillRandom(new byte[SET_SIZE][dataSize]);
        signedData = new byte[data.length][];
        for (int i = 0; i < data.length; i++) {
            signedData[i] = sign(data[i]);
        }

    }

    public byte[] sign(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = (prov == null) ? Signature.getInstance(algorithm) : Signature.getInstance(algorithm, prov);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    @Benchmark
    public byte[] sign() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] d = data[index];
        index = (index + 1) % SET_SIZE;
        return sign(d);
    }

    @Benchmark
    public boolean verify() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = (prov == null) ? Signature.getInstance(algorithm) : Signature.getInstance(algorithm, prov);
        signature.initVerify(publicKey);
        byte[] d = data[index];
        byte[] s = signedData[index];
        index = (index + 1) % SET_SIZE;
        signature.update(d);
        return signature.verify(s);
    }

    public static class RSA extends SignatureBench {

        @Param({"MD5withRSA", "SHA256withRSA"})
        private String algorithm;

        @Param({"1024", "2048", "3072"})
        private int keyLength;

    }

    public static class ECDSA extends SignatureBench {

        @Param({"SHA256withECDSA"})
        private String algorithm;

        @Param({"256"})
        private int keyLength;

    }

    public static class EdDSA extends SignatureBench {

        @Param({"EdDSA"})
        private String algorithm;

        @Param({"255", "448"})
        private int keyLength;

    }

    public static class MLDSA extends SignatureBench {

        @Param({"ML-DSA-44", "ML-DSA-65", "ML-DSA-87" })
        private String algorithm;

        @Param({"0"}) // ML-DSA key length is not supported
        private int keyLength;
    }

}
