package com.gulesserian.net.utils.CrawlerUtil;

//package crawler;

/**
 * Crawler.
 *
 * @author 
 * @version: $Revision:   1.1  $
 */



public interface Worker
{
	/**
	 * Method invoked to request worker to perform task
	 * @param data Data passed to the worker.
	 * @return void
	 */
	public void run(Object data);
}
