package com.poc.binarybroker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Binary Broker - receives file information (id, file path, or any other data) to obtain the external file.
 * In this example - locationId, fileId parameters are used to get a file from s3 bucket (non-protected)
 * In real life - the binary broker handles access to protected external resources, for example, by generating s3 signed urls
 */
public class BinaryBroker {

    /**
     * In this example we obtain file based on locationId and fileId parameters.
     * Note: locationId, fileId and userId parameters are used solely for the purpose of this example: any number of parameter can be used in a real implementation
     */
    public static InputStream getBinary(String locationId, String fileId, String userId) throws Exception {
        String url = "https://nuxeo-test-bucket.s3.us-east-2.amazonaws.com/" + fileId;
        return new BufferedInputStream(new URL(url).openStream());
    }

    public static String getBinaryUrl(String locationId, String fileId, String userId) throws Exception {
         return "https://nuxeo-test-bucket.s3.us-east-2.amazonaws.com/" + fileId;
    }
}
