package com.poc.binarybroker;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class BinaryBrokerApi {

    /**
     * URL Expiration milliseconds. Should be configurable
     */
    private int expirationMilliseconds = 600000; //10 mins.

    private HashGenerator hashGenerator = new HashGenerator();

    /**
     * Example how to generate a signed url from request url
     *
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
            String token = hashGenerator.generateHash(url);
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

            //mime-type needs to be specified to create a correct response
            String mimeType = request.getParameter("mime-type");

            //response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", mimeType);
            //CORS headers are required:
            String origin = request.getHeader("Origin");
            //TODO may need to check if origin allowed
            if(origin != null) {
                headers.add("Access-Control-Allow-Credentials", "true");
                headers.add("Access-Control-Allow-Methods", "GET,OPTIONS");
                headers.add("Access-Control-Allow-Origin", origin);
                headers.add("Access-Control-Max-Age", "3000");
            }

            //obtain binary from the external source
            String locationId = request.getParameter("locationId");
            String fileId = request.getParameter("fileId");
            String userId = request.getParameter("userId");

            return ResponseEntity.ok()
                    .headers(headers)
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
     *
     * @return
     */
    private void verifyExpiration(HttpServletRequest request) throws Exception {
        String expires = decodeParam(request.getParameter("expires"));
        expires = expires.replace(" ", "+");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date expirationDate = df.parse(expires);
        if (expirationDate.before(new Date())) {
            throw new Exception("URL is expired:"
                    + " <br/>expiration date: " + df.format(expirationDate)
                    + " <br/>current date: " + df.format(new Date()));

        }
    }

    /**
     * Verify security token
     * @param request
     * @throws Exception
     */
    private void verifyToken(HttpServletRequest request) throws Exception {

        //original url
        String url = request.getRequestURI() + "?" + decodeParam(request.getQueryString());
        //remove token param from the url
        url = url.substring(0, url.indexOf("&token="));

        //token param
        String token = request.getParameter("token");

        //check that token is valid
        if (!hashGenerator.matches(url, token)) {
            throw new Exception("Token is invalid for url " + url
                    + " <br/>token: " + token);
        }
    }

    /**
     * url parameters need to be decoded, for example, the correct value for "My%20Test%20Pdf" is "My Test Pdf"
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    private String decodeParam(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }
}
