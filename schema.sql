-- Run this once in MySQL Workbench / CLI before running the Java app.

CREATE DATABASE IF NOT EXISTS simplibank;
USE simplibank;

CREATE TABLE IF NOT EXISTS accounts (
    account_number  VARCHAR(20)   PRIMARY KEY,
    account_holder  VARCHAR(100)  NOT NULL,
    balance         DECIMAL(15,2) NOT NULL,
    pin_hash        VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS transactions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    account_number  VARCHAR(20)   NOT NULL,
    type            VARCHAR(20)   NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    balance_after   DECIMAL(15,2) NOT NULL,
    txn_time        DATETIME      NOT NULL,
    category        VARCHAR(50)   DEFAULT 'Other',
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);