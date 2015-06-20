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

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * The config BUILDER for various dbx configuration options.
 *
 */
public final class DropboxFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private static final DropboxFileSystemConfigBuilder BUILDER = new DropboxFileSystemConfigBuilder();

    private static final String DATA_TIMEOUT = DropboxFileSystemConfigBuilder.class.getName() + ".DATA_TIMEOUT";
    private static final String SO_TIMEOUT = DropboxFileSystemConfigBuilder.class.getName() + ".SO_TIMEOUT";

    private static final String ENCODING =
            DropboxFileSystemConfigBuilder.class.getName() + ".ENCODING";

    private DropboxFileSystemConfigBuilder()
    {
        super("dbx.");
    }

    public static DropboxFileSystemConfigBuilder getInstance()
    {
        return BUILDER;
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass()
    {
        return DropboxFileSystem.class;
    }

    /**
     * @param opts The FileSystemOptions.
     * @return The timeout as an Integer.
     * @see #setDataTimeout
     */
    public Integer getDataTimeout(FileSystemOptions opts)
    {
        return getInteger(opts, DATA_TIMEOUT);
    }

    /**
     * set the data timeout for the dbx client.<br />
     * If you set the dataTimeout to <code>null</code> no dataTimeout will be set on the
     * dbx client.
     *
     * @param opts The FileSystemOptions.
     * @param dataTimeout The timeout value.
     */
    public void setDataTimeout(FileSystemOptions opts, Integer dataTimeout)
    {
        setParam(opts, DATA_TIMEOUT, dataTimeout);
    }

    /**
     * @param opts The FileSystem options.
     * @return The timeout value.
     * @see #getDataTimeout
     * @since 2.0
     */
    public Integer getSoTimeout(FileSystemOptions opts)
    {
        return (Integer) getParam(opts, SO_TIMEOUT);
    }

    /**
     * set the socket timeout for the dbx client.<br />
     * If you set the socketTimeout to <code>null</code> no socketTimeout will be set on the
     * dbx client.
     *
     * @param opts The FileSystem options.
     * @param soTimeout The timeout value.
     * @since 2.0
     */
    public void setSoTimeout(FileSystemOptions opts, Integer soTimeout)
    {
        setParam(opts, SO_TIMEOUT, soTimeout);
    }

    public void setControlEncoding(FileSystemOptions opts, String encoding)
    {
        setParam(opts, ENCODING, encoding);
    }

    /**
     * @param opts The FileSystemOptions.
     * @return The encoding.
     * @since 2.0
     * */
    public String getControlEncoding(FileSystemOptions opts)
    {
        return  (String) getParam(opts, ENCODING);
    }
}
