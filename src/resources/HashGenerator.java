import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

public class HashGenerator {
    public static void main(String[] args) throws Exception {
      
    }

    public static ArrayList<String> Eryption(String password){
        
        try {
            if (password != null && password.isEmpty() == false) {
                // Generate Salt
                SecureRandom random = new SecureRandom();
                byte[] saltBytes = new byte[16];
                random.nextBytes(saltBytes);
                String salt = Base64.getEncoder().encodeToString(saltBytes);

                // Hash Password + Salt
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(Base64.getDecoder().decode(salt));
                byte[] hashedBytes = md.digest(password.getBytes());
                String hash = Base64.getEncoder().encodeToString(hashedBytes);

                ArrayList<String> passwordlist = new ArrayList<>();
                passwordlist.add(hash); // Store hash
                passwordlist.add(salt); // Store salt

                return passwordlist; // Return hash and salt together
            } else {
                System.out.println("ERROR: Please retry and enter a valid information");
                return null;
            }
        } catch (Exception e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
            return null;
        }
}
}