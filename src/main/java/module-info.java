module com.example.housingloancalculator {
    requires javafx.controls;
    requires javafx.fxml;
    requires poi;
    requires poi.ooxml;

    opens com.loanCalculator to javafx.base;
    opens com.example.housingloancalculator to javafx.fxml;

    exports com.example.housingloancalculator;
}