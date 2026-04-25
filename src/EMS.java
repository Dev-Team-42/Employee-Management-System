import java.io.Console;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class EMS {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/ems";
        String user = "root";
        String password = "VIPGSUpass2005#";

        String username = "username";
        String hash = "hash";
        String salt = "salt";

        ArrayList<Employee> employees = new ArrayList<>();
		Reports("employment", url, user, password, employees);
        Login(url, user, password, username, hash, salt);
    }
    
    public static void Login(String url, String user, String password, String username, String hash, String salt) {
        Console console = System.console();
        String u_username = console.readLine("Enter your username: ");
        String user_password = console.readLine("Enter your password: ");

        ArrayList<String> encrypted_passwords = HashGenerator.Eryption(user_password);
        String passwordHash = encrypted_passwords.get(0);
        String passwordSalt = encrypted_passwords.get(1);

        String sqlcommand = """
        SELECT 'ADMIN' 
        FROM system_admins 
        WHERE username = ? AND passwordHash = ?

        UNION

        SELECT 'EMPLOYEE' 
        FROM employees 
        WHERE username = ? AND passwordHash = ?
        """;


        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(sqlcommand)) {

                // set parameters (6 total)
                stmt.setString(1, username);
                stmt.setString(2, hash);
                stmt.setString(3, salt);

                stmt.setString(4, username);
                stmt.setString(5, hash);
                stmt.setString(6, salt);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String role = rs.getString(1);

                    if (role.equals("ADMIN")) {
                        System.out.println("Admin login");
                    } else {
                        System.out.println("Employee login");
                    }
                } else {
                    System.out.println("Invalid login");
                }

            } catch (Exception e) {
                System.out.println("ERROR " + e.getMessage());
            }
        }
    

    public static void Reports(String reportName, String url, String user, String password, ArrayList<Employee> employees) {
        
        if(reportName.toLowerCase().equals("employment")) {
            Console console = System.console();
	        String sqlcommand = "SELECT empID, firstName, lastName, email, hireDate "+ 
	        					"FROM employees ORDER BY hireDate; ";
        
	        try (Connection myConn2 = DriverManager.getConnection(url, user, password)) {
	            Statement mymyStmt = myConn2.createStatement();
	            ResultSet myRS = mymyStmt.executeQuery(sqlcommand);
                if (!myRS.next()) {
                    System.out.println("No Employees");
                    return;
                } else {
                    do {
                        Employee temp = new Employee();
                        temp.setEmpID(myRS.getInt(1));
                        temp.setFname(myRS.getString(2));
                        temp.setLname(myRS.getString(3));
                        temp.setEmail(myRS.getString(4));
                        temp.setHireDate(myRS.getString(5));
                        employees.add(temp);
                    } while( myRS.next());
                }
	            myConn2.close();
	        } catch (Exception e) {
	            System.out.println("ERROR " + e.getLocalizedMessage());
	        } finally {
	        }

        String idInput = console.readLine("Enter Employee ID (leave blank to skip): ");
        // String idInput = scanner.nextLine();
        Integer sID = null; 
        if (!idInput.trim().isEmpty()) {
            try {
                sID = Integer.parseInt(idInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format. Please enter a numeric value.");
            }
        }

        String sSSN = console.readLine("Enter SSN (leave blank to skip): ");
        // String sSSN = scanner.nextLine();
        if (sSSN.trim().isEmpty()) {
            sSSN = null;
        }

        String sDOB = console.readLine("Enter DOB (leave blank to skip): ");
        // String sDOB = scanner.nextLine();
        if (sDOB.trim().isEmpty()) {
            sDOB = null;
        }

        Employee foundEmp = EmpDataAccess.EmpSearch(sID, sSSN, sDOB, url, user, password);
        
        if (foundEmp != null) {
            System.out.println("\n--- Employee Record Found ---");
            System.out.println("ID:        " + foundEmp.getEmpID());
            System.out.println("Name:      " + foundEmp.getFname() + " " + foundEmp.getLname());
            System.out.println("Email:     " + foundEmp.getEmail());
            System.out.println("Hire Date: " + foundEmp.getHireDate());
            System.out.println("SSN:       " + foundEmp.getSSN());
            System.out.println("DOB:       " + foundEmp.getDOB()); 
        } else {
            System.out.println("\nNo employee found");
        }

        // scanner.close();
    };
    
    public static void PrintEmployees(ArrayList<Employee> myEmployees) {

        System.out.println("\n\n\nCurrent Employees Report at Company Z\n");
        System.out.println("ID\tName\t\tEmail\t\tHire Date");
        for(Employee e:myEmployees) {
            System.out.println(e.getEmpID()+"\t"+ e.getFname()+" "+e.getLname()+"\t"+e.getEmail()+"\t"+e.getHireDate());
        }
    }
}
}
