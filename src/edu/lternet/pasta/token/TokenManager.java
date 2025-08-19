/*
 * Copyright 2011-2013 the University of New Mexico.
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
 */

package edu.lternet.pasta.token;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.lternet.pasta.common.SqlEscape;
import edu.lternet.pasta.common.edi.EdiToken;
import edu.lternet.pasta.portal.ConfigurationListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.codec.binary.Base64;

import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Mar 9, 2012
 * @modified Sept 19, 2019
 *        <p/>
 *        The TokenManager manages the NIS Data Portal "tokenstore" for
 *        storing user
 *        authentication tokens provided by PASTA.
 */
public class TokenManager {
    
    /*
     * Class variables 
     */

    private static final Logger logger = Logger.getLogger(edu.lternet.pasta
                                                              .token
                                                              .TokenManager
                                                              .class);
    private static String dbDriver;   // database driver
    private static String dbURL;      // database URL
    private static String dbUser;     // database user name
    private static String dbPassword; // database user password

    /*
     * Instance variables
     */

    private String extToken = null;
    private String b64Token = null;
    private String token = null;
    private String signature = null;
    private String uid = null;
    private String authSystem = null;
    private Long ttl;
    private ArrayList<String> groups;
    private String ediToken = null;
    private boolean ediUseAuth = false;

    /*
     * Constructors
     */

    public TokenManager(HashMap<String, String> tokenSet) {

        Configuration options = ConfigurationListener.getOptions();
        this.ediUseAuth = Boolean.parseBoolean(options.getString("edi.auth.use"));

        this.extToken = tokenSet.get("auth-token");
        if (extToken != null) {
            this.b64Token = extToken.split("-")[0];
            this.token = new String(Base64.decodeBase64(this.b64Token));
            this.signature = extToken.split("-")[1];
            String[] tokenParts = this.token.split("\\*");
            this.uid = tokenParts[0];
            this.authSystem = tokenParts[1];
            this.ttl = new Long(tokenParts[2]);
            this.groups = this.getGroups(token);
        }

        this.ediToken = tokenSet.get("edi-token");
        if (ediUseAuth) {
            EdiToken et = new EdiToken(ediToken);
            this.uid = et.getSubject();
        }
    }
    
    /*
     * Methods
     */

    public String getExtToken() {
        return this.extToken;
    }

    public String getB64Token() {
        return this.b64Token;
    }

