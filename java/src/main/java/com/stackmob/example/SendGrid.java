/**
 * Copyright 2012 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.example;

import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.SDKServiceProvider;
import com.stackmob.sdkapi.*;

import com.stackmob.sdkapi.http.HttpService;
import com.stackmob.sdkapi.http.request.HttpRequest;
import com.stackmob.sdkapi.http.request.GetRequest;
import com.stackmob.sdkapi.http.response.HttpResponse;
import com.stackmob.core.ServiceNotActivatedException;
import com.stackmob.sdkapi.http.exceptions.AccessDeniedException;
import com.stackmob.sdkapi.http.exceptions.TimeoutException;
import com.stackmob.core.InvalidSchemaException;
import com.stackmob.core.DatastoreException;

import java.net.MalformedURLException;
import com.stackmob.sdkapi.http.request.PostRequest;
import com.stackmob.sdkapi.http.Header;
import com.stackmob.sdkapi.LoggerService;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class SendGrid implements CustomCodeMethod {

  //Create your SendGrid Acct at sendgrid.com
  static String API_USER = "YOUR_SENDGRID_USERNAME";
  static String API_KEY = "YOUR_SENDGRID_PASSWORD";
	
  @Override
  public String getMethodName() {
    return "sendgrid_email";
  }
    
    
  @Override
  public List<String> getParams() {
    return Arrays.asList("username","subject","text","from");
  }  
    

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    int responseCode = 0;
    String responseBody = "";
    String body = "";
    String url = "";
    String to = "";
    String toname = "";
    String api_user = API_USER;
    String api_key = API_KEY;
    
    LoggerService logger = serviceProvider.getLoggerService(SendGrid.class);
     
    // username should be a valid username in the StackMob user schema
    String username = request.getParams().get("username");
	  
    if (username == null || username.isEmpty()) {
      HashMap<String, String> errParams = new HashMap<String, String>();
      errParams.put("error", "the username passed was empty or null");
      return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
    }
    	
    // get the StackMob datastore service and assemble the query
    DataService dataService = serviceProvider.getDataService();
    	 
    // build a query
    List<SMCondition> query = new ArrayList<SMCondition>();
    query.add(new SMEquals("username", new SMString(username)));

    SMObject userObject;
    
    List<SMObject> result;
    try {
      // return results from user query
      result = dataService.readObjects("user", query);
      if (result != null && result.size() == 1) {
        userObject = result.get(0);
        to = userObject.getValue().get("email").toString();
        toname = userObject.getValue().get("name").toString();
      } else {
        HashMap<String, String> errMap = new HashMap<String, String>();
        errMap.put("error", "no user found");
        errMap.put("detail", "no matches for the username passed");
        return new ResponseToProcess(HttpURLConnection.HTTP_OK, errMap); // http 500 - internal server error
      }
      
    } catch (InvalidSchemaException e) {
      HashMap<String, String> errMap = new HashMap<String, String>();
      errMap.put("error", "invalid_schema");
      errMap.put("detail", e.toString());
      return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error
    } catch (DatastoreException e) {
      HashMap<String, String> errMap = new HashMap<String, String>();
      errMap.put("error", "datastore_exception");
      errMap.put("detail", e.toString());
      return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error
    } catch(Exception e) {
      HashMap<String, String> errMap = new HashMap<String, String>();
      errMap.put("error", "unknown");
      errMap.put("detail", e.toString());
      return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error
    }
    
    // The following values could be static or dynamic 
    // and could be populated with values from the StackMob datastore
    //  SUBJECT - subject line for the email
    String subject = request.getParams().get("subject");
 
    // TEXT - the text of the email
    String text = request.getParams().get("text");
       
    // FROM - the text of the email
    String from = request.getParams().get("from");
   
    if (subject == null || subject.equals("")) {
      logger.error("Subject is missing");
    }
    
    String queryParams = "api_user=" + api_user + "&api_key=" + api_key + "&to=" + to + "&toname=" + toname + "&subject=" + subject + "&text=" + text + "&from=" + from;
  
    try {
      URI uri = new URI(
        "https", 
        "sendgrid.com", 
        "/api/mail.send.json",
        queryParams,
        null);
      url = uri.toASCIIString();
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(), e);
      responseCode = -1;
      responseBody = e.getMessage();    
    }
 
    Header accept = new Header("Accept-Charset", "utf-8");
    Header content = new Header("Content-Type", "application/x-www-form-urlencoded");
    
    Set<Header> set = new HashSet();
    set.add(accept);
    set.add(content);
  
    try {  
      HttpService http = serviceProvider.getHttpService();
          
      PostRequest req = new PostRequest(url,set,body);
             
      HttpResponse resp = http.post(req);
      responseCode = resp.getCode();
      responseBody = resp.getBody();
                  
    } catch(TimeoutException e) {
      logger.error(e.getMessage(), e);
      responseCode = -1;
      responseBody = e.getMessage();
                 
    } catch(AccessDeniedException e) {
      logger.error(e.getMessage(), e);
      responseCode = -1;
      responseBody = e.getMessage();
              
    } catch(MalformedURLException e) {
      logger.error(e.getMessage(), e);
      responseCode = -1;
      responseBody = e.getMessage();
           
    } catch(ServiceNotActivatedException e) {
      logger.error(e.getMessage(), e);
      responseCode = -1;
      responseBody = e.getMessage();
    }
      
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("response_body", responseBody);
    map.put("encodedURL", url);
     
    return new ResponseToProcess(responseCode, map);
  }
}
