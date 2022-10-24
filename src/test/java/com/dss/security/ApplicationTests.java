package com.dss.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

    @Test
    void testEncryprion() throws Exception {

        String plainText = "this is plain text to be encrypted";
        String key = "adhgsgrgewrheruydys6f6467343dgfhgfjhdf";

        String hash = HashGenerator.generateHash(plainText, key);
        String hash1 = HashGenerator.generateHash(plainText, key);

        Assertions.assertEquals(hash, hash1);
    }

}