    public String getToken() {
        return this.token;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getUid() {
        return this.uid;
    }

    public String getAuthSystem() {
        return this.authSystem;
    }

    public Long getTtl() {
        return this.ttl;
    }

    public ArrayList<String> getGroups() {
        return this.groups;
    }

    /**
     * Returns a connection to the database.
     *
     * @return The database Connection object.
     */
    private static Connection getConnection() throws ClassNotFoundException {

        Configuration options = ConfigurationListener.getOptions();

        TokenManager.dbDriver = options.getString("db.Driver");
        TokenManager.dbURL = options.getString("db.URL");
        TokenManager.dbUser = options.getString("db.User");
        TokenManager.dbPassword = options.getString("db.Password");

        Connection conn = null;

        SQLWarning warn;

        // Load the jdbc driver.
        try {
            Class.forName(TokenManager.dbDriver);
        }
        catch (ClassNotFoundException e) {
            logger.error("Can't load driver " + e.getMessage());
            throw (e);
        }

        // Make the database connection
        try {
            conn = DriverManager.getConnection(TokenManager.dbURL, TokenManager.dbUser,
                    TokenManager.dbPassword);

            // If a SQLWarning object is available, print its warning(s).
            // There may be multiple warnings chained.
            warn = conn.getWarnings();

            if (warn != null) {
                while (warn != null) {
                    logger.warn("SQLState: " + warn.getSQLState());
                    logger.warn("Message: " + warn.getMessage());
                    logger.warn("Vendor: " + warn.getErrorCode());
                    warn = warn.getNextWarning();
                }
            }
        }
        catch (SQLException e) {
            logger.error("Database access failed " + e);
        }

        return conn;

    }

    /**
     * Sets the token and user id into the tokenstore.
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void storeToken() throws SQLException, ClassNotFoundException {

        String sql = String.format(
                "SELECT authtoken.tokenstore.token FROM authtoken.tokenstore WHERE authtoken.tokenstore.user_id=%s",
                SqlEscape.str(this.uid)
        );
        logger.info(String.format("sql=%s", sql));

        Connection dbConn = null; // database connection object
        try {
            dbConn = getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    // uid already in token store, perform "update".
                    sql = String.format(
                            "UPDATE authtoken.tokenstore SET token=%s, edi_token=%s, date_created=now()" +
                                    " WHERE authtoken.tokenstore.user_id=%s",
                            SqlEscape.str(this.extToken),
                            SqlEscape.str(this.ediToken),
                            SqlEscape.str(this.uid)
                    );
                    logger.info(String.format("sql=%s", sql));

                    if (stmt.executeUpdate(sql) == 0) {
                        String msg = String.format("setToken: update '%s' failed", sql);
                        throw new SQLException(msg);
                    }

                } else {

                    // uid not in token store, perform "insert".
                    sql = String.format(
                            "INSERT INTO authtoken.tokenstore VALUES (%s,%s, %s, now())",
                            SqlEscape.str(this.uid),
                            SqlEscape.str(this.extToken),
                            SqlEscape.str(this.ediToken)
                    );
                    logger.info(String.format("sql=%s", sql));

                    if (stmt.executeUpdate(sql) == 0) {
                        String msg = String.format("setToken: insert '%s' failed", sql);
                        throw new SQLException(msg);
                    }

                }

            }
            catch (SQLException e) {
                logger.error("setToken: " + e.getMessage());
                logger.error(sql);
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("setToken: " + e);
            throw (e);
        }

    }

    /**
     * Return the token of the user based on the uid.
     *
     * @param uid The user identifier.
     * @return A String object representing the base64 encrypted token.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static HashMap<String, String> getTokenSet(String uid) throws SQLException, ClassNotFoundException {

        String sql = String.format(
                "SELECT authtoken.tokenstore.token, authtoken.tokenstore.edi_token FROM authtoken.tokenstore" +
                        " WHERE authtoken.tokenstore.user_id=%s",
                SqlEscape.str(uid)
        );
        logger.info(String.format("sql=%s", sql));

        Connection dbConn = null; // database connection object

        HashMap<String, String> tokenSet = new HashMap<String, String>(2);
        try {
            dbConn = TokenManager.getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    tokenSet.put("auth-token", rs.getString("token"));
                    tokenSet.put("edi-token", rs.getString("edi_token"));
                } else {
                    String msg = String.format("getToken: uid '%s' not in authtoken.tokenstore", uid);
                    throw new SQLException(msg);
                }
            }
            catch (SQLException e) {
                logger.error("getToken: " + e.getMessage());
                logger.error(sql);
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("getToken: " + e.getMessage());
            throw (e);
        }

        return tokenSet;

    }

    /**
     * Deletes the user token from the tokenstore.
     *
     * @param uid The user identifier.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void deleteToken(String uid) throws SQLException,
                                                   ClassNotFoundException {

        String sql = String.format(
                "DELETE FROM authtoken.tokenstore WHERE authtoken.tokenstore.user_id=%s",
                SqlEscape.str(uid)
        );
        logger.info(String.format("sql=%s", sql));

        Connection dbConn = null; // database connection object

        try {
            dbConn = getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                if (stmt.executeUpdate(sql) == 0) {
                    SQLException e = new SQLException("deleteToken: delete '" + sql + "' failed");
                    throw (e);
                }
            }
            catch (SQLException e) {
                logger.error("deleteToken: " + e);
                logger.error(sql);
                e.printStackTrace();
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("deleteToken: " + e);
            e.printStackTrace();
            throw (e);
        }

    }

    private ArrayList<String> getGroups(String token) {
        ArrayList<String> groups = new ArrayList<String>();
        String[] tokenParts = token.split("\\*");

        for (int i = 3; i < tokenParts.length; i++) {
            groups.add(tokenParts[i]);
        }

        return groups;

    }
}
