import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypts and decrypts CSV data stored on disk.
 *
 * <p>The password field still uses one-way hashing in {@link PasswordService}.
 * This service protects the rest of the CSV file contents at rest with AES-GCM,
 * while keeping FileStorage's public load/save APIs unchanged.</p>
 */
public final class DataCryptoService {
    private static final String PREFIX = "ENCv1:";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final String KEY_ENV = "TA_DATA_ENCRYPTION_KEY";
    private static final String KEY_PROPERTY = "ta.data.encryption.key";
    private static final String DEFAULT_KEY_MATERIAL = "BUPT-QMUL-Group98-TA-Recruitment-System-Data-Key-v1";
    private static final SecureRandom RANDOM = new SecureRandom();

    private DataCryptoService() {
    }

    public static boolean isEncrypted(String storedText) {
        return storedText != null && storedText.startsWith(PREFIX);
    }

    public static String encrypt(String plainText) throws IOException {
        if (plainText == null) {
            plainText = "";
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] stored = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, stored, 0, iv.length);
            System.arraycopy(cipherText, 0, stored, iv.length, cipherText.length);
            return PREFIX + Base64.getEncoder().encodeToString(stored);
        } catch (Exception ex) {
            throw new IOException("Unable to encrypt CSV data", ex);
        }
    }

    public static String decryptIfNeeded(String storedText) throws IOException {
        if (!isEncrypted(storedText)) {
            return storedText;
        }
        try {
            byte[] stored = Base64.getDecoder().decode(storedText.substring(PREFIX.length()).trim());
            if (stored.length <= IV_BYTES) {
                throw new IOException("Encrypted CSV payload is too short.");
            }
            byte[] iv = Arrays.copyOfRange(stored, 0, IV_BYTES);
            byte[] cipherText = Arrays.copyOfRange(stored, IV_BYTES, stored.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Unable to decrypt CSV data. Check the data encryption key.", ex);
        }
    }

    private static SecretKeySpec keySpec() throws Exception {
        String material = System.getenv(KEY_ENV);
        if (ValidationUtils.isBlank(material)) {
            material = System.getProperty(KEY_PROPERTY);
        }
        if (ValidationUtils.isBlank(material)) {
            material = DEFAULT_KEY_MATERIAL;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = Arrays.copyOf(digest.digest(material.getBytes(StandardCharsets.UTF_8)), 16);
        return new SecretKeySpec(key, "AES");
    }
}
