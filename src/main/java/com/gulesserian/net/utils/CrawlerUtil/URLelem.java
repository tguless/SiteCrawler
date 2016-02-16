package com.gulesserian.net.utils.CrawlerUtil;

//package crawler;

import java.io.*;
import java.util.*;
import java.net.*;


/**
 * Crawler.
 *
 * @author 
 * @version: $Revision:   1.2  $
 */

public class URLelem {
      public URL location;
      int depth;

      public URLelem (URL locationp, int depthp) {
        location = locationp;
        depth = depthp;
      }

 }