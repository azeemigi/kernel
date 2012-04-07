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
package org.wso2.carbon.core.deployment;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.core.CarbonAxisConfigurator;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.multitenancy.TenantAxisConfigurator;

/**
 * ClusterMessage for sending a deployment repository synchronization request
 */
public class SynchronizeRepositoryRequest extends ClusteringMessage {

    private int tenantId;
    private transient static final Log log = LogFactory.getLog(SynchronizeRepositoryRequest.class);


    public SynchronizeRepositoryRequest() {
    }

    public SynchronizeRepositoryRequest(int tenantId) {
        this.tenantId = tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void execute(ConfigurationContext configContext) throws ClusteringFault{
        updateDeploymentRepository(configContext);
        doDeployment(configContext);
    }

    private void doDeployment(ConfigurationContext configContext) {
        AxisConfigurator axisConfigurator = configContext.getAxisConfiguration().getConfigurator();
        if(axisConfigurator instanceof CarbonAxisConfigurator) {
            ((CarbonAxisConfigurator) axisConfigurator).runDeployment();
        } else if (axisConfigurator instanceof TenantAxisConfigurator){
            ((TenantAxisConfigurator) axisConfigurator).runDeployment();
        }
    }

    private void updateDeploymentRepository(ConfigurationContext configContext) {
        BundleContext bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        ServiceReference reference =
                bundleContext.getServiceReference(DeploymentSynchronizer.class.getName());
        if (reference != null) {
            ServiceTracker serviceTracker =
                    new ServiceTracker(bundleContext, DeploymentSynchronizer.class.getName(), null);
            try {
                serviceTracker.open();
                for (Object obj : serviceTracker.getServices()) {
                    ((DeploymentSynchronizer) obj).update(tenantId);
                }
            } catch (Exception e) {
                log.error("Repository update failed for tenant " + tenantId, e);
                setRepoUpdateFailed(configContext);
            } finally {
                serviceTracker.close();
            }
        }
    }

    private void setRepoUpdateFailed(ConfigurationContext configContext) {
        AxisConfigurator axisConfigurator = configContext.getAxisConfiguration().getConfigurator();
        if(axisConfigurator instanceof CarbonAxisConfigurator) {
            ((CarbonAxisConfigurator) axisConfigurator).setRepoUpdateFailed();
        } else if (axisConfigurator instanceof TenantAxisConfigurator){
            ((TenantAxisConfigurator) axisConfigurator).setRepoUpdateFailed();
        }
    }

    public ClusteringCommand getResponse(){
        return new SynchronizeRepositoryResponse();
    }
}
