public class SystemSmokeTest {
    public static void main(String[] args) {
        FileStorage.initialise();

        if (FileStorage.loadUsers().size() < 5) {
            throw new IllegalStateException("Expected seeded demo users.");
        }
        if (FileStorage.loadJobs().isEmpty()) {
            throw new IllegalStateException("Expected seeded jobs.");
        }

        TAProfile profile = FileStorage.findProfileByUserId(2);
        Job job = FileStorage.findJobById(1);
        MatchResult result = MatchingService.evaluate(profile, job);

        if (result.score <= 0) {
            throw new IllegalStateException("Expected a positive match score.");
        }

        System.out.println("Smoke test passed.");
        System.out.println("Users: " + FileStorage.loadUsers().size());
        System.out.println("Jobs: " + FileStorage.loadJobs().size());
        System.out.println("Applications: " + FileStorage.loadApplications().size());
        System.out.println("Sample match: " + result.score + "% - " + result.summary);
    }
}
