package com.indexing.lucene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

public class Indexer {
    private final Path documentsPath;
    private final Path indexPath;

    public Indexer(Path documentsPath, Path indexPath) {
        this.documentsPath = documentsPath;
        this.indexPath = indexPath;
    }

    public void indexDocuments() throws IOException {
        if (!Files.isDirectory(documentsPath)) {
            throw new IOException("Documents directory not found: " + documentsPath);
        }

        Files.createDirectories(indexPath);

        try (Analyzer analyzer = AnalyzerFactory.createAnalyzer();
             Directory directory = FSDirectory.open(indexPath)) {
            
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setCodec(new SimpleTextCodec());

            try (IndexWriter writer = new IndexWriter(directory, config);
                 Stream<Path> files = Files.walk(documentsPath)) {

                files.filter(file -> Files.isRegularFile(file))
                     .filter(file -> file.getFileName().toString().toLowerCase().endsWith(".txt"))
                     .forEach(file -> {
                        try {
                            Document document = new Document();

                            String fileName = file.getFileName().toString();
                            document.add(new TextField(IndexFields.FILE_NAME, fileName, Field.Store.YES));

                            String content = Files.readString(file);
                            document.add(new TextField(IndexFields.CONTENT, content, Field.Store.YES));

                            writer.addDocument(document);
                            System.out.println("Indexed file: " + fileName);
                        } catch (IOException e) {
                            System.err.println("Error while reading file \" + file + \": \" + e.getMessage()");
                        }
                     });
                System.out.println("Indexing completed successfully!");
                System.out.println("Indexed documents: " + writer.getDocStats().numDocs);
                writer.commit();
            }

        }
    }
}

