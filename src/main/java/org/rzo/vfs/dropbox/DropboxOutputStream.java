/*
 * Copyright (c) 2018 Lukas Plechinger. Alle Rechte vorbehalten.
 */

package org.rzo.vfs.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.files.WriteMode;

import java.io.IOException;
import java.io.OutputStream;

public class DropboxOutputStream extends OutputStream {

    private final OutputStream os;

    private final UploadUploader uploader;

    public DropboxOutputStream(DbxClientV2 client, String fileName, boolean append) throws DbxException {
        WriteMode mode;
        if (!append) {
            mode = WriteMode.OVERWRITE;
        } else {
            mode = WriteMode.ADD;
        }

        this.uploader = client.files().uploadBuilder(fileName).withMode(mode).start();
        this.os = uploader.getOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
        try {
            uploader.finish();
        } catch (DbxException e) {
            throw new IOException(e); //wrap exception
        }
    }
}