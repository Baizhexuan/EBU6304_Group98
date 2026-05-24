import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Verifies that MO-side applicant CV export creates a usable PDF file.
 */
public class PdfExportServiceTest {
    public static void main(String[] args) throws Exception {
        TestSupport.withIsolatedData(new TestSupport.CheckedRunnable() {
            @Override
            public void run() throws Exception {
                FileStorage.initialise();

                Application application = firstApplication();
                User taUser = FileStorage.findUserById(application.taId);
                TAProfile profile = FileStorage.findProfileByUserId(application.taId);
                Job job = FileStorage.findJobById(application.jobId);
                MatchResult match = ScoringService.evaluate(profile, job);

                File exportFile = new File("data", "test_applicant_cv_export.pdf");
                Files.deleteIfExists(exportFile.toPath());
                boolean copiedOriginal = PdfExportService.exportApplicantCv(exportFile, taUser, profile,
                        application, job, match);

                TestSupport.assertTrue(exportFile.isFile(), "CV export should create a PDF file.");
                String header = new String(Files.readAllBytes(exportFile.toPath()), 0, 5, StandardCharsets.ISO_8859_1);
                TestSupport.assertEquals("%PDF-", header, "CV export should use the PDF file signature.");
                TestSupport.assertTrue(!copiedOriginal,
                        "Seed CV paths are placeholders, so the test should generate a profile summary PDF.");
                Files.deleteIfExists(exportFile.toPath());

                System.out.println("PdfExportServiceTest passed.");
            }
        });
    }

    private static Application firstApplication() {
        for (Application application : FileStorage.loadApplications()) {
            return application;
        }
        throw new IllegalStateException("No application found for PDF export test.");
    }
}
