/*
 *
 * $Author: dcosta $
 *
 * Copyright 2010-2018 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.client;

import org.apache.http.client.methods.HttpGet;
import org.apache.commons.codec.binary.Base64;


/**
 * 
 * @author Duane Costa
 * 
 * A wrapper class for manufacturing HttpGet objects that may include cookie headers for auth-token and
 * robot values.
 *
 */
public class HttpGetFactory {
    
    
    /*
     * Class Methods
     */
    
	
	/**
	 * Manufactures an HttpGet object, adding auth-token and robot cookies as appropriate.
	 * 
	 * @param url       The URL of the request
	 * @param token     Auth-token string, possibly null
	 * @param robot     Robot string, possibly null
	 * @return          An Httpget object
	 */
    public static HttpGet makeHttpGet(String url, String token, String robot) {
        HttpGet httpGet = new HttpGet(url);
        if (token != null) {
            httpGet.setHeader("Cookie", "auth-token=" + token);
        }
        if (robot != null) {
            String b64Robot = new String(Base64.encodeBase64(robot.getBytes()));
            httpGet.setHeader("Cookie", "robot=" + b64Robot);
        }
        
        return httpGet;
    }
    
}
