package com.indexing.lucene;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
    
    private final Path indexPath;

    public Searcher(Path indexPath) {
        this.indexPath = indexPath;
    }

    public void search(Query query) throws IOException {

        try (Analyzer analyzer = AnalyzerFactory.createAnalyzer();
             Directory directory = FSDirectory.open(indexPath);
             DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs results = searcher.search(query, 10);

            System.out.println("\nSearch results:");
            System.out.println("Query: " + query);
            System.out.println("Documents found: " + results.totalHits.value);

            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document document = searcher.storedFields()
                        .document(scoreDoc.doc);

                System.out.println("\n-------------------------");
                System.out.println("Filename: " + document.get(IndexFields.FILE_NAME));
                System.out.println("Content: " + document.get(IndexFields.CONTENT));
                System.out.println("Score: " + scoreDoc.score);
            }
        }
    }
}
