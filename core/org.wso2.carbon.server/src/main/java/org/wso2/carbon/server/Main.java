/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.server.extensions.*;
import org.wso2.carbon.server.util.Utils;

import java.io.File;

public class Main {
    private static Log log = LogFactory.getLog(Main.class);

    /**
     * Launch the Carbon server.
     *  1) Process and set system properties
     *  2) Invoke extensions.
     *  3) Launch OSGi framework.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        //Setting Carbon Home
        if (System.getProperty(LauncherConstants.CARBON_HOME) == null) {
            System.setProperty(LauncherConstants.CARBON_HOME, ".");
        }
        System.setProperty(LauncherConstants.AXIS2_HOME, System.getProperty(LauncherConstants.CARBON_HOME));

        //To keep track of the time taken to start the Carbon server.
        System.setProperty("wso2carbon.start.time", System.currentTimeMillis() + "");

        processCmdLineArgs(args);
        invokeExtensions();
        launchCarbon();
    }

    /**
     * Process command line arguments and set corresponding system properties.
     * @param args cmd line args
     */
    public static void processCmdLineArgs(String[] args){
        String cmd = null;
        int index = 0;

        // Set the System properties
        for (String arg : args) {
            index++;
            if (arg.startsWith("-D")) {
                int indexOfEq = arg.indexOf("=");
                String property;
                String value;
                if (indexOfEq != -1) {
                    property = arg.substring(2, indexOfEq);
                    value = arg.substring(indexOfEq + 1);
                } else {
                    property = arg.substring(2);
                    value = "true";
                }
                System.setProperty(property, value);
            } else if (arg.toUpperCase().endsWith(LauncherConstants.COMMAND_HELP)) {
                Utils.printUsages();
                System.exit(0);
            } else if (arg.toUpperCase().endsWith(LauncherConstants.COMMAND_CLEAN_REGISTRY)) {
                // sets the system property marking a registry cleanup
                System.setProperty("carbon.registry.clean", "true");
            } else {
                if (cmd == null) {
                    cmd = arg;
                }
            }
        }
    }

    /**
     *  Invoke the extensions specified in the carbon.xml
     */
    public static void invokeExtensions(){
        //TODO Read extensions from the carbon.xml and execute them - Sameera.

        //converting jars found under components/lib and putting them in components/dropins dir
        new DefaultBundleCreator().perform();
        new SystemBundleExtensionCreator().perform();
        new Log4jPropFileFragmentBundleCreator().perform();

        //Add bundles in the dropins directory to the bundles.info file.
        new DropinsBundleDeployer().perform();

        //copying patched jars to components/plugins dir
        new PatchInstaller().perform();
    }

    /**
     *  Launch the Carbon Server.
     */
    public static void launchCarbon(){
        CarbonLauncher carbonLauncher = new CarbonLauncher();
        carbonLauncher.launch();
    }
}




