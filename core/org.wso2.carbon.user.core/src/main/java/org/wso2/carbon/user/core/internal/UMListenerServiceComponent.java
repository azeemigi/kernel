/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.user.core.internal;

import org.wso2.carbon.caching.core.CacheInvalidator;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserStoreManagerListener;
import org.wso2.carbon.user.core.tenant.LDAPTenantManager;

import java.util.*;

/**
 * @scr.component name="org.wso2.carbon.user.core.listener" immediate="true"
 * @scr.reference name="authorization.manager.listener.service"
 *                interface="org.wso2.carbon.user.core.listener.AuthorizationManagerListener"
 *                cardinality="0..n" policy="dynamic"
 *                bind="setAuthorizationManagerListenerService"
 *                unbind="unsetAuthorizationManagerListenerService"
 * @scr.reference name="user.store.manager.listener.service"
 *                interface="org.wso2.carbon.user.core.listener.UserStoreManagerListener"
 *                cardinality="0..n" policy="dynamic"
 *                bind="setUserStoreManagerListenerService"
 *                unbind="unsetUserStoreManagerListenerService"
 * @scr.reference name="user.operation.event.listener.service"
 *                interface="org.wso2.carbon.user.core.listener.UserOperationEventListener"
 *                cardinality="0..n" policy="dynamic"
 *                bind="setUserOperationEventListenerService"
 *                unbind="unsetUserOperationEventListenerService" *
 * @scr.reference name="ldap.tenant.manager.listener.service"
 *                interface="org.wso2.carbon.user.core.tenant.LDAPTenantManager"
 *                cardinality="0..n" policy="dynamic"
 *                bind="addLDAPTenantManager"
 *                unbind="removeLDAPTenantManager"
 * @scr.reference name="cache.invalidation.service"
 *                interface="org.wso2.carbon.caching.core.CacheInvalidator"
 *                cardinality="0..1" policy="dynamic"
 *                bind="setCacheInvalidator"
 *                unbind="removeCacheInvalidator"
 */
public class UMListenerServiceComponent {

    private static Map<Integer, AuthorizationManagerListener> authorizationManagerListeners;
    private static Map<Integer, UserStoreManagerListener> userStoreManagerListeners;
    private static Map<Integer, UserOperationEventListener> userOperationEventListeners;
    private static Collection<AuthorizationManagerListener> authorizationManagerListenerCollection;
    private static Collection<UserStoreManagerListener> userStoreManagerListenerCollection;
    private static Collection<UserOperationEventListener> userOperationEventListenerCollection;
    private static Map<Integer, LDAPTenantManager> tenantManagers;
    private static CacheInvalidator cacheInvalidator;
    
    protected static synchronized void setAuthorizationManagerListenerService(
            AuthorizationManagerListener authorizationManagerListenerService) {
        authorizationManagerListenerCollection = null;
        if (authorizationManagerListeners == null) {
            authorizationManagerListeners =
                    new TreeMap<Integer, AuthorizationManagerListener>();
        }
        authorizationManagerListeners.put(authorizationManagerListenerService.getExecutionOrderId(),
                authorizationManagerListenerService);
    }

    protected static synchronized void unsetAuthorizationManagerListenerService(
            AuthorizationManagerListener authorizationManagerListenerService) {
        if (authorizationManagerListenerService != null
                && authorizationManagerListeners != null) {
            authorizationManagerListeners.remove(
                    authorizationManagerListenerService.getExecutionOrderId());
            authorizationManagerListenerCollection = null;
        }
    }

    protected static synchronized void setUserStoreManagerListenerService(
            UserStoreManagerListener userStoreManagerListenerService) {
        userStoreManagerListenerCollection = null;
        if (userStoreManagerListeners == null) {
            userStoreManagerListeners =
                    new TreeMap<Integer, UserStoreManagerListener>();
        }
        userStoreManagerListeners.put(userStoreManagerListenerService.getExecutionOrderId(),
                userStoreManagerListenerService);
    }

