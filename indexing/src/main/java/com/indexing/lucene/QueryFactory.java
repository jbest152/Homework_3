package com.indexing.lucene;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
//import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class QueryFactory {

    // modifiers:
    // + :- required, - :- optional, "" :- phrase query
    public static Query parseQuery(String field, String searchText) {
        try {
            return new QueryParser(field, AnalyzerFactory.createAnalyzer()).parse(searchText);
        } catch (Exception e) {
            System.err.println("Error creating query: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /* 
    public static Query allDocsQuery(String field) {
        return MatchAllDocsQuery.INSTANCE;
    }
    */

    public static Query termQuery(String field, String value) {
        return new TermQuery(new Term(field, normalize(field, value)));
    }

    public static Query phraseQuery(String field, String... terms) {

        PhraseQuery.Builder builder = new PhraseQuery.Builder();

        for (int i = 0; i < terms.length; i++) {
            builder.add(new Term(field, normalize(field, terms[i])), i);
        }

        return builder.build();
    }

    // clauses: MUST, MUST_NOT, SHOULD, FILTER
    public static Query booleanQuery(List<BooleanClause> clauses) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (BooleanClause clause : clauses) {
            builder.add(clause);
        }

        return builder.build();
    }

    public static String normalize(String field, String value) {
        try (TokenStream tokenStream = AnalyzerFactory.createAnalyzer().tokenStream(field, value)) {
            CharTermAttribute attribute =
                tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            if (!tokenStream.incrementToken()) {
                return null;
            }
            String normalizedValue = attribute.toString();
            if (tokenStream.incrementToken()) {
                throw new IllegalArgumentException(
                    "Value produces multiple tokens. Cannot normalize."
                );
            }
            tokenStream.end();
            return normalizedValue;
        } catch (IOException e) {
            throw new RuntimeException("Normalization error", e);
        }
    }
}
