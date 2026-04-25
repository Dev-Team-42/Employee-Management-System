import java.util.ArrayList;
import java.util.Scanner;

public class EMS {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/ems";
        String user = "root";
        String password = "password";
        Scanner scanner = new Scanner(System.in);

        PrintEmployees(EmpDataAccess.CurrentEmployees(url, user, password));

        System.out.println("--- Employee Search ---");

        System.out.print("Enter Employee ID (leave blank to skip): ");
        String idInput = scanner.nextLine();
        Integer sID = null; 
        if (!idInput.trim().isEmpty()) {
            try {
                sID = Integer.parseInt(idInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format. Please enter a numeric value.");
            }
        }

        System.out.print("Enter SSN (leave blank to skip): ");
        String sSSN = scanner.nextLine();
        if (sSSN.trim().isEmpty()) {
            sSSN = null;
        }

        System.out.print("Enter DOB (leave blank to skip): ");
        String sDOB = scanner.nextLine();
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

        scanner.close();
    };
    
    public static void PrintEmployees(ArrayList<Employee> myEmployees) {

        System.out.println("\n\n\nCurrent Employees Report at Company Z\n");
        System.out.println("ID\tName\t\tEmail\t\tHire Date");
        for(Employee e:myEmployees) {
            System.out.println(e.getEmpID()+"\t"+ e.getFname()+" "+e.getLname()+"\t"+e.getEmail()+"\t"+e.getHireDate());
        }
    }
}
