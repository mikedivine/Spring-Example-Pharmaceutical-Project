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
	public String createDoctor(Doctor doctor, Model model) {
		
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
	public String updateDoctor(Doctor doctor, Model model) {
		try (Connection con = getConnection();) {

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

}
