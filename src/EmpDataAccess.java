import java.sql.*;
import java.util.ArrayList;
import java.util.Set;

public class EmpDataAccess {

    private static final Set<String> UPDATABLE_FIELDS = Set.of(
        "firstName", "lastName", "email", "role",
        "addressID", "ssn", "DOB", "hireDate"
    );

    public static ArrayList<Employee> CurrentEmployees(String url, String user, String password) {
        ArrayList<Employee> emps = new ArrayList<>();
        String sqlcommand = "SELECT empID, firstName, lastName, email, hireDate " +
                            "FROM employees ORDER BY hireDate";

        try (Connection myConn = DriverManager.getConnection(url, user, password);
             Statement myStmt = myConn.createStatement();
             ResultSet myRS = myStmt.executeQuery(sqlcommand)) {

            while (myRS.next()) {
                Employee temp = new Employee();
                temp.setEmpID(myRS.getInt(1));
                temp.setFname(myRS.getString(2));
                temp.setLname(myRS.getString(3));
                temp.setEmail(myRS.getString(4));
                temp.setHireDate(myRS.getString(5));
                emps.add(temp);
            }
        } catch (Exception e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
        }
        return emps;
    }

    public static Employee EmpSearch(Integer sID, String sSSN, String sDOB, String firstname, String lastname,
                                     String url, String user, String password) {
        if (sID == null && sSSN == null && sDOB == null) return null;

        String sql = "SELECT empID, firstName, lastName, email, hireDate, ssn, DOB, addressID, role " +
                     "FROM employees " +
                     "WHERE empID = ? OR (ssn = ? AND DOB = ?) or (firstName = ? AND lastName = ?)";

        try (Connection myConn = DriverManager.getConnection(url, user, password);
             PreparedStatement myStmt = myConn.prepareStatement(sql)) {

            if (sID != null) myStmt.setInt(1, sID);    else myStmt.setNull(1, Types.INTEGER);
            if (sSSN != null) myStmt.setString(2, sSSN); else myStmt.setNull(2, Types.VARCHAR);
            if (sDOB != null) myStmt.setString(3, sDOB); else myStmt.setNull(3, Types.VARCHAR);
            if (firstname != null) myStmt.setString(4, firstname); else myStmt.setNull(4, Types.VARCHAR);
            if (lastname != null) myStmt.setString(5, lastname); else myStmt.setNull(5, Types.VARCHAR);

            try (ResultSet rs = myStmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
        }
        return null;
    }

    public static Employee EmpSearchByName(String firstName, String lastName,
                                           String url, String user, String password) {
        return EmpSearch(null, null, null, firstName, lastName, url, user, password);
    }

    public static Employee EmpSearchBySSN(String ssn, String dob, String url, String user, String password) {
        return EmpSearch(null, ssn, dob, null, null, url, user, password);
    }

    public static Employee EmpSearchByID(int empID, String url, String user, String password) {
        return EmpSearch(empID, null, null, null, null, url, user, password);
    }

    public static boolean updateField(int empID, String field, String newValue,
                                      String url, String user, String password) {
        if (!UPDATABLE_FIELDS.contains(field)) {
            System.out.println("Field '" + field + "' cannot be updated.");
            return false;
        }

        String sql = "UPDATE employees SET " + field + " = ? WHERE empID = ?";

        try (Connection myConn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = myConn.prepareStatement(sql)) {

            if (newValue == null || newValue.isEmpty()) {
                stmt.setNull(1, Types.VARCHAR);
            } else {
                stmt.setString(1, newValue);
            }
            stmt.setInt(2, empID);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ERROR " + e.getMessage());
            return false;
        }
    }

    public static boolean updateEmpData(Employee emp, String url, String user, String password) {
        String updateEmployeeSQL =
            "UPDATE employees SET firstName = ?, lastName = ?, email = ?, addressID = ?, role = ? " +
            "WHERE empID = ?";

        try (Connection myConn = DriverManager.getConnection(url, user, password)) {
            myConn.setAutoCommit(false);
            try (PreparedStatement myStmtEmp = myConn.prepareStatement(updateEmployeeSQL)) {
                myStmtEmp.setString(1, emp.getFname());
                myStmtEmp.setString(2, emp.getLname());
                myStmtEmp.setString(3, emp.getEmail());
                myStmtEmp.setInt(4, emp.getAddressID());
                myStmtEmp.setString(5, emp.getRole());
                myStmtEmp.setInt(6, emp.getEmpID());

                myStmtEmp.executeUpdate();
                myConn.commit();
                return true;
            } catch (SQLException e) {
                myConn.rollback();
                System.out.println("Update failed. Transaction rolled back: " + e.getMessage());
                return false;
            } finally {
                myConn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns employees whose hireDate falls within the inclusive date range.
     */
    public static ArrayList<Employee> NewHires(String startDate, String endDate,
                                               String url, String user, String password) {
        ArrayList<Employee> emps = new ArrayList<>();
        String sql = "SELECT empID, firstName, lastName, email, hireDate " +
                     "FROM employees " +
                     "WHERE hireDate BETWEEN ? AND ? " +
                     "ORDER BY hireDate";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee e = new Employee();
                    e.setEmpID(rs.getInt("empID"));
                    e.setFname(rs.getString("firstName"));
                    e.setLname(rs.getString("lastName"));
                    e.setEmail(rs.getString("email"));
                    e.setHireDate(rs.getString("hireDate"));
                    emps.add(e);
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + e.getMessage());
        }
        return emps;
    }

    private static Employee mapRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setEmpID(rs.getInt("empID"));
        emp.setFname(rs.getString("firstName"));
        emp.setLname(rs.getString("lastName"));
        emp.setEmail(rs.getString("email"));
        emp.setHireDate(rs.getString("hireDate"));
        emp.setSSN(rs.getString("ssn"));
        emp.setDOB(rs.getString("DOB"));
        emp.setAddressID(rs.getInt("addressID"));
        emp.setRole(rs.getString("role"));
        return emp;
    }
}