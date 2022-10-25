package com.poc.binarybroker;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class BinaryBrokerApi {

    /**
     * Key to generate/ validate hash. Should be configurable
     */
    private String hashGeneratorKey = "dgfasgdf3456sd76dgcfdsfde76dghcbg";

    /**
     * URL Expiration milliseconds. Should be configurable
     */
    private int expirationMilliseconds = 600000; //10 mins.

    /**
     * Generate signed url from request url
     * @param request
     * @return
     */
    @GetMapping("/generate-url")
    public String generateUrl(HttpServletRequest request) {
        try {
            //construct url
            String query = request.getQueryString(); //original url query string
            String url = "/file?" + query;

            //add expiration date parameter
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String expires = df.format(new Date(new Date().getTime() + expirationMilliseconds));
            url = url + "&expires=" + expires;

            //add token param
            String token = HashGenerator.generateHash(url, hashGeneratorKey);
            url = url + "&token=" + token;
            return url;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {

        try {

            //verify expiration
            verifyExpiration(request);

            //verify token
            verifyToken(request);

            //response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/pdf");
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Methods", "application/pdf");
            headers.add("Access-Control-Allow-Origin", "http://local.nuxeo.io:8080");
            headers.add("Access-Control-Max-Age", "3000");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            //obtain binary from the external source
            String locationId = request.getParameter("locationId");
            String fileId = request.getParameter("fileId");
            String userId = request.getParameter("userId");
            String mimeType = request.getParameter("mimeType");

            return ResponseEntity.ok()
                    .headers(headers)
                    //.contentLength(file.length())
                    .contentType(new MediaType(mimeType))
                    .body(new InputStreamResource(
                            BinaryBroker.getBinary(locationId, fileId, userId)
                    ));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    /**
     * Verify if URL is expired
     * @return
     */
    private void verifyExpiration(HttpServletRequest request) throws Exception {
        String expires = request.getParameter("expires");
        expires = expires.replace(" ", "+");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date expirationDate = df.parse(expires);
        if(expirationDate.before(new Date())){
            throw new Exception("URL is expired");
        }
    }

    private void verifyToken(HttpServletRequest request) throws Exception {

        //original url
        String url = request.getRequestURI() + "?" + request.getQueryString();
        //remove token param from the url
        url = url.substring(0, url.indexOf("&token="));

        //generate hash from url
        String hash = HashGenerator.generateHash(url, hashGeneratorKey);

        //token param
        String token = request.getParameter("token");

        //compare generate hash with the token
        if(!hash.equals(token)) {
            throw new Exception("Token is invalid for url " + url
                    + " <br/>Hash _code: " + hash
                    + " <br/>Hash value: " + token);
        }
    }
}
