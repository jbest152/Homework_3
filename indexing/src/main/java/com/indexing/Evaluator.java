package com.indexing;
import java.util.Set;

public class Evaluator {

    public static double recall(Set<String> retrievedDocs, Set<String> relevantDocs) {
        if (relevantDocs.isEmpty()) {
            return 0.0;
        }

        long relevantRetrieved = retrievedDocs.stream()
                .filter(relevantDocs::contains)
                .count();

        return (double) relevantRetrieved / relevantDocs.size();
    }

    public static double precision(Set<String> retrievedDocs, Set<String> relevantDocs) {

        if (retrievedDocs.isEmpty()) {
            return 0.0;
        }

        long relevantRetrieved = retrievedDocs.stream()
                .filter(relevantDocs::contains)
                .count();

        return (double) relevantRetrieved / retrievedDocs.size();
    }

    public static double f1(Set<String> retrievedDocs, Set<String> relevantDocs) {
        double p = precision(retrievedDocs, relevantDocs);
        double r = recall(retrievedDocs, relevantDocs);

        if (p + r == 0) {
            return 0.0;
        }

        return 2 * p * r / (p + r);
    }
}
