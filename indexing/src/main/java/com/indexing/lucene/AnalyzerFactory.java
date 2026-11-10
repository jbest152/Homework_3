package com.indexing.lucene;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerFactory {
    public static Analyzer createAnalyzer() {
        Analyzer standardAnalyzer = new StandardAnalyzer();
        Analyzer englishAnalyzer = new EnglishAnalyzer();
        //Analyzer italianAnalyzer = new ItalianAnalyzer();

        Map<String, Analyzer> byField = Map.of(
            IndexFields.FILE_NAME, standardAnalyzer,
            IndexFields.CONTENT, englishAnalyzer
        );

        return new PerFieldAnalyzerWrapper(standardAnalyzer, byField);
    }
}