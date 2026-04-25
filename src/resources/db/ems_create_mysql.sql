CREATE DATABASE IF NOT EXISTS ems;
USE ems;

DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS employee_job_titles;
DROP TABLE IF EXISTS employee_division;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS job_titles;
DROP TABLE IF EXISTS division;
DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS states;
DROP TABLE IF EXISTS system_admins;


CREATE TABLE states (
    stateID INT AUTO_INCREMENT PRIMARY KEY,
    stateCode CHAR(2) NOT NULL UNIQUE
);

CREATE TABLE cities (
    cityID INT AUTO_INCREMENT PRIMARY KEY,
    cityName VARCHAR(25) NOT NULL
);

CREATE TABLE division (
    divID INT AUTO_INCREMENT PRIMARY KEY,
    divisionName VARCHAR(100) NOT NULL UNIQUE,
    divisionDescription VARCHAR(255)
);

CREATE TABLE job_titles (
    job_titleID INT AUTO_INCREMENT PRIMARY KEY,
    jobTitleName VARCHAR(100) NOT NULL UNIQUE,
    jobDescription VARCHAR(255)
);


CREATE TABLE addresses (
    addressID INT AUTO_INCREMENT PRIMARY KEY,
    street VARCHAR(150) NOT NULL,
    cityID INT NOT NULL,
    stateID INT NOT NULL,
    zip VARCHAR(10) NOT NULL,

    CONSTRAINT fk_addresses_city
        FOREIGN KEY (cityID) REFERENCES cities(cityID),

    CONSTRAINT fk_addresses_state
        FOREIGN KEY (stateID) REFERENCES states(stateID)
);


CREATE TABLE employees (
    empID INT NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE, -- Added username
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    hireDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    ssn CHAR(11) NOT NULL UNIQUE,
    DOB DATE not null,
    addressID INT,
    
    -- Authentication Columns:
    password VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(64) NOT NULL, 
    passwordSalt VARCHAR(32) NOT NULL, 
    role VARCHAR(20) DEFAULT 'General',

    CONSTRAINT fk_employees_address
        FOREIGN KEY (addressID) REFERENCES addresses(addressID)
);

CREATE TABLE system_admins (
    adminID INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL UNIQUE,
    firstName VARCHAR(50) NOT NULL UNIQUE,
    lastName VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(64) NOT NULL, -- 64 chars for Hex or 44 for Base64 SHA-256
    passwordSalt VARCHAR(32) NOT NULL  -- 24 chars for Base64 salt
);

CREATE TABLE employee_division (
    empID INT NOT NULL,
    divID INT NOT NULL,
    assignedDate DATE DEFAULT (CURRENT_DATE),
    PRIMARY KEY (empID, divID),

    CONSTRAINT fk_employee_division_emp
        FOREIGN KEY (empID) REFERENCES employees(empID),

    CONSTRAINT fk_employee_division_div
        FOREIGN KEY (divID) REFERENCES division(divID)
);


CREATE TABLE employee_job_titles (
    empID INT NOT NULL,
    job_titleID INT NOT NULL,
    effectiveDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    endDate DATE NULL,
    PRIMARY KEY (empID, job_titleID, effectiveDate),

    CONSTRAINT fk_employee_job_titles_emp
        FOREIGN KEY (empID) REFERENCES employees(empID),

    CONSTRAINT fk_employee_job_titles_title
        FOREIGN KEY (job_titleID) REFERENCES job_titles(job_titleID)
);


CREATE TABLE payroll (
    payrollID INT AUTO_INCREMENT PRIMARY KEY,
    empID INT NOT NULL,
    payPeriodStart DATE NOT NULL,
    payPeriodEnd DATE NOT NULL,
    payDate DATE NOT NULL,
    grossPay DECIMAL(10,2) NOT NULL,
    federalTax DECIMAL(10,2) DEFAULT 0.00,
    stateTax DECIMAL(10,2) DEFAULT 0.00,
    otherDeductions DECIMAL(10,2) DEFAULT 0.00,
    netPay DECIMAL(10,2) NOT NULL,
    salaryAmount DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_payroll_employee
        FOREIGN KEY (empID) REFERENCES employees(empID)
);

-- search indexes

CREATE INDEX idx_employees_name ON employees(lastName, firstName);
CREATE INDEX idx_employees_ssn ON employees(ssn);
CREATE INDEX idx_payroll_payDate ON payroll(payDate);


INSERT INTO states (stateCode) VALUES
('AL'),
('AK'),
('AZ'),
('AR'),
('CA'),
('CO'),
('CT'),
('DE'),
('FL'),
('GA'),
('HI'),
('ID'),
('IL'),
('IN'),
('IA'),
('KS'),
('KY'),
('LA'),
('ME'),
('MD'),
('MA'),
('MI'),
('MN'),
('MS'),
('MO'),
('MT'),
('NE'),
('NV'),
('NH'),
('NJ'),
('NM'),
('NY'),
('NC'),
('ND'),
('OH'),
('OK'),
('OR'),
('PA'),
('RI'),
('SC'),
('SD'),
('TN'),
('TX'),
('UT'),
('VT'),
('VA'),
('WA'),
('WV'),
('WI'),
('WY');