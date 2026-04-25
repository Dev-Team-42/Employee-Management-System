public class Employee {

    private int empID, addressID;
    private String fname, lname, email, hireDate, ssn, dob, role;

    public Employee() {
    }

    public Employee(int empID, String fname, String lname, String email, String hireDate) {
        this.empID = empID;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.hireDate = hireDate;
    }

    public Employee(int empID, String fname, String lname, String email, String hireDate,
                    String ssn, String dob, int addressID, String role) {
        this.empID = empID;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.hireDate = hireDate;
        this.ssn = ssn;
        this.dob = dob;
        this.addressID = addressID;
        this.role = role;
    }

    public int getEmpID() { return this.empID; }
    public void setEmpID(int empID) { this.empID = empID; }

    public String getFname() { return this.fname; }
    public void setFname(String fname) { this.fname = fname; }

    public String getLname() { return this.lname; }
    public void setLname(String lname) { this.lname = lname; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getHireDate() { return this.hireDate; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }

    public String getSSN() { return this.ssn; }
    public void setSSN(String ssn) { this.ssn = ssn; }

    public String getDOB() { return this.dob; }
    public void setDOB(String dob) { this.dob = dob; }

    public int getAddressID() { return this.addressID; }
    public void setAddressID(int addressID) { this.addressID = addressID; }

    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
}