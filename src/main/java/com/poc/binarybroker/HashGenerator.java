package com.poc.binarybroker;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

public class HashGenerator {

    public static String generateHash(String data, String key) throws Exception {

        byte[] salt = key.getBytes();

        //create a PBEKeySpec and a SecretKeyFactory which we'll instantiate using the PBKDF2WithHmacSHA1 algorithm
        int iterationCount = 65536; //the strength parameter indicates how many iterations that this algorithm run for, increasing the time it takes to produce the hash.
        int keyLength = 1024;
        KeySpec spec = new PBEKeySpec(data.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getUrlEncoder().encodeToString(hash);
    }
}
