import java.io.Console;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class HashGenerator {
    public static void main(String[] args) throws Exception {
        
        Console console = System.console();

        if (console != null){
            String plainPassword = console.readLine("Enter your password for encryption: ");
            
            // Generate Salt
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);

            // Hash Password + Salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = md.digest(plainPassword.getBytes());
            String hash = Base64.getEncoder().encodeToString(hashedBytes);

            System.out.println("Password: " + plainPassword);
            System.out.println("Salt: " + salt);
            System.out.println("Hash: " + hash);

        } else {
            System.out.println("No Console");
        }
    }
}