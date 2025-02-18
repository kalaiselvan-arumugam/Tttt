package com.kalaiselvan.util;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.logging.LogManager;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import com.echo.domain.UssdRequest;
import com.echo.util.Constants;
import com.echo.util.StackTrace;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.application.Application;

public class Dinstar {
	
	private static final Logger logger = LogManager.getLogger(Dinstar.class);
	
    public String httpsPost(String endPoint, JsonObject requestBody) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null; 
        String responseBody = null;
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        JsonObject device = Application.properties.get("dinstar").getAsJsonObject();
	    JsonObject credentials = device.get("credentials").getAsJsonObject();
	    logger.info("{} , {}, {}, {}", device.get("ipAddress").getAsString(), device.get("port").getAsInt(), credentials.get("userName").getAsString(), credentials.get("password").getAsString());
	    credentialsProvider.setCredentials(
	             new AuthScope(device.get("ipAddress").getAsString(), device.get("port").getAsInt()),
	             new UsernamePasswordCredentials(credentials.get("userName").getAsString(), credentials.get("password").getAsString())
	         );
        try {
            httpclient = validateSSL(httpclient, credentialsProvider);
            if (httpclient == null) {
               logger.error("HTTP client is null or invalid");
               return null;
            }
            HttpPost httpPost = new HttpPost(endPoint.replace("{{address}}", device.get("ipAddress").getAsString()));
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new ByteArrayEntity(requestBody.toString().getBytes()));
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            responseBody = EntityUtils.toString(entity);
            if (response.getStatusLine().getStatusCode() == 200) {
            	logger.info("Sent : {}", responseBody);
            } else {
            	logger.error("Failed : {}", responseBody);
            }
            return responseBody;
        } catch (Exception ex) {
        	logger.error("Exception while making HTTP POST request: {}", StackTrace.getMessage(ex));
        	return null;
        }
    }
    
    public String httpsGet(String endPoint) {
    	CloseableHttpClient httpclient = null;
    	CloseableHttpResponse response = null; 
    	String responseBody = null;
    	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    	JsonObject device = Application.properties.get("dinstar").getAsJsonObject();
    	JsonObject credentials = device.get("credentials").getAsJsonObject();
    	logger.debug("{} , {}, {}, {}", device.get("ipAddress").getAsString(), device.get("port").getAsInt(), credentials.get("userName").getAsString(), credentials.get("password").getAsString());
    	credentialsProvider.setCredentials(
    			new AuthScope(device.get("ipAddress").getAsString(), device.get("port").getAsInt()),
    			new UsernamePasswordCredentials(credentials.get("userName").getAsString(), credentials.get("password").getAsString()));
    	try {
    		URL url = new URL(endPoint.replace("{{address}}", device.get("ipAddress").getAsString()));
    		logger.debug("URL : {}", url);
    		String endpoint = url.getPath();
    		String query = url.getQuery();
    		String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
    		String uri = url.getProtocol()+ "://" + device.get("ipAddress").getAsString() + ":" + device.get("port").getAsInt() + endpoint + "?" + encodedQuery;
    		httpclient = validateSSL(httpclient, credentialsProvider);
    		if (httpclient == null) {
    			logger.error("HTTP client is null or invalid");
    			return null;
    		}
    		HttpGet httpGet = new HttpGet(uri);
    		httpGet.setConfig(RequestConfig.custom().setSocketTimeout(30000).build());
    		httpGet.addHeader("Content-Type", "application/json");
    		response = httpclient.execute(httpGet);
    		responseBody = EntityUtils.toString(response.getEntity());
    		if (response.getStatusLine().getStatusCode() == 200) {
    			logger.debug("Success : {} ",responseBody);
    			response.close();
    		} else {
    			logger.error("Failed : {}",responseBody);
    		}
    		return responseBody;
    	} catch (Exception ex) {
    		logger.error("Exception while making HTTP GET request: {}", StackTrace.getMessage(ex));
    		return null;
    	}
    }

    public static CloseableHttpClient validateSSL(CloseableHttpClient httpClient, CredentialsProvider credentialsProvider) {
        int timeOut = 30000;
        try {
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeOut * 1000)
                .setConnectionRequestTimeout(timeOut * 1000)
                .setSocketTimeout(timeOut * 1000).build();
            SSLContextBuilder sslBuilder = SSLContexts.custom();
            File file = new File(Constants.API_CERTIFICATE_FILE_PATH);
            sslBuilder = sslBuilder.loadTrustMaterial(file, Application.properties.get("jksCredentials").getAsString().toCharArray());
            SSLContext sslcontext = sslBuilder.build();
            SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
            HttpClientBuilder clientbuilder = HttpClients.custom().setSSLSocketFactory(sslConSocFactory).setDefaultCredentialsProvider(credentialsProvider);
            httpClient = clientbuilder.setDefaultRequestConfig(config).build();
        } catch (KeyManagementException ex) {
            logger.error("KeyManagementException while validating the SSL certificate : {}", StackTrace.getMessage(ex));
        } catch (NoSuchAlgorithmException ex) {
            logger.error("NoSuchAlgorithmException while validating the SSL certificate : {}", StackTrace.getMessage(ex));
        } catch (KeyStoreException ex) {
            logger.error("KeyStoreException while validating the SSL certificate : {}", StackTrace.getMessage(ex));
        } catch (CertificateException ex) {
            logger.error("CertificateException while validating the SSL certificate : {}", StackTrace.getMessage(ex));
        } catch (IOException ex) {
            logger.error("IOException while validating the SSL certificate : {}", StackTrace.getMessage(ex));
        } catch (Exception ex) {
            logger.error("Exception while validating the SSL certificate : {}", StackTrace.getMessage(ex));
        }
        return httpClient;
    }
} 
 