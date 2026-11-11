package com.indexing.lucene;

import java.nio.file.Paths;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;

public class AnalyzerFactory {
    public static Analyzer createAnalyzer() {
        Analyzer standardAnalyzer = new StandardAnalyzer();
        Analyzer englishAnalyzer = new EnglishAnalyzer();
        Analyzer fileNameAnalyzer = null;
        try {
            fileNameAnalyzer = CustomAnalyzer.builder(Paths.get("src/main/resources"))    
                .withTokenizer(StandardTokenizerFactory.NAME)
                .addTokenFilter("wordDelimiterGraph")
                .addTokenFilter("lowercase")
                .addTokenFilter(StopFilterFactory.NAME, 
                    "ignoreCase", "false",
                    "words", "stopwords.txt",
                    "format", "wordset")
                .build();
        } catch (Exception e) {
            System.err.println("Error creating file name analyzer: " + e.getMessage());
            e.printStackTrace();
        }

        Map<String, Analyzer> byField = Map.of(
            IndexFields.FILE_NAME, fileNameAnalyzer,
            IndexFields.CONTENT, englishAnalyzer
        );

        return new PerFieldAnalyzerWrapper(standardAnalyzer, byField);
    }
}