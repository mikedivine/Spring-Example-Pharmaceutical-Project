package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatient {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Request blank patient registration form.
	 */
	@GetMapping("/patient/new")
	public String newPatient(Model model) {
		// return blank form for new patient registration
		model.addAttribute("patient", new Patient());
		return "patient_register";
	}
	
	/*
	 * Process new patient registration	 */
	@PostMapping("/patient/new")
	public String newPatient(Patient patient, Model model) {

		try (Connection con = getConnection()) {
			PreparedStatement ps = con.prepareStatement("insert into patient(patientSSN, fName, lName, birthdate, street, city, state, zip, doctorSSN) values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
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

			// display message and patient information
			model.addAttribute("message", "New patient added successfully.");
			model.addAttribute("patient", patient);
			return "patient_show";

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", patient);
			return "patient_register";
		}
	}

	/*
	 * Request blank form for patient search.
	 */
	@GetMapping("/patient/get")
	public String getPatient(Model model) {
		// return form to enter patient SSN
		model.addAttribute("patient", new Patient());
		return "patient_get";
	}

	/*
	 * Search for patient by SSN.
	 */
	@PostMapping("/patient/get")
	public String getPatient(Patient patient, Model model) {

		try (Connection con = getConnection()) {
			// for DEBUG
			System.out.println("start getPatient " + patient);
			PreparedStatement ps = con.prepareStatement("select lName, fName, birthdate, street, city, state, zip, doctorSSN from patient where patientSSN=?");
			ps.setString(1, patient.getPatientSSN());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				patient.setlName(rs.getString(1));
				patient.setfName(rs.getString(2));
				patient.setBirthdate(rs.getString(3));
				patient.setStreet(rs.getString(4));
				patient.setCity(rs.getString(5));
				patient.setState(rs.getString(6));
				patient.setZip(rs.getString(7));
				patient.setDoctorSSN(rs.getString(8));
				model.addAttribute("patient", patient);
				// for DEBUG
				System.out.println("end getPatient "+patient);
				return "patient_show";

			} else {
				model.addAttribute("message", "Patient not found.");
				return "patient_get";
			}

		} catch (SQLException e) {
			System.out.println("SQL error in getPatient "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", patient);
			return "patient_get";
		}
	}

	/*
	 * search for patient by SSN.
	 */
	@GetMapping("/patient/edit/{patientSSN}")
	public String getPatient(@PathVariable String patientSSN, Model model) {
		Patient patient = new Patient();
		patient.setPatientSSN(patientSSN);
		try (Connection con = getConnection()) {
			// for DEBUG
			System.out.println("start getPatient "+patient);
			PreparedStatement ps = con.prepareStatement("select lName, fName, birthdate, street, city, state, zip, doctorSSN from patient where patientSSN=?");
			ps.setString(1, patient.getPatientSSN());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				patient.setlName(rs.getString(1));
				patient.setfName(rs.getString(2));
				patient.setBirthdate(rs.getString(3));
				patient.setStreet(rs.getString(4));
				patient.setCity(rs.getString(5));
				patient.setState(rs.getString(6));
				patient.setZip(rs.getString(7));
				patient.setDoctorSSN(rs.getString(8));
				model.addAttribute("patient", patient);
				// for DEBUG
				System.out.println("end getPatient "+patient);
				return "patient_edit";

			} else {
				model.addAttribute("message", "Patient not found.");
				return "patient_get";
			}

		} catch (SQLException e) {
			System.out.println("SQL error in getPatient "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", patient);
			return "patient_get";
		}
	}
	/*
	 * process profile update for patient.  Change address or doctor.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(Patient patient, Model model) {
		try (Connection con = getConnection()) {

			PreparedStatement ps = con.prepareStatement("update patient set Street=?, City=?, State=?, Zip=?, doctorSSN=? where patientSSN=?");
			ps.setString(1, patient.getStreet());
			ps.setString(2, patient.getCity());
			ps.setString(3, patient.getState());
			ps.setString(4, patient.getZip());
			ps.setString(5, patient.getDoctorSSN());
			ps.setString(6, patient.getPatientSSN());

			int rc = ps.executeUpdate();
			if (rc==1) {
				model.addAttribute("message", "Update successful");
				model.addAttribute("patient", patient);
				return "patient_show";

			}else {
				model.addAttribute("message", "Error. Update was not successful");
				model.addAttribute("patient", patient);
				return "patient_edit";
			}

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", patient);
			return "patient_edit";
		}
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

	public Patient sanitize(Patient patient) {

		Integer patCheck = check(patient.getfName());
		if (patCheck == 1) {
			patient.setMessage("Patient First Name cannot be empty.");
			return patient;
		} else if (patCheck == 2) {
			patient.setMessage("Patient First Name must only be letters.");
			return patient;
		}

		patCheck = check(patient.getlName());
		if (patCheck == 1) {
			patient.setMessage("Patient Last Name cannot be empty.");
			return patient;
		} else if (patCheck == 2) {
			patient.setMessage("Patient Last Name must only be letters.");
			return patient;
		}

		patCheck = checkSSN(patient.getPatientSSN());
		if (patCheck == 1) {
			patient.setMessage("Patient SSN cannot be empty.");
			return patient;
		} else if (patCheck == 2) {
			patient.setMessage("Patient SSN must contain 9 digits.");
			return patient;
		} else if (patCheck == 3) {
			patient.setMessage("Patient SSN must use only numbers.");
			return patient;
		} else if (patCheck == 4 || patCheck == 5) {
			patient.setMessage("Invalid SSN for Patient.");
			return patient;
		}

		patCheck = checkSSN(patient.getDoctorSSN());
		if (patCheck == 1) {
			patient.setMessage("Doctor SSN cannot be empty.");
			return patient;
		} else if (patCheck == 2) {
			patient.setMessage("Doctor SSN must contain 9 digits.");
			return patient;
		} else if (patCheck == 3) {
			patient.setMessage("Doctor SSN must use only numbers.");
			return patient;
		} else if (patCheck == 4 || patCheck == 5) {
			patient.setMessage("Invalid SSN for Doctor.");
			return patient;
		}

		patCheck = checkDate(patient.getBirthdate());
		if (patCheck == 1) {
			patient.setMessage("Patient Birth Date cannot be empty.");
			return patient;
		} else if (patCheck == 2) {
			patient.setMessage("Patient Birth Date must be in the proper format of YYYY-MM-DD.");
			return patient;
		} else if (patCheck == 3) {
			patient.setMessage("Patient Birth Date year cannot be before 1900 or after 2023.");
			return patient;
		} else if (patCheck == 4) {
			patient.setMessage("Patient Birth Date month cannot be less than 1 or more than 12.");
			return patient;
		}	else if (patCheck == 5) {
			patient.setMessage("Patient Birth Date day cannot be less than 1 or more than 31.");
			return patient;
		}

		patient.setMessage("");
		return patient; // ALL is good, send it
	}

	public Integer check(String s) {
		if (s == null || s.length() == 0) {
			return 1;
		}
		int length = s.length();
		for (int i = 0; i < length; i++) {
			if (!Character.isLetter(s.charAt(i))) {
				return 2;
			}
		}
		return 0;
	}

	public Integer checkSSN(String s) {

		if (s == null || s.length() == 0) {
			return 1; // SSN must not be blank
		}

		int length = s.length();

		if (length != 9) {
			return 2; // SSN must be 9 digits
		}

		int digit = 0;

		for (int i = 0; i < length; i++) {

			if (!Character.isDigit(s.charAt(i))) {
				return 3; // SSN must be numbers
			}

			if (i == 0) {
				digit = s.charAt(i) - '0';
				if (digit < 1 || digit > 8) {
					return 4; // Social security numbers never start with a 0 or a 9
				}
			}

			if (i == 4 || i == 8) {
				digit = s.charAt(i) - '0';
				if (digit < 1) {
					return 5; // The middle 2 digits are 01-99 (never 00).  And the last 4 digits are 0001-9999 (never 0000).
				}
			}
		}
		return 0; // SSN checks to be good
	}

	public Integer checkDate(String s) {

		if (s.equals("") ||  s.length() == 0) {
			return 1; // Doctor Start Date cannot be empty
		}

		String[] splitDate = s.split("-", 0);
		if (splitDate.length != 3) {
			return 2; // Doctor Start Date size is incorrect
		}

		Integer year = Integer.parseInt(splitDate[0]);
		if (year < 1900 || year > 2023) {
			return 3; // Doctor Start Date year cannot be before 1900 or after 2023
		}

		Integer month = Integer.parseInt(splitDate[1]);
		if (month < 1 || month > 12) {
			return 4; // Doctor Start Date month cannot be less than 1 or more than 12
		}

		Integer day = Integer.parseInt(splitDate[2]);
		if (day < 1 || day > 31) {
			return 5; // Doctor Start Date day cannot be less than 1 or more than 31
		}

		return 0;
	}
}
