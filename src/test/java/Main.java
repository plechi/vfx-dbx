package test;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;


public class Main
{
	static ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
	static long count = 0;
	static FileObject file = null;
	static long lastmodified = 0;
	
	public static void main(String[] args)
	{
		Runnable command = new Runnable()
		{
			
			
			public void run()
			{
				//System.out.println("running");
				if (count == 0)
					try
					{
						file = VFS.getManager().resolveFile("dbx://cmd.txt");
						System.out.println("resolved "+file);
						if (file != null && file.exists())
							lastmodified = file.getContent().getLastModifiedTime();
						System.out.println(lastmodified);
					}
					catch (FileSystemException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				count++;
				try
				{
					file = VFS.getManager().resolveFile("dbx://cmd.txt");
					if (file != null && file.exists() )
					{
						 long l = file.getContent().getLastModifiedTime();
						 System.out.println(l);
						 if (lastmodified < l)
					{
					    lastmodified = l;
						InputStream in = file.getContent().getInputStream();
						System.out.println("found");
						Scanner s = new Scanner(in);
						while (s.hasNext()) {
			                System.out.println(s.next());
						}
			            s.close();
					}
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("terminated");
				
			}
		};
		executor.scheduleWithFixedDelay(command, 0, 1, TimeUnit.SECONDS);
	}

}
