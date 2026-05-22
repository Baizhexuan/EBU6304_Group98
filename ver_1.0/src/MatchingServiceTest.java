import java.util.Set;

/**
 * Regression checks for deterministic skill tokenisation and matching summaries.
 */
public class MatchingServiceTest {
    private MatchingServiceTest() {
    }

    /**
     * Runs matching-service checks.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        Set<String> tokens = MatchingService.tokenise("Java; OOP, Communication / Java | ");
        TestSupport.assertTrue(tokens.contains("java"),
                "Tokenisation should normalise skill case.");
        TestSupport.assertTrue(tokens.contains("oop"),
                "Tokenisation should split semicolon-delimited skills.");
        TestSupport.assertTrue(tokens.contains("communication"),
                "Tokenisation should split comma- and slash-delimited skills.");
        TestSupport.assertIntEquals(3, tokens.size(),
                "Tokenisation should remove duplicates and empty tokens.");

        TAProfile profile = new TAProfile();
        profile.skills = "Java; OOP";

        Job job = new Job();
        job.requiredSkills = "Java; OOP; Python";

        MatchResult result = MatchingService.evaluate(profile, job);
        TestSupport.assertIntEquals(67, result.score,
                "Two out of three required skills should round to a 67 percent match.");
        TestSupport.assertContains(result.summary, "Matched: java, oop",
                "Match summary should list matched skills.");
        TestSupport.assertContains(result.summary, "Missing: python",
                "Match summary should list missing skills.");

        MatchResult missingData = MatchingService.evaluate(null, job);
        TestSupport.assertIntEquals(0, missingData.score,
                "Missing profile data should produce a zero score.");

        System.out.println("MatchingServiceTest passed.");
    }
}
