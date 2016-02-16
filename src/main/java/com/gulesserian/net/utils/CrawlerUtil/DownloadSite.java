package com.gulesserian.net.utils.CrawlerUtil;

import java.net.*;
import java.io.*;
import java.util.Date;

/**
 * Crawler.
 *
 * @author
 * @version: $Revision:   1.4  $
 */



public class DownloadSite
{

	public static void main(String [] args) throws IOException
	{

		String dest = null;
		URL u = null;
		File f = null;
		switch(args.length)
		{
			case 0:
				System.err.println("Usage: java DownloadSite site_URL [location]");
				System.exit(-1);
				break;
			case 1:
				dest = System.getProperty("user.dir");
				break;
			case 2:
				f = new File(args[1]);
				dest = f.getAbsolutePath();
				break;
			default:
				System.err.println("Usage: java DownloadSite site_URL [location]");
				System.exit(-1);
				break;
		}

		try
		{
			u = new URL(args[0]);
		}
		catch(MalformedURLException e)
		{
			System.err.println(e.getMessage());
			System.err.println("Malformed URL. Check the URL provided.");
		}
		Date today = new Date();
		System.setErr(new PrintStream(new FileOutputStream("logfile" + ".txt"), true));
		long starttime = System.currentTimeMillis();
    	System.err.println("starttime::" + starttime);

		// Start work after initialization
		Downloader d = new Downloader(u, dest);
		d.download();

	}
}
