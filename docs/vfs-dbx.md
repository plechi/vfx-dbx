1.  [What is vfs-dbx ?](#mozTocId231248)
    1.  [Project page](#mozTocId361243)
    2.  [Quickstart](#mozTocId230065)
    3.  [Supported Capabilities](#mozTocId885777)
    4.  [Authentication](#mozTocId274201)
    5.  [Third party libraries](#mozTocId860275)
    6.  [Change History](#mozTocId12638)

## <a class="mozTocH2" name="mozTocId231248"></a>What is vfs-dbx ?

vfs-dbx is a java library which enables one to access [dropbox](http://www.dropbox.com) files using the [apache commons vfs library](http://commons.apache.org/proper/commons-vfs).  

## <a class="mozTocH2" name="mozTocId361243"></a>Project page

The project is hosted on [sourceforge](http://sourceforge.net/projects/vfs-dbx/). Here you will find the latest downloads,  [issue tracker](http://sourceforge.net/p/vfs-dbx/tickets/) and [discussion board](http://sourceforge.net/p/vfs-dbx/discussion/).  

Please use the help discussion board for support.  
Please use the [general discussion board](http://sourceforge.net/p/vfs-dbx/discussion/general/) for sharing ideas or for general comments.  

## <a class="mozTocH2" name="mozTocId230065"></a>Quickstart

1\. Go to the [dropbox developer page](https://www.dropbox.com/developers/apps), create a new app, and copy the app key and secret.  
2\. Download the vfs-dbx project  
3\. In the vfs-dbx project folder edit the json file <span style="font-weight: bold;">app.auth</span>.  
4\. Enter the app key and secret into the file and save it.  
5\. Execute auth.bat or auth.sh  
6\. Invoke the displayed URL (link) in an internet browser  
7\. If required login to dropbox and click "Allow"  
8\. Enter the authorization code into the auth.bat application.  
9 The application creates the file <span style="font-weight: bold;">token.auth</span> and terminates.  

<table style="text-align: left; width: 823px; height: 232px;" border="1" cellpadding="2" cellspacing="2">

<tbody>

<tr>

<td style="background-color: rgb(235, 235, 235);"><span style="font-style: italic;">  
</span>

<div style="margin-left: 40px;"><span style="font-weight: bold;">public class QuickStart</span>  
<span style="font-weight: bold;">{</span>  
<span style="font-weight: bold;">    public static void main(String[] args) throws Exception</span>  
<span style="font-weight: bold;">    {</span>  
<span style="font-weight: bold;">        ((DefaultFileSystemManager)VFS.getManager()).addProvider("dbx", new DropboxFileProvider());        (1)  
        FileObject fo = VFS.getManager().resolveFile("dbx:/");                                                                                (2)  
        System.out.println("found root file: "+fo);  
     }  
</span><span style="font-weight: bold;">}</span>  
<span style="font-style: italic;"></span></div>

</td>

</tr>

</tbody>

</table>

(1) define "dbx" as dropbox URL schema  

(2) invoke vfs methods  

The above program will print the root folder of your application.  

## <a class="mozTocH2" name="mozTocId885777"></a>Supported Capabilities

<table style="width: 755px; height: 56px;" class="bodyTable table table-striped table-bordered" border="2" cellpadding="2" cellspacing="2">

<tbody>

<tr class="a">

<th>File System</th>

<th>Directory Contents</th>

<th>Authentication</th>

<th>Read</th>

<th>Write</th>

<th>Create/Delete</th>

<th>Random</th>

<th>Version</th>

<th>Rename</th>

<td align="undefined" valign="undefined"><span style="font-weight: bold;">Append</span></td>

</tr>

<tr class="b">

<td>dbx</td>

<td>Yes</td>

<td>Yes</td>

<td>Yes</td>

<td>Yes</td>

<td>Yes</td>

<td>No</td>

<td>No</td>

<td>Yes</td>

<td align="undefined" valign="undefined">  No </td>

</tr>

</tbody>

</table>

Other capabilities may be added in the future.  

## <a class="mozTocH2" name="mozTocId274201"></a>Authentication

Per default authentification is done through files:  

The authenticator first searches for the file <span style="font-weight: bold;">token.auth</span> and tries to authenticate using this file.  
If the file does not exist or if the authentification fails the authenticator searches for the file <span style="font-weight: bold;">app.auth</span>.  
The user is displayed the authentication URL and asked to provide the authentication code.  
The code is stored in the file token.auth.  

Per default the files are stored in the working dir of the application.  
The java system properties <span style="font-weight: bold;">vfs.dbx.app.auth</span>, <span style="font-weight: bold;">vfs.dbx.token.auth</span> allow to override the default file names.  

The authentication can also be done programmatically:  

<table style="text-align: left; width: 823px; height: 232px;" border="1" cellpadding="2" cellspacing="2">

<tbody>

<tr>

<td style="background-color: rgb(235, 235, 235);"><span style="font-style: italic;">  
</span>

<div style="margin-left: 40px;"><span style="font-weight: bold;">public class AuthenticationExample  
{  
    public static void main(String[] args) throws Exception  
    {  
        DropboxAuthenticator auth = new DropboxAuthenticator("accessToken");                                                      (1)  
        FileSystemOptions opts = new FileSystemOptions();  
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);          
        ((DefaultFileSystemManager)VFS.getManager()).addProvider("dbx", new DropboxFileProvider());             (2)  
        FileObject fo = VFS.getManager().resolveFile("dbx:/", opts);                                                                            (3)  
        System.out.println("root folder "+fo);  
    }  
}  
</span>  
<span style="font-style: italic;"></span></div>

</td>

</tr>

</tbody>

</table>

(1) create an authenticator given an access token (or application key and secret)  
(2) associate the dbx schema with the dropbox file provider  
(3) invoke vfs methods using the authenticator  

## <a class="mozTocH2" name="mozTocId860275"></a>Third party libraries

*   [commons vfs 2.0 [APACHE]](http://commons.apache.org/proper/commons-vfs/)
*   [core dropbox java sdk [MIT]](https://github.com/dropbox/dropbox-sdk-java)

## <a class="mozTocH2" name="mozTocId12638"></a>Change History

[can be found here](changes.html)
