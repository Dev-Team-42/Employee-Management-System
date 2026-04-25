import java.util.ArrayList;

public class EMS {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/ems";
        String user = "root";
        String password = "password";

        PrintCurrentEmployees(EmpDataAccess.CurrentEmployees(url, user, password));
    }
    
    public static void PrintCurrentEmployees(ArrayList<Employee> myEmployees) {

        System.out.println("\n\n\nCurrent Employees Report at Company Z\n");
        System.out.println("Hire Date\tName\t\tID\tEmail");
        for(Employee e:myEmployees) {
            System.out.println(e.getHireDate()+"\t"+ e.getFname()+" "+e.getLname()+"\t"+e.getEmpID()+"\t"+e.getEmail());
        }
    }
}