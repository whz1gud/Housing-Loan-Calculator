package com.example.housingloancalculator;

import com.loanCalculator.Loan;
import com.loanCalculator.TableContent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class Controller implements Initializable {

    Loan input = new Loan();
    public ObservableList<TableContent> entries = FXCollections.observableArrayList();
    @FXML
    private TextField defermentInterest_id;
    @FXML
    private TextField defermentMonth_id;
    @FXML
    private TextField defermentYear_id;
    @FXML
    private TextField from_id;
    @FXML
    private TextField to_id;
    @FXML
    private TextField year_id;
    @FXML
    private LineChart<?, ?> chart_id;
    @FXML
    private Label inputLabel_id;
    @FXML
    private Label exportLabel_id;
    @FXML
    private TextField interest_id;
    @FXML
    private TextField loan_id;
    @FXML
    private TextField month_id;
    @FXML
    public TableView<TableContent> table_id;
    @FXML
    private TableColumn<TableContent, Double> table_interest_id;
    @FXML
    private TableColumn<TableContent, Integer> table_month_id;
    @FXML
    private TableColumn<TableContent, Double> table_payed_id;
    @FXML
    private TableColumn<TableContent, Double> table_payment_id;
    @FXML
    private TableColumn<TableContent, Double> table_residue_id;
    private FilteredList<TableContent> filteredList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        table_interest_id.setCellValueFactory(new PropertyValueFactory<TableContent, Double>("Interest"));
        table_month_id.setCellValueFactory(new PropertyValueFactory<TableContent, Integer>("Month"));
        table_payed_id.setCellValueFactory(new PropertyValueFactory<TableContent, Double>("Payed"));
        table_payment_id.setCellValueFactory(new PropertyValueFactory<TableContent, Double>("Payment"));
        table_residue_id.setCellValueFactory(new PropertyValueFactory<TableContent, Double>("Residue"));
    }

    @FXML
    void btnCalcuate(ActionEvent event) {
        getInput();
        if (input.isInputCorrect) {
            input.monthlyInterest = (input.yearlyInterest / 100) / 12;
            createListOfEntries();
            if (input.fromFilter != 0) {
                filteredList = new FilteredList<>(entries, new Predicate<TableContent>() {
                    @Override
                    public boolean test(TableContent tableContent) {
                        return input.fromFilter <= tableContent.month && input.toFilter >= tableContent.month;
                    }
                });
                table_id.setItems(filteredList);
                getGraph(filteredList);
            } else {
                table_id.setItems(entries);
                getGraph(entries);
            }
        }
    }

    @FXML
    void btnExport(ActionEvent event) {
        exportDataToExcel(entries, "LoanData.xlsx");
    }

    @FXML
    void btnClear(ActionEvent event) {
        inputLabel_id.setText("");
        exportLabel_id.setText("");
        chart_id.getData().clear();
        entries.clear();
        input = new Loan();
    }

    @FXML
    void radioAnnuity(ActionEvent event) {
        input.isAnnuity = true;
        input.isCompound = false;
    }

    @FXML
    void radioCompound(ActionEvent event) {
        input.isAnnuity = false;
        input.isCompound = true;
    }

    public void getInput() {
        input.isInputCorrect = true;
        try {
            input.loanAmount = Double.parseDouble(loan_id.getText());
            input.year = Integer.parseInt(year_id.getText());
            if (month_id.getText().isEmpty()) {
                input.month = 0;
            } else {
                input.month = Integer.parseInt(month_id.getText());
            }
            input.yearlyInterest = Double.parseDouble(interest_id.getText());
            if (input.loanAmount <= 0 || input.year <= 0 || input.month < 0 || input.yearlyInterest <= 0) {
                System.out.println("Input's can not be negative!");
                inputLabel_id.setText("Input's can not be negative!");
                input.isInputCorrect = false;
            }
            input.totalMonths = (input.year * 12) + input.month;
            if (!defermentYear_id.getText().isEmpty() && !defermentMonth_id.getText().isEmpty() && !defermentInterest_id.getText().isEmpty()) {
                input.fromMonth = Integer.parseInt(defermentYear_id.getText());
                input.toMonth = Integer.parseInt(defermentMonth_id.getText());
                input.defermentInterest = Double.parseDouble(defermentInterest_id.getText());
                if (input.defermentInterest <= 0 || input.fromMonth < 1 || input.fromMonth > input.totalMonths ||
                        input.toMonth <= input.fromMonth || input.toMonth > input.totalMonths) {
                    System.out.println("Deferment input's are incorrect!");
                    inputLabel_id.setText("Deferment input's are incorrect!");
                    input.isInputCorrect = false;
                }
                input.additionalMonths = input.toMonth - input.fromMonth;
            }
            if (!from_id.getText().isEmpty() && !to_id.getText().isEmpty()) {
                input.fromFilter = Integer.parseInt(from_id.getText());
                input.toFilter = Integer.parseInt(to_id.getText());
                if (input.fromFilter < 1 || input.fromFilter > input.totalMonths || input.toFilter < input.fromFilter || input.toFilter > input.totalMonths) {
                    System.out.println("Filter input's are incorrect!");
                    inputLabel_id.setText("Filter input's are incorrect!");
                    input.isInputCorrect = false;
                }
            }

        } catch (Exception e) {
            input.isInputCorrect = false;
            System.out.println("Fields' data must be numbers!");
            inputLabel_id.setText("Fields' data must be numbers!");
        }
    }

    public void createListOfEntries() {
        entries.clear();
        double payed = 0;
        double left = input.loanAmount;
        int totalMonths = input.totalMonths;
        if (input.isAnnuity) {
            double annuityPayment = (input.loanAmount * input.monthlyInterest) / (1 - (1 / (Math.pow(1 + input.monthlyInterest, input.totalMonths))));
            annuityPayment = roundDown(annuityPayment);
            for (int i = 0; i < totalMonths; i++) {
                if (input.fromMonth != 0 && input.fromMonth == i) {
                    System.out.println(input.fromMonth);
                    System.out.println(input.additionalMonths);
                    for (int j = input.fromMonth; j < input.additionalMonths + input.fromMonth; j++) {
                        System.out.println(j);
                        double tempInterest = ((input.loanAmount * input.defermentInterest / 100) / 12);
                        tempInterest = roundDown(tempInterest);
                        payed += tempInterest;
                        payed = roundDown(payed);
                        i++;
                        totalMonths++;
                        entries.add(new TableContent(j + 1, 0, left, payed, tempInterest));
                    }
                }
                double interest = left * input.monthlyInterest;
                interest = roundDown(interest);
                double principal = annuityPayment - interest;
                payed += annuityPayment;
                left -= principal;
                payed = roundDown(payed);
                left = roundDown(left);

                entries.add(new TableContent(i + 1, annuityPayment, left, payed, interest));
            }
        } else {
            // Compound loan calculation
            for (int i = 0; i < totalMonths; i++) {
                if (input.fromMonth != 0 && input.fromMonth == i) {
                    System.out.println(input.fromMonth);
                    System.out.println(input.additionalMonths);
                    for (int j = input.fromMonth; j < input.additionalMonths + input.fromMonth; j++) {
                        System.out.println(j);
                        double tempInterest = ((input.loanAmount * input.defermentInterest / 100) / 12);
                        tempInterest = roundDown(tempInterest);
                        payed += tempInterest;
                        payed = roundDown(payed);
                        i++;
                        totalMonths++;
                        entries.add(new TableContent(j + 1, 0, left, payed, tempInterest));
                    }
                }
                double interest = left * input.monthlyInterest;
                interest = roundDown(interest);
                double principal = input.loanAmount / input.totalMonths;
                principal = roundDown(principal);
                double payment = principal + interest;
                payment = roundDown(payment);
                payed += payment;
                left -= principal;
                payed = roundDown(payed);
                left = roundDown(left);

                entries.add(new TableContent(i + 1, payment, left, payed, interest));
            }
        }
    }

    public static double roundDown(double num) {
        return Math.floor(num * 100) / 100;
    }

    public void getGraph(ObservableList<TableContent> list) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Months");
        yAxis.setLabel("Total");

        LineChart<Number, Number> lineChart = (LineChart<Number, Number>) chart_id;
        lineChart.getXAxis().setLabel("Months");
        lineChart.getYAxis().setLabel("Total");

        XYChart.Series series = new XYChart.Series();
        if (input.isAnnuity) {
            lineChart.setTitle("Annuity");
            series.setName("Annuity");
        } else {
            lineChart.setTitle("Compound");
            series.setName("Compound");
        }

        for (TableContent data : list) {
            series.getData().add(new XYChart.Data<>(String.valueOf(data.getMonth()), data.getPayment()));
        }
        lineChart.getData().add(series);
    }

    public void exportDataToExcel(ObservableList<TableContent> data, String fileName) {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Loan data");
        String[] headers = {"Month", "Payment", "Residue", "Payed", "Interest"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        for (int i = 0; i < data.size(); i++) {
            TableContent rowContent = data.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(rowContent.getMonth());
            row.createCell(1).setCellValue(rowContent.getPayment());
            row.createCell(2).setCellValue(rowContent.getResidue());
            row.createCell(3).setCellValue(rowContent.getPayed());
            row.createCell(4).setCellValue(rowContent.getInterest());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
            System.out.println("Data has been exported to " + fileName);
            exportLabel_id.setText("Data has been exported to " + fileName);
        } catch (IOException e) {
            System.out.println("Error occurred while exporting data: " + e.getMessage());
        }
    }
}
