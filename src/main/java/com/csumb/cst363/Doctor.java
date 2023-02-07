package com.csumb.cst363;

/*
 * This class is used to transfer data to/from doctor templates
 *  for registering new doctor and updating doctor profile.
 */
public class Doctor {

	private String doctorSSN;
	private String fName;
	private String lName;
	private String specialty;
	private String startDate;  // YYYY-MM_DD
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDoctorSSN() {
		return doctorSSN;
	}

	public void setDoctorSSN(String doctorSSN) {
		this.doctorSSN = doctorSSN;
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

	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	@Override
	public String toString() {
		return "Doctor [doctorSSN=" + doctorSSN + ", fName=" + fName + ", lName=" + lName + ", specialty="
				+ specialty + ", startDate=" + startDate + "]";
	}
}
