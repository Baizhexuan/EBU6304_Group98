import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

/**
 * Creates a PDF copy or profile summary for an applicant CV download.
 */
public final class PdfExportService {
    private static final int PAGE_WIDTH = 612;
    private static final int PAGE_HEIGHT = 792;
    private static final int LEFT = 54;
    private static final int TOP = 742;
    private static final int LINE_HEIGHT = 15;
    private static final int IMAGE_SCALE = 2;

    private PdfExportService() {
    }

    public static boolean exportApplicantCv(File destination, User taUser, TAProfile profile, Application application,
            Job job, MatchResult currentMatch) throws IOException {
        if (destination == null || profile == null) {
            throw new IOException("Applicant profile data is missing.");
        }
        File target = ensurePdfExtension(destination);
        File source = resolveExistingPdf(profile.cvPath);
        if (source != null) {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        writeGeneratedCvSummary(target, taUser, profile, application, job, currentMatch);
        return false;
    }

    public static File ensurePdfExtension(File destination) {
        String path = destination.getPath();
        if (path.toLowerCase().endsWith(".pdf")) {
            return destination;
        }
        return new File(path + ".pdf");
    }

    private static File resolveExistingPdf(String cvPath) {
        if (ValidationUtils.isBlank(cvPath)) {
            return null;
        }
        File file = new File(cvPath.trim());
        if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
            return file;
        }
        return null;
    }

    private static void writeGeneratedCvSummary(File destination, User taUser, TAProfile profile,
            Application application, Job job, MatchResult currentMatch) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("TA CV / Applicant Profile Export");
        lines.add("Exported at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        lines.add("");
        lines.add("Applicant");
        lines.add("Name: " + safe(profile.fullName, taUser == null ? "" : taUser.getSafeDisplayName()));
        lines.add("Email: " + safe(profile.email, "N/A"));
        lines.add("Student ID: " + safe(profile.studentId, "N/A"));
        lines.add("GPA: " + profile.gpa);
        lines.add("Skills: " + safe(profile.skills, "N/A"));
        lines.add("Availability: " + safe(profile.availability, "N/A"));
        lines.add("Original CV path: " + safe(profile.cvPath, "N/A"));
        lines.add("");
        lines.add("Application");
        lines.add("Application ID: " + (application == null ? "N/A" : application.id));
        lines.add("Status: " + safe(application == null ? "" : application.status, "N/A"));
        lines.add("Applied at: " + safe(application == null ? "" : application.appliedAt, "N/A"));
        lines.add("Stored match at application time: "
                + (application == null ? "N/A" : application.matchScore + "%"));
        lines.add("Current match: " + (currentMatch == null ? "N/A" : currentMatch.score + "%"));
        lines.add("");
        lines.add("Job");
        lines.add("Title: " + safe(job == null ? "" : job.title, "N/A"));
        lines.add("Module: " + safe(job == null ? "" : job.module, "N/A"));
        lines.add("Required skills: " + safe(job == null ? "" : job.requiredSkills, "N/A"));
        lines.add("Hours: " + (job == null ? "N/A" : job.maxHours + "h"));
        lines.add("Location: " + safe(job == null ? "" : job.location, "N/A"));
        lines.add("");
        lines.add("Personal Statement");
        lines.add(safe(profile.statement, "N/A"));
        lines.add("");
        lines.add("Match Summary");
        lines.add(currentMatch == null ? safe(application == null ? "" : application.matchSummary, "N/A")
                : currentMatch.summary);

        writeSimplePdf(destination, lines);
    }

