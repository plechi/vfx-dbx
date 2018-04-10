
package org.rzo.vfs.dropbox;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.rzo.vfs.dropbox.auth.DropboxAuthenticator;


public class Main {

    public static void main(String[] args) throws FileSystemException {


        /*DropboxAuthenticator auth = new DropboxAuthenticator("e3zxow3ahnf0e1o","	"k9uktv262lmnii1");

        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);*/

        ((DefaultFileSystemManager) VFS.getManager()).addProvider("dbx", new DropboxFileProvider());


        DropboxAuthenticator auth = new DropboxAuthenticator("e3zxow3ahnf0e1o", "k9uktv262lmnii1");
        auth.setAccessToken("E3JJjCpDHIMAAAAAAAAaAM8uYL_ENzjykNNujZBeNp643o5RN4YLWldD3_Yt676X");


        FileSystemOptions opts = new FileSystemOptions();
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

        FileSystemManager mgr = VFS.getManager();
        FileObject file = mgr.resolveFile("dbx://", opts);
        System.out.println(file.getName());
        System.out.println(file);
        System.out.println("isFile:" + file.isFile());
        System.out.println("isFolder:" + file.isFolder());


        for (FileObject child : file.getChildren()) {
            System.out.println(" - " + child.getName());
        }





        /*try {
            FileObject textfile = mgr.resolveFile("dbx:///test.txt", opts);
            System.out.println("test.txt exists:" + textfile.exists());
            InputStream is = textfile.getContent().getInputStream();

            String content = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));

            is.close();

            System.out.println("Content:\n====");
            System.out.println(content);
            System.out.println("====");


            OutputStream os = textfile.getContent().getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

            writer.write(content);
            writer.write(new Date().toString());
            writer.newLine();
            writer.flush();
            writer.close();
            os.close();

            textfile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }*/

/*
//APPENDING DOES NOT WORK YET

        try {
            FileObject textfile = mgr.resolveFile("dbx:///test.txt", opts);
            System.out.println("test.txt exists:" + textfile.exists());


            OutputStream os = textfile.getContent().getOutputStream(true);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

            writer.write("append!");
            writer.newLine();
            writer.close();
            os.close();

            textfile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
 */
    }

}
