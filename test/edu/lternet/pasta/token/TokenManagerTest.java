/**
 *
 * $Date:$
 * $Author:$
 * $Revision:$
 *
 * Copyright 2011,2012 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.token;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.portal.ConfigurationListener;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Mar 13, 2012
 *
 */
public class TokenManagerTest {
	
	/*
	 * Class variables
	 */
	
	private static final Logger logger = Logger.getLogger(edu.lternet.pasta.token.TokenManagerTest.class);
	
	private static String username = null;
	private static String token = null;
	private static final String testToken = "dWlkPWNoYXNlLG89RURJLGRjPWVkaXJlcG9zaXRvcnksZGM9b3JnKmh0dHBzOi8vcGFzdGEuZWRpcmVwb3NpdG9yeS5vcmcvYXV0aGVudGljYXRpb24qMTY1MTgzMTU5NTM4NSphdXRoZW50aWNhdGVkKnZldHRlZA==-Yx4CXRxUtQDibJn9yFY8lmq+cSTnGGLEOlsbZBncEM+YN7dAXc9MjaExi+cMK78bKy9Oy+Do+KUAPg83xo1pRX7R0xIr1o5f6GffpNUk8A+kYI3la5q1nt9IUN1tUOd1lUYVWYq9/3Zg9B9oxdZCPHh0L6wH1A2FWJZu9uQW+CvyiHXJyMSmlcDEn/md4THbGo7CtWDSZ44Sd29H9CfgpRQz+sSe00dOwaWtVYiwgTf5MYh58i8LrWVZZEOaFeDFg/TgozE0fqyN7d1SCAnewwPig1xRZn7Evm+57qbli+3wd0mfQtz9EnSNC5pKRhvOla714dmlUXGRUagEH02SRg==";
    private static final String testClearTextToken = "uid=chase,o=EDI,dc=edirepository,dc=org*https://pasta.edirepository.org/authentication*1651831595385*authenticated*vetted";
    private static final String testDn = "uid=chase,o=EDI,dc=edirepository,dc=org";
    private static final String testAuthSystem = "https://pasta.edirepository.org/authentication";
    private static final Long testTimeToLive = 1651831595385L;
    private static final String testGroup1 = "authenticated";
    private static final String testGroup2 = "vetted";
    private static final String testSignature = "Yx4CXRxUtQDibJn9yFY8lmq+cSTnGGLEOlsbZBncEM+YN7dAXc9MjaExi+cMK78bKy9Oy+Do+KUAPg83xo1pRX7R0xIr1o5f6GffpNUk8A+kYI3la5q1nt9IUN1tUOd1lUYVWYq9/3Zg9B9oxdZCPHh0L6wH1A2FWJZu9uQW+CvyiHXJyMSmlcDEn/md4THbGo7CtWDSZ44Sd29H9CfgpRQz+sSe00dOwaWtVYiwgTf5MYh58i8LrWVZZEOaFeDFg/TgozE0fqyN7d1SCAnewwPig1xRZn7Evm+57qbli+3wd0mfQtz9EnSNC5pKRhvOla714dmlUXGRUagEH02SRg==";
	
	/*
	 * Instance variables
	 */
	
	private TokenManager tokenManager;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		ConfigurationListener.configure();
		Configuration options = ConfigurationListener.getOptions();
 
		if (options == null) {
			fail("Failed to load the DataPortal properties file: 'dataportal.properties'");
		} else {
			username = options.getString("tokenmanager.username");
			if (username == null) {
				fail("No value found for DataPortal property: 'tokenmanager.username'");
			}
			token = options.getString("tokenmanager.token");
			if (token == null) {
				fail("No value found for DataPortal property: 'tokenmanager.token'");
			}
			if (!testToken.equals(token)) {
				fail("DataPortal property 'token' does not match test token");
			}
		}
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		username = null;
		token = null;
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
        HashMap<String, String> tokenSet = new HashMap<String, String>(2);
        tokenSet.put("auth-token", token);
        tokenSet.put("edi-token", "");
        this.tokenManager = new TokenManager(tokenSet);

        try {
            this.tokenManager.storeToken();
        } catch (SQLException e) {
            fail("SQL exception with call to setToken: " + e);
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to setToken: " + e);
        }

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

        try {
            this.tokenManager.deleteToken(username);
        } catch (SQLException e) {
            fail("SQL exception with call to deleteToken: " + e);
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to deleteToken: " + e);
        }

        this.tokenManager = null;
		
	}

	@Test
	public void testSetToken() {
		
		try {
			this.tokenManager.storeToken();
		} catch (SQLException e) {
			fail("SQL exception with call to setToken: " + e);
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException exception with call to setToken: " + e);
		}
	
	}

	@Test
	public void testGetToken() {
        HashMap<String, String> tokenSet = new HashMap<String, String>(2);
        tokenSet.put("auth-token", testToken);
        tokenSet.put("edi-token", "");
		String authToken;
        this.tokenManager = new TokenManager(tokenSet);
		authToken = this.tokenManager.getAuthToken();
		boolean isTokenEqual = testToken.equals(authToken);
		// Test whether the token returned from the database is equal to the test token.
		assertTrue(isTokenEqual);
		
	}

    @Test
    public void testGetCleartextToken() {


        String clearTextToken = null;
        clearTextToken = this.tokenManager.getToken();
        assertTrue(testClearTextToken.equals(clearTextToken));

    }

    @Test
    public void testGetUserDistinguishedName() {

        String dn = null;
        dn = this.tokenManager.getUid();
        assertTrue(testDn.equals(dn));

    }

    @Test
    public void testGetTokenAuthenticationSystem() {

        String authSystem = null;
        authSystem = this.tokenManager.getAuthSystem();
        assertTrue(testAuthSystem.equals(authSystem));

    }

    @Test
    public void testGetTimeToLive() {

        Long ttl = null;

        ttl = this.tokenManager.getTtl();
        assertEquals(testTimeToLive, ttl);

    }

    @Test
    public void testGetUserGroups() {

        ArrayList<String> groups = new ArrayList<String>();
        groups = this.tokenManager.getGroups();

        for(String group: groups) {
            assertTrue(group.equals(testGroup1) || group.equals(testGroup2));
        }
    }

    @Test
    public void testGetTokenSignature() {

        String signature = null;
        signature = this.tokenManager.getSignature();
        assertTrue(testSignature.equals(signature));

    }

    @Test
	public void testDeleteToken() {
		
		try {
			TokenManager.deleteToken(username);
		} catch (SQLException e) {
			fail("SQL exception with call to deleteToken: " + e);
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException exception with call to deleteToken: " + e);
		}
		
		// Now attempt to read the deleted token from the "tokenstore".
		try {
			TokenManager.getTokenSet(username);
		} catch (SQLException e) {
			// This exception should be caught in this test.
			logger.error("SQL exception with call to deleteToken: " + e);
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException exception with call to deleteToken: " + e);
		}

        // Add token back into tokenstore
        try {
            this.tokenManager.storeToken();
        } catch (SQLException e) {
            fail("SQL exception with call to setToken: " + e);
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to setToken: " + e);
        }



    }

}
