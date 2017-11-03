package helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.ArrayUtils;

public class InputValidator {

	public boolean validateInput(String input, String type) {
		switch (type) {
			case "email":
				return emailValid(input);
			case "boolean":
				return booleanValid(input);
			case "date":
				return dateValid(input);
			case "decimal":
				return decimalValid(input);
			case "number":
				return integerValid(input);
			default:
				// type text
				return true;
		}
	}
	
	private boolean emailValid(String email) {
		try {
			InternetAddress tryEmail = new InternetAddress(email);
			tryEmail.validate();
			return true;
		} catch (AddressException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean dateValid(String date) {
		String datePattern = "yyyy-mm-dd";
		
		try {
			DateFormat dateFormat = new SimpleDateFormat(datePattern);
			dateFormat.setLenient(false);
			dateFormat.parse(date);
			return true;
		}
		catch (ParseException exc) {
			return false;
		}
	}
	
	private boolean booleanValid(String bool) {
		String[] validTrue = {"TRUE", "'t'", "'true'", "'y'", "'yes'", "'on'", "'1'"};
		String[] validFalse = {"FALSE", "'f'", "'false'", "'n'", "'no'", "'off'", "'0'"};
		
		return ArrayUtils.contains(validTrue, bool) || ArrayUtils.contains(validFalse, bool);
	}
	
	private boolean decimalValid(String decimal) {
		try {
			Double.parseDouble(decimal);
			return true;
		}
		catch (NumberFormatException exc) {
			return false;
		}
	}
	
	private boolean integerValid(String integer) {
		try {
			Integer.parseInt(integer);
			return true;
		}
		catch (NumberFormatException exc) {
			return false;
		}
	}
}
