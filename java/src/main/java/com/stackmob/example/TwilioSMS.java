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

import com.stackmob.sdkapi.http.HttpService;
import com.stackmob.sdkapi.http.request.HttpRequest;
import com.stackmob.sdkapi.http.request.GetRequest;
import com.stackmob.sdkapi.http.response.HttpResponse;
import com.stackmob.core.ServiceNotActivatedException;
import com.stackmob.sdkapi.http.exceptions.AccessDeniedException;
import com.stackmob.sdkapi.http.exceptions.TimeoutException;
import java.net.MalformedURLException;
import com.stackmob.sdkapi.http.request.PostRequest;
import com.stackmob.sdkapi.http.Header;
import com.stackmob.sdkapi.LoggerService;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;

public class TwilioSMS implements CustomCodeMethod {

  //Create your Twilio Acct at twilio.com and enter 
  //Your accountsid and accesstoken below.
  static String accountsid = "XXXXXXXXXXXXXXX";
  static String accesstoken = "XXXXXXXXXXXXXXX";
    
  @Override
  public String getMethodName() {
    return "twilio_sms";
  }
    
  @Override
  public List<String> getParams() {
    return new ArrayList<String>();
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
	  
	  LoggerService logger = serviceProvider.getLoggerService(TwilioSMS.class);
	  
      // You can pass to,from and body parameters from your client
	  //String to = request.getParams().get("to"); 
      //String from = request.getParams().get("from"); 
      //String body = request.getParams().get("body"); 
      
      // The TO phonenumber should be YOUR cel phone
      // The FROM phonenumber should be one create in 
      // the twilio dashboard at twilio.com
      String body = "To=4155551212&From=4083331234&Body=hello";

      int responseCode = 0;
      String responseBody = "";
      
      String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountsid + "/SMS/Messages.json";
    
      String pair = accountsid + ":" + accesstoken;
      
      // Base 64 Encode the accountsid/accesstoken
      byte[] b =Base64.encodeBase64(pair.getBytes()); 
      String encodedString = new String(b);
    
      Header accept = new Header("Accept-Charset", "utf-8");
      Header auth = new Header("Authorization","Basic " + encodedString);
      Header content = new Header("Content-Type", "application/x-www-form-urlencoded");

      Set<Header> set = new HashSet();
      set.add(accept);
      set.add(content);
      set.add(auth);
      
      try {  
          HttpService http = serviceProvider.getHttpService();
          try {
              PostRequest req = new PostRequest(url,set,body);
              try {
                  try {
                      HttpResponse resp = http.post(req);
                      responseCode = resp.getCode();
                      responseBody = resp.getBody();
                  
                  } catch(TimeoutException e) {
                	  logger.error(e.getMessage(), e);
                      responseCode = -1;
                      responseBody = e.getMessage();
                  }
              } catch(AccessDeniedException e) {
            	  logger.error(e.getMessage(), e);
                  responseCode = -1;
                  responseBody = e.getMessage();
              }
              
          } catch(MalformedURLException e) {
        	  logger.error(e.getMessage(), e);
              responseCode = -1;
              responseBody = e.getMessage();
          }
           
      } catch(ServiceNotActivatedException e) {
    	  logger.error(e.getMessage(), e);
          responseCode = -1;
          responseBody = e.getMessage();
      }
      
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("response_code", responseCode);
      map.put("response_body", responseBody);
     
    return new ResponseToProcess(HttpURLConnection.HTTP_OK, map);
  }

}
