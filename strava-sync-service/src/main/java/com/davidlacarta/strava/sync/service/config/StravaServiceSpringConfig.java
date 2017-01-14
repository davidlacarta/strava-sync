package com.davidlacarta.strava.sync.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * StravaSyncServiceSpringConfig
 *
 * TODO: implemnt
 */
@Configuration
@EnableScheduling
public class StravaServiceSpringConfig {

    public static final String CRYPT_ALGORITHM = "AES";

    @Bean
    public SecretKey secretKey() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        return KeyGenerator.getInstance(CRYPT_ALGORITHM).generateKey();

    }

    @Bean
    public Cipher cipher(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {

        return Cipher.getInstance(secretKey.getAlgorithm() + "/CBC/PKCS5Padding");

    }
}
