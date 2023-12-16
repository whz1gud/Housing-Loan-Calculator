package com.loanCalculator;

public class Loan {
    public boolean isInputCorrect;
    public double loanAmount;
    public int year, month;
    public int fromMonth, toMonth;
    public int additionalMonths;
    public int fromFilter, toFilter;
    public double yearlyInterest;
    public double defermentInterest;
    public int totalMonths;
    public double monthlyInterest;
    public boolean isAnnuity = true, isCompound = false;

}
