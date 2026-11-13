package com.indexing;

import com.indexing.lucene.IndexFields;
import com.indexing.lucene.QueryFactory;
import com.indexing.lucene.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Tester {

    public record TestCase(String name, Query query, Set<String> relevant) {}

    /** Risultato di una singola query. */
    public record Result(String name, int retrieved, int relevant,
                         double precision, double recall, double f1) {}

    private final Searcher searcher;

    public Tester(Searcher searcher) {
        this.searcher = searcher;
    }

    /** Testa una singola query. */
    public Result test(TestCase tc) throws IOException {
        Set<String> retrieved = searcher.searchFileNames(tc.query());
        return new Result(
                tc.name(),
                retrieved.size(),
                tc.relevant().size(),
                Evaluator.precision(retrieved, tc.relevant()),
                Evaluator.recall(retrieved, tc.relevant()),
                Evaluator.f1(retrieved, tc.relevant()));
    }

    /** Testa tutte le query, stampa il report e restituisce i risultati. */
    public List<Result> test(List<TestCase> cases) throws IOException {
        List<Result> results = new ArrayList<>();
        for (TestCase tc : cases) {
            results.add(test(tc));
        }
        printReport(results);
        return results;
    }

    private void printReport(List<Result> results) {
        System.out.printf("%-28s %6s %6s %9s %9s %9s%n",
                "Query", "Retr.", "Rel.", "Precision", "Recall", "F1");
        System.out.println("-".repeat(74));

        double sumP = 0, sumR = 0, sumF = 0;
        for (Result r : results) {
            System.out.printf("%-28s %6d %6d %9.3f %9.3f %9.3f%n",
                    r.name(), r.retrieved(), r.relevant(),
                    r.precision(), r.recall(), r.f1());
            sumP += r.precision();
            sumR += r.recall();
            sumF += r.f1();
        }

        int n = Math.max(1, results.size());
        System.out.println("-".repeat(74));
        System.out.printf("%-28s %6s %6s %9.3f %9.3f %9.3f%n",
                "MACRO AVERAGE", "", "", sumP / n, sumR / n, sumF / n);
    }

    // ---------------------------------------------------------------
    // Casi di test sul corpus generato (ground truth da SEARCH_TESTS.md)
    // ---------------------------------------------------------------

    public static List<TestCase> buildCases() {
        String c = IndexFields.CONTENT;
        String t = IndexFields.FILE_NAME;
        List<TestCase> cases = new ArrayList<>();

        cases.add(new TestCase("content: zephyrquartz",
                QueryFactory.termQuery(c, "zephyrquartz"),
                Set.of("recipe_vegetable_soup.txt",
                       "2025-02-10_git_branching.txt",
                       "meeting-design-review.txt")));

        cases.add(new TestCase("content: glimmerfjord",
                QueryFactory.termQuery(c, "glimmerfjord"),
                Set.of("travel-iceland-road-trip.txt",
                       "2025-08-18_frankenstein.txt")));

        cases.add(new TestCase("content: needleinhaystack",
                QueryFactory.termQuery(c, "needleinhaystack"),
                Set.of("very_long_document.txt")));

        cases.add(new TestCase("content: saturn",
                QueryFactory.termQuery(c, "saturn"),
                Set.of("mismatch_banana_bread.txt")));

        // "DOC-0042" produce 2 token con StandardAnalyzer: normalize() lancerebbe
        // eccezione, quindi si usa una phrase query con i due token separati.
        cases.add(new TestCase("content: \"DOC 0042\"",
                QueryFactory.phraseQuery(c, "doc", "0042"),
                Set.of("2026-08-17_rust_ownership.txt")));

        // Query che non deve trovare nulla (con la convenzione dell'Evaluator vale 1.0)
        cases.add(new TestCase("content: xyznotfound",
                QueryFactory.termQuery(c, "xyznotfound"),
                Set.of()));

                        // ---------- Term query ----------
        cases.add(new TestCase("content: lemon",
                QueryFactory.termQuery(c, "lemon"),
                Set.of("recipe_lemon_pasta.txt")));

        cases.add(new TestCase("content: telescope",
                QueryFactory.termQuery(c, "telescope"),
                Set.of("AstronomyCometTracking.txt", "AstronomyJupiterMoons.txt",
                       "AstronomyLunarCraters.txt", "AstronomyMilkyWay.txt",
                       "AstronomySolarEclipse.txt")));

        cases.add(new TestCase("content: nutmeg",
                QueryFactory.termQuery(c, "nutmeg"),
                Set.of("recipe_chicken_curry.txt", "recipe_chocolate_cake.txt",
                       "recipe_greek_salad.txt", "recipe_lemon_pasta.txt",
                       "recipe_mushroom_risotto.txt", "recipe_tomato_sauce.txt")));

        cases.add(new TestCase("content: camera",
                QueryFactory.termQuery(c, "camera"),
                Set.of("travel-bali-beaches.txt", "travel-iceland-road-trip.txt",
                       "travel-kyoto-temples.txt", "travel-morocco-markets.txt",
                       "travel-new-york-in-winter.txt", "travel-scottish-highlands.txt")));

        cases.add(new TestCase("content: gloves",
                QueryFactory.termQuery(c, "gloves"),
                Set.of("home-assemble-desk.txt", "home-fix-squeaky-door.txt",
                       "home-install-shelves.txt", "home-insulate-attic.txt",
                       "home-paint-walls.txt", "home-replace-light-switch.txt",
                       "home-seal-windows.txt", "home-unclog-drain.txt")));

        cases.add(new TestCase("content: patience",
                QueryFactory.termQuery(c, "patience"),
                Set.of("EmailContractRenewal.txt", "EmailEventInvitation.txt",
                       "EmailFeedbackRequest.txt", "EmailHolidaySchedule.txt",
                       "EmailPasswordReset.txt", "EmailThankYouNote.txt",
                       "EmailWelcomeOnboarding.txt", "Gardening - Fruit Trees.txt",
                       "Gardening - Herb Garden.txt", "Gardening - Pest Control.txt",
                       "Gardening - Succulent Care.txt", "Gardening - Tomato Growing.txt",
                       "Gardening - Winter Mulching.txt")));

        cases.add(new TestCase("content: budget",
                QueryFactory.termQuery(c, "budget"),
                Set.of("business_budget_forecast.txt", "business_customer_survey.txt",
                       "business_inventory_audit.txt", "business_marketing_plan.txt",
                       "business_product_roadmap.txt", "business_supplier_contract.txt",
                       "meeting-budget-meeting.txt", "travel-kyoto-temples.txt",
                       "travel-lisbon-weekend.txt", "travel-morocco-markets.txt",
                       "travel-patagonia-trekking.txt", "travel-prague-old-town.txt")));

        cases.add(new TestCase("content: unsubscribe",
                QueryFactory.termQuery(c, "unsubscribe"),
                Set.of("EmailEventInvitation.txt", "EmailFeedbackRequest.txt",
                       "EmailInvoiceReminder.txt", "EmailMeetingRequest.txt",
                       "EmailWelcomeOnboarding.txt")));

        // Parola accentata: funziona così solo se l'analyzer non fa ASCII folding
        cases.add(new TestCase("content: café",
                QueryFactory.termQuery(c, "café"),
                Set.of("unicode_café_résumé.txt")));

        cases.add(new TestCase("content: identical",
                QueryFactory.termQuery(c, "identical"),
                Set.of("duplicate_a.txt", "duplicate_b.txt")));

        // ---------- Phrase query ----------
        cases.add(new TestCase("content phrase: \"olive oil\"",
                QueryFactory.phraseQuery(c, "olive", "oil"),
                Set.of("recipe_apple_pie.txt", "recipe_banana_bread.txt",
                       "recipe_beef_stew.txt", "recipe_chocolate_cake.txt",
                       "recipe_greek_salad.txt", "recipe_lemon_pasta.txt",
                       "recipe_tomato_sauce.txt", "recipe_vegetable_soup.txt")));

        cases.add(new TestCase("content phrase: \"book club\"",
                QueryFactory.phraseQuery(c, "book", "club"),
                Set.of("2025-03-25_dracula.txt", "2025-07-06_the_hobbit.txt",
                       "2025-07-24_ulysses.txt", "2025-08-18_frankenstein.txt",
                       "2025-12-01_emma.txt", "2026-02-19_dune.txt",
                       "2026-08-13_walden.txt")));

        cases.add(new TestCase("content phrase: \"customer care\"",
                QueryFactory.phraseQuery(c, "customer", "care"),
                Set.of("EmailContractRenewal.txt", "EmailEventInvitation.txt",
                       "EmailFeedbackRequest.txt", "EmailHolidaySchedule.txt",
                       "EmailMeetingRequest.txt", "EmailPasswordReset.txt",
                       "EmailShippingDelay.txt", "EmailThankYouNote.txt",
                       "EmailWelcomeOnboarding.txt")));

        cases.add(new TestCase("content phrase: \"DOC 0007\"",
                QueryFactory.phraseQuery(c, "doc", "0007"),
                Set.of("recipe_mushroom_risotto.txt")));

        // ---------- Boolean query ----------
        cases.add(new TestCase("content bool: budget -meeting",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(c, "budget"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(c, "meeting"), BooleanClause.Occur.MUST_NOT)))),
                Set.of("business_budget_forecast.txt", "business_customer_survey.txt",
                       "business_inventory_audit.txt", "business_marketing_plan.txt",
                       "business_product_roadmap.txt", "business_supplier_contract.txt",
                       "travel-kyoto-temples.txt", "travel-lisbon-weekend.txt",
                       "travel-morocco-markets.txt", "travel-patagonia-trekking.txt",
                       "travel-prague-old-town.txt")));

        cases.add(new TestCase("content bool: +soil +patience",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(c, "soil"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(c, "patience"), BooleanClause.Occur.MUST)))),
                Set.of("Gardening - Fruit Trees.txt", "Gardening - Herb Garden.txt",
                       "Gardening - Succulent Care.txt", "Gardening - Tomato Growing.txt",
                       "Gardening - Winter Mulching.txt")));

        cases.add(new TestCase("content bool: +budget +data -travel",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(c, "budget"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(c, "data"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(c, "travel"), BooleanClause.Occur.MUST_NOT)))),
                Set.of("business_budget_forecast.txt", "business_customer_survey.txt",
                       "business_inventory_audit.txt", "business_marketing_plan.txt",
                       "business_product_roadmap.txt", "business_supplier_contract.txt")));

        cases.add(new TestCase("content bool: telescope OR nutmeg",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(c, "telescope"), BooleanClause.Occur.SHOULD)),
                        (new BooleanClause(QueryFactory.termQuery(c, "nutmeg"), BooleanClause.Occur.SHOULD)))),
                Set.of("AstronomyCometTracking.txt", "AstronomyJupiterMoons.txt",
                       "AstronomyLunarCraters.txt", "AstronomyMilkyWay.txt",
                       "AstronomySolarEclipse.txt", "recipe_chicken_curry.txt",
                       "recipe_chocolate_cake.txt", "recipe_greek_salad.txt",
                       "recipe_lemon_pasta.txt", "recipe_mushroom_risotto.txt",
                       "recipe_tomato_sauce.txt")));

        cases.add(new TestCase("content bool: lemon OR banana OR apple",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(c, "lemon"), BooleanClause.Occur.SHOULD)),
                        (new BooleanClause(QueryFactory.termQuery(c, "banana"), BooleanClause.Occur.SHOULD)),
                        (new BooleanClause(QueryFactory.termQuery(c, "apple"), BooleanClause.Occur.SHOULD)))),
                Set.of("recipe_lemon_pasta.txt", "recipe_banana_bread.txt",
                       "recipe_apple_pie.txt")));

        cases.add(new TestCase("title: pasta",
                QueryFactory.termQuery(t, "pasta"),
                Set.of("recipe_lemon_pasta.txt")));

        cases.add(new TestCase("title: budget",
                QueryFactory.termQuery(t, "budget"),
                Set.of("business_budget_forecast.txt", "meeting-budget-meeting.txt")));

        cases.add(new TestCase("title: meeting",
                QueryFactory.termQuery(t, "meeting"),
                Set.of("EmailMeetingRequest.txt", "meeting-board-meeting.txt",
                       "meeting-budget-meeting.txt")));

        cases.add(new TestCase("title: vegetable",
                QueryFactory.termQuery(t, "vegetable"),
                Set.of("Gardening - Vegetable Beds.txt", "recipe_vegetable_soup.txt")));

        cases.add(new TestCase("title: care",
                QueryFactory.termQuery(t, "care"),
                Set.of("Gardening - Lawn Care.txt", "Gardening - Succulent Care.txt")));

        cases.add(new TestCase("title: plan",
                QueryFactory.termQuery(t, "plan"),
                Set.of("business_marketing_plan.txt", "fitness_morning_run_plan.txt")));

        cases.add(new TestCase("title: tomato",
                QueryFactory.termQuery(t, "tomato"),
                Set.of("Gardening - Tomato Growing.txt", "recipe_tomato_sauce.txt")));

        cases.add(new TestCase("title: review",
                QueryFactory.termQuery(t, "review"),
                Set.of("meeting-design-review.txt")));

        cases.add(new TestCase("title: dune",
                QueryFactory.termQuery(t, "dune"),
                Set.of("2026-02-19_dune.txt")));

        // Il titolo di questo file parla di Saturno: il nome file no
        cases.add(new TestCase("title: saturn",
                QueryFactory.termQuery(t, "saturn"),
                Set.of("mismatch_banana_bread.txt")));

        // Dipende dall'analyzer: senza ASCII folding il termine accentato funziona
        cases.add(new TestCase("title: café",
                QueryFactory.termQuery(t, "café"),
                Set.of("unicode_café_résumé.txt")));

        // ---------- Phrase query sul titolo ----------
        cases.add(new TestCase("title phrase: \"black holes\"",
                QueryFactory.phraseQuery(t, "black", "holes"),
                Set.of("AstronomyBlackHoles.txt")));

        cases.add(new TestCase("title phrase: \"unit testing\"",
                QueryFactory.phraseQuery(t, "unit", "testing"),
                Set.of("2025-11-04_unit_testing.txt")));

        cases.add(new TestCase("title phrase: \"weekly standup\"",
                QueryFactory.phraseQuery(t, "weekly", "standup"),
                Set.of("meeting-weekly-standup.txt")));

        cases.add(new TestCase("title phrase: \"herb garden\"",
                QueryFactory.phraseQuery(t, "herb", "garden"),
                Set.of("Gardening - Herb Garden.txt")));

        cases.add(new TestCase("title phrase: \"sprint planning\"",
                QueryFactory.phraseQuery(t, "sprint", "planning"),
                Set.of("meeting-sprint-planning.txt")));

        // ---------- Boolean sul titolo ----------
        cases.add(new TestCase("title bool: +vegetable -soup",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(t, "vegetable"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(t, "soup"), BooleanClause.Occur.MUST_NOT)))),
                Set.of("Gardening - Vegetable Beds.txt")));

        cases.add(new TestCase("title bool: +care +succulent",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(t, "care"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(t, "succulent"), BooleanClause.Occur.MUST)))),
                Set.of("Gardening - Succulent Care.txt")));

        cases.add(new TestCase("title bool: budget OR review",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(t, "budget"), BooleanClause.Occur.SHOULD)),
                        (new BooleanClause(QueryFactory.termQuery(t, "review"), BooleanClause.Occur.SHOULD)))),
                Set.of("business_budget_forecast.txt", "meeting-budget-meeting.txt",
                       "meeting-design-review.txt")));

        cases.add(new TestCase("title bool: +meeting -budget",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(t, "meeting"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(t, "budget"), BooleanClause.Occur.MUST_NOT)))),
                Set.of("EmailMeetingRequest.txt", "meeting-board-meeting.txt")));

        // ---------- Titolo + contenuto insieme ----------
        cases.add(new TestCase("+title:pasta +content:nutmeg",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(t, "pasta"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(c, "nutmeg"), BooleanClause.Occur.MUST)))),
                Set.of("recipe_lemon_pasta.txt")));

        cases.add(new TestCase("+title:budget +content:risks",
                QueryFactory.booleanQuery(List.of(
                        (new BooleanClause(QueryFactory.termQuery(t, "budget"), BooleanClause.Occur.MUST)),
                        (new BooleanClause(QueryFactory.termQuery(c, "risks"), BooleanClause.Occur.MUST)))),
                Set.of("business_budget_forecast.txt")));

        return cases;
    }

    public static void main(String[] args) throws IOException {
        Searcher searcher = new Searcher(Path.of("index"));
        new Tester(searcher).test(buildCases());
    }
}