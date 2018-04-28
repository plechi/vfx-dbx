package org.rzo.vfs.dropbox.auth;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxHost;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

public class DropboxAuthenticator implements UserAuthenticator {

    private DbxAuthInfo authInfo = null;
    private DbxAppInfo appInfo = null;

    public DropboxAuthenticator(String accessToken) {
        this.setAccessToken(accessToken);
    }

    public DropboxAuthenticator(String appKey, String appSecret) {
        this.setAppInfo(appKey, appSecret);
    }

    public DropboxAuthenticator() {
    }

    public DropboxAuthenticator(DbxAppInfo info){
        this.setAppInfo(info);
    }

    public void setAuthInfo(DbxAuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public void setAppInfo(DbxAppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public void setAccessToken(String accessToken) {
        this.setAuthInfo(new DbxAuthInfo(accessToken, DbxHost.DEFAULT));
    }

    public void setAppInfo(String appKey, String appSecret) {
        this.setAppInfo(new DbxAppInfo(appKey, appSecret));
    }

    public UserAuthenticationData requestAuthentication(UserAuthenticationData.Type[] paramArrayOfType) {
        try {
            UserAuthenticationData result = new TokenData();
            result.setData(TokenData.TOKEN, UserAuthenticatorUtils.toChar(this.authInfo.getAccessToken()));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public FileSystemOptions toOpts() throws FileSystemException {
        FileSystemOptions opts = new FileSystemOptions();
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, this);
        return opts;
    }
}
