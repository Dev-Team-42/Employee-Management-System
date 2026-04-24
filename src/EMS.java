import java.sql.*;
import java.util.ArrayList;

public class EMS {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/ems";
        String user = "root";
        String password = "password";
        
        ArrayList<Employee> employees = new ArrayList<>();
		Reports("employment", url, user, password, employees);
    }
    
    public static void Reports(String reportName, String url, String user, String password, ArrayList<Employee> employees) {
        
        if(reportName.toLowerCase().equals("employment")) {
	        String sqlcommand = "SELECT empID, firstName, lastName, email, hireDate "+ 
	        					"FROM employees ORDER BY hireDate; ";
        
	        try (Connection myConn = DriverManager.getConnection(url, user, password)) {
	            Statement myStmt = myConn.createStatement();
	            ResultSet myRS = myStmt.executeQuery(sqlcommand);
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
	            myConn.close();
	        } catch (Exception e) {
	            System.out.println("ERROR " + e.getLocalizedMessage());
	        } finally {
	        }

            PrintEmployees(employees);

        } else {
            System.out.println("\n\n****ERROR**** Invalid report requested\n\n");
        }
    }
    
    public static void PrintEmployees(ArrayList<Employee> myEmployees) {
		
		//**  change title to your name 
        System.out.println("\n\n\nCurrent Employees Report at Company Z\n");
        System.out.println("Hire Date\tName\t\tID\tEmail");
        for(Employee e:myEmployees) {
            System.out.println(e.getHireDate()+"\t"+ e.getFname()+" "+e.getLname()+"\t"+e.getEmpID()+"\t"+e.getEmail());
        }
    }
}