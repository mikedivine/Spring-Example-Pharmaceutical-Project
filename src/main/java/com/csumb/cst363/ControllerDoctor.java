package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/*
 * Controller class for doctor registration and profile update.
 */
@Controller
public class ControllerDoctor {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Request for new doctor registration form.
	 */
	@GetMapping("/doctor/register")
	public String newDoctor(Model model) {
		// return blank form for new doctor registration
		model.addAttribute("doctor", new Doctor());
		return "doctor_register";
	}
	
	/*
	 * Process doctor registration.
	 */
	@PostMapping("/doctor/register")
	public String createDoctor(Doctor doctor1, Model model) {

		Doctor doctor = sanitize(doctor1);

		if (!doctor.getMessage().equals(""))  {
			model.addAttribute("message", doctor.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_register";
		}
		
		try (Connection con = getConnection()) {
			PreparedStatement ps = con.prepareStatement("insert into doctor(LName, FName, specialty, startDate, doctorSSN) values(?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, doctor.getlName());
			ps.setString(2, doctor.getfName());
			ps.setString(3, doctor.getSpecialty());
			ps.setString(4, doctor.getStartDate());
			ps.setString(5, doctor.getDoctorSSN());
			
			ps.executeUpdate();
		
			// display message and patient information
			model.addAttribute("message", "Registration successful.");
			model.addAttribute("doctor", doctor);
			return "doctor_show";
			
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_register";	
		}
	}
	
	/*
	 * Request blank form for doctor search.
	 */
	@GetMapping("/doctor/get")
	public String getDoctor(Model model) {
		// return form to enter doctor id and name
		model.addAttribute("doctor", new Doctor());
		return "doctor_get";
	}
	
	/*
	 * Search for doctor by SSN
	 */
	@PostMapping("/doctor/get")
	public String getDoctor(Doctor doctor, Model model) {

		Integer drCheck = checkSSN(doctor.getDoctorSSN());
		doctor.setMessage("");
		if (drCheck == 1) {
			model.addAttribute("message", "Doctor SSN cannot be empty.");
			return "doctor_get";
		} else if (drCheck == 2) {
			model.addAttribute("message", "Doctor SSN must contain 9 digits.");
			return "doctor_get";
		} else if (drCheck == 3) {
			model.addAttribute("message", "Doctor SSN must use only numbers.");
			return "doctor_get";
		} else if (drCheck == 4 || drCheck == 5) {
			model.addAttribute("message", "Invalid SSN.");
			return "doctor_get";
		}

		if (!doctor.getMessage().equals(""))  {
			model.addAttribute("message", doctor.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_get";
		}
		
		try (Connection con = getConnection()) {
			// for DEBUG 
			System.out.println("start getDoctor "+doctor);
			PreparedStatement ps = con.prepareStatement("select lName, fName, specialty, startDate from doctor where doctorSSN=?");
			ps.setString(1, doctor.getDoctorSSN());
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doctor.setlName(rs.getString(1));
				doctor.setfName(rs.getString(2));
				doctor.setStartDate(rs.getString(4));
				doctor.setSpecialty(rs.getString(3));
				model.addAttribute("doctor", doctor);
				// for DEBUG 
				System.out.println("end getDoctor "+doctor);
				return "doctor_show";
				
			} else {
				model.addAttribute("message", "Doctor not found.");
				return "doctor_get";
			}
						
		} catch (SQLException e) {
			System.out.println("SQL error in getDoctor "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_get";
		}
	}
	
	/*
	 * search for doctor by SSN.
	 */
	@GetMapping("/doctor/edit/{doctorSSN}")
	public String getDoctor(@PathVariable String doctorSSN, Model model) {

		Doctor doctor = new Doctor();
		doctor.setDoctorSSN(doctorSSN);

		Integer drCheck = checkSSN(doctorSSN);
		if (drCheck == 1) {
			model.addAttribute("message", "Doctor SSN cannot be empty.");
			return "doctor_get";
		} else if (drCheck == 2) {
			model.addAttribute("message", "Doctor SSN must contain 9 digits.");
			return "doctor_get";
		} else if (drCheck == 3) {
			model.addAttribute("message", "Doctor SSN must use only numbers.");
			return "doctor_get";
		} else if (drCheck == 4 || drCheck == 5) {
			model.addAttribute("message", "Invalid SSN.");
			return "doctor_get";
		}

		try (Connection con = getConnection()) {

			PreparedStatement ps = con.prepareStatement("select lName, fName, specialty, startDate from doctor where doctorSSN="+doctorSSN);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doctor.setlName(rs.getString(1));
				doctor.setfName(rs.getString(2));
				doctor.setSpecialty(rs.getString(3));
				doctor.setStartDate(rs.getString(4));

				model.addAttribute("doctor", doctor);
				return "doctor_edit";
			} else {
				model.addAttribute("message", "Doctor not found.");
				model.addAttribute("doctor", doctor);
				return "doctor_get";
			}
			
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_get";
			
		}
	}
	
	/*
	 * process profile update for doctor.  Change specialty or year of practice.
	 */
	@PostMapping("/doctor/edit")
	public String updateDoctor(Doctor doctor1, Model model) {

		Doctor doctor = sanitize(doctor1);

		if (!doctor.getMessage().equals(""))  {
			model.addAttribute("message", doctor.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_edit";
		}

		try (Connection con = getConnection()) {

			PreparedStatement ps = con.prepareStatement("update doctor set specialty=?, startDate=? where doctorSSN=?");
			ps.setString(1, doctor.getSpecialty());
			ps.setString(2, doctor.getStartDate());
			ps.setString(3, doctor.getDoctorSSN());
			
			int rc = ps.executeUpdate();
			if (rc==1) {
				model.addAttribute("message", "Update successful");
				model.addAttribute("doctor", doctor);
				return "doctor_show";
				
			}else {
				model.addAttribute("message", "Error. Update was not successful");
				model.addAttribute("doctor", doctor);
				return "doctor_edit";
			}
				
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_edit";
		}
	}
	
	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

	public Doctor sanitize(Doctor doctor) {

		Integer drCheck = check(doctor.getfName());
		if (drCheck == 1) {
			doctor.setMessage("Doctor First Name cannot be empty.");
			return doctor;
		} else if (drCheck == 2) {
			doctor.setMessage("Doctor First Name must only be letters.");
			return doctor;
		}

		drCheck = check(doctor.getlName());
		if (drCheck == 1) {
			doctor.setMessage("Doctor Last Name cannot be empty.");
			return doctor;
		} else if (drCheck == 2) {
			doctor.setMessage("Doctor Last Name must only be letters.");
			return doctor;
		}

		drCheck = check(doctor.getSpecialty());
		if (drCheck == 1) {
			doctor.setMessage("Doctor Specialty cannot be empty.");
			return doctor;
		} else if (drCheck == 2) {
			doctor.setMessage("Doctor Specialty must only be letters.");
			return doctor;
		}

		drCheck = checkSSN(doctor.getDoctorSSN());
		if (drCheck == 1) {
			doctor.setMessage("Doctor SSN cannot be empty.");
			return doctor;
		} else if (drCheck == 2) {
			doctor.setMessage("Doctor SSN must contain 9 digits.");
			return doctor;
		} else if (drCheck == 3) {
			doctor.setMessage("Doctor SSN must use only numbers.");
			return doctor;
		} else if (drCheck == 4 || drCheck == 5) {
			doctor.setMessage("Invalid SSN.");
			return doctor;
		}

		drCheck = checkDate(doctor.getStartDate());
		if (drCheck == 1) {
			doctor.setMessage("Doctor Start Date cannot be empty.");
			return doctor;
		} else if (drCheck == 2) {
			doctor.setMessage("Doctor Start Date must be in the proper format of YYYY-MM-DD.");
			return doctor;
		} else if (drCheck == 3) {
			doctor.setMessage("Doctor Start Date year cannot be before 1900 or after 2023.");
			return doctor;
		} else if (drCheck == 4) {
			doctor.setMessage("Doctor Start Date month cannot be less than 1 or more than 12.");
			return doctor;
		}	else if (drCheck == 5) {
			doctor.setMessage("Doctor Start Date day cannot be less than 1 or more than 31.");
			return doctor;
		}

		doctor.setMessage("");
		return doctor; // ALL is good, send it
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
