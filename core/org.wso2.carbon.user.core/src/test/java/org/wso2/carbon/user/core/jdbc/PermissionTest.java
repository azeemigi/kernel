/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.user.core.jdbc;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.io.InputStream;

public class PermissionTest extends BaseTestCase {

    private UserRealm realm;

    private static String TEST_URL = "jdbc:h2:target/PermissionTest/CARBON_TEST";

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testStuff() throws Exception {
        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff();
        checkPermission();
    }

    public void initRealmStuff() throws Exception {
        String dbFolder = "target/PermissionTest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        // ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        // ds.setUrl("jdbc:derby:target/databasetest/CARBON_TEST;create=true");

        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(TEST_URL);
        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();

        realm = new DefaultRealm();

        InputStream inStream = this.getClass().getClassLoader().getResource(
                JDBCRealmTest.JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), 0);
    }

    public void checkPermission() throws Exception {
        UserStoreManager usManager = realm.getUserStoreManager();
        usManager.addRole("role1", null, null);
        usManager.addUser("user1", "pass1", new String[] { "role1" }, null, null, false);
        
        AuthorizationManager authManager = realm.getAuthorizationManager();
        authManager.authorizeUser("dish", "/r1/", "read");
        authManager.denyUser("dish", "/r1/r2", "read");
        assertFalse(authManager.isUserAuthorized("dish", "/x1/x2", "read"));

        authManager.authorizeRole("role1", "/x1", "read");
        authManager.denyRole("role1", "/x1/x2", "read");
        assertFalse(authManager.isRoleAuthorized("role1", "/x1/x2", "read"));

        assertTrue(authManager.isRoleAuthorized("role1", "/x1", "read"));
        usManager.updateRoleName("role1", "role2");
        assertTrue(authManager.isRoleAuthorized("role2", "/x1", "read"));
        assertFalse(authManager.isRoleAuthorized("role1", "/x1", "read"));

        assertFalse(authManager.isUserAuthorized("user1", "/x1/x2", "read"));
        assertTrue(authManager.isUserAuthorized("user1", "/x1", "read"));
        
        usManager.addRole("bizdevrole", null, null);
        usManager.addUser("bizuser", "pass2", new String[] { "bizdevrole", "everyone" }, null, null, false);
        
        authManager.authorizeRole("everyone", "/", "read");
        authManager.denyRole("everyone", "/wso2/bizzness", "read");
        authManager.authorizeRole("bizdevrole", "/wso2/bizzness", "read");
        assertTrue(authManager.isUserAuthorized("bizuser", "/wso2/bizzness", "read"));
    }
    
    public void checkRepeatingPermission() throws Exception {
        AuthorizationManager authManager = realm.getAuthorizationManager();
        UserStoreManager usAdmin = realm.getUserStoreManager();
        usAdmin.addRole("everyone", null, null);
        usAdmin.addUser("sameera", "password", new String[] { "everyone", "bizzrole" }, null, null, false);
        usAdmin.addUser("dimuthug", "password", new String[] { "everyone" }, null, null, false);

        authManager.authorizeRole("everyone", "/", "read");
        authManager.authorizeRole("everyone", "/top", "read");
        //authManager.authorizeRole("everyone", "/top/wso2", "read");
        authManager.denyRole("everyone", "/top/wso2/bizzness", "read");
        authManager.authorizeRole("bizzrole", "/top/wso2/bizzness", "read");

        assertEquals(1, authManager.getAllowedRolesForResource("/top/wso2/bizzness", "read").length);
        assertEquals(1, authManager.getDeniedRolesForResource("/top/wso2/bizzness", "read").length);
        assertFalse(authManager.isRoleAuthorized("everyone", "/top/wso2/bizzness", "read"));
        assertFalse(authManager.isUserAuthorized("dimuthu", "/top/wso2/bizzness", "read"));
    }
    
    

}
