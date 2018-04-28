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

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.local.LocalFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.rzo.vfs.dropbox.auth.TokenData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A wrapper to the dbxClient to allow automatic reconnect on connection loss.<br />
 */
class DropboxClientWrapper implements DropboxClient {
    private final LocalFileName root;
    private final FileSystemOptions fileSystemOptions;

    private DbxClientV2 dbxClient = null;


    DropboxClientWrapper(final LocalFileName root, final FileSystemOptions fileSystemOptions) throws FileSystemException {
        this.root = root;
        this.fileSystemOptions = fileSystemOptions;
        getDbxClient(); // fail-fast
    }

    public LocalFileName getRoot() {
        return root;
    }

    public FileSystemOptions getFileSystemOptions() {
        return fileSystemOptions;
    }

    private DbxClientV2 createClient() {
        //System.out.println("Create client");
        final LocalFileName rootName = getRoot();

        UserAuthenticationData authData = null;
        try {
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, DropboxFileProvider.AUTHENTICATOR_TYPES);
            char[] token = UserAuthenticatorUtils.getData(authData, TokenData.TOKEN, null);



            if (token == null){
                //System.out.println("Token is null");
                return null;
            }


            DbxRequestConfig config = DbxRequestConfig.newBuilder(DropboxClient.CLIENT_IDENTIFIER).withAutoRetryEnabled().build();

            DbxClientV2 client = new DbxClientV2(config, new String(token));
            //System.out.println("Client created"+client);
            return client;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }
    }

    private DbxClientV2 getDbxClient() {
        if (dbxClient == null) {
            dbxClient = createClient();
        }

        return dbxClient;
    }

    public boolean isConnected() {
        return dbxClient != null;// && dbxClient.getAccountInfo().displayName != null;
    }

    public void disconnect() throws IOException {
        try {
            try {
                //getDbxClient().disableAccessToken();
                //System.out.println("Disconnect client");
            } catch (Exception e) {
                throw new IOException(e);
            }
        } finally {
            dbxClient = null;
        }
    }

    public List<Metadata> listFiles(String relPath) throws IOException, DbxException {
        try {
            return listFilesInDirectory(relPath);
        } catch (IOException e) {
            disconnect(); //FIXME: do we need this with apiv2?
            return listFilesInDirectory(relPath);
        }
    }

    private List<Metadata> listFilesInDirectory(String relPath) throws IOException, DbxException {
        ListFolderResult result = getDbxClient().files().listFolder(relPath);
        return result.getEntries();
    }

    public boolean removeDirectory(String relPath) throws IOException {
        try {
            getDbxClient().files().deleteV2(absolutePath(relPath));
            return true;
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }

    public boolean deleteFile(String relPath) throws IOException {
        try {
            getDbxClient().files().deleteV2(relPath);
            return true;
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }

    public boolean rename(String oldName, String newName) throws IOException {
        try {
            return getDbxClient().files().moveV2(oldName, newName) != null; //FIXME: better check
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }

    public boolean makeDirectory(String relPath) throws IOException {
        try {
            return getDbxClient().files().createFolderV2(relPath) != null;  //FIXME: better check
        } catch (DbxException e) {
            throw new IOException(e);
        }

    }

    public InputStream retrieveFileStream(String relPath) throws IOException {
        try {
            return getDbxClient().files().downloadBuilder(relPath).start().getInputStream();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public OutputStream storeFileStream(String relPath, boolean append) throws IOException {
        try {
            return new DropboxOutputStream(getDbxClient(), relPath, append);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public boolean abort() throws IOException {
        try {
            disconnect();
            return true;
        } catch (IOException e) {
            disconnect();
        }
        return true;
    }

    private String absolutePath(String relatPath) {
        return root.getPath() + relatPath;
    }


    public long getLastModifiedTime(String path) throws IOException {
        Metadata entry;
        try {
            entry = getDbxClient().files().getMetadataBuilder(path).start();
        } catch (DbxException e) {
            throw new IOException(e);
        }
        if (entry instanceof FileMetadata)
            return ((FileMetadata) entry).getClientModified().getTime();
        return 0;
    }


    public long getContentSize(String path) throws IOException {
        Metadata entry;
        try {
            entry = getDbxClient().files().getMetadataBuilder(path).start();
        } catch (DbxException e) {
            throw new IOException(e);
        }
        if (entry instanceof FileMetadata)
            return ((FileMetadata) entry).getSize();
        return 0;
    }

}
