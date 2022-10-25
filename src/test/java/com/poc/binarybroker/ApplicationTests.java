package com.poc.binarybroker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

    /**
     * Test that given the same input, the same output is always produced
     */
    @Test
    void testEncryprion() throws Exception {

        String plainText = "this is plain text to be encrypted";

        HashGenerator hashGenerator = new HashGenerator();
        String hash = hashGenerator.generateHash(plainText);

        Assertions.assertTrue(hashGenerator.matches(plainText, hash));
    }

}
