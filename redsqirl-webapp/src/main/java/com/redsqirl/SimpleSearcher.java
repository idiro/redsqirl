/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SimpleSearcher {
	
	private static Logger logger = Logger.getLogger(SimpleSearcher.class);
	
	public static void main(String[] args) throws Exception {
		
		String path = System.getProperty("user.home");
		File indexDir = new File("/usr/share/redsqirl/users/igor/lucene/index");
		String query = "oozie";
		int hits = 100;
		SimpleSearcher searcher = new SimpleSearcher();
		searcher.searchIndex(indexDir, query, hits);
		
	}
	
	public List<String> searchIndex(File indexDir, String queryStr, int maxHits) throws Exception {
		
		Directory directory = FSDirectory.open(indexDir);

		IndexSearcher searcher = new IndexSearcher(directory);
		QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new SimpleAnalyzer());
		Query query = parser.parse(queryStr);
		
		TopDocs topDocs = searcher.search(query, maxHits);
		
		List<String> list = new ArrayList<String>();
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			logger.info(d.get("filename"));
			list.add(d.get("filename"));
		}
		
		logger.info("Found " + hits.length);
		return list;
	}

}