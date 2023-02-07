package com.csumb.cst363;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
  A program to generate 100 random patients,
  10 random doctors
  and 100 random prescriptions.
 */

public class DataGenerate {

  static String[] listOfFNames;
  static String[] listOfLNames;
  static String[] specialties;
  static String[] states = {"AL","AK","AZ","AR","CA","CZ","CO","CT","DE","DC","FL","GA","GU","HI","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","PR","RI","SC","SD","TN","TX","UT","VT","VI","VA","WA","WV","WI","WY"};
  static String[] cities ;
  static String[] streets;

  ArrayList<String> doctorSSNList = new ArrayList<>();
  ArrayList<String> patientSSNList = new ArrayList<>();
  static final String DBURL = "jdbc:mysql://localhost:3306/drugco";  // database URL
  static final String USERID = "root";
  static final String PASSWORD = "csuMB@)@#";
  public static void main(String[] args) {
    initializeArrays();
    new DataGenerate();
  }

  public DataGenerate() {

    for (int i = 0; i < 10; i++) {
      addDoctor();
    }

    for (int i = 0; i < 100; i++) {
      addPatient();
    }

    for (int i = 0; i < 100; i++) {
      addPrescription();
    }
  }

  public void addPrescription() {
    Prescription prescription = getPrescription();

    try (Connection con = DriverManager.getConnection(DBURL, USERID, PASSWORD)) {
      PreparedStatement ps = con.prepareStatement("INSERT INTO prescriptions(patientSSN, doctorSSN, drugID, datePrescribed, Qty) values(?, ?, ?, ?, ?)");
      ps.setString(1, prescription.getPatientSSN());
      ps.setString(2, prescription.getDoctorSSN());
      ps.setInt(3, prescription.getDrugID());
      ps.setString(4, prescription.getDatePrescribed());
      ps.setInt(5, prescription.getQty());

      ps.executeUpdate();

    } catch (SQLException e) {
      System.out.println("MySQL error on prescription add. " + e);
    }
  }
  public void addDoctor() {

    Doctor doctor = getDoctor();

    try (Connection con = DriverManager.getConnection(DBURL, USERID, PASSWORD)) {
      PreparedStatement ps = con.prepareStatement("INSERT INTO doctor(doctorSSN, fName, lName, specialty, startDate) values(?, ?, ?, ?, ?)");
      ps.setString(1, doctor.getDoctorSSN());
      ps.setString(2, doctor.getfName());
      ps.setString(3, doctor.getlName());
      ps.setString(4, doctor.getSpecialty());
      ps.setString(5, doctor.getStartDate());

      ps.executeUpdate();

    } catch (SQLException e) {
      System.out.println("MySQL error on doctor add. " + e);
    }
  }

