package com.redsqirl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class SimpleFileIndexer {
	
	private static Logger logger = Logger.getLogger(SimpleFileIndexer.class);

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();
		String path = System.getProperty("user.home");
		File indexDir = new File(path+"/igor/index");
		File dataDir = new File(path+"/igor/pages");
		String suffix = "html";
		SimpleFileIndexer indexer = new SimpleFileIndexer();
		int numIndex = indexer.index(indexDir, dataDir, suffix);
		System.out.println("Total files indexed " + numIndex);
		System.out.println((System.currentTimeMillis() - start));
		
	}

	public int index(File indexDir, File dataDir, String suffix) throws Exception {

		IndexWriter indexWriter = new IndexWriter(
				FSDirectory.open(indexDir), 
				new SimpleAnalyzer(),
				true,
				IndexWriter.MaxFieldLength.LIMITED);
		indexWriter.setUseCompoundFile(false);

		indexDirectory(indexWriter, dataDir, suffix);

		int numIndexed = indexWriter.maxDoc();
		indexWriter.optimize();
		indexWriter.close();

		return numIndexed;

	}

	public void indexDirectory(IndexWriter indexWriter, File dataDir, String suffix) throws IOException {
		File[] files = dataDir.listFiles();
		if(files != null){
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				if (f.isDirectory()) {
					indexDirectory(indexWriter, f, suffix);
				}
				else {
					indexFileWithIndexWriter(indexWriter, f, suffix);
				}
			}
		}
	}

	public void indexFileWithIndexWriter(IndexWriter indexWriter, File f, String suffix) throws IOException {
		if (f.isHidden() || f.isDirectory() || !f.canRead() || !f.exists()) {
			return;
		}
		if (suffix!=null && !f.getName().endsWith(suffix)) {
			return;
		}
		logger.info("Indexing file " + f.getCanonicalPath());

		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));		
		doc.add(new Field("filename", f.getCanonicalPath(), Field.Store.YES, Field.Index.ANALYZED));

		indexWriter.addDocument(doc);
	}

	public void merge(String indexPath, String pathA, String pathB) throws CorruptIndexException, LockObtainFailedException, IOException{
		File dir = new File(indexPath);
		SimpleFSDirectory d = new SimpleFSDirectory(dir);
		IndexWriter writer = new IndexWriter(d,new StandardAnalyzer(Version.LUCENE_CURRENT),IndexWriter.MaxFieldLength.LIMITED);

		File INDEXES_DIR = new File(pathA);
		Directory indexes[] = new Directory[2];
		indexes[0] = FSDirectory.open(new File(pathA));
		indexes[1] = FSDirectory.open(new File(pathB));
		
		/*
		for (int i = 0; i < INDEXES_DIR.list().length; i++) {
			System.out.println("Adding: " + INDEXES_DIR.list()[i]);
			File fil = new File(pathB+"/"+INDEXES_DIR.list()[i]);
		}
		indexes[i] = FSDirectory.open(pathB);
		 */
		
		logger.info(" Merging added indexes ");
		writer.addIndexesNoOptimize(indexes);
		logger.info(" Optimizing index ");
		writer.optimize();
		writer.commit();
		writer.close();
	}

}