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
import com.stackmob.sdkapi.*;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoTest implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "geotest";
  }

  @Override
  public List<String> getParams() {
    return new ArrayList<String>();
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
try {
    SMNear near = new SMNear( 
         "location", 
         new SMDouble(25.9203), // latitude 
         new SMDouble(-81.7283), // longitude 
         new SMDouble(0.2523) // radius, can be null 
); 
List<SMCondition> query = new ArrayList<SMCondition>(); 
query.add(near); 
List<SMObject> testResults = serviceProvider.getDataService().readObjects("geopoint", query); 
    
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("results", testResults);
    return new ResponseToProcess(HttpURLConnection.HTTP_OK, map);
} catch (Exception e) {
  return new ResponseToProcess(HttpURLConnection.HTTP_OK, null);
}
  }

}
