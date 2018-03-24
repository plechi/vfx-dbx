package org.rzo.vfs.dropbox.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticationData.Type;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.apache.commons.vfs2.UserAuthenticator;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class DropboxAuthenticator  implements UserAuthenticator
{
	
	private final String _appFile;
	private final String _tokenFile;
	private final String _accountFile;
	private DbxAuthInfo _authInfo = null;
	private DbxAppInfo _appInfo = null;

	
	
	public DropboxAuthenticator(String appFile, String tokenFile, String accountFile, DbxAppInfo appInfo, DbxAuthInfo authInfo)
	{
		System.out.println("vfs_dbx_app_auth="+System.getProperty("vfs_dbx_app_auth"));
		System.out.println("vfs_dbx_token_auth="+System.getProperty("vfs_dbx_token_auth"));
		_appFile = appFile;
		_tokenFile = tokenFile;
		_accountFile = accountFile;
		_authInfo = authInfo;
		_appInfo = appInfo;
	}

	public DropboxAuthenticator()
	{
    	this(System.getProperty("vfs_dbx_app_auth", "app.auth"),
    		 System.getProperty("vfs_dbx_token_auth", "token.auth"),
    	     System.getProperty("vfs_dbx_account_auth", "account.auth"),
    	     null,
    	     null);
	}
	
	public DropboxAuthenticator(String appKey, String appSecret)
	{
    	this(System.getProperty("vfs_dbx_app_auth", "app.auth"),
       		 System.getProperty("vfs_dbx_token_auth", "token.auth"),
       	     System.getProperty("vfs_dbx_account_auth", "account.auth"),
       	     new DbxAppInfo(appKey, appSecret),
       	     null);
	}

	public DropboxAuthenticator(String accessToken)
	{
    	this(System.getProperty("vfs.dbx.app.auth", "app.auth"),
       		 System.getProperty("vfs.dbx.token.auth", "token.auth"),
       	     System.getProperty("vfs.dbx.account.auth", "account.auth"),
       	     null,
       	     new DbxAuthInfo(accessToken, DbxHost.Default));
	}

	
	public UserAuthenticationData requestAuthentication(UserAuthenticationData.Type[] paramArrayOfType)
	{
		try
		{
			return requestAuthenticationInternal();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private UserAuthenticationData requestAuthenticationInternal() throws Exception
	{
		
		File tokenFile = new File(_tokenFile);
		File appFile = new File(_appFile);
        DbxRequestConfig config = new DbxRequestConfig(
                "vfs-dbx/00.01", Locale.getDefault().toString());

		if (tokenFile.exists() && _authInfo == null)
		try
		{
			_authInfo = DbxAuthInfo.Reader.readFromFile(tokenFile);
			if (!checkToken(_authInfo, config))
			{
				System.out.println("token file seems to be invalid -> delete and retry: "+tokenFile.getAbsolutePath());
				throw new RuntimeException("logon error");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (_authInfo == null)
		{
		if (!appFile.exists() && _appInfo == null)
		{
        	System.out.println("check file : "+appFile.getAbsolutePath()+"/"+tokenFile.getAbsolutePath());			
		}
		if (_appInfo == null)
			_appInfo =  DbxAppInfo.Reader.readFromFile(appFile);
        
        _authInfo = doWebAuth(config, _appInfo);

        try {
            DbxAuthInfo.Writer.writeToFile(_authInfo, tokenFile);
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
		}

        UserAuthenticationData result = new TokenData();
        result.setData(TokenData.TOKEN, UserAuthenticatorUtils.toChar(_authInfo.accessToken));
        return result;
	}

	private DbxAuthInfo doWebAuth(DbxRequestConfig config, DbxAppInfo appInfo)
	{
		DbxAuthInfo authInfo = null;
        	authInfo = doManWebAuth(config, appInfo);
		return authInfo;
	}

	private DbxAuthInfo doManWebAuth(DbxRequestConfig config, DbxAppInfo appInfo)
	{
		DbxAuthInfo authInfo = null;
		try
		{
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        String authorizeUrl = webAuth.start();
        
        System.out.println("1. Go to: " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first)");
        System.out.println("3. Copy the authorization code.");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
        DbxAuthFinish authFinish = webAuth.finish(code);
        authInfo = new DbxAuthInfo(authFinish.accessToken, DbxHost.Default);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return authInfo;
	}

	private boolean checkToken(DbxAuthInfo authInfo, DbxRequestConfig config)
	{
		try
		{
        DbxClient client = new DbxClient(config, authInfo.accessToken);
        return (client.getAccountInfo() != null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

}
