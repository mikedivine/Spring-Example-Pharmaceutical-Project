package com.csumb.cst363;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
  A program to generate 100 random patients,
  10 random doctors
  and 100 random prescriptions.
 */
@Controller
public class DataGenerate {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  Patient patient;
  Doctor doctor;
  Prescription prescription;
  static String[] listOfFNames;
  static String[] listOfLNames;
  static String[] specialties;
  static String[] states = {"AL","AK","AZ","AR","CA","CZ","CO","CT","DE","DC","FL","GA","GU","HI","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","PR","RI","SC","SD","TN","TX","UT","VT","VI","VA","WA","WV","WI","WY"};
  static String[] cities ;
  static String[] streets;

  ArrayList<String> doctorSSNList = new ArrayList<>();
  public static void main(String[] args) {
    initializeArrays();
    new DataGenerate();
  }

  public DataGenerate() {

    System.out.println(SSNGenerator());
    System.out.println(SSNGenerator());
    System.out.println(SSNGenerator());
    System.out.println(getRandomDate());
  }
  public Patient getPatient() {
    Random r = new Random();
    Integer randInt = r.nextInt(11-1) + 1;
    patient.setDoctorSSN(doctorSSNList.get(randInt));
    patient.setPatientSSN(SSNGenerator());
    patient.setfName(getFName());
    patient.setlName(getLName());
    return patient;
  }
  public Doctor getDoctor() {
    String doctorSSN = SSNGenerator();
    doctorSSNList.add(doctorSSN);
    doctor.setDoctorSSN(doctorSSN);
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
  public String SSNGenerator() {
    String SSN;
    String  SSNstart;
    String  SSNmiddle;
    String  SSNend;
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

    return SSNstart + "-" + SSNmiddle + "-" + SSNend;
  }

  public String zipGenerate() {
    Random r = new Random();

    Integer zipcode = r.nextInt(99999-601) + 601;
    return zipcode.toString();
  }
  private Connection getConnection() throws SQLException {
    Connection conn = jdbcTemplate.getDataSource().getConnection();
    return conn;
  }

  public static String[] getArray(String file)
    throws IOException {

    List<String> listOfStrings
      = new ArrayList<String>();

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
