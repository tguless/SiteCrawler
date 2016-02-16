package com.gulesserian.net.utils.CrawlerUtil;

import java.net.*;
import java.util.*;

/**
 * Crawler.
 *
 * @author
 * @version: $Revision:   1.7  $
 */


class URLlist
{
	private URL baseURL = null;
	private Vector listOfURLs = null;
   private int curdepth = 0;
	Object data;
	String orginal_server = Downloader.hostName;
	String replace_server = (System.getProperty("replace.server"));
	String change_server = (System.getProperty("replace.hostserver"));

	URLlist(URL u, Vector v, int cd)
	{
		listOfURLs = v;
		baseURL = u;
      curdepth = cd;
	}

	public void addURLs()
	{
		int sizeOfVec = listOfURLs.size();
		for(int i = 0; i < sizeOfVec; i++)
		{
			String href = (String)listOfURLs.elementAt(i);
         /*if ((href.indexOf("FF.html"))>0)
				continue;*/

			try
			{
				if(href != "")
				{
					if(href.startsWith("http://"))
					{
						ExtendedURL eURL = new ExtendedURL(new URL(href));
						addToList(eURL.getURL());

					}
					else if(href.indexOf(':') != -1)
					{
						// do nothing
					}
					else
					{
						ExtendedURL eURL = new ExtendedURL(baseURL, href);
						addToList(eURL.getURL());
					}
				}
			}
			catch(MalformedURLException e)
			{
				System.err.println("Could not generate URL using " + baseURL + " and " + href);
				System.err.println(e.getMessage());

			}
			catch(Exception e)
			{
				System.err.println("Exp in addURLs");
				e.printStackTrace();
			}

		}

		return;
	}

	/*
	 * Add the URL to the Vector.
	 * It checks if the URL is already present in the list and adds
	 * only if the URL is not already present.
	 */
	public synchronized  void addToList(URL u)
	{

      Integer urldepth;

		if(u == null)
			return;

		if(Downloader.hostName.equals(u.getHost()))
      {
			if(change_server.equals("true"))
         {
            String newu = null;
    			newu = DownloadFiles.sandrep(orginal_server, replace_server, u.toString());
    			try{
    			  u = new URL(newu);
    			}
    			catch(MalformedURLException e)
    		   {}
		   }
	      URLelem nextURL = new URLelem(u,curdepth + 1);
			Integer depth = new Integer(curdepth);

         if (Downloader.AllURLs.containsKey(u.toString()))
			{
			  	urldepth = (Integer)Downloader.AllURLs.get(u.toString());
			  	if (urldepth.intValue() > curdepth)
			  		Downloader.AllURLs.put(u.toString(), (new Integer(curdepth)));
        	}
        	else {
        	   int indx;
            if ((indx=u.toString().indexOf("//images"))>0)
               return;
        	   if ((indx=u.toString().indexOf("//index"))>0)
        	      return;
        		Downloader.AllURLs.put(u.toString(), (new Integer(curdepth + 1)));
			  	Downloader.PendingURLs.push(nextURL);
		   }
		}
	}

}
