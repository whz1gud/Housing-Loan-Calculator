package com.loanCalculator;

public class TableContent {
    public int month;
    public double payment;
    public double residue;
    public double payed;
    public double interest;

    public TableContent(int month, double payment, double residue, double payed, double interest) {
        this.month = month;
        this.payment = payment;
        this.residue = residue;
        this.payed = payed;
        this.interest = interest;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public double getResidue() {
        return residue;
    }

    public void setResidue(double residue) {
        this.residue = residue;
    }

    public double getPayed() {
        return payed;
    }

    public void setPayed(double payed) {
        this.payed = payed;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }
}
