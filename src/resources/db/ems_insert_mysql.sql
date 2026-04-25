INSERT INTO cities (cityName) VALUES
('Atlanta'),
('Decatur'),
('Savannah'),
('Marietta');

INSERT INTO division (divisionName, divisionDescription) VALUES
('Human Resources', 'Handles employee records and HR operations'),
('Finance', 'Handles payroll, budgeting, and financial reporting'),
('IT', 'Handles systems, infrastructure, and support'),
('Operations', 'Handles daily business operations');

INSERT INTO job_titles (jobTitleName, jobDescription) VALUES
('HR Admin', 'Manages employee data and HR system functions'),
('Payroll Specialist', 'Processes payroll and pay statements'),
('Software Developer', 'Builds and maintains software systems'),
('Operations Manager', 'Oversees business operations');

INSERT INTO addresses (street, cityID, stateID, zip) VALUES
('123 Peachtree St', 1, 10, '30303'),
('456 Oak Ave', 2, 10, '30030');

INSERT INTO employees (
	empID,
	username,
    firstName,
    lastName,
    email,
    hireDate,
    ssn,
    DOB,
    addressID,
    passwordHash,
    passwordSalt,
    role
)values(
        101,
        'jsmith',
        'John',
        'Smith',
        'john.smith@companyz.com',
        '2022-01-15',
        '123-45-6789',
        "2000-2-15",
        1,
        'Z4uEz0d3/knJPYGeg6mE7k/xk7/d6504PRan/YdnukM=',
        'fTuQZM5TymaBCW/vWHlZhQ==',
        'General'
),
(
        102,
        'lbrown',
        'Lisa',
        'Brown',
        'lisa.brown@companyz.com',
        '2023-07-15',
        '987-65-4321',
        "1982-10-15",
        2,
        '9lGIii7Me6yqBQzSPS5NMD9EXmxxwnZSRMYnAWoWSJ8=',
        'HYkEvkXj4DbwOCiMqFrnKQ==',
        'General'
);

INSERT INTO system_admins (username, passwordHash, passwordSalt) VALUES
('developer', 'OcAJ9CIMc36+G53+zZPz5XDNVbWVvkHxXBIE53HF6/Q=', 'R7dyNQpY/KN4frGQXyPI7A==');

INSERT INTO employee_division (empID, divID, assignedDate) VALUES
(101, 1, default),
(102, 2, default);

INSERT INTO employee_job_titles (empID, job_titleID, effectiveDate, endDate) VALUES
(101, 1, default, NULL),
(102, 2, default, NULL);

INSERT INTO payroll (
    empID,
    payPeriodStart,
    payPeriodEnd,
    payDate,
    grossPay,
    federalTax,
    stateTax,
    otherDeductions,
    netPay,
    salaryAmount
) VALUES
(
    101,
    '2026-03-01',
    '2026-03-15',
    '2026-03-20',
    5000.00,
    750.00,
    250.00,
    100.00,
    3900.00,
    120000.00
),
(
    102,
    '2026-03-01',
    '2026-03-15',
    '2026-03-20',
    4200.00,
    600.00,
    210.00,
    90.00,
    3300.00,
    100800.00
);


-- testing selects

SELECT * FROM states;
SELECT * FROM cities;
SELECT * FROM division;
SELECT * FROM job_titles;
SELECT * FROM addresses;
SELECT * FROM employees;
SELECT * FROM employee_division;
SELECT * FROM employee_job_titles;
SELECT * FROM payroll;