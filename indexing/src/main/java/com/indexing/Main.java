package com.indexing;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.indexing.lucene.Indexer;

public class Main {
    public static void main(String[] args) {
        Path documentsPath = Paths.get("documents");
        Path indexPath = Paths.get("index");

        System.out.println(documentsPath);
        System.err.println(indexPath);
        Indexer indexer = new Indexer(documentsPath, indexPath);

        try {
            indexer.indexDocuments();
        } catch (Exception e) {
            System.err.println("Indexing error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}