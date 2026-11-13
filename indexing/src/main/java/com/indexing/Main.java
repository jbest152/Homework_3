package com.indexing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import com.indexing.lucene.IndexFields;
import com.indexing.lucene.Indexer;
import com.indexing.lucene.QueryFactory;
import com.indexing.lucene.Searcher;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        Path documentsPath = Paths.get("documents");
        Path indexPath = Paths.get("index");

        Indexer indexer = new Indexer(documentsPath, indexPath);

        try {
            long start = System.nanoTime();
            indexer.indexDocuments();
            long end = System.nanoTime();
            System.out.println("Indexing completed in " + (end - start) / 1_000_000 + " ms.");
            Searcher searcher = new Searcher(indexPath);
            runSearchMenu(searcher);

        } catch (Exception e) {
            System.err.println("Indexing error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runSearchMenu(Searcher searcher) {

        boolean running = true;

        while (running) {

            System.out.println("\n========== SEARCH MENU ==========");
            System.out.println("1. QueryParser");
            System.out.println("2. TermQuery");
            System.out.println("3. PhraseQuery");
            System.out.println("4. BooleanQuery");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1" -> executeQueryParser(searcher);
                    case "2" -> executeTermQuery(searcher);
                    case "3" -> executePhraseQuery(searcher);
                    case "4" -> executeBooleanQuery(searcher);
                    case "0" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.err.println("Search error: " + e.getMessage());
            }
        }
    }

    private static String chooseField() {
        System.out.println("\nChoose the field:");
        System.out.println("1. Filename");
        System.out.println("2. Content");
        System.out.print("Choice: ");

        String choice = scanner.nextLine();
        return switch (choice) {
            case "1" -> IndexFields.FILE_NAME;
            case "2" -> IndexFields.CONTENT;
            default -> throw new IllegalArgumentException(
                "Invalid field selection."
            );
        };
    }

    private static void executeQueryParser(Searcher searcher) throws Exception {
        String field = chooseField();
        System.out.print("\nEnter your query: ");
        String text = scanner.nextLine();
        Query query = QueryFactory.parseQuery(field, text);

        searcher.search(query);  
    }

    private static void executeTermQuery(Searcher searcher) throws Exception {
        String field = chooseField();
        System.out.print("Enter the term: ");
        String term = scanner.nextLine();
        Query query = QueryFactory.termQuery(field, term);

        searcher.search(query);
    }

    private static void executePhraseQuery(Searcher searcher) throws Exception {
        String field = chooseField();
        System.out.print("Enter the phrase: ");
        String phrase = scanner.nextLine();
        Query query = QueryFactory.phraseQuery(field, phrase.trim().split("\\s+"));

        searcher.search(query);
    }

    private static void executeBooleanQuery(Searcher searcher) throws Exception {
        List<BooleanClause> clauses = new ArrayList<>();

        boolean addingClauses = true;

        while (addingClauses) {
            System.out.println("\nChoose query type:");
            System.out.println("1. QueryParser");
            System.out.println("2. TermQuery");
            System.out.println("3. PhraseQuery");
            System.out.print("Choice: ");

            String queryType = scanner.nextLine();
            String field = chooseField();

            Query query;

            switch (queryType) {
                case "1" -> {
                    System.out.print("Enter your query: ");
                    String text = scanner.nextLine();

                    query = QueryFactory.parseQuery(field, text);
                }
                case "2" -> {
                    System.out.print("Enter the term: ");
                    String term = scanner.nextLine();
                    query = QueryFactory.termQuery(field, term);
                }
                case "3" -> {
                    System.out.print("Enter the phrase: ");
                    String phrase = scanner.nextLine();
                    query = QueryFactory.phraseQuery(field, phrase.trim().split("\\s+"));
                }
                default -> {
                    System.out.println("Invalid query type.");
                    continue;
                }
            }

            System.out.println("\nChoose BooleanClause.Occur:");
            System.out.println("1. MUST");
            System.out.println("2. MUST_NOT");
            System.out.println("3. SHOULD");
            System.out.println("4. FILTER");
            System.out.print("Choice: ");

            String occurChoice = scanner.nextLine();

            BooleanClause.Occur occur = switch (occurChoice) {
                case "1" -> BooleanClause.Occur.MUST;
                case "2" -> BooleanClause.Occur.MUST_NOT;
                case "3" -> BooleanClause.Occur.SHOULD;
                case "4" -> BooleanClause.Occur.FILTER;
                default -> null;
            };

            if (occur == null) {
                System.out.println("Invalid clause.");
                continue;
            }

            clauses.add(new BooleanClause(query, occur));
            System.out.print("Add another clause? (y/n): ");
            addingClauses = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        if (clauses.isEmpty()) {
            System.out.println("No clauses added.");
            return;
        }

        Query query = QueryFactory.booleanQuery(clauses);
        searcher.search(query);
    }
}