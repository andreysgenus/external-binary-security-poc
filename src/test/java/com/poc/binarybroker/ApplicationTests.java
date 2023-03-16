package com.poc.binarybroker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

    /**
     * Test that given the same input, the same output is always produced
     */
    @Test
    void testEncryption() throws Exception {

        String plainText = "this is plain text to be encrypted";

        HashGenerator hashGenerator = new HashGenerator();

        String hash = hashGenerator.generateHash(plainText);

        Assertions.assertTrue(hashGenerator.matches(plainText, hash));
    }


    /**
     * Use fixed salt value to reproduce the same token value for the same input string
     * @throws Exception
     */
    @Test
    void testEncryptionFixedSalt() throws Exception {

        HashGenerator hashGenerator = new HashGenerator();
        //set fixed salt value to be able to reproduce the same token value
        String saltStr = "1234567890123456";
        //set secret key (DEV or QA/TEST)
        hashGenerator.setSecretKey("dgfasgdf3456sd76dgcfdsfde76dghcbg");

        hashGenerator.setSaltValue(saltStr);

        //string to encode
        String str = "/binary-broker-api/adapter/id/netapp/WEBDOCS/NFS/ARKONAP/2019/179/ARKP-000000000077454_000000000.pdf?expires=2050-01-01T00:00:00.000Z";

        //generate token
        String token = hashGenerator.generateHash(str);

        //check generated token value
        //when default secret key is used
        assertEquals(token, "MTIzNDU2Nzg5MDEyMzQ1NiLo5e-EDcCJkctC4GbNBpWKqMdmK1bj9RN0mobRyOa4");

        //when DEV secret key is used:
        //assertEquals(token, "MTIzNDU2Nzg5MDEyMzQ1NkR3gL0l_qkwbWWpPipMB-EU4CmCyCCIciqbhFAMgWCN");

        //when QA/TEST secret key is used:
        //assertEquals(token, "MTIzNDU2Nzg5MDEyMzQ1NvJ1sFYz9FIAhigWtc36NucfUO4QfQqbN_H_Kdz8vWmH");
    }

}
