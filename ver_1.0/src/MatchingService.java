import java.util.LinkedHashSet;
import java.util.Set;

public class MatchingService {
    public static MatchResult evaluate(TAProfile profile, Job job) {
        if (profile == null || job == null) {
            return new MatchResult(0, "Profile or job data is missing.");
        }

        Set<String> candidateSkills = tokenise(profile.skills);
        Set<String> requiredSkills = tokenise(job.requiredSkills);

        if (requiredSkills.isEmpty()) {
            return new MatchResult(100, "No mandatory skills listed.");
        }

        int matchedCount = 0;
        Set<String> matched = new LinkedHashSet<String>();
        Set<String> missing = new LinkedHashSet<String>();

        for (String required : requiredSkills) {
            if (candidateSkills.contains(required)) {
                matchedCount++;
                matched.add(required);
            } else {
                missing.add(required);
            }
        }

        int score = (int) Math.round(matchedCount * 100.0 / requiredSkills.size());
        StringBuilder builder = new StringBuilder();
        if (!matched.isEmpty()) {
            builder.append("Matched: ").append(String.join(", ", matched));
        }
        if (!missing.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append("Missing: ").append(String.join(", ", missing));
        }
        if (builder.length() == 0) {
            builder.append("No comparable skills found.");
        }
        return new MatchResult(score, builder.toString());
    }

    public static Set<String> tokenise(String rawSkills) {
        Set<String> skills = new LinkedHashSet<String>();
        if (rawSkills == null) {
            return skills;
        }

        String[] tokens = rawSkills.split("[;,/|]");
        for (String token : tokens) {
            String normalised = token.trim().toLowerCase();
            if (!normalised.isEmpty()) {
                skills.add(normalised);
            }
        }
        return skills;
    }
}
