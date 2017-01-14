package com.davidlacarta.strava.sync.service;

import com.davidlacarta.strava.sync.service.config.StravaServiceSpringConfig;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Cryptography
 *
 * TODO: implement
 */
@Component
public class Cryptography {

    private SecretKey secretKey;
    private Cipher cipher;

    @Autowired
    public Cryptography(SecretKey secretKey, Cipher cipher) {
        this.secretKey = secretKey;
        this.cipher = cipher;
    }

//    public static void main(String[] args) throws Exception {
//
//        SecretKey secretKey = KeyGenerator.getInstance(StravaServiceSpringConfig.CRYPT_ALGORITHM).generateKey();
//        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm() + "/CBC/PKCS5Padding");
//        Cryptography cryptography = new Cryptography(secretKey, cipher);
//
//        String plaintext = "This is a good secret.";
//        System.out.println(plaintext);
//
//        String ciphertext = cryptography.encrypt(plaintext);
//        System.out.println(ciphertext);
//
//        String decrypted = cryptography.decrypt(ciphertext);
//        System.out.println(decrypted);
//
//    }

    public static byte[] generateIV() {

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        return iv;

    }

    public String encrypt(String plaintext) throws Exception {

        return encrypt(generateIV(), plaintext);

    }

    public String encrypt(byte[] iv, String plaintext) throws Exception {

        return new StringBuilder()
                .append(Base64.encodeBase64String(iv))
                .append(":")
                .append(Base64.encodeBase64String(encrypt(iv, plaintext.getBytes())))
                .toString();

    }

    public String decrypt(String ciphertext) throws Exception {

        String[] parts = ciphertext.split(":");

        return new String(decrypt(Base64.decodeBase64(parts[0]), Base64.decodeBase64(parts[1])));

    }

    public byte[] encrypt(byte[] iv, byte[] plaintext) throws Exception {

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        return cipher.doFinal(plaintext);

    }

    public byte[] decrypt(byte[] iv, byte[] ciphertext) throws Exception {

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        return cipher.doFinal(ciphertext);

    }
}
