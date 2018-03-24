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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.local.LocalFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.rzo.vfs.dropbox.auth.TokenData;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;
import com.dropbox.core.DbxClient.Uploader;

/**
 * A wrapper to the dbxClient to allow automatic reconnect on connection loss.<br />
 */
class DropboxClientWrapper implements DropboxClient
{
    private final LocalFileName root;
    private final FileSystemOptions fileSystemOptions;

    private DbxClient dbxClient;
    
    public class DropboxOutputStream extends OutputStream
    {
    	Uploader _uploader;
    	OutputStream _out;
    	
    	DropboxOutputStream(Uploader uploader)
    	{
    		_uploader = uploader;
    		_out = _uploader.getBody();
    	}

		@Override
		public void write(int b) throws IOException
		{
			_out.write(b);
		}
		
		@Override
		public void close() throws IOException
		{
			try
			{
				_uploader.finish();
			}
			catch (DbxException e)
			{
				throw new IOException(e);
			}
			_uploader.close();
		}
    	
    }

    DropboxClientWrapper(final LocalFileName root, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        this.root = root;
        this.fileSystemOptions = fileSystemOptions;
        getDbxClient(); // fail-fast
    }

    public LocalFileName getRoot()
    {
        return root;
    }

    public FileSystemOptions getFileSystemOptions()
    {
        return fileSystemOptions;
    }

    private DbxClient createClient() throws FileSystemException
    {
        final LocalFileName rootName = getRoot();

        UserAuthenticationData authData = null;
        try
        {
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, DropboxFileProvider.AUTHENTICATOR_TYPES);
            char[] token = UserAuthenticatorUtils.getData(authData, TokenData.TOKEN, null);
            if (token == null)
            	return null;
            DbxRequestConfig config = new DbxRequestConfig(
                    "vfs-dbx/00.01", Locale.getDefault().toString());
            DbxClient client = new DbxClient(config, new String(token));
            return client;
        }
        finally
        {
            UserAuthenticatorUtils.cleanup(authData);
        }
    }

    private DbxClient getDbxClient() throws FileSystemException
    {
        if (dbxClient == null)
        {
            dbxClient = createClient();
        }

        return dbxClient;
    }

    public boolean isConnected() throws FileSystemException
    {
        try
		{
			return dbxClient != null;// && dbxClient.getAccountInfo().displayName != null;
		}
		catch (Exception e)
		{
			throw new FileSystemException(e);
		}
    }

    public void disconnect() throws IOException
    {
        try
        {
            try
			{
				//getDbxClient().disableAccessToken();
			}
			catch (Exception e)
			{
				throw new IOException(e);
			}
        }
        finally
        {
            dbxClient = null;
        }
    }

    public DbxEntry[] listFiles(String relPath) throws IOException
    {
        try
        {
        	DbxEntry[] files = listFilesInDirectory(relPath);
            return files;
        }
        catch (IOException e)
        {
            disconnect();

            DbxEntry[] files = listFilesInDirectory(relPath);
            return files;
        }
    }

    private DbxEntry[] listFilesInDirectory(String relPath) throws IOException
    {
    	DbxEntry[] files = null;
    	DbxEntry.WithChildren listing;
		try
		{
			listing = getDbxClient().getMetadataWithChildren(relPath);
		}
		catch (DbxException e)
		{
			throw new IOException(e);
		}
    	if (listing != null)
    		files = listing.children.toArray(new DbxEntry[listing.children.size()]);
    	return files;
    }

    public boolean removeDirectory(String relPath) throws IOException
    {
        try
        {
            getDbxClient().delete(absolutePath(relPath));
            return true;
        }
        catch (DbxException e)
        {
        	throw new IOException(e);
        }
    }

    public boolean deleteFile(String relPath) throws IOException
    {
        try
        {
            getDbxClient().delete(relPath);
            return true;
        }
        catch (DbxException e)
        {
        	throw new IOException(e);
        }
    }

    public boolean rename(String oldName, String newName) throws IOException
    {
        try
        {
            return getDbxClient().move(oldName, newName) != null;
        }
        catch (DbxException e)
        {
        	throw new IOException(e);
        }
    }

    public boolean makeDirectory(String relPath) throws IOException
    {
        try
        {
            return getDbxClient().createFolder(relPath) != null;
        }
        catch (DbxException e)
        {
        	throw new IOException(e);
        }

    }

    public InputStream retrieveFileStream(String relPath) throws IOException
    {
        try
        {
            return getDbxClient().startGetFile(relPath, null).body;
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }
    }

    public OutputStream storeFileStream(String relPath) throws IOException
    {
        try
        {
            return new DropboxOutputStream (getDbxClient().startUploadFile(relPath, DbxWriteMode.force(), -1));
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }
    }

    public boolean abort() throws IOException
    {
        try
        {
            disconnect();
            return true;
        }
        catch (IOException e)
        {
            disconnect();
        }
        return true;
    }
    
    private String absolutePath(String relatPath)
    {
    	return root.getPath()+relatPath;
    }

	
	public long getLastModifiedTime(String path) throws IOException
	{
		DbxEntry entry;
		try
		{
			entry = getDbxClient().getMetadata(absolutePath(path));
		}
		catch (DbxException e)
		{
			throw new IOException(e);
		}
		if (entry.isFile())
			return entry.asFile().lastModified.getTime();
		else 
			if (entry.asFolder().lastModified != null)
				return entry.asFolder().lastModified.getTime();
		return 0;
	}

	
	public long getContentSize(String path) throws IOException
	{
		DbxEntry entry;
		try
		{
			entry = getDbxClient().getMetadata(absolutePath(path));
		}
		catch (DbxException e)
		{
			throw new IOException(e);
		}
		if (entry.isFile())
			return entry.asFile().numBytes;
		else
			return entry.asFolder().numBytes;
	}

}
