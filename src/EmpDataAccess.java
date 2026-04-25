import java.sql.*;
import java.util.ArrayList;

public class EmpDataAccess {

    public static ArrayList<Employee> CurrentEmployees(String url, String user, String password) {
        
        ArrayList<Employee> emps = new ArrayList<>();
        String sqlcommand = "SELECT empID, firstName, lastName, email, hireDate "+ 
                            "FROM employees ORDER BY hireDate; ";
        
        try (Connection myConn = DriverManager.getConnection(url, user, password)) {
            Statement myStmt = myConn.createStatement();
            ResultSet myRS = myStmt.executeQuery(sqlcommand);
            if (myRS.next()) {
                do {
                Employee temp = new Employee();
                temp.setEmpID(myRS.getInt(1));
                temp.setFname(myRS.getString(2));
                temp.setLname(myRS.getString(3));
                temp.setEmail(myRS.getString(4));
                temp.setHireDate(myRS.getString(5));
                emps.add(temp);
                } while (myRS.next());
                
                return emps;
            }
        } catch (Exception e) {
            System.out.println("ERROR " + e.getLocalizedMessage());
        }
        return null; // No employees
    };

    public static Employee EmpSearch(Integer sID, String sSSN, String sDOB, String url, String user, String password) {
        // Using a JOIN assuming DOB was added to the addresses table
        String sql = "SELECT empID, firstName, lastName, email, hireDate, ssn, DOB " +
                     "FROM employees " +
                     "WHERE empID = ? OR (ssn = ? AND DOB = ?)";

	    try (Connection myConn = DriverManager.getConnection(url, user, password)) {
	        PreparedStatement myStmt = myConn.prepareStatement(sql);

            // Replace '?'s in SQL string with search parameters
            myStmt.setInt(1, sID);
            myStmt.setString(2, sSSN);
            myStmt.setString(3, sDOB);
            ResultSet rs = myStmt.executeQuery();

            if (rs.next()) {
                Employee emp = new Employee();
                emp.setEmpID(rs.getInt("empID"));
                emp.setFname(rs.getString("firstName"));
                emp.setLname(rs.getString("lastName"));
                emp.setEmail(rs.getString("email"));
                emp.setHireDate(rs.getString("hireDate"));
                emp.setSSN(rs.getString("ssn"));
                emp.setDOB(rs.getString("DOB"));
                return emp;
            }
        } catch (SQLException e) {
	        System.out.println("ERROR " + e.getLocalizedMessage());
        }
        return null; // Employee is not found
    };
    
    public static boolean updateEmpData(Employee emp, String url, String user, String password) {
        
        // Define the UPDATE queries for both tables
        String updateEmployeeSQL = "UPDATE employees SET firstName = ?, lastName = ?, email = ?, addressID = ?, role = ? WHERE empID = ?";
        

        try (Connection myConn = DriverManager.getConnection(url, user, password)) {
            
            myConn.setAutoCommit(false);

            try (PreparedStatement myStmtEmp = myConn.prepareStatement(updateEmployeeSQL)) {

                // set parameters for the employees table update
                myStmtEmp.setString(1, emp.getFname());
                myStmtEmp.setString(2, emp.getLname());
                myStmtEmp.setString(3, emp.getEmail());
                myStmtEmp.setInt(4, emp.getAddressID());
                myStmtEmp.setInt(5, emp.getEmpID());
                myStmtEmp.setString(3, emp.getRole());

                myStmtEmp.executeUpdate();

                // If both succeed without errors, commit the changes to the database
                myConn.commit();
                return true;

            } catch (SQLException e) {
                // rollback the transaction so no partial data is saved
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
};