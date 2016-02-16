package com.gulesserian.net.utils.CrawlerUtil;

import org.apache.oro.text.regex.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Crawler.
 *
 * @author
 * @version: $Revision:   1.23  $
 */

/**
 * Implements the Worker interface
 */
public class DownloadFiles implements Worker {
	// Hold the URL of the page that is currently being downloaded
	private URL currentURL = null;

	int curr_depth;

	String change_basehref = (System.getProperty("change.basehref"));

	String regex = "HREF\\s*?=\\s*?\"(\\S+)\"|HREF=\"(\\S+)\"|SRC=\"(\\S+)\"|SRC\\s*?=\\s*?\"(\\S+)\"|archive=(\\S+)|src=\'(\\S+)\'|HREF=\'(\\S+)\'|src\\s*?=\\s*?\'(\\S+)\'|HREF\\s*?=\\s*?\'(\\S+)\'|code=(\\S+)|src=(\\S+)|HREF=(\\S+)|background=\"(\\S+)\"|background=\'(\\S+)\'";

	Perl5Matcher matcher = null;
	Perl5Compiler compiler = null;
	Perl5Pattern pattern = null;
	MatchResult result = null;

	/**
	 * Invoked by the Pool when a job comes in for the Worker
	 * 
	 * @param data
	 *            Worker data
	 * @return void
	 */
	public void init() {
		return;
	}

