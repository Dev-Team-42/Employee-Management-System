import java.io.Console;
import java.sql.*;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;

public class EMS {

    private static final String URL = "jdbc:mysql://localhost:3306/ems";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Narwhals22??";

    public static void main(String[] args) {
        LoginResult login = login();
        if (login == null) return;

        if (login.role.equals("ADMIN")) {
            adminMenu();
        } else {
            employeeMenu(login.empID);
        }
    }

    // ============================================================
    // LOGIN
    // ============================================================

    public static class LoginResult {
        public final String role;
        public final Integer empID;  // null for admin
        LoginResult(String role, Integer empID) {
            this.role = role;
            this.empID = empID;
        }
    }

    public static LoginResult login() {
        Console console = System.console();
        if (console == null) {
            System.out.println("ERROR: No console available. Run from a terminal.");
            return null;
        }

        String uUsername = console.readLine("Enter your username: ");
        char[] pwChars = console.readPassword("Enter your password: ");
        String uPassword = (pwChars != null) ? new String(pwChars) : "";

        // DEV BYPASS — remove before submitting
if (uUsername.equals("admin") && uPassword.equals("admin")) {
    System.out.println("Admin login successful (dev mode)");
    return new LoginResult("ADMIN", null);
}
if (uUsername.equals("employee") && uPassword.equals("employee")) {
    System.out.println("Employee login successful (dev mode)");
    return new LoginResult("EMPLOYEE", 1);  // ← change 1 to a real empID from your DB
}

        String sql =
            "SELECT 'ADMIN' AS role, NULL AS empID, passwordHash, passwordSalt " +
            "FROM system_admins WHERE username = ? " +
            "UNION " +
            "SELECT 'EMPLOYEE' AS role, empID, passwordHash, passwordSalt " +
            "FROM employees WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uUsername);
            stmt.setString(2, uUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    Integer empID = rs.getObject("empID") != null ? rs.getInt("empID") : null;
                    String storedHash = rs.getString("passwordHash");
                    String storedSalt = rs.getString("passwordSalt");

                    String computedHash = HashGenerator.hashWithSalt(uPassword, storedSalt);
                    if (computedHash != null
                            && HashGenerator.constantTimeEquals(computedHash, storedHash)) {
                        System.out.println(role + " login successful");
                        return new LoginResult(role, empID);
                    }
                }
                System.out.println("Invalid login");
                return null;
            }
        } catch (Exception e) {
            System.out.println("ERROR " + e.getMessage());
            return null;
        }
    }

    // ============================================================
    // ADMIN MENU
    // ============================================================

    public static void adminMenu() {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Search for employee data");
            System.out.println("2. Reports");
            System.out.println("3. Update salaries by range");
            System.out.println("4. Logout");
            String choice = prompt("Choose an option: ");

            switch (choice) {
                case "1": searchEmployeeMenu();   break;
                case "2": adminReportsMenu();     break;
                case "3": salaryRaiseMenu();      break;
                case "4": System.out.println("Goodbye."); return;
                default:  System.out.println("Invalid choice.");
            }
        }
    }

    public static void searchEmployeeMenu() {
        Employee found = searchEmployeeFlow();
        if (found == null) {
            System.out.println("\nNo employee found.");
            return;
        }

        printEmployeeDetail(found);

        String yn = prompt("\nUpdate this employee? (y/n): ");
        if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes")) {
            updateEmployeeMenu(found);
        }
    }

    /**
     * Shared search flow used by both admin (for editing) and employee
     * (for viewing). Returns the matched Employee or null.
     */
    private static Employee searchEmployeeFlow() {
        System.out.println("\n--- Search Employee ---");
        System.out.println("1. By name");
        System.out.println("2. By DOB");
        System.out.println("3. By SSN");
        System.out.println("4. By Employee ID");
        System.out.println("5. Back");
        String choice = prompt("Choose an option: ");

        switch (choice) {
            case "1": {
                String fn = prompt("First name: ");
                String ln = prompt("Last name: ");
                return EmpDataAccess.EmpSearchByName(fn, ln, URL, DB_USER, DB_PASS);
            }
            case "2": {
                String dob = prompt("DOB (YYYY-MM-DD): ");
                return EmpDataAccess.EmpSearch(null, null, dob, URL, DB_USER, DB_PASS);
            }
            case "3": {
                String ssn = prompt("SSN: ");
                return EmpDataAccess.EmpSearchBySSN(ssn, URL, DB_USER, DB_PASS);
            }
            case "4": {
                Integer id = parseIntOrNull(prompt("Employee ID: "));
                if (id == null) {
                    System.out.println("Invalid ID.");
                    return null;
                }
                return EmpDataAccess.EmpSearchByID(id, URL, DB_USER, DB_PASS);
            }
            case "5": return null;
            default:
                System.out.println("Invalid choice.");
                return null;
        }
    }

    public static void updateEmployeeMenu(Employee emp) {
        while (true) {
            System.out.println("\n--- Update Employee #" + emp.getEmpID() + " ---");
            System.out.println("1. First name   (" + emp.getFname()    + ")");
            System.out.println("2. Last name    (" + emp.getLname()    + ")");
            System.out.println("3. Email        (" + emp.getEmail()    + ")");
            System.out.println("4. Role         (" + emp.getRole()     + ")");
            System.out.println("5. Address ID   (" + emp.getAddressID()+ ")");
            System.out.println("6. SSN          (" + emp.getSSN()      + ")");
            System.out.println("7. DOB          (" + emp.getDOB()      + ")");
            System.out.println("8. Hire date    (" + emp.getHireDate() + ")");
            System.out.println("9. Done");
            String choice = prompt("What do you want to update? ");

            String field = null, label = null;
            switch (choice) {
                case "1": field = "firstName"; label = "first name"; break;
                case "2": field = "lastName";  label = "last name";  break;
                case "3": field = "email";     label = "email";      break;
                case "4": field = "role";      label = "role";       break;
                case "5": field = "addressID"; label = "address ID"; break;
                case "6": field = "ssn";       label = "SSN";        break;
                case "7": field = "DOB";       label = "DOB";        break;
                case "8": field = "hireDate";  label = "hire date";  break;
                case "9": return;
                default:
                    System.out.println("Invalid choice.");
                    continue;
            }

            String newVal = prompt("New " + label + ": ");
            boolean ok = EmpDataAccess.updateField(
                    emp.getEmpID(), field, newVal, URL, DB_USER, DB_PASS);

            if (ok) {
                System.out.println("Updated.");
                applyToObject(emp, field, newVal);
            } else {
                System.out.println("Update failed.");
            }
        }
    }

    private static void applyToObject(Employee emp, String field, String newVal) {
        try {
            switch (field) {
                case "firstName": emp.setFname(newVal); break;
                case "lastName":  emp.setLname(newVal); break;
                case "email":     emp.setEmail(newVal); break;
                case "role":      emp.setRole(newVal);  break;
                case "addressID": emp.setAddressID(Integer.parseInt(newVal)); break;
                case "ssn":       emp.setSSN(newVal);   break;
                case "DOB":       emp.setDOB(newVal);   break;
                case "hireDate":  emp.setHireDate(newVal); break;
            }
        } catch (NumberFormatException e) {
            // local copy may be stale, DB is correct
        }
    }

    // ============================================================
    // SALARY RAISE
    // ============================================================

    public static void salaryRaiseMenu() {
        System.out.println("\n--- Salary Raise by Range ---");

        Double min = parseDoubleOrNull(prompt("Minimum salary: "));
        Double max = parseDoubleOrNull(prompt("Maximum salary: "));
        Double pct = parseDoubleOrNull(prompt("Percentage raise (e.g. 5 for 5%): "));

        if (min == null || max == null || pct == null) {
            System.out.println("Invalid number entered.");
            return;
        }
        if (max < min) {
            System.out.println("Max must be greater than or equal to min.");
            return;
        }

        int affected = PayrollDataAccess.countEmployeesInRange(min, max, URL, DB_USER, DB_PASS);
        if (affected == 0) {
            System.out.println("No employees fall in that salary range.");
            return;
        }

        String confirm = prompt(
            String.format("This will raise %d employee(s) by %.2f%%. Confirm? (y/n): ",
                          affected, pct));
        if (!(confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes"))) {
            System.out.println("Cancelled.");
            return;
        }

        PayrollDataAccess.RaiseResult result =
            PayrollDataAccess.raiseSalariesInRange(min, max, pct, URL, DB_USER, DB_PASS);

        if (result.success) {
            System.out.printf("Done. %d payroll row(s) updated for %d employee(s).%n",
                              result.rowsUpdated, result.employeesAffected);
        } else {
            System.out.println("Raise failed: " + result.errorMessage);
        }
    }

    // ============================================================
    // ADMIN REPORTS
    // ============================================================

    public static void adminReportsMenu() {
        while (true) {
            System.out.println("\n--- Reports ---");
            System.out.println("1. Employment report (all employees)");
            System.out.println("2. Total pay by job title (for a month)");
            System.out.println("3. Total pay by division (for a month)");
            System.out.println("4. New hires within a date range");
            System.out.println("5. Back");
            String choice = prompt("Choose an option: ");

            switch (choice) {
                case "1": printEmploymentReport();    break;
                case "2": payByJobTitleReport();      break;
                case "3": payByDivisionReport();      break;
                case "4": newHiresReport();           break;
                case "5": return;
                default:  System.out.println("Invalid choice.");
            }
        }
    }

    public static void printEmploymentReport() {
        ArrayList<Employee> employees = EmpDataAccess.CurrentEmployees(URL, DB_USER, DB_PASS);
        printEmployees(employees);
    }

    public static void payByJobTitleReport() {
        String ym = prompt("Enter year-month (YYYY-MM): ");
        String[] range = monthRange(ym);
        if (range == null) {
            System.out.println("Invalid year-month format.");
            return;
        }

        Map<String, Double> totals = PayrollDataAccess.totalPayByJobTitle(
                range[0], range[1], URL, DB_USER, DB_PASS);

        System.out.println("\nTotal Pay by Job Title — " + ym);
        System.out.println("Job Title\t\tTotal Pay");
        System.out.println("-----------------------------------");
        if (totals.isEmpty()) {
            System.out.println("(no data)");
            return;
        }
        for (Map.Entry<String, Double> e : totals.entrySet()) {
            System.out.printf("%-20s\t$%,.2f%n", e.getKey(), e.getValue());
        }
    }

    public static void payByDivisionReport() {
        String ym = prompt("Enter year-month (YYYY-MM): ");
        String[] range = monthRange(ym);
        if (range == null) {
            System.out.println("Invalid year-month format.");
            return;
        }

        Map<String, Double> totals = PayrollDataAccess.totalPayByDivision(
                range[0], range[1], URL, DB_USER, DB_PASS);

        System.out.println("\nTotal Pay by Division — " + ym);
        System.out.println("Division\t\tTotal Pay");
        System.out.println("-----------------------------------");
        if (totals.isEmpty()) {
            System.out.println("(no data)");
            return;
        }
        for (Map.Entry<String, Double> e : totals.entrySet()) {
            System.out.printf("%-20s\t$%,.2f%n", e.getKey(), e.getValue());
        }
    }

    public static void newHiresReport() {
        String start = prompt("Start date (YYYY-MM-DD): ");
        String end   = prompt("End date   (YYYY-MM-DD): ");

        ArrayList<Employee> hires = EmpDataAccess.NewHires(start, end, URL, DB_USER, DB_PASS);

        System.out.println("\nNew Hires Between " + start + " and " + end);
        System.out.println("ID\tName\t\tEmail\t\tHire Date");
        System.out.println("--------------------------------------------------");
        if (hires.isEmpty()) {
            System.out.println("(no hires in this range)");
            return;
        }
        for (Employee e : hires) {
            System.out.println(e.getEmpID() + "\t"
                    + e.getFname() + " " + e.getLname() + "\t"
                    + e.getEmail() + "\t"
                    + e.getHireDate());
        }
    }

    // ============================================================
    // EMPLOYEE MENU (general employee)
    // ============================================================

    public static void employeeMenu(Integer empID) {
        if (empID == null) {
            System.out.println("ERROR: Could not determine your employee ID.");
            return;
        }

        while (true) {
            System.out.println("\n=== Employee Menu ===");
            System.out.println("1. View my information");
            System.out.println("2. View my pay history");
            System.out.println("3. Logout");
            String choice = prompt("Choose an option: ");

            switch (choice) {
                case "1": viewOwnInfo(empID);    break;
                case "2": viewPayHistory(empID); break;
                case "3": System.out.println("Goodbye."); return;
                default:  System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewOwnInfo(int empID) {
        Employee me = EmpDataAccess.EmpSearchByID(empID, URL, DB_USER, DB_PASS);
        if (me == null) {
            System.out.println("Could not load your record.");
            return;
        }
        printEmployeeDetail(me);
    }

    private static void viewPayHistory(int empID) {
        ArrayList<PayrollDataAccess.PayStub> stubs =
                PayrollDataAccess.getPayHistory(empID, URL, DB_USER, DB_PASS);

        System.out.println("\n--- Pay History (most recent first) ---");
        System.out.println("Pay Date\tPeriod\t\t\tGross\tNet\tSalary");
        System.out.println("---------------------------------------------------------------");
        if (stubs.isEmpty()) {
            System.out.println("(no pay history)");
            return;
        }
        for (PayrollDataAccess.PayStub s : stubs) {
            System.out.printf("%s\t%s to %s\t$%,.2f\t$%,.2f\t$%,.2f%n",
                    s.payDate, s.payPeriodStart, s.payPeriodEnd,
                    s.grossPay, s.netPay, s.salaryAmount);
        }
    }

    // ============================================================
    // PRINTING HELPERS
    // ============================================================

    public static void printEmployees(ArrayList<Employee> myEmployees) {
        System.out.println("\nCurrent Employees Report at Company Z\n");
        System.out.println("ID\tName\t\tEmail\t\tHire Date");
        if (myEmployees == null || myEmployees.isEmpty()) {
            System.out.println("(no employees)");
            return;
        }
        for (Employee e : myEmployees) {
            System.out.println(e.getEmpID() + "\t"
                    + e.getFname() + " " + e.getLname() + "\t"
                    + e.getEmail() + "\t"
                    + e.getHireDate());
        }
    }

    private static void printEmployeeDetail(Employee e) {
        System.out.println("\n--- Employee Record ---");
        System.out.println("ID:        " + e.getEmpID());
        System.out.println("Name:      " + e.getFname() + " " + e.getLname());
        System.out.println("Email:     " + e.getEmail());
        System.out.println("Hire Date: " + e.getHireDate());
        System.out.println("SSN:       " + e.getSSN());
        System.out.println("DOB:       " + e.getDOB());
        System.out.println("Address:   " + e.getAddressID());
        System.out.println("Role:      " + e.getRole());
    }

    // ============================================================
    // INPUT HELPERS
    // ============================================================

    private static String prompt(String message) {
        Console console = System.console();
        if (console == null) return "";
        String s = console.readLine(message);
        return (s == null) ? "" : s.trim();
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private static Double parseDoubleOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Converts "YYYY-MM" to ["YYYY-MM-01", "YYYY-MM-DD"] where the second
     * value is the last day of that month. Returns null on bad input.
     */
    private static String[] monthRange(String yearMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth);
            String start = ym.atDay(1).toString();
            String end   = ym.atEndOfMonth().toString();
            return new String[] { start, end };
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}