  public void addPatient() {

      Patient patient = getPatient();

      try (Connection con = DriverManager.getConnection(DBURL, USERID, PASSWORD)) {
        PreparedStatement ps = con.prepareStatement("INSERT INTO patient(patientSSN, fName, lName, birthdate, street, city, state, zip, doctorSSN) values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, patient.getPatientSSN());
        ps.setString(2, patient.getfName());
        ps.setString(3, patient.getlName());
        ps.setString(4, patient.getBirthdate());
        ps.setString(5, patient.getStreet());
        ps.setString(6, patient.getCity());
        ps.setString(7, patient.getState());
        ps.setString(8, patient.getZip());
        ps.setString(9, patient.getDoctorSSN());

        ps.executeUpdate();

      } catch (SQLException e) {
        System.out.println("MySQL error on patient add. " + e);
      }
  }

  public Prescription getPrescription() {
    Prescription prescription = new Prescription();
    Random r = new Random();
    Integer randInt = r.nextInt(100-1) + 1;
    prescription.setPatientSSN(patientSSNList.get(randInt));
    randInt = r.nextInt(10-1) + 1;
    prescription.setDoctorSSN(doctorSSNList.get(randInt));
    randInt = r.nextInt(21-1) + 1;
    prescription.setDrugID(randInt);
    prescription.setDatePrescribed(getRandomDate());
    randInt = r.nextInt(151-1) + 1;
    prescription.setQty(randInt);
    return prescription;
  }

  public Patient getPatient() {
    Patient patient = new Patient();
    Random r = new Random();
    Integer randInt = r.nextInt(10-1) + 1;
    patient.setDoctorSSN(doctorSSNList.get(randInt));
    String patientSSN = SSNGenerator();
    patientSSNList.add(patientSSN);
    patient.setPatientSSN(patientSSN);
    patient.setfName(getFName());
    patient.setlName(getLName());
    patient.setBirthdate(getRandomDate());
    patient.setStreet(getStreet());
    patient.setCity(getCity());
    patient.setState(getState());
    patient.setZip(zipGenerate());
    return patient;
  }
  public Doctor getDoctor() {
    Doctor doctor = new Doctor();
    String doctorSSN = SSNGenerator();
    doctorSSNList.add(doctorSSN);
    doctor.setDoctorSSN(doctorSSN);
    doctor.setfName(getFName());
    doctor.setlName(getLName());
    doctor.setSpecialty(getSpecialty());
    doctor.setStartDate(getRandomDate());
    return doctor;
  }

  public static void initializeArrays() {

    try {
      listOfFNames = getArray("src/main/resources/txt/fnames.txt");
    } catch (Exception e) {
      System.out.println("File input error.");
    }

    try {
      listOfLNames = getArray("src/main/resources/txt/lnames.txt");
    } catch (Exception e) {
      System.out.println("File input error.");
    }

    try {
      streets = getArray("src/main/resources/txt/streets.txt");
    } catch (Exception e) {
      System.out.println("File input error.");
    }

    try {
      specialties = getArray("src/main/resources/txt/specialties.txt");
    } catch (Exception e) {
      System.out.println("File input error.");
    }

    try {
      cities = getArray("src/main/resources/txt/cities.txt");
    } catch (Exception e) {
      System.out.println("File input error.");
    }
  }
  public String getRandomDate() {
    LocalDate startDate = LocalDate.of(1960, 1, 1); //start date
    long start = startDate.toEpochDay();

    LocalDate endDate = LocalDate.now(); //end date
    long end = endDate.toEpochDay();

    long randomEpochDay = ThreadLocalRandom.current().longs(start, end).findAny().getAsLong();
    return LocalDate.ofEpochDay(randomEpochDay).toString();
  }
  public String getLName() {
    Random r = new Random();
    Integer randInt = r.nextInt(4096-1) + 1;
    return listOfLNames[randInt];
  }

  public String getFName() {
    Random r = new Random();
    Integer randInt = r.nextInt(4096-1) + 1;
    return listOfFNames[randInt];
  }

  public String getState() {
    Random r = new Random();
    Integer randInt = r.nextInt(51-1) + 1;
    return states[randInt];
  }

  public String getSpecialty() {
    Random r = new Random();
    Integer randInt = r.nextInt(39-1) + 1;
    return specialties[randInt];
  }

  public String SSNGenerator() {
    String SSNstart;
    String SSNmiddle;
    String SSNend;
    Random r = new Random();

    Integer start = r.nextInt(900-1) + 1;

    if(start < 10) {
      SSNstart = "00" + start.toString();
    } else if (start < 100) {
      SSNstart = "0" + start.toString();
    } else {
      SSNstart = start.toString();
    }

    Integer middle = r.nextInt(99-1) + 1;

    if(middle < 10) {
      SSNmiddle = "0" + middle.toString();
    } else {
      SSNmiddle = middle.toString();
    }

    Integer end = r.nextInt(9999-1) + 1;

    if(end < 10) {
      SSNend = "000" + end.toString();
    } else if (end < 100) {
      SSNend = "00" + end.toString();
    } else if (end < 1000) {
      SSNend = "0" + end.toString();
    } else {
      SSNend = end.toString();
    }

    return SSNstart + SSNmiddle + SSNend;
  }

  public String zipGenerate() {
    Random r = new Random();

    Integer zipcode = r.nextInt(99999-601) + 601;
    return zipcode.toString();
  }

  public String getStreet() {
    Random r = new Random();
    Integer randInt = r.nextInt(153-1) + 1;
    return streets[randInt];
  }

  public String getCity() {
    Random r = new Random();
    Integer randInt = r.nextInt(1961-1) + 1;
    return cities[randInt];
  }

  public static String[] getArray(String file)
    throws IOException {

    List<String> listOfStrings = new ArrayList<>();

    FileReader fr = new FileReader(file);
    String s = new String();
    char ch;

    while (fr.ready()) {
      ch = (char)fr.read();
      if (ch == ',') {
        listOfStrings.add(s.toString());
        s = new String();
      }
      else {
        s += ch;
      }
    }

    if (s.length() > 0) {
      listOfStrings.add(s.toString());
    }

    return listOfStrings.toArray(new String[0]);
  }
}
