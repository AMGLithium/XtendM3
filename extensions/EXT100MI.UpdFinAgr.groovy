/** Name: EXT100MI.UpdFinAgr.groovy
 *
 * The API transaction EXT100MI.UpdFinAgr is used to update the status
 * of an existing record in DB table FFAMAS (AGS100) to 40 or 90.
 * 
 *
 *  author    Frank Zahlten (frank.zahlten@columbusglobal.com)
 *  date      2024-10-29
 *  version   1.0
 *
 *  1.0  2024-10-29   Frank Zahlten     initial creation
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import M3.DBContainer;

public class UpdFinAgr extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final ProgramAPI program;
	private final LoggerAPI logger;

	private DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
	private DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");
	
	private String iCono = "";
	private int intCono = 0;
	private String iDivi = "";
	private String iSpyn = "";
	private String iFagn = "";
	private String iFahs = "";
	private int iIntFahs = 0;
	
	private int todaysDate = 0;

	public UpdFinAgr(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.program = program;
		this.logger = logger;
	}

	public void main() {
		getInputVariables();
		if (!validateInput()) {
			logger.debug("EXT100MI/UpdFinAgr validateInput ended with false!");
			mi.write();
			return;
		}
		updateDbRecord();
	}

	/**
	 *  getInputVariables
	 *
	 *  set values to the required variables configured for the method using MIAPI
	 */
	private void getInputVariables() {
		intCono = program.LDAZD.get("CONO");
		iCono =  program.LDAZD.get("CONO");
		iDivi = mi.inData.get("DIVI");
		iSpyn = mi.inData.get("SPYN");
		iFagn = mi.inData.get("FAGN");
		iFahs = mi.inData.get("FAHS");

		logger.debug("EXT100MI/UpdFinAgr input field intCono : " + intCono);
		logger.debug("EXT100MI/UpdFinAgr input field DIVI: " +  iDivi);
		logger.debug("EXT100MI/UpdFinAgr input field SPYN: " + iSpyn);
		logger.debug("EXT100MI/UpdFinAgr input field FAGN: " + iFagn);
		logger.debug("EXT100MI/UpdFinAgr input field FAHS: " + iFahs);
	}


	/**
	 *  validateInput
	 *
	 *  Check if fields are null, empty, or whitespace (using trim); if so, log an error and return false.
	 */
	private boolean validateInput() {
		logger.debug("EXT100MI/UpdFinAgr validateInput started");
		if (!iDivi?.trim()) {
			iDivi = "   ";
		}
		if (!iSpyn?.trim()) {
			mi.error("Zahler " + iSpyn + " ist nicht zulässig");
			return false;
		}
		if (!iFagn?.trim()) {
			mi.error("Agreement-Nr. " + iFagn + " ist nicht zulässig");
			return false;
		}
		if (!iFahs?.trim()) {
			mi.error("neuer Agreementstatus " + iFahs + " ist nicht zulässig");
			return false;
		}
		if (iFahs.trim().contentEquals("40")
		||  iFahs.trim().contentEquals("90")) {
			iIntFahs = Integer.parseInt(iFahs);
		} else {
			mi.error("nur Statusangabe 40 oder 90 sind zulässig");
			return false;
		}
		
		LocalDateTime now = LocalDateTime.now();
		String formatDate = now.format(format1);
		String formatTime = now.format(format2);
		todaysDate = Integer.parseInt(formatDate);

		DBAction action = database.table("FFAMAS")
				.selection("FMFAED", "FMFAHS")
				.index("00")
				.build();
		DBContainer container = action.createContainer();
		container.set("FMCONO", intCono);
		container.set("FMDIVI", iDivi);
		container.set("FMSPYN", iSpyn);
		container.set("FMFAGN", iFagn);
		if (!action.read(container)) {
			mi.error("AGS100 record isn't existing DIVI/SPYN/FAGN"
				 + " " + iDivi
				 + " " + iSpyn
				 + " " + iFagn);
			return false;
		}
		
		int intFaed = container.get("FMFAED");
		int intFahs = container.get("FMFAHS");
	
		if (intFahs >=  90) {
			mi.error("Der Finanzvertrag hat bereits Status gleich oder größer 90. Bitte Ansicht aktualisieren.");
			return false;
		}
		if (intFahs >=  40
		&&  iIntFahs == 40) {
			mi.error("Der Finanzvertrag hat bereits Status gleich oder größer 40. Bitte Ansicht aktualisieren.");
			return false;
		}

		return true;
	}

	/**
	 * UpdateDbRecord
	 *
	 * Read and lock the FFAMAS record and updated the status field FMFAHS
	 */
	private void updateDbRecord(){
		logger.debug("EXT100MI/UpdFinAgr updateDbRecord started");
		DBAction action = database
				.table("FFAMAS")
				.index("00")
				.selection("FMCHNO")
				.build();
		DBContainer container = action.createContainer();
		container.set("FMCONO", intCono);
		container.set("FMDIVI", iDivi);
		container.set("FMSPYN", iSpyn);
		container.set("FMFAGN", iFagn);

		action.readLock(container, { LockedResult lockedResult ->
			int intChno = lockedResult.get("FMCHNO");
			intChno++;
			lockedResult.set("FMFAHS", iIntFahs);
			lockedResult.set("FMLMDT", todaysDate);
			lockedResult.set("FMCHNO", intChno);
			lockedResult.set("FMCHID", program.getUser());
			lockedResult.update();
		});
	}
}