    protected static synchronized void unsetUserStoreManagerListenerService(
            UserStoreManagerListener userStoreManagerListenerService) {
        if (userStoreManagerListenerService != null &&
                userStoreManagerListeners != null) {
            userStoreManagerListeners.remove(userStoreManagerListenerService.getExecutionOrderId());
            userStoreManagerListenerCollection = null;
        }
    }

    protected static synchronized void setUserOperationEventListenerService(
            UserOperationEventListener userOperationEventListenerService) {
        userOperationEventListenerCollection = null;
        if (userOperationEventListeners == null) {
            userOperationEventListeners = new TreeMap<Integer, UserOperationEventListener>();
        }
        userOperationEventListeners.put(userOperationEventListenerService.getExecutionOrderId(),
                userOperationEventListenerService);
    }

    protected static synchronized void unsetUserOperationEventListenerService(
            UserOperationEventListener userOperationEventListenerService) {
        if (userOperationEventListenerService != null &&
                userOperationEventListeners != null) {
            userOperationEventListeners.remove(userOperationEventListenerService.getExecutionOrderId());
            userOperationEventListenerCollection = null;
        }
    }


    public static synchronized Collection<AuthorizationManagerListener> getAuthorizationManagerListeners() {
        if (authorizationManagerListeners == null) {
            authorizationManagerListeners = new TreeMap<Integer, AuthorizationManagerListener>();
        }
        if (authorizationManagerListenerCollection == null) {
            authorizationManagerListenerCollection =
                    authorizationManagerListeners.values();
        }
        return authorizationManagerListenerCollection;
    }

    public static synchronized Collection<UserStoreManagerListener> getUserStoreManagerListeners() {
        if (userStoreManagerListeners == null) {
            userStoreManagerListeners = new TreeMap<Integer, UserStoreManagerListener>();
        }
        if (userStoreManagerListenerCollection == null) {
            userStoreManagerListenerCollection =
                    userStoreManagerListeners.values();
        }
        return userStoreManagerListenerCollection;
    }
    
    protected void setCacheInvalidator(CacheInvalidator invalidator) {
    	cacheInvalidator = invalidator;
    }
    
    protected void removeCacheInvalidator(CacheInvalidator invalidator) {
    	cacheInvalidator = null;
    }

    public static CacheInvalidator getCacheInvalidator() {
    	return cacheInvalidator;
    }

	public static synchronized Collection<UserOperationEventListener> getUserOperationEventListeners() {
        if (userOperationEventListeners == null) {
            userOperationEventListeners = new TreeMap<Integer, UserOperationEventListener>();
        }
        if (userOperationEventListenerCollection == null) {
            userOperationEventListenerCollection =
                    userOperationEventListeners.values();
        }
        return userOperationEventListenerCollection;
    }    

    /**
     * Main purpose of this method is to make a dependency to LDAP server component.
     * Then LDAP server bundle will get started before user core.
     * In addition this method can be used to register all tenant managers.
     * @param tenantManager An implementation of LDAPTenantManager.
     */
    protected static synchronized void addLDAPTenantManager(LDAPTenantManager tenantManager) {

        if (tenantManagers == null) {
            tenantManagers = new HashMap<Integer, LDAPTenantManager>();
        }

        tenantManagers.put(tenantManager.hashCode(), tenantManager);

    }

    /**
     * This method will remove an already registered tenant manager.
     * @param tenantManager An implementation of LDAPTenantManager.
     */
    protected static synchronized void removeLDAPTenantManager(LDAPTenantManager tenantManager) {

        if (tenantManagers != null && tenantManagers.containsKey(tenantManager.hashCode())) {
            tenantManagers.remove(tenantManager.hashCode());
        }
    }

    /**
     * Returns all registered tenant managers.
     * @return A map of tenant managers with their hash codes.
     */
    public static Map<Integer, LDAPTenantManager> getTenantManagers () {
        return tenantManagers;
    }
    

}
