public class Employee {
	
	private int empID;
	private String fname, lname, email, hireDate, ssn;

	public int getEmpID() {
		return this.empID;
	}

	public void setEmpID(int empID) {
		this.empID = empID;
	}

	public String getFname() {
		return this.fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return this.lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHireDate() {
		return this.hireDate;
	}

	public void setHireDate(String hireDate) {
		this.hireDate = hireDate;
	}

	public String getSSN() {
		return this.ssn;
	}

	public void setSSN(String ssn) {
		this.ssn = ssn;
	}
		
	public Employee()
	{
		// all values handled and set individually
	}
}
