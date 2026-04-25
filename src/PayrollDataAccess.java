import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PayrollDataAccess {

    // ============================================================
    // SALARY RAISE
    // ============================================================

    public static class RaiseResult {
        public final int employeesAffected;
        public final int rowsUpdated;
        public final boolean success;
        public final String errorMessage;

        public RaiseResult(int employeesAffected, int rowsUpdated, boolean success, String errorMessage) {
            this.employeesAffected = employeesAffected;
            this.rowsUpdated = rowsUpdated;
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    public static int countEmployeesInRange(double minSalary, double maxSalary,
                                            String url, String user, String password) {
        String sql = "SELECT COUNT(DISTINCT empID) FROM payroll " +
                     "WHERE salaryAmount BETWEEN ? AND ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, minSalary);
            stmt.setDouble(2, maxSalary);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + e.getMessage());
        }
        return 0;
    }

    public static RaiseResult raiseSalariesInRange(double minSalary, double maxSalary,
                                                   double percent,
                                                   String url, String user, String password) {
        if (percent <= 0) return new RaiseResult(0, 0, false, "Percent must be greater than 0.");
        if (minSalary < 0 || maxSalary < minSalary) return new RaiseResult(0, 0, false, "Invalid salary range.");

        double factor = 1.0 + (percent / 100.0);
        int affectedEmployees = countEmployeesInRange(minSalary, maxSalary, url, user, password);

        String updateSql =
            "UPDATE payroll " +
            "SET salaryAmount = salaryAmount * ?, " +
            "    grossPay     = grossPay     * ?, " +
            "    netPay       = (grossPay * ?) - COALESCE(federalTax,0) - COALESCE(stateTax,0) - COALESCE(otherDeductions,0) " +
            "WHERE salaryAmount BETWEEN ? AND ?";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, factor);
                stmt.setDouble(2, factor);
                stmt.setDouble(3, factor);
                stmt.setDouble(4, minSalary);
                stmt.setDouble(5, maxSalary);

                int rowsUpdated = stmt.executeUpdate();
                conn.commit();
                return new RaiseResult(affectedEmployees, rowsUpdated, true, null);
            } catch (SQLException e) {
                conn.rollback();
                return new RaiseResult(0, 0, false, "Update failed, rolled back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return new RaiseResult(0, 0, false, "Database connection error: " + e.getMessage());
        }
    }

    // ============================================================
    // PAY HISTORY (employee self-service)
    // ============================================================

    public static class PayStub {
        public final String payPeriodStart, payPeriodEnd, payDate;
        public final double grossPay, federalTax, stateTax, otherDeductions, netPay, salaryAmount;

        public PayStub(String payPeriodStart, String payPeriodEnd, String payDate,
                       double grossPay, double federalTax, double stateTax,
                       double otherDeductions, double netPay, double salaryAmount) {
            this.payPeriodStart = payPeriodStart;
            this.payPeriodEnd = payPeriodEnd;
            this.payDate = payDate;
            this.grossPay = grossPay;
            this.federalTax = federalTax;
            this.stateTax = stateTax;
            this.otherDeductions = otherDeductions;
            this.netPay = netPay;
            this.salaryAmount = salaryAmount;
        }
    }

    /**
     * Returns all pay stubs for the given employee, sorted by payDate (most recent first).
     */
    public static ArrayList<PayStub> getPayHistory(int empID,
                                                   String url, String user, String password) {
        ArrayList<PayStub> stubs = new ArrayList<>();
        String sql = "SELECT payPeriodStart, payPeriodEnd, payDate, grossPay, " +
                     "       federalTax, stateTax, otherDeductions, netPay, salaryAmount " +
                     "FROM payroll WHERE empID = ? ORDER BY payDate DESC";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stubs.add(new PayStub(
                        rs.getString("payPeriodStart"),
                        rs.getString("payPeriodEnd"),
                        rs.getString("payDate"),
                        rs.getDouble("grossPay"),
                        rs.getDouble("federalTax"),
                        rs.getDouble("stateTax"),
                        rs.getDouble("otherDeductions"),
                        rs.getDouble("netPay"),
                        rs.getDouble("salaryAmount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + e.getMessage());
        }
        return stubs;
    }

    // ============================================================
    // MONTHLY TOTAL PAY BY JOB TITLE (admin report)
    // ============================================================

    /**
     * Returns a map of jobTitleName -> total grossPay for payroll rows whose
     * payDate falls in the given month. The job title is determined by the
     * employee_job_titles row that was active during the pay period
     * (effectiveDate <= payPeriodStart, endDate IS NULL or >= payPeriodEnd).
     *
     * Returned as LinkedHashMap to preserve sort order (highest total first).
     */
    public static Map<String, Double> totalPayByJobTitle(String monthStart, String monthEnd,
                                                          String url, String user, String password) {
        LinkedHashMap<String, Double> totals = new LinkedHashMap<>();
        String sql =
            "SELECT jt.jobTitleName, SUM(p.grossPay) AS total " +
            "FROM payroll p " +
            "JOIN employee_job_titles ejt " +
            "  ON ejt.empID = p.empID " +
            " AND ejt.effectiveDate <= p.payPeriodStart " +
            " AND (ejt.endDate IS NULL OR ejt.endDate >= p.payPeriodEnd) " +
            "JOIN job_titles jt ON jt.job_titleID = ejt.job_titleID " +
            "WHERE p.payDate BETWEEN ? AND ? " +
            "GROUP BY jt.jobTitleName " +
            "ORDER BY total DESC";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, monthStart);
            stmt.setString(2, monthEnd);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("jobTitleName"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + e.getMessage());
        }
        return totals;
    }

    // ============================================================
    // MONTHLY TOTAL PAY BY DIVISION (admin report)
    // ============================================================

    /**
     * Returns a map of divisionName -> total grossPay. employee_division has
     * no endDate, so "current division" = the row with the latest assignedDate
     * for each employee.
     */
    public static Map<String, Double> totalPayByDivision(String monthStart, String monthEnd,
                                                         String url, String user, String password) {
        LinkedHashMap<String, Double> totals = new LinkedHashMap<>();
        String sql =
            "SELECT d.divisionName, SUM(p.grossPay) AS total " +
            "FROM payroll p " +
            "JOIN ( " +
            "    SELECT ed.empID, ed.divID " +
            "    FROM employee_division ed " +
            "    INNER JOIN ( " +
            "        SELECT empID, MAX(assignedDate) AS latestDate " +
            "        FROM employee_division " +
            "        GROUP BY empID " +
            "    ) latest ON ed.empID = latest.empID AND ed.assignedDate = latest.latestDate " +
            ") current_div ON current_div.empID = p.empID " +
            "JOIN division d ON d.divID = current_div.divID " +
            "WHERE p.payDate BETWEEN ? AND ? " +
            "GROUP BY d.divisionName " +
            "ORDER BY total DESC";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, monthStart);
            stmt.setString(2, monthEnd);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("divisionName"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + e.getMessage());
        }
        return totals;
    }
}