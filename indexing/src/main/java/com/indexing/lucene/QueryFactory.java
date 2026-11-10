package com.indexing.lucene;

import java.util.List;

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
        return new TermQuery(new Term(field, value));
    }

    public static Query phraseQuery(String field, String... terms) {

        PhraseQuery.Builder builder = new PhraseQuery.Builder();

        for (int i = 0; i < terms.length; i++) {
            builder.add(new Term(field, terms[i]), i);
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
}
