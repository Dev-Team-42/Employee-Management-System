import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

public class HashGenerator {

    public static void main(String[] args) throws Exception {
    }

    public static ArrayList<String> eryption(String password) {
        try {
            if (password != null && !password.isEmpty()) {
                SecureRandom random = new SecureRandom();
                byte[] saltBytes = new byte[16];
                random.nextBytes(saltBytes);
                String salt = Base64.getEncoder().encodeToString(saltBytes);

                String hash = hashWithSalt(password, salt);

                ArrayList<String> passwordlist = new ArrayList<>();
                passwordlist.add(hash);
                passwordlist.add(salt);
                return passwordlist;
            } else {
                System.out.println("ERROR: Please retry and enter valid information");
                return null;
            }
        } catch (Exception e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
            return null;
        }
    }

    public static String hashWithSalt(String password, String salt) {
        try {
            if (password == null || password.isEmpty() || salt == null || salt.isEmpty()) {
                return null;
            }
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
            return null;
        }
    }

    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }
}