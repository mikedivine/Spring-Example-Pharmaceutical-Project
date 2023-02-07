package com.csumb.cst363;

import java.sql.*;
import java.util.Scanner;
import static java.lang.Integer.parseInt;

/**
 * An FDA government official is looking for the quantity of drugs that each doctor
 * has prescribed. The report shows the doctor’s name and quantity prescribed.
 * Input is drug name (may be partial name) and a start and end date range
 */

public class FDA_Report_App {

  static final String DBURL = "jdbc:mysql://localhost:3306/drugco";  // database URL
  static final String USERID = "root";
  static final String PASSWORD = "csuMB@)@#";

  public static void main(String[] args) {

    try (Connection conn = DriverManager.getConnection(DBURL, USERID, PASSWORD)) {
      Scanner input = new Scanner(System.in);
      System.out.print("Enter drug name to query: "); // Currently matches either the trade name or generic name
      String drugQuery = input.nextLine();
      String startDate = "";
      String endDate = "";
      boolean validDate = false;

      while (!validDate) {
        System.out.print("Enter start date (yyyy-MM-dd): "); // MySQL valid date range is '1000-01-01' to '9999-12-31'
        startDate = input.nextLine();
        validDate = isValidDate(startDate);
      }
      validDate = false;
      while (!validDate) {
        System.out.print("Enter end date (yyyy-MM-dd): ");
        endDate = input.nextLine();
        validDate = isValidDate(endDate);
      }

      PreparedStatement ps = conn.prepareStatement("SELECT FName, LName, sum(Qty) total, d.TradeName, d.GenericName FROM prescriptions p JOIN doctor dr ON p.DoctorSSN = dr.DoctorSSN JOIN drug d ON p.DrugID = d.DrugID WHERE TradeName LIKE ? or GenericName LIKE ? AND DatePrescribed BETWEEN ? AND ? GROUP BY dr.DoctorSSN, d.TradeName, d.GenericName ORDER BY d.TradeName, total DESC");
      ps.setString(1, '%' + drugQuery + '%');
      ps.setString(2, '%' + drugQuery + '%');
      ps.setString(3, startDate);
      ps.setString(4, endDate);
      ps.execute();
      ResultSet rs = ps.getResultSet();

      if (!rs.next()) {
        System.out.println("No results found!");
      } else {
        String tradeName = "";
        String genericName = "";
        do {
          // Check if the results are for the same drug and prints a header
          if (((tradeName != null) && !(tradeName.equals(rs.getString("TradeName")))) || !(genericName.equals(rs.getString("GenericName")))) {
            tradeName = rs.getString("TradeName");
            genericName = rs.getString("GenericName");
            printDrug(tradeName, genericName, startDate, endDate);
          }
          // Prints the doctor’s name and quantity prescribed for each drug
          System.out.printf("%-35s %,6d\n", rs.getString(1) + " " + rs.getString(2), rs.getInt(3));
        } while (rs.next());
      }
    } catch (SQLException se) {
      System.out.println("Error: SQLException " + se.getMessage() );
    }
  }

  private static void printDrug(String tradeName, String genericName, String startDate, String endDate) {
    if (tradeName != null) {
      System.out.printf("\nTotal quantity prescribed for: %s (generic: %s)\nBetween dates %s and %s\n", tradeName, genericName, startDate, endDate);
      System.out.printf("Doctor %35s\n%s\n","Quantity", "-".repeat(42));
    } else {
      System.out.printf("\nTotal quantity prescribed for: %s (generic)\nBetween dates %s and %s\n", genericName, startDate, endDate);
      System.out.printf("Doctor %35s\n%s\n","Quantity", "-".repeat(42));
    }
  }

  private static boolean isValidDate(String date) {
    if (date.matches("^[1-9][0-9]{3}-[0-1]?[0-9]{1}-[0-3]?[0-9]{1}$")){
      String[] values = date.split("-");
      if ((1 <= parseInt(values[1])) && (parseInt(values[1]) <= 12)) {
        if ((1 <= parseInt(values[2])) && (parseInt(values[2]) <= 31)) {
          return true;
        }
      }
    }
    System.out.println("Invalid date. Valid range is 1000-01-01 to 9999-12-31. Valid format is yyyy-MM-dd. Ex: 1990-05-25");
    return false;
  }
}
