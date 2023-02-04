package com.csumb.cst363;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller   
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	/* 
	 * Patient requests form to search for prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new Prescription());
		return "prescription_fill";
	}

	@PostMapping("/prescription/fill")
	public String processFillForm(Prescription prescription,  Model model) {

		// Validates that prescription contains rx, pharmacy name and pharmacy address
		if (prescription.getRX().equals("") || prescription.getPharmacyName().equals("")
				|| prescription.getPharmacyStreet().equals("") || prescription.getPharmacyCity().equals("")
				|| prescription.getPharmacyState().equals("") || prescription.getPharmacyZip().equals("")) {
			model.addAttribute("message", "Do not leave fields empty.");
			return "prescription_fill";
		}

		// Verify uniquely identifies a prescription make sure patient name matches PatientSSN
		try (Connection con = getConnection()) {
			PreparedStatement ps = con.prepareStatement("SELECT pre.RX, pre.qty, pat.Fname, doc.Fname, doc.lname, drug.GenericName, pha.phone, pha.pharmacyID, phadrug.price FROM prescriptions pre, patient pat, doctor doc, drug, pharmacy pha, pharmacydrugs phadrug WHERE pre.drugid = drug.drugID AND pre.drugID = phadrug.drugID AND pre.PatientSSN = pat.PatientSSN AND pha.PharmacyID = phadrug.PharmacyID AND pre.DoctorSSN = doc.DoctorSSN AND pat.lName=? AND pre.RX=? AND pha.Name=?");
			ps.setString(1, prescription.getPatientLName());
			ps.setInt(2, prescription.getRX());
			ps.setString(3, prescription.getPharmacyName());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				// checking RX exists
				if (rs.getInt(1) != prescription.getRX()) {
					model.addAttribute("message", "No matching Prescription.");
					return "prescription_fill";
				}
				// set Patient SSN from matching RX
				prescription.setQty(rs.getInt(2));
				prescription.setPatientFName(rs.getString(3));
				prescription.setDoctorFName(rs.getString(4));
				prescription.setDoctorLName(rs.getString(5));
				prescription.setDrugName(rs.getString(6));
				prescription.setPharmacyPhone(rs.getString(7));
				prescription.setPharmacyID(rs.getInt(8));

				Integer Currency = rs.getInt(9)*prescription.getQty();
				BigDecimal cost = new BigDecimal(Currency).movePointLeft(2);
				prescription.setCost(cost);
			} else {
				model.addAttribute("message", "Prescription Not Found.");
				return "prescription_fill";
			}

		} catch (SQLException e) {
			System.out.println("SQL error in getPrescription "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("Prescription", prescription);
			return "prescription_fill";
		}




		// Update prescription with pharmacyID, name and address and update prescription with today's date.
		try (Connection con = getConnection()) {
			PreparedStatement ps = con.prepareStatement("UPDATE prescriptions SET pharmacyID =?, DateFilled=? WHERE RX=?");
			ps.setInt(1, prescription.getPharmacyID());
			ps.setString(2, new SimpleDateFormat("yyyy-MM_dd").format(new Date()));
			ps.setInt(3, prescription.getRX());

			ps.executeUpdate();

			prescription.setDateFilled(new SimpleDateFormat("yyyy-MM_dd").format(new Date()));
			// display message and prescription information
			model.addAttribute("message", "Prescription filled.");
			model.addAttribute("prescription", prescription);
			return "prescription_show";

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("prescription", prescription);
			return "prescription_fill";
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