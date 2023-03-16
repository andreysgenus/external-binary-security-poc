package com.poc.binarybroker;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generate secure token.
 * Using a PBKDF2 algorithm to generate hash
 */
public class HashGenerator {

    /**
     * A configurable secret appended to the random salt. Should be configurable
     */
    private String secretKey = "dgfasgdf3456sd76dgcfdsfde76dghcbg";

    /**
     * The strength parameter indicates how many iterations that this algorithm run for, increasing the time it takes to produce the hash.
     */
    int iterationCount = 65536;

    int keyLength = 256; // SHA-256

    int saltKeyLength = 16;

    private String saltValue; //salt string used for testing only. this will produce the same token value for the same input string

    public String generateHash(String data) throws Exception {

        //generate salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[saltKeyLength];
        random.nextBytes(salt);

        if(saltValue != null) {
            //for testing only
            salt = saltValue.getBytes(StandardCharsets.UTF_8);
        }

        //encode
        byte[] hash = encode(data, salt);
        return Base64.getUrlEncoder().encodeToString(hash);
    }

    private byte[] encode(String data, byte[] salt) throws Exception {

        PBEKeySpec spec = new PBEKeySpec(data.toCharArray(),
                concatenate(salt, this.secretKey.getBytes(StandardCharsets.UTF_8)), this.iterationCount, this.keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return concatenate(salt, factory.generateSecret(spec).getEncoded());
    }

    public boolean matches(String originalData, String encodedData) throws Exception {
        byte[] encoded = Base64.getUrlDecoder().decode(encodedData);
        byte[] salt = subArray(encoded, 0, saltKeyLength);
        return MessageDigest.isEqual(encoded, encode(originalData, salt));
    }

    /**
     * Combine the individual byte arrays into one array.
     */
    public static byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] newArray = new byte[length];
        int destPos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, newArray, destPos, array.length);
            destPos += array.length;
        }
        return newArray;
    }

    /**
     * Extract a sub array of bytes out of the byte array.
     *
     * @param array      the byte array to extract from
     * @param beginIndex the beginning index of the sub array, inclusive
     * @param endIndex   the ending index of the sub array, exclusive
     */
    public static byte[] subArray(byte[] array, int beginIndex, int endIndex) {
        int length = endIndex - beginIndex;
        byte[] subarray = new byte[length];
        System.arraycopy(array, beginIndex, subarray, 0, length);
        return subarray;
    }

    public void setSaltValue(String saltValue) {
        this.saltValue = saltValue;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
