/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.caching;

import net.sf.jsr107cache.Cache;
import org.wso2.carbon.caching.core.registry.GhostResource;
import org.wso2.carbon.caching.core.registry.RegistryCacheKey;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CachingHandler using to handle the cached results of registry operation. We are removing the the
 * data from cache for all the write operations.
 */
public class CachingHandler extends Handler {

    private Map<String, DataBaseConfiguration> dbConfigs =
            new HashMap<String, DataBaseConfiguration>();

    /**
     * Default Constructor
     */
    public CachingHandler() {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        for (Mount mount : registryContext.getMounts()) {
            for(RemoteConfiguration configuration : registryContext.getRemoteInstances()) {
                if (configuration.getDbConfig() != null &&
                        mount.getInstanceId().equals(configuration.getId())) {
                    dbConfigs.put(mount.getTargetPath(),
                            registryContext.getDBConfig(configuration.getDbConfig()));
                }
            }
        }
    }

    /**
     * used to store cached data
     */
    private static Cache cache =
            RegistryUtils.getCommonCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID);

    /**
     * used to clear cache for the registry write operations
     *
     * @param requestContext registryContext
     * @param cachePath      cached resource path
     * @param recursive      whether this operation must be recursively applied on child resources
     */
    private void clearCache(RequestContext requestContext, String cachePath, boolean recursive) {
        String connectionId = "";
        DataBaseConfiguration dataBaseConfiguration = null;
        // first check for mounts.
        if (dbConfigs.size() > 0) {
            for (String targetPath : dbConfigs.keySet()) {
                if (cachePath.startsWith(targetPath)) {
                    dataBaseConfiguration = dbConfigs.get(targetPath);
                    break;
                }
            }
        }
        // if not found, then use the default DB configuration.
        if (dataBaseConfiguration == null) {
            RegistryContext registryContext = requestContext.getRegistryContext();
            if (registryContext == null) {
                registryContext = RegistryContext.getBaseInstance();
            }
            dataBaseConfiguration = registryContext.getDefaultDataBaseConfiguration();
        }
        if (dataBaseConfiguration != null) {
            connectionId = dataBaseConfiguration.getUserName() + "@" +
                           dataBaseConfiguration.getDbUrl();
        }
        int tenantId;
        tenantId = CurrentSession.getTenantId();
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            tenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        }
        removeFromCache(connectionId, tenantId, cachePath);
        String parentPath = RegistryUtils.getParentPath(cachePath);

        for (Object key : cache.keySet()) {
            String path = ((RegistryCacheKey) key).getPath();
            if (recursive) {
                if (path.startsWith(cachePath)) {
                    removeFromCache(connectionId, tenantId, path);
                }
            }
        }
        clearAncestry(connectionId, tenantId, parentPath);
    }

    private void clearAncestry(String connectionId, int tenantId, String parentPath) {
        boolean cleared = removeFromCache(connectionId, tenantId, parentPath);
        String pagedParentPathPrefix = "^" + Pattern.quote((parentPath == null) ? "" : parentPath)
                + "(" + RegistryConstants.PATH_SEPARATOR + ")?(;start=.*)?$";
        Pattern pattern = Pattern.compile(pagedParentPathPrefix);
        for (Object key : cache.keySet()) {
            String path = ((RegistryCacheKey) key).getPath();
            if (pattern.matcher(path).matches()) {
                cleared = cleared || removeFromCache(connectionId, tenantId, path);
            }
        }
        if (!cleared && parentPath != null && !parentPath.equals(RegistryConstants.ROOT_PATH)) {
            clearAncestry(connectionId, tenantId, RegistryUtils.getParentPath(parentPath));
        }
    }

    private boolean removeFromCache(String connectionId, int tenantId, String path) {
        RegistryCacheKey cacheKey =
                RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, path);
        if (cache.containsKey(cacheKey)) {
            cache.remove(cacheKey);
            return true;
        } else {
            return false;
        }
    }


    @SuppressWarnings("unchecked")
    public void put(RequestContext requestContext) throws RegistryException {
        // for collections, we need to ensure that paginated gets that were cached needs to be
        // cleared.
        Resource resource = requestContext.getResource();
        if (resource.getProperty(RegistryConstants.REGISTRY_LINK) != null) {
            String path = resource.getProperty(RegistryConstants.REGISTRY_REAL_PATH);
            if (path != null) {
                path = path.substring(path.indexOf("/resourceContent?path=") +
                        "/resourceContent?path=".length());
                clearCache(requestContext, path,
                        requestContext.getResource() instanceof Collection);
            }
        }
        clearCache(requestContext, requestContext.getResourcePath().getPath(),
                requestContext.getResource() instanceof Collection ||
                        (requestContext.getResource() instanceof GhostResource
                                &&
                                ((GhostResource<Resource>) requestContext.getResource())
                                        .getResource() instanceof Collection));


        super.put(requestContext);
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getResourcePath().getPath(), false);
        super.importResource(requestContext);
    }

    public String move(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getSourcePath(), true);
        clearCache(requestContext, requestContext.getTargetPath(), true);
        return super.move(requestContext);
    }
    public String copy(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getTargetPath(), true);
        return super.copy(requestContext);
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getSourcePath(), true);
        return super.rename(requestContext);
    }

    public void createLink(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getResourcePath().getPath(), true);
        super.createLink(requestContext);
    }

    public void removeLink(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getResourcePath().getPath(), true);
        super.removeLink(requestContext);
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getResourcePath().getPath(), true);
        super.delete(requestContext);
    }

    public void restore(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, requestContext.getResourcePath().getPath(), true);
        super.restore(requestContext);
    }

    public void restoreVersion(RequestContext requestContext) throws RegistryException {
        clearCache(requestContext, new ResourcePath(requestContext.getVersionPath()).getPath(),
                true);
        super.restoreVersion(requestContext);
    }
}