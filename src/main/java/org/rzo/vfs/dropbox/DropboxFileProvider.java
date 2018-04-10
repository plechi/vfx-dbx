/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rzo.vfs.dropbox;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.local.LocalFileName;
import org.rzo.vfs.dropbox.auth.DropboxAuthenticator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A provider for dbx file systems.
 */
public class DropboxFileProvider extends AbstractOriginatingFileProvider {

    /**
     * Authenticator types.
     */
    public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[]
            {
                    UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD
            };

    static final Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
            {
                    Capability.CREATE,
                    Capability.DELETE,
                    Capability.RENAME,
                    Capability.GET_TYPE,
                    Capability.LIST_CHILDREN,
                    Capability.READ_CONTENT,
                    Capability.GET_LAST_MODIFIED,
                    Capability.WRITE_CONTENT,
                    //Capability.APPEND_CONTENT //appending actually does not work yet
            }));

    public static final FileSystemOptions DEFAULT_OPTIONS = new FileSystemOptions();

    public DropboxFileProvider() {
        super();

    }

    /**
     * Creates the filesystem.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        // Create the file system
        final LocalFileName rootName = (LocalFileName) name;

        if (fileSystemOptions == null) {
            fileSystemOptions = DEFAULT_OPTIONS;
        }


        DropboxClientWrapper dbxClient = new DropboxClientWrapper(rootName, fileSystemOptions);

        return new DropboxFileSystem(rootName, dbxClient, fileSystemOptions);
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return DropboxFileSystemConfigBuilder.getInstance();
    }

    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