	public void run(Object data) {
		int counter = 0;
		compiler = new Perl5Compiler();
		matcher = new Perl5Matcher();
		URLelem URLs = null;
		// Attempt to compile the pattern. If the pattern is not valid,
		// report the error and exit.
		try {
			pattern = (Perl5Pattern) compiler.compile(regex,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
		} catch (MalformedPatternException e) {
			System.err.println("Bad pattern.");
			System.err.println(e.getMessage());
			System.exit(1);
		}

		try {
			while (true) {
				URLs = GetURLFromStack();
				if (null != URLs) {
					URL u = URLs.location;
					curr_depth = URLs.depth;
					Process(u, curr_depth);
				} else {
					if ((counter++) == 3) {
						return;
					} else {
						Thread.sleep(90000);
						continue;
					}
				}
			}
		} catch (Exception e) {
			System.err.println("GetURLFromStack.");
			// e.printStackTrace();
		}
	}

	/*
	 * Process the link and create dir structure
	 */

	public void Process(URL u, int depthlimit) {
		currentURL = u;

		try {
			InputStream in = null;
			try {
				in = u.openStream();
			} catch (IOException e) {
				System.err.println("Unable to download : " + u);
				System.err.println(e.getMessage());
				// e.printStackTrace();
			}
			// Setting the output stream with the help of ExtendedURL class
			ExtendedURL eURL = new ExtendedURL(u);
			String currdir = null;
			if (null != eURL.getDirectory()) {
				currdir = eURL.getDirectory();
				int indx1;
				if ((indx1 = currdir.indexOf("www")) > 0)
					return;
				if ((indx1 = currdir.indexOf(":")) > 0)
					return;
				if ((indx1 = currdir.indexOf(">")) > 0)
					return;
				if ((indx1 = currdir.indexOf(".")) > 0)
					return;
			} else {
				currdir = "/";
			}
			File ffile = null;
			String fileName = eURL.getFile();
			// int indx;
			File fdir = null;
			fdir = new File(Downloader.destination + currdir);
			if (!fdir.exists())
				fdir.mkdirs();
			// if the file name is null, set it to index.html
			if (fileName == null) {
				ffile = new File(fdir, "index.html");
				fileName = "index.html";
			} else
				ffile = new File(fdir, fileName);

			OutputStream out = null;

			if ((curr_depth <= Downloader.depth)
					|| (curr_depth == Downloader.depth + 1)) {
				if (Downloader.depthControl.equals("true")) {
					if ((curr_depth == Downloader.depth + 1)) {
						if (ffile.exists()) {
							return;
						} else {
							curr_depth = curr_depth - 1;
						}
					}
				}

				try {
					if (!ffile.isDirectory())
						out = new FileOutputStream(ffile);
				} catch (FileNotFoundException e) {
					System.err.println("Unable to create file : "
							+ ffile.getPath());
					System.err.println(e.getMessage());
				} catch (IOException e) {
					System.err.println("IO exp in Process");
					System.err.println(e.getMessage());
					// e.printStackTrace();
				}

				if (nonHtmlfile(fileName)) {
					downloadFile(in, out, fileName, "", "nonhtml", curr_depth);
					return;
				} else {
					downloadFile(in, out, fileName, currdir, "html", curr_depth);
					return;
				}
			} else {
				return;
			}
		} catch (Exception e) {
			System.err.println("exp in Process");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void downloadFile(InputStream in, OutputStream out,
			String fileName, String currdir, String isHtml, int curr_depth) {
		int nbytes = 0; // number of bytes read
		byte[] buffer = new byte[4096]; // buffer to hold the data
		byte[] bufferf = new byte[4096];

		if (in == null || out == null)
			return;
		String strInput = null;
		StringBuffer strFileBuffer = new StringBuffer("");
		String tmpstr1 = null;

		try {
			if (isHtml.equals("html")) {
				while ((nbytes = in.read(buffer)) != -1) {
					strInput = new String(buffer, 0, nbytes);
					strFileBuffer.append(strInput);
					strInput = null;
					buffer = null;
					buffer = new byte[4096];
				}
				if (change_basehref.equals("true")) {
					tmpstr1 = sandrep("<base href", "<base href",
							strFileBuffer.toString());
					bufferf = tmpstr1.getBytes();
					nbytes = bufferf.length;

					out.write(bufferf, 0, nbytes);
					ParseString(tmpstr1, currdir, curr_depth);
					// System.gc();
				} else {
					tmpstr1 = strFileBuffer.toString();
					bufferf = tmpstr1.getBytes();
					nbytes = bufferf.length;

					out.write(bufferf, 0, nbytes);
					ParseString(tmpstr1, currdir, curr_depth);
					// System.gc();
				}
			} else {
				while ((nbytes = in.read(buffer)) != -1)
					out.write(buffer, 0, nbytes);
			}
		} catch (IOException e) {
			System.err.println("IO exp in DownloadFile");
			System.err.println(e.getMessage());
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				buffer = null;
				bufferf = null;
				strFileBuffer = null;
			} catch (IOException e) {
				System.err.println("IO exp in closing stream");
				System.err.println(e.getMessage());
			}
		}
	}

	private void ParseString(String strtoParse, String currdir, int curr_depth) {
		PatternMatcherInput input = null;

		try {
			Vector v = new Vector(20);
			input = new PatternMatcherInput(strtoParse);

			while (matcher.contains(input, pattern)) {
				result = matcher.getMatch();
				String strLink = ProcessResult(result, currdir);
				if (null != strLink)
					v.addElement(strLink);
			}
			AddURLstoGlobalList(v, curr_depth);
		} catch (Exception e) {
			System.err.println("exp in Parse String");
			System.err.println(e.getMessage());
			// e.printStackTrace();
		}
	}

	/*
	 * This method takes file name as the argument and returns false if the file
	 * is a html file) and true otherwise.
	 */
	private boolean nonHtmlfile(String file) {
		if (file == null)
			return true;
		String lcFile = file.toLowerCase();
		if (lcFile.endsWith(".htm") || lcFile.endsWith(".html"))
			return false;
		return true;
	}

	private void AddURLstoGlobalList(Vector v, int curr_depth) {
		URLlist ulist = null;
		ulist = new URLlist(currentURL, v, curr_depth);
		ulist.addURLs();
		return;
	}

	private String ProcessLink(String rawLink, String dir) {
		int indx1 = 0;
		int indx2 = 0;
		String strProcessedLink = null;
		String currProtocol = new String(currentURL.getProtocol());
		int currentPort = currentURL.getPort();
		String tmpStr = null;
		String tmpDir = null;

		if (rawLink == null)
			return null;
		if (rawLink.startsWith("//"))
			return null;
		indx1 = rawLink.indexOf("ftp");
		if (indx1 >= 0)
			return null;
		indx1 = rawLink.indexOf("|");
		if (indx1 >= 0)
			return null;
		indx1 = rawLink.indexOf("=");
		if (indx1 >= 0)
			return null;
		indx1 = rawLink.toLowerCase().indexOf("javascript");
		if (indx1 >= 0)
			return null;
		indx1 = rawLink.indexOf("http:");
		indx2 = rawLink.indexOf(Downloader.hostName);

		if ((indx1 >= 0) & (indx2 < 0)) {
			return null;
		} else if (indx1 >= 0) {
			if ((indx1 = rawLink.indexOf('#')) > 0) {
				tmpStr = new String(rawLink.substring(0, indx1));
				return tmpStr;
			}

			if ((indx1 = rawLink.indexOf('?')) > 0) {
				tmpStr = new String(rawLink.substring(0, indx1));
				return tmpStr;
			}

			if (Downloader.FileofInterest(rawLink))
				return rawLink;
			if (!rawLink.endsWith("/"))
				return rawLink + "/";
			else
				return rawLink;

		}

		if (rawLink.indexOf('/') >= 0)
			tmpDir = "";
		else
			tmpDir = dir + "/";

		if (Downloader.FileofInterest(rawLink)) {
			tmpStr = new String(currProtocol + "://" + Downloader.hostName
					+ tmpDir + rawLink);
			return tmpStr;
		}

		if ((indx1 = rawLink.indexOf('#')) > 0) {
			tmpStr = new String(currProtocol + "://" + Downloader.hostName
					+ tmpDir + rawLink.substring(0, indx1));
			return tmpStr;
		}
		if ((indx1 = rawLink.indexOf('?')) > 0)
			tmpStr = new String(currProtocol + "://" + Downloader.hostName
					+ tmpDir + rawLink.substring(0, indx1));
		indx2 = rawLink.length();

		if ((!rawLink.endsWith("/")) & (indx1 = rawLink.lastIndexOf('/')) > 0) {
			if (rawLink.substring(indx1, indx2).indexOf(".") < 0) {
				tmpStr = new String(currProtocol + "://" + Downloader.hostName
						+ tmpDir + rawLink + "/");
			}
		}
		if ((rawLink.startsWith("/")) & (rawLink.endsWith("/"))) {
			tmpStr = new String(currProtocol + "://" + Downloader.hostName
					+ tmpDir + rawLink);
		}
		return tmpStr;
	}

	private String ProcessResult(MatchResult result, String currDir) {
		String strProcessedLink = null;
		String a = null;
		String b = null;
		int indx;
		for (int i = 1; i <= 14; i++) {
			if (null != result.group(i)) {
				a = result.group(i).replace('\"', ' ').replace('\'', ' ')
						.trim();
				if ((indx = a.indexOf(">")) > 0)
					b = a.substring(0, (indx - 1));
				else
					b = a;
				if ((indx = b.indexOf(" ")) > 0)
					b = b.substring(0, (indx));
				strProcessedLink = ProcessLink(b, currDir);
			}
		}
		return strProcessedLink;
	}

	public synchronized URLelem GetURLFromStack() {
		URLelem URLs = null;
		URL u = null;
		int count = 0;
		try {
			URLs = (URLelem) Downloader.PendingURLs.pop();
			u = (URL) URLs.location;
			count = Downloader.PendingURLs.size();
		} catch (EmptyStackException e) {
			System.err.println("Empty Stack exp");
			System.err.println(e.getMessage());
			// e.printStackTrace();;
		}
		return URLs;
	}

	public static String sandrep(String regularExpression, String sub,
			String input) {
		int limit, interps;
		PatternMatcher matcher = new Perl5Matcher();
		Pattern pattern = null;
		PatternCompiler compiler = new Perl5Compiler();
		String result;

		limit = Util.SUBSTITUTE_ALL;
		// interps = Util.INTERPOLATE_ALL;

		try {
			pattern = compiler.compile(regularExpression);
		} catch (MalformedPatternException e) {
			System.err.println("Bad pattern.");
			System.err.println(e.getMessage());
			System.exit(1);
		}

		result = Util.substitute(matcher, pattern, new Perl5Substitution(sub), input, limit);

		if (Downloader.readFile.equals("true")) {
			for (int i = 0; i < Downloader.fileExc.size(); i++) {
				try {
					pattern = compiler.compile(Downloader.fileExc.elementAt(i)
							.toString());
				} catch (MalformedPatternException e) {
					System.err.println("Bad pattern.");
					System.err.println(e.getMessage());
					System.exit(1);
				}
				result = Util.substitute(matcher, pattern, new Perl5Substitution(Downloader.fileExc
						.elementAt(i + 1).toString()), result, limit);
				i++;
			}
		}
		return result;
	}

}
