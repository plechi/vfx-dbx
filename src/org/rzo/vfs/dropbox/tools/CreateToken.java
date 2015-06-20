package org.rzo.vfs.dropbox.tools;

import java.util.Date;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.rzo.vfs.dropbox.DropboxFileProvider;
import org.rzo.vfs.dropbox.auth.DropboxAuthenticator;

public class CreateToken
{

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		((DefaultFileSystemManager)VFS.getManager()).addProvider("dbx", new DropboxFileProvider());
		FileObject fo = VFS.getManager().resolveFile("dbx:/");
		System.out.println("root folder "+fo);
	}

}
