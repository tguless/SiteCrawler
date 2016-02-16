package com.gulesserian.net.utils.CrawlerUtil;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Crawler.
 *
 * @author
 * @version: $Revision: 1.13 $
 */

public class Downloader {
	// Destination directory
	public static String destination = null;

	// URL of the first page
	private URL firstPageURL = null;

	// Host Name.
	public static String hostName = null;

	// pool size
	public static int poolSize;

	// max depth to parse
	public static int depth;

	// Files of Interest
	public static Vector fileVect = new Vector(20);

	// URLs in File
	public static Vector fileExc = new Vector(20);

	Properties p = new Properties(System.getProperties());

	public static Hashtable AllURLs = new Hashtable(50, 0.5F);

	public static Stack PendingURLs = new Stack();

	// Handler threads
	private Pool _pool = null;

	public static String readFile;

	public static String depthControl;

	public Downloader(URL u, String dest) {
		firstPageURL = u;
		hostName = u.getHost();
		destination = dest;
		Init();
	}

	public void download() {
		URLelem fPageURL;
		Integer depth = new Integer(0);
		fPageURL = new URLelem(firstPageURL, 0);
		try {
			Boolean b = new Boolean(true);
			Downloader.AllURLs.put(firstPageURL.toString(), depth);
			Downloader.PendingURLs.push(fPageURL);

			for (int i = 0; i < poolSize; i++) {
				_pool.performWork(b);
				Thread.sleep(10000);
			}
			if (_pool.Processed())
				System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Init() {
		String FileExc = "LinkFile.txt";
		try {
			FileInputStream propFile = new FileInputStream(
					"crawlerProperties.txt");
			p.load(propFile);
			System.setProperties(p);

			poolSize = Integer.parseInt(System.getProperty("pool.size"));
			depth = Integer.parseInt(System.getProperty("depth.limit"));
			readFile = System.getProperty("read.exceptionfile");
			depthControl = System.getProperty("depth.control");
			System.err.println("depth::" + depth);
			System.err.println("poolSize::" + poolSize);
			_pool = new Pool(poolSize, DownloadFiles.class);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalError(e.getMessage());
		}
		String FilesofInterest[] = { ".htm", ".html", ".txt", ".jpg", ".jpeg",
				".gif", ".java", ".class", ".jar", ".js", ".css" };
		for (int i = 0, n = FilesofInterest.length; i < n; i++)
			fileVect.addElement(new String(FilesofInterest[i]));

		if (readFile.equals("true"))
			readMyFile(FileExc);
	}

	public static boolean FileofInterest(String filename) {
		for (int i = 0; i < fileVect.size(); i++)
			if (filename.toLowerCase().endsWith((String) fileVect.elementAt(i)))
				return true;
		return false;
	}

	public void readMyFile(String Filename) {
		String input;
		try {
			FileInputStream fis = new FileInputStream(Filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			try {
				while ((input = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(input);
					while (st.hasMoreTokens()) {
						fileExc.addElement(st.nextToken());
					}
				}
				br.close();
				fis.close();
			} catch (IOException e) {
				System.out.println(e);
			}
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}

	}

}
