/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.integration.framework.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;

/**
 * A set of utility methods such as starting & stopping a Carbon server.
 */
public class ServerUtils {
    private static final Log log = LogFactory.getLog(ServerUtils.class);

    private Process process;
    private String carbonHome;
    private String originalUserDir = null;
    private InputStreamHandler inputStreamHandler;

    private static final String SERVER_SHUTDOWN_MESSAGE = "Halting JVM";
    private static final long DEFAULT_START_STOP_WAIT_MS = 1000 * 60 * 5;
    private int defaultHttpsPort = 9443;

    public synchronized void startServerUsingCarbonHome(String carbonHome, final int portOffset) {
        if (process != null) { // An instance of the server is running
            return;
        }
        Process tempProcess;
        try {
            CodeCoverageUtils.instrument(carbonHome);
            FrameworkSettings.init();
            defaultHttpsPort = Integer.parseInt(FrameworkSettings.HTTPS_PORT);
            int defaultHttpPort = Integer.parseInt(FrameworkSettings.HTTP_PORT);
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
            originalUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", carbonHome);
            File commandDir = new File(carbonHome);
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                commandDir = new File(carbonHome + File.separator + "bin");
                tempProcess =
                        Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "wso2server.bat",
                                                               "-DportOffset=" + String.valueOf(portOffset),
                                                               "-Demma.properties=" + System.getProperty("emma.properties"),
                                                               "-Demma.rt.control.port=" + (47653 + portOffset)},
                                                  null, commandDir);
            } else {
                tempProcess =
                        Runtime.getRuntime().exec(new String[]{"sh", "bin/wso2server.sh",
                                                               "-DportOffset=" + String.valueOf(portOffset),
                                                               "-Demma.properties=" + System.getProperty("emma.properties"),
                                                               "-Demma.rt.control.port=" + (47653 + portOffset)},
                                                  null, commandDir);
            }
            InputStreamHandler errorStreamHandler =
                    new InputStreamHandler("errorStream", tempProcess.getErrorStream());
            inputStreamHandler = new InputStreamHandler("inputStream", tempProcess.getInputStream());

            // start the stream readers
            inputStreamHandler.start();
            errorStreamHandler.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        log.info("Shutting down server...");
                        shutdown(portOffset);
                    } catch (Exception e) {
                        log.error("Cannot shutdown server", e);
                    }
                }
            });
            ClientConnectionUtil.waitForPort(defaultHttpsPort + portOffset,
                                             DEFAULT_START_STOP_WAIT_MS, false);
            ClientConnectionUtil.waitForPort(defaultHttpPort + portOffset,
                                             DEFAULT_START_STOP_WAIT_MS, false);
            ClientConnectionUtil.waitForLogin(portOffset);
            log.info("Server started successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Unable to start server", e);
        }
        process = tempProcess;
    }

    public synchronized String setUpCarbonHome(String carbonServerZipFile)
            throws IOException {
        if (process != null) { // An instance of the server is running
            return carbonHome;
        }
        CodeCoverageUtils.init();
        int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(carbonServerZipFile + " is not a zip file");
        }
        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if (fileSeparator.equals("\\")) {
            carbonServerZipFile = carbonServerZipFile.replace("/", "\\");
        }
        String extractedCarbonDir =
                carbonServerZipFile.substring(carbonServerZipFile.lastIndexOf(fileSeparator) + 1,
                                              indexOfZip);
        FileManipulator.deleteDir(extractedCarbonDir);
        String extractDir = "carbontmp" + System.currentTimeMillis();
        new ArchiveManipulator().extract(carbonServerZipFile, extractDir);
        String baseDir = (System.getProperty("basedir", ".")) + File.separator + "target";
        return carbonHome =
                new File(baseDir).getAbsolutePath() + File.separator + extractDir + File.separator +
                extractedCarbonDir;
    }

    public synchronized void shutdown(int portOffset) throws Exception {
        if (process != null) {
            if (ClientConnectionUtil.isPortOpen(defaultHttpsPort + portOffset)) {
                shutdownServer(portOffset);
                long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
                while (!inputStreamHandler.getOutput().contains(SERVER_SHUTDOWN_MESSAGE) &&
                       System.currentTimeMillis() < time) {
                    // wait until server shutdown is completed
                }
                log.info("Server stopped successfully...");
            }
            process.destroy();
            process = null;
            System.clearProperty(ServerConstants.CARBON_HOME);
            System.setProperty("user.dir", originalUserDir);
        }
    }

    private void shutdownServer(int portOffset) throws Exception {
        int httpsPort = defaultHttpsPort + portOffset;
        try {
            String serviceBaseURL = "https://localhost:" + httpsPort + "/services/";
            String sessionCookie = new LoginLogoutUtil(portOffset).login();
            //shutdown the server through ServerAdmin
            ServerAdminClient serverAdminClient =
                    new ServerAdminClient(null, serviceBaseURL, sessionCookie, null);
            serverAdminClient.shutdownGracefully();
        } catch (Exception e) {
            log.error("Error when shutting down the server.", e);
            throw e;
        }
    }

}