    private static void writeSimplePdf(File destination, List<String> rawLines) throws IOException {
        BufferedImage pageImage = renderSummaryPage(rawLines);
        byte[] imageBytes = compressedRgbBytes(pageImage);
        String drawImage = "q\n" + PAGE_WIDTH + " 0 0 " + PAGE_HEIGHT + " 0 0 cm\n/Im1 Do\nQ\n";
        byte[] contentBytes = drawImage.getBytes(StandardCharsets.US_ASCII);

        List<byte[]> objects = new ArrayList<byte[]>();
        objects.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        objects.add(bytes("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objects.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT
                + "] /Resources << /XObject << /Im1 4 0 R >> >> /Contents 5 0 R >>"));
        objects.add(concat(bytes("<< /Type /XObject /Subtype /Image /Width " + pageImage.getWidth()
                + " /Height " + pageImage.getHeight()
                + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode /Length "
                + imageBytes.length + " >>\nstream\n"), imageBytes, bytes("\nendstream")));
        objects.add(concat(bytes("<< /Length " + contentBytes.length + " >>\nstream\n"),
                contentBytes, bytes("endstream")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(bytes("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<Integer>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            out.write(bytes((i + 1) + " 0 obj\n"));
            out.write(objects.get(i));
            out.write(bytes("\nendobj\n"));
        }
        int xrefStart = out.size();
        out.write(bytes("xref\n0 " + (objects.size() + 1) + "\n"));
        out.write(bytes("0000000000 65535 f \n"));
        for (Integer offset : offsets) {
            out.write(bytes(String.format("%010d 00000 n \n", offset)));
        }
        out.write(bytes("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n"));
        out.write(bytes("startxref\n" + xrefStart + "\n%%EOF\n"));
        Files.write(destination.toPath(), out.toByteArray());
    }

    private static BufferedImage renderSummaryPage(List<String> rawLines) {
        int width = PAGE_WIDTH * IMAGE_SCALE;
        int height = PAGE_HEIGHT * IMAGE_SCALE;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(22, 43, 71));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font bodyFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10 * IMAGE_SCALE);
        Font headingFont = new Font(Font.SANS_SERIF, Font.BOLD, 13 * IMAGE_SCALE);
        int x = LEFT * IMAGE_SCALE;
        int y = (PAGE_HEIGHT - TOP) * IMAGE_SCALE;
        int maxWidth = (PAGE_WIDTH - LEFT * 2) * IMAGE_SCALE;
        int bottom = (PAGE_HEIGHT - 54) * IMAGE_SCALE;

        for (String raw : rawLines) {
            String text = raw == null ? "" : raw;
            boolean heading = isHeading(text);
            g.setFont(heading ? headingFont : bodyFont);
            FontMetrics metrics = g.getFontMetrics();
            if (text.isEmpty()) {
                y += LINE_HEIGHT * IMAGE_SCALE;
                continue;
            }
            for (String line : wrapForWidth(text, metrics, maxWidth)) {
                if (y + metrics.getAscent() > bottom) {
                    g.dispose();
                    return image;
                }
                y += metrics.getAscent();
                g.drawString(line, x, y);
                y += heading ? 5 * IMAGE_SCALE : 4 * IMAGE_SCALE;
            }
            y += heading ? 3 * IMAGE_SCALE : 0;
        }
        g.dispose();
        return image;
    }

    private static List<String> wrapForWidth(String text, FontMetrics metrics, int maxWidth) {
        List<String> lines = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            current.append(text.charAt(i));
            if (metrics.stringWidth(current.toString()) <= maxWidth) {
                continue;
            }
            int breakAt = lastWhitespace(current);
            if (breakAt <= 0) {
                current.deleteCharAt(current.length() - 1);
                lines.add(current.toString());
                current.setLength(0);
                current.append(text.charAt(i));
            } else {
                String line = current.substring(0, breakAt).trim();
                lines.add(line);
                String remainder = current.substring(breakAt).trim();
                current.setLength(0);
                current.append(remainder);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString().trim());
        }
        return lines;
    }

    private static int lastWhitespace(StringBuilder builder) {
        for (int i = builder.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(builder.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isHeading(String line) {
        return "TA CV / Applicant Profile Export".equals(line) || "Applicant".equals(line)
                || "Application".equals(line) || "Job".equals(line) || "Personal Statement".equals(line)
                || "Match Summary".equals(line);
    }

    private static byte[] compressedRgbBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream(image.getWidth() * image.getHeight() * 3);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                raw.write((rgb >> 16) & 0xff);
                raw.write((rgb >> 8) & 0xff);
                raw.write(rgb & 0xff);
            }
        }
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
            deflater.write(raw.toByteArray());
        }
        return compressed.toByteArray();
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] concat(byte[]... parts) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.write(part);
        }
        return out.toByteArray();
    }

    private static String safe(String value, String fallback) {
        return ValidationUtils.isBlank(value) ? fallback : value.trim();
    }
}
