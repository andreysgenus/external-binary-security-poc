package com.poc.binarybroker;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class BinaryBrokerApi {

    /**
     * URL Expiration milliseconds. Should be configurable
     */
    private int expirationMilliseconds = 600000; //10 mins.

    private HashGenerator hashGenerator = new HashGenerator();

    @GetMapping("/external-url")
    public String getExternalUrlUrl(HttpServletRequest request) {
        try{
            String locationId = decodeParam(request.getParameter("locationId"));
            String fileId = decodeParam(request.getParameter("fileId"));
            String userId = decodeParam(request.getParameter("userId"));

            return BinaryBroker.getBinaryUrl(locationId, fileId, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    /**
     * Example how to generate a signed url from request url
     *
     * @param request
     * @return
     */
    @GetMapping("/generate-url")
    public String generateUrl(HttpServletRequest request) {
        try {
            //get original url
            URL url = new URL(request.getRequestURL().toString() + "?" + request.getQueryString());

            //construct url
            //add url parameters from request
            StringBuilder sb = new StringBuilder("/file?");

            Map<String, String> params = parseUrlParameters(url);
            for(Map.Entry<String, String> param : params.entrySet()) {
                sb.append("&").append(param.getKey()).append("=").append(encodeParam(param.getValue()));
            }
            //add expiration date parameter
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String expires = df.format(new Date(new Date().getTime() + expirationMilliseconds));

            sb.append("&expires=").append(encodeParam(expires));

            //add token param
            String token = hashGenerator.generateHash(sb.toString());
            sb.append("&token=").append(token);

            return sb.toString();

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
            String mimeType = decodeParam(request.getParameter("mime-type"));

            //response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", mimeType);
            //CORS headers are required:
            String origin = request.getHeader("Origin");
            //TODO may need to check if origin is allowed
            if(origin != null) {
                headers.add("Access-Control-Allow-Credentials", "true");
                headers.add("Access-Control-Allow-Methods", "GET,OPTIONS");
                headers.add("Access-Control-Allow-Origin", origin);
                headers.add("Access-Control-Max-Age", "3000");
            }

            //obtain binary from the external source
            String locationId = decodeParam(request.getParameter("locationId"));
            String fileId = decodeParam(request.getParameter("fileId"));
            String userId = decodeParam(request.getParameter("userId"));

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
        //expires = expires.replace(" ", "+");
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
        String url = request.getRequestURI() + "?" + request.getQueryString();
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
     * All request parameters except for token are URL encoded
     * They need to be decoded, for example, the correct value for "My%20Test%20Pdf" is "My Test Pdf"
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    private String encodeParam(String value) throws UnsupportedEncodingException {
        if(value != null) {
            String encoded = value.replace( "+", "%2B");
            return URLEncoder.encode(encoded, StandardCharsets.UTF_8.toString());
        }
        return null;
    }

    /**
     * All request parameters are URL encoded
     * They need to be decoded, for example, the correct value for "My%20Test%20Pdf" is "My Test Pdf"
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    private String decodeParam(String value) throws UnsupportedEncodingException {
        if(value != null) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        }
        return null;
    }

    /**
     * Parse a URI Parameters into Name-Value Map
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, String> parseUrlParameters(URL url) throws UnsupportedEncodingException {
        Map<String, String> params = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int pos = pair.indexOf("=");
            params.put(URLDecoder.decode(pair.substring(0, pos), "UTF-8"), URLDecoder.decode(pair.substring(pos + 1), "UTF-8"));
        }
        return params;
    }
}
