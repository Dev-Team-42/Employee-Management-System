import java.io.Console;
import java.sql.*;

public class Login {

    private int empID;
    private String username, password;
    private String firstName, lastName, role;


    public Login(int empID, String username, String password, String firstName, String lastName, String role)  {
        this.empID = empID;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public int getEmpID() { return this.empID; }
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getRole() { return this.role; }

    public static Login loginMenu(String url, String DB_USER, String DB_PASS) {
        Console console = System.console();
        boolean Valid = false;

        while (!Valid) {
            String uUsername = console.readLine("Enter your username: ");
            String uPassword = console.readLine("Enter your password: ");
            

            // ArrayList<String> encrypted_passwords = HashGenerator.eryption(password);
            // String passwordHash = encrypted_passwords.get(0);
            // String passwordSalt = encrypted_passwords.get(1);

            
            String sqlcommand = """
            SELECT 'ADMIN' AS role, adminID AS empID, firstName, lastName
            FROM system_admins 
            WHERE username = ? AND password = ? 

            UNION

            SELECT 'EMPLOYEE' AS role, empID, firstName, lastName
            FROM employees 
            WHERE username = ? AND password = ? 
            """;

            try (Connection conn = DriverManager.getConnection(url, DB_USER, DB_PASS);
            PreparedStatement stmt = conn.prepareStatement(sqlcommand)) {

                // set parameters (6 total)
                stmt.setString(1, uUsername);
                stmt.setString(2, uPassword);
                //stmt.setString(2, passwordHash);
                // stmt.setString(3, passwordSalt);

                stmt.setString(3, uUsername);
                stmt.setString(4, uPassword);
                //stmt.setString(5, passwordHash);
                //stmt.setString(6, passwordSalt);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Valid = true;
                    String uRole = rs.getString(1);
                    int uEmpID = rs.getInt(2);
                    String fName = rs.getString(3);
                    String lName = rs.getString(4);

                    return new Login(uEmpID, uUsername, uPassword, fName, lName, uRole);
                    
                } else {
                    System.out.println("Invalid login, please try again.\n");
                }

            } catch (Exception e) {
                System.out.println("ERROR " + e.getMessage());
                return null;
            }
        }

        return null;
    }
}