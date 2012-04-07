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
package org.wso2.carbon.registry.synchronization;

import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.synchronization.operation.CheckInCommand;
import org.wso2.carbon.registry.synchronization.operation.CheckOutCommand;
import org.wso2.carbon.registry.synchronization.operation.UpdateCommand;

import java.io.File;

/**
 * An interface to the synchronization API of the Registry Kernel. Using the methods of this class,
 * it is possible to synchronize a Registry with the filesystem and vice versa.
 * <p/>
 * Three synchronization operations are currently supported. These are, {@link #checkIn}, {@link
 * #checkOut} and {@link #update}.
 */
@SuppressWarnings("unused")
public final class RegistrySynchronizer {

    // We don't want users to create an instance of this class.
    private RegistrySynchronizer() {
    }

    /**
     * Method to check-in some filesystem based resources and collections (which are files and
     * directories), into a specified registry instance.
     *
     * @param registry        the registry instance to be used.
     * @param filePath        the path on the filesystem containing the corresponding resources and
     *                        collections.
     * @param registryPath    the target path of the registry that check-in should be done.
     * @param ignoreConflicts to ignore the conflicts
     * @param forcedCheckIn   check-in the changes irrespective of whether the content has changed
     *                        or not.
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void checkIn(UserRegistry registry, String filePath, String registryPath,
                               boolean ignoreConflicts, boolean forcedCheckIn)
            throws SynchronizationException {
        CheckInCommand operation =
                new CheckInCommand(null, filePath, registryPath, registry.getUserName(), true,
                        ignoreConflicts, !forcedCheckIn);
        operation.execute(registry);
    }

    /**
     * Method to check-in some filesystem based resources and collections (which are files and
     * directories), into a specified registry instance.
     *
     * @param registry        the registry instance to be used.
     * @param filePath        the path on the filesystem containing the corresponding resources and
     *                        collections.
     * @param registryPath    the target path of the registry that check-in should be done.
     * @param ignoreConflicts to ignore the conflicts
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void checkIn(UserRegistry registry, String filePath, String registryPath,
                               boolean ignoreConflicts)
            throws SynchronizationException {
        checkIn(registry, filePath, registryPath, ignoreConflicts, false);
    }

    /**
     * Method to check-in some filesystem based resources and collections (which are files and
     * directories), into a specified registry instance.
     *
     * @param registry     the registry instance to be used.
     * @param filePath     the path on the filesystem containing the corresponding resources and
     *                     collections.
     * @param registryPath the target path of the registry that check-in should be done.
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void checkIn(UserRegistry registry, String filePath, String registryPath)
            throws SynchronizationException {
        checkIn(registry, filePath, registryPath, false);
    }

    /**
     * Method to check-in some filesystem based resources and collections (which are files and
     * directories), into a specified registry instance.
     *
     * @param registry the registry instance to be used.
     * @param filePath the path on the filesystem containing the corresponding resources and
     *                 collections.
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void checkIn(UserRegistry registry, String filePath)
            throws SynchronizationException {
        checkIn(registry, filePath, null);
    }

    /**
     * Method to check-in some filesystem based resources and collections (which are files and
     * directories), into a specified registry instance.
     *
     * @param registry        the registry instance to be used.
     * @param filePath        the path on the filesystem containing the corresponding resources and
     *                        collections.
     * @param ignoreConflicts to ignore the conflicts
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void checkIn(UserRegistry registry, String filePath, boolean ignoreConflicts)
            throws SynchronizationException {
        checkIn(registry, filePath, null, ignoreConflicts);
    }

    /**
     * Method to check-out some resources and collections on a registry into a filesystem which will
     * create some files and directories in the process. This will also create some metadata
     * directories which will be used in future check-in and update operations.
     *
     * @param registry     the registry instance to be used.
     * @param filePath     the path on the filesystem to which the corresponding resources and
     *                     collections will be checked out.
     * @param resourcePath the path on the registry instance where the resources and the collections
     *                     can be found.
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void checkOut(UserRegistry registry, String filePath, String resourcePath)
            throws SynchronizationException {
        CheckOutCommand operation =
                new CheckOutCommand(null, filePath, resourcePath, registry.getUserName(), false);
        operation.execute(registry);
    }

    /**
     * Method to update an already checked out resource or collection to the latest version found on
     * the specified registry instance.
     *
     * @param registry the registry instance to be used.
     * @param filePath the path on the filesystem containing the corresponding resources and
     *                 collections.
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void update(UserRegistry registry, String filePath)
            throws SynchronizationException {
        update(registry, filePath, false);
    }

    /**
     * Method to update an already checked out resource or collection to the latest version found on
     * the specified registry instance. In the case of the {@link #update(UserRegistry, String)}
     * command, a .mine and a .server file will be created if a conflict occurred. However, in this
     * method, you could specify whether conflicts should simply be ignored.
     *
     * @param registry        the registry instance to be used.
     * @param filePath        the path on the filesystem containing the corresponding resources and
     *                        collections.
     * @param ignoreConflicts ignore any conflicts, and avoid creating .mine and .server files.
     *
     * @throws SynchronizationException if an error occurred while performing the operation.
     */
    public static void update(UserRegistry registry, String filePath, boolean ignoreConflicts)
            throws SynchronizationException {
        UpdateCommand operation =
                new UpdateCommand(null, filePath, null, ignoreConflicts, registry.getUserName(), 
                        false);
        operation.execute(registry);
    }

    /**
     * Method to determine whether a check-out has already been made at the given directory
     * location.
     *
     * @param directoryPath the path of the directory on the filesystem
     *
     * @return true if a check-out has been made at the given path.
     */
    public static boolean isCheckedOut(String directoryPath) {
        return new File(directoryPath + File.separator +
                SynchronizationConstants.META_DIRECTORY).exists();
    }

}
