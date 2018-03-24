package org.rzo.vfs.dropbox.examples;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.rzo.vfs.dropbox.DropboxFileProvider;
import org.rzo.vfs.dropbox.auth.DropboxAuthenticator;

public class AuthenticationExample
{
	public static void main(String[] args) throws Exception
	{
		DropboxAuthenticator auth = new DropboxAuthenticator("accessToken"); 
		FileSystemOptions opts = new FileSystemOptions(); 
		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth); 		
		((DefaultFileSystemManager)VFS.getManager()).addProvider("dbx", new DropboxFileProvider());
		FileObject fo = VFS.getManager().resolveFile("dbx:/", opts);
		System.out.println("root folder "+fo);
	}
}
