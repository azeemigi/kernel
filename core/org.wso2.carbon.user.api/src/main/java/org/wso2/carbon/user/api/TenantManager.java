/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.user.api;

public interface TenantManager {

    /**
     * Adds a tenant to the system
     * 
     * @param tenant The tenant to be added
     * @return The Id of the tenant
     * @throws UserStoreException
     */
    public int addTenant(Tenant tenant) throws UserStoreException;
    
    /**
     * Updates a tenant in the system
     *  
     * @param tenant The tenant to be updated
     * @throws UserStoreException
     */
    public void updateTenant(Tenant tenant) throws UserStoreException;

    /**
     * Gets a Tenant object
     * 
     * @param tenantId The tenant Id of the tenant
     * @return The tenant object
     * @throws UserStoreException
     */
    public Tenant getTenant(int tenantId) throws UserStoreException;

    /**
     * Gets all tenants in the system.
     * 
     * @return An array of all tenants
     * 
     * @throws UserStoreException
     */
    public Tenant[] getAllTenants() throws UserStoreException;

    /**
     * Retrieves the domain given a tenant Id
     * 
     * @param tenantId The Id of the tenant
     * @return
     * @throws UserStoreException
     */
    public String getDomain(int tenantId) throws UserStoreException;

    /**
     * Retrieves the tenant Id given the domain
     * 
     * @param domain The domain of the tenant
     * @return
     * @throws UserStoreException
     */
    public int getTenantId(String domain) throws UserStoreException;

    /**
     * Activates a tenant
     * 
     * @param tenantId The Id of the tenant
     * @throws UserStoreException
     */
    public void activateTenant(int tenantId) throws UserStoreException;

    /**
     * De-activates a tenant
     * 
     * @param tenantId The Id of the tenant
     * @throws UserStoreException
     */
    public void deactivateTenant(int tenantId) throws UserStoreException;

    /**
     * Checks whether a tenant is active
     * 
     * @param tenantId The Id of the tenant
     * @return
     * @throws UserStoreException
     */
    public boolean isTenantActive(int tenantId) throws UserStoreException;

    /**
     * Deletes a tenant from the system
     * 
     * @param tenantId
     * @throws UserStoreException
     */
    public void deleteTenant(int tenantId) throws UserStoreException;

    /**
     * Checks whether the super tenant.
     * 
     * @return
     * @throws UserStoreException
     */
    public String getSuperTenantDomain() throws UserStoreException;
}
