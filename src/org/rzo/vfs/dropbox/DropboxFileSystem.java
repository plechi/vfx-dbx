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

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.local.LocalFileName;

import com.dropbox.core.DbxException;

/**
 * An Dropbox file system.
 *
 * 
 */
public class DropboxFileSystem extends AbstractFileSystem
{
    private static final Log LOG = LogFactory.getLog(DropboxFileSystem.class);
    

//    private final String hostname;
//    private final int port;
//    private final String username;
//    private final String password;

    // An idle client
    private final AtomicReference<DropboxClient> idleClient = new AtomicReference<DropboxClient>();

    /**
     * @param rootName The root of the file system.
     * @param dbxClient The dbxClient.
     * @param fileSystemOptions The FileSystemOptions.
     * @since 2.0 (was protected)
     * */
    public DropboxFileSystem(final LocalFileName rootName, final DropboxClient dbxClient,
                          final FileSystemOptions fileSystemOptions)
    {
    	// use default options, since this is part of the key in the file systems map.
    	// otherwise we will be creating each time a new file system -> out of memory
        super(rootName, null, fileSystemOptions == null ? DropboxFileProvider.DEFAULT_OPTIONS : fileSystemOptions);
        // hostname = rootName.getHostName();
        // port = rootName.getPort();

        idleClient.set(dbxClient);
    }

    @Override
    protected void doCloseCommunicationLink()
    {
        DropboxClient idle = idleClient.getAndSet(null);
        // Clean up the connection
        if (idle != null)
        {
            closeConnection(idle);
        }
    }

    /**
     * Adds the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps)
    {
        caps.addAll(DropboxFileProvider.capabilities);
    }

    /**
     * Cleans up the connection to the server.
     * @param client The dbxClient.
     */
    private void closeConnection(final DropboxClient client)
    {
        try
        {
            // Clean up
            if (client.isConnected())
            {
                client.disconnect();
            }
        }
        catch (final Exception e)
        {
            // getLogger().warn("vfs.provider.dbx/close-connection.error", e);
            VfsLog.warn(getLogger(), LOG, "vfs.provider.dbx/close-connection.error", e);
        }
    }

    /**
     * Creates an dbx client to use.
     * @return An dbxCleint.
     * @throws FileSystemException if an error occurs.
     */
    public DropboxClient getClient() throws FileSystemException
    {
        DropboxClient client = idleClient.getAndSet(null);

        try
		{
			if (client == null || !client.isConnected())
			{
			    client = new DropboxClientWrapper((LocalFileName) getRoot().getName(), getFileSystemOptions());
			}
		}
		catch (DbxException e)
		{
			throw new FileSystemException(e);
		}

        return client;
    }

    /**
     * Returns an dbx client after use.
     * @param client The dbxClient.
     */
    public void putClient(final DropboxClient client)
    {
        // Save client for reuse if none is idle.
        if (!idleClient.compareAndSet(null, client))
        {
            // An idle client is already present so close the connection.
            closeConnection(client);
        }
    }


    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name)
        throws FileSystemException
    {
        return new DropboxFileObject(name, this, getRootName());
    }
}
