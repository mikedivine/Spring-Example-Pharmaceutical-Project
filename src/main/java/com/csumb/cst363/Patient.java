package com.csumb.cst363;

/*
 * This class is used to transfer data to/from patient templates.
 */
public class Patient {

	private String patientSSN;
	private String fName;
	private String lName;
	private String birthdate;  // yyyy-mm-dd
	private String street;
	private String city;
	private String state;
	private String zip;
	private String doctorSSN;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPatientSSN() {
		return patientSSN;
	}

	public void setPatientSSN(String patientSSN) {
		this.patientSSN = patientSSN;
	}

	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public String getlName() {
		return lName;
	}

	public void setlName(String lName) {
		this.lName = lName;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getDoctorSSN() {
		return doctorSSN;
	}

	public void setDoctorSSN(String doctorSSN) {
		this.doctorSSN = doctorSSN;
	}

	@Override
	public String toString() {
		return "Patient [patientSSN=" + patientSSN + ", lName=" + lName + ", fName=" + fName
				+ ", birthdate=" + birthdate + ", street=" + street + ", city=" + city + ", state="
				+ state + ", zip=" + zip + ", doctorSSN=" + doctorSSN + "]";
	}
}
