/** Name: CREXTPRT.SCE_Batch_CREXTPRT_QMS601PF.groovy
 *
 * This extension is used to allow additional or selected output data for QMS601PF. It will
 * be called with RIDI, RIDN and ITNO and store data in EXT6000 and EXT601
 *
 * Date         Changed By                         Description
 * 21.03.2024   Frank Zahlten (Columbus)           creation
 * 18.06.2024   Frank Zahlten (Columbus)           select already existing data for delete with part key
 * 28.08.2024   Frank Zahlten (Columbus)           use QMSOTS.QTDCCD for the count of output decimals
 * 17.10.2024   Frank Zahlten (Columbus)           check SPEC from QMSOTS via QMSRQS
 *
 */
import java.util.Map;
import M3.DBContainer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

public class SCE_Batch_CREXTPRT_QMS601PF extends ExtendM3Trigger {

	private final MethodAPI method;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final DatabaseAPI database;


	private static final DecimalFormat df1 = new DecimalFormat("0.0");
	private static final DecimalFormat df2 = new DecimalFormat("0.00");
	private static final DecimalFormat df3 = new DecimalFormat("0.000");
	private static final DecimalFormat df4 = new DecimalFormat("0.0000");
	private static final DecimalFormat df5 = new DecimalFormat("0.00000");
	private static final DecimalFormat df6 = new DecimalFormat("0.000000");

	private String printerFile = "";
	private String jobNumber = "";
	private String structure = "";
	private int variant = 0;
	private int rpbk  = 0;
	private HashMap<String, Object> fieldMap;

	private String iBano = "";
	private String iItno = "";
	private String iRidi = "";
	private String iRidn = "";

	private long longRidi = 0l;
	private double roundedPxnum = 0d;

	private String mitxBano = "";
	private String mitxItno = "";
	private String mitxRidn = "";
	private int mitxRidl = 0;
	private int mitxRidx = 0;
	private String mitxWhlo= "";

	private String qmsrqhFaci = "";
	private String qmsrqhQrid = "";
	private int qmsrqhQsta = 0;

	private String qmsotsQtst = "";
	private int qmsotsTsty = 0;
	private int qmsotsQte1 = 0;
	private int qmsotsQte2 = 0;
	private int qmsotsQse1 = 0;
	private int qmsotsQse2 = 0;
	private int qmsotsDccd = 0;
	private double qmsotsEvmx = 0d;
	private double qmsotsEvmn = 0d;

	private String qmsrqtQrid = "";
	private String qmsrqtQtst = "";
	private double qmsrqtSmsz = 0d;
	private double qmsrqtQtrs = 0d;

	private boolean foundQmsrqsSpec = false;
	private String qmsrqsSpec = "";

	private double resultQtrs =  0d;
	private String resultQtkz = " ";

	static int arrMax = 200;
	static int constTTYP31 = 31;
	static int constRORC3 = 3;

	private String[] arrRidn = new String [arrMax];
	private int[] arrRidl = new int [arrMax];
	private int[] arrRidx = new int [arrMax];
	private String[] arrWhlo = new String [arrMax];
	private String[] arrItno = new String [arrMax];
	private String[] arrBano = new String [arrMax];
	private String[] arrFaci = new String [arrMax];
	private String[] arrQrid = new String [arrMax];

	private String[] arr0Ridn = new String [arrMax];
	private int[] arr0Ridl = new int [arrMax];
	private int[] arr0Ridx = new int [arrMax];
	private String[] arr0Itno = new String [arrMax];
	private String[] arr0Bano = new String [arrMax];

	private int i = 0;
	private int intVal = 0;
	private int compareResult = 0;
	private int intCono = program.LDAZD.get("CONO");
	private String iCono = program.LDAZD.get("CONO");
	private String compareValue = "";
	private String strJobn = program.getJobNumber();
	private String[] arrJOBN = new String [10];
	private int regdate=0;
	private int regtime=0;

	public SCE_Batch_CREXTPRT_QMS601PF(MethodAPI method, LoggerAPI logger, ProgramAPI program, DatabaseAPI database) {
		this.logger = logger;
		this.method = method;
		this.program = program;
		this.database = database;
	}


	public void main() {
		logger.debug ("SCE_Batch_CREXTPRT_QMS601PF");
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = now.format(format1);
		DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");
		String formatTime = now.format(format2);

		regdate=Integer.parseInt(formatDate);
		regtime=Integer.parseInt(formatTime);

		workOnQMS601PF();
		reorgEXT601();
	}

	/**
	 * start working for print out data of QMS601
	 */
	void workOnQMS601PF() {
		//check if correct printer file is used for following instructions
		printerFile = method.getArgument(0) as String;
		if (!printerFile.equals("QMS601PF")) {
			logger.debug ("printerfile is not QMS601PF");
			return;
		}
		//check if correct printer block of the printer file is used
		jobNumber = method.getArgument(1) as String;
		structure = method.getArgument(2) as String;
		variant = method.getArgument(3) as int;
		rpbk  = method.getArgument(4) as int;
		//name of the structure
		if (!structure.equals("CD_CUS_02-01")
				&&  !structure.equals("M3_STD_02-01")
				) {
			logger.debug ("QMS601PF - not used structure: " + structure + " rpbk: " + rpbk);
			return;
		}

		logger.debug ("QMS601PF used structure: " + structure + " rpbk: " + rpbk);
		//only start working for QMS601PF xml block 20
		if (rpbk != 20) {
			return;
		}

		//check if the print block is including expected data
		fieldMap = method.getArgument(5) as HashMap;
		if (fieldMap == null) {
			logger.debug ("no data found in 'method.getArgument(5)' ");
			return;
		} else {
			logger.debug ("fieldMap: " + fieldMap);
		}

		iBano = fieldMap.get("&BANO");
		iItno = fieldMap.get("&ITNO");
		iRidn = fieldMap.get("&RIDN");
		iRidi = fieldMap.get("&RIDI");

		logger.debug("iBano: " + iBano
				+ "iItno: " + iItno
				+ "iRidn: " + iRidn
				+ "iRidi: " + iRidi);

		//check iBano
		if (iBano == null) {
			iBano = "";
		}
		//check iItno
		if (iItno == null) {
			iItno = "";
		}
		if (iItno.trim().isEmpty()) {
			logger.debug ("SCE_Batch_CREXTPRT_QMS601PF field ITNO from fieldMap is emtpy");
			return;
		}
		//check iRidn
		if (iRidn == null) {
			iRidn = "";
		}
		if (iRidn.trim().isEmpty()) {
			logger.debug ("SCE_Batch_CREXTPRT_QMS601PF field RIDN from fieldMap is emtpy");
			return;
		}
		//check iRidi
		if (iRidi == null) {
			iRidi = "";
		}
		if (iRidi.trim().isEmpty()) {
			logger.debug ("SCE_Batch_CREXTPRT_QMS601PF field RIDI from fieldMap is emtpy");
			return;
		}
		intVal = iRidi.indexOf('.');

		longRidi = Long.parseLong(iRidi.substring(0, intVal));

		mitxRidn = "";
		mitxRidl = 0l;
		mitxRidx = 0l;
		mitxWhlo = "";
		mitxItno = "";
		mitxBano = "";
		maintainArr600("clear", mitxRidn, mitxRidl, mitxRidx, mitxItno, mitxBano);
		maintainArrays("clear", mitxRidn, mitxRidl, mitxRidx, mitxWhlo, mitxItno, mitxBano);

		DBAction actionMitalo = database.table("MITALO")
				.index("20")
				.selection("MQTTYP", "MQRIDN", "MQRIDL", "MQRIDX", "MQWHLO", "MQBANO", "MQITNO")
				.build();
		DBContainer MITALO = actionMitalo.getContainer();
		MITALO.set("MQCONO", intCono);
		MITALO.set("MQTTYP", constTTYP31);
		MITALO.set("MQRIDN", iRidn);

		actionMitalo.readAll(MITALO, 3, mitaloForRidn);

		DBAction actionMittra = database.table("MITTRA")
				.index("20")
				.selection("MTTTYP", "MTRIDN", "MTRIDL", "MTRIDX", "MTWHLO", "MTBANO", "MTITNO")
				.build();
		DBContainer MITTRA = actionMittra.getContainer();
		MITTRA.set("MTCONO", intCono);
		MITTRA.set("MTITNO", iItno);
		MITTRA.set("MTBANO", iBano);
		MITTRA.set("MTTTYP", constTTYP31);
		MITTRA.set("MTRIDN", iRidn);

		actionMittra.readAll(MITTRA, 5, mittraForRidn);

		logger.debug("after mittraForRidn.readAll ");

		for (int x = 0; x < arrMax; x++) {
			mitxRidn = arrRidn[x];
			if (mitxRidn.isEmpty()) {
				break;
			} else {
				mitxRidl = arrRidl[x];
				mitxRidx = arrRidx[x];
				mitxWhlo = arrWhlo[x];
				mitxItno = arrItno[x];
				mitxBano = arrBano[x];
				getHighestQRIDFromQRH(x, mitxItno, mitxBano);
			}
		}

		for (int x = 0; x < arrMax; x++) {
			mitxRidn = arrRidn[x];
			if (mitxRidn.isEmpty()) {
				break;
			} else {
				deleteEXT600_RIDN(arrRidn[x], arrRidl[x], arrRidx[x], arrItno[x], arrBano[x]);
				deleteEXT601_RIDN(arrRidn[x], arrRidl[x], arrRidx[x], arrItno[x], arrBano[x]);
			}
		}

		for (int x = 0; x < arrMax; x++) {
			mitxRidn = arrRidn[x];
			if (mitxRidn.isEmpty()) {
				break;
			}
			qmsrqhQrid =  arrQrid[x];
			if (!qmsrqhQrid.isEmpty()) {
				mitxRidn = arrRidn[x].trim();
				mitxRidl = arrRidl[x];
				mitxRidx = arrRidx[x];
				mitxWhlo = arrWhlo[x].trim();
				mitxItno = arrItno[x].trim();
				mitxBano =  arrBano[x].trim();
				qmsrqhFaci = arrFaci[x].trim();
				qmsrqhQrid = arrQrid[x].trim();
				intVal = x;
				logger.debug("after getHighestQRIDFromQRH start createEXT601 with "
						+ "RIDN: " + mitxRidn
						+ "RIDL: " + mitxRidl
						+ "RIDX: " + mitxRidx
						+ "WHLO: " + mitxWhlo
						+ "ITNO: " + mitxItno
						+ "FACI: " + qmsrqhFaci
						+ "QRID: " + qmsrqhQrid
						+ "intVal: " + x
						);
				createEXT601FromQMSRQT();
			}
		}
		for (int x = 0; x < arrMax; x++) {
			mitxRidn = arr0Ridn[x];
			if (mitxRidn.isEmpty()) {
				break;
			}
			mitxRidn = arr0Ridn[x].trim();
			mitxRidl = arr0Ridl[x];
			mitxRidx = arr0Ridx[x];
			mitxItno = arr0Itno[x].trim();
			mitxBano = arr0Bano[x].trim();
			intVal = x;
			createEXT600();
		}

		//delete old RIDN related data, not belonging to this JOB
		mitxRidn = "";
		for (int x = 0; x < arrMax; x++) {
			if (!mitxRidn.isEmpty()
					&&  arrRidn[x].trim().contentEquals(mitxRidn)) {
				continue;
			}
			mitxRidn = arrRidn[x].trim();
			if (mitxRidn.isEmpty()) {
				break;
			}

			deleteEXT600_RIDN(arrRidn[x]);
			deleteEXT601_RIDN(arrRidn[x]);
		}
	}

	Closure<?> mitaloForRidn = { DBContainer MITALO ->
		mitxBano = MITALO.get("MQBANO");
		mitxItno = MITALO.get("MQITNO");
		logger.debug("mitaloForRidn check mitxItno: " +  mitxItno + " EQ iItno: " + iItno);
		if (mitxItno.trim().contentEquals(iItno.trim())) {
			mitxRidn = MITALO.get("MQRIDN");
			mitxRidl = MITALO.get("MQRIDL");
			mitxRidx = MITALO.get("MQRIDX");
			mitxWhlo = MITALO.get("MQWHLO");
			maintainArrays("add", mitxRidn, mitxRidl, mitxRidx, mitxWhlo, mitxItno, mitxBano);
		}
	}

	Closure<?> mittraForRidn = { DBContainer MITTRA ->
		mitxBano = MITTRA.get("MTBANO");
		mitxItno = MITTRA.get("MTITNO");
		logger.debug("mittraForRidn check mitxItno: " +  mitxItno + " EQ iItno: " + iItno);
		if (mitxItno.trim().contentEquals(iItno.trim())) {
			mitxRidn = MITTRA.get("MTRIDN");
			mitxRidl = MITTRA.get("MTRIDL");
			mitxRidx = MITTRA.get("MTRIDX");
			mitxWhlo = MITTRA.get("MTWHLO");
			maintainArrays("add", mitxRidn, mitxRidl, mitxRidx, mitxWhlo, mitxItno, mitxBano);
		}
	}

	/**
	 * getHighestQRIDFromQRH()
	 *
	 * get numeric highest (latest) value QRID for already done tests
	 */
	public void getHighestQRIDFromQRH(int x, String itno, String bano) {
		logger.debug("getHighestQRIDFromQRH x/itno/bano " + x + "/" + itno + "/" + bano);
		intVal = x;
		DBAction actionQMSRQH = database.table("QMSRQH")
				.index("30")
				.selection("RHFACI", "RHQRID", "RHQSTA")
				.build();
		DBContainer QMSRQH = actionQMSRQH.getContainer();
		QMSRQH.set("RHCONO", intCono);
		QMSRQH.set("RHITNO", itno);
		QMSRQH.set("RHBANO", bano);

		actionQMSRQH.readAll(QMSRQH, 3, qmsrqhForBANO);
	}

	Closure<?> qmsrqhForBANO = { DBContainer QMSRQH ->
		qmsrqhFaci = QMSRQH.get("RHFACI");
		qmsrqhQrid = QMSRQH.get("RHQRID");
		qmsrqhQsta = QMSRQH.get("RHQSTA");
		logger.debug ("qmsrqhForBANO qmsrqhQID: " + qmsrqhQrid);
		if (qmsrqhQsta == 3) {
			if (arrFaci[intVal].isEmpty()) {
				arrFaci[intVal] = qmsrqhFaci;
				arrQrid[intVal] = qmsrqhQrid;
				logger.debug ("save first QMSRQH FACI/QRID: " + qmsrqhFaci + "/" + qmsrqhQrid);
			} else {

				compareResult = qmsrqhQrid.trim().compareTo(arrQrid[intVal].trim());
				if (compareResult < 0)  {
					arrQrid[intVal] = qmsrqhQrid;
					arrFaci[intVal] = qmsrqhFaci;
					logger.debug ("save comparation result QMSRQH FACI/QRID: " + qmsrqhFaci + "/" + qmsrqhQrid);
				} else {
					logger.debug ("comparation result < 0 FACI/QRID: " + qmsrqhFaci + "/" + qmsrqhQrid);
				}
			}
		} else {
			logger.debug ("qmsrqhForBANO qmsrqhQsta != 3 qmsrqhQsta: " + qmsrqhQsta);
		}
	}

	/**
	 * createEXT600()
	 *
	 * create not existing EXT600 data
	 */
	public void createEXT600() {
		Optional<DBContainer> EXT600 = readEXT600();
		if(!EXT600.isPresent()){
			logger.debug ("not EXT600 isPresent >>>  call addEXT600")
			addEXT600();
		}
	}

	/**
	 * readEXT600()
	 *
	 * returns the EXT600 record or an empty record for the given key data
	 */
	private Optional<DBContainer> readEXT600() {
		logger.debug("start readEXT601 with key jobn: " + strJobn
				+ " ridn: " + mitxRidn
				+ " ridl: " + mitxRidl
				+ " ridx: " + mitxRidx
				+ " itno: " + mitxItno
				+ " bano: " + mitxBano
				);
		DBAction actionEXT600 = database.table("EXT600")
				.index("00")
				.build();
		DBContainer EXT600 = actionEXT600.getContainer();
		// Key value for read
		EXT600.set("EXCONO", intCono);
		EXT600.set("EXRORC", constRORC3);
		EXT600.set("EXRORN", mitxRidn);
		EXT600.set("EXRORL", mitxRidl);
		EXT600.set("EXRORX", mitxRidx);
		EXT600.set("EXITNO", mitxItno);
		EXT600.set("EXBANO", mitxBano);
		EXT600.set("EXJOBN", strJobn);

		// Read
		if (actionEXT600.read(EXT600)) {
			logger.debug("readEXT600 record is existing");
			return Optional.of(EXT600);
		}

		logger.debug("readEXT600 record is not existing");
		return Optional.empty();
	}

	/**
	 * addEXT600
	 *
	 * add a new record to file EXT600
	 */
	private void addEXT600() {
		logger.debug("OutputProcessGY_CMR addEXT600");
		DBAction action = database.table("EXT600")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer EXT600 = action.createContainer();
		// Key value
		EXT600.set("EXJOBN", strJobn);
		EXT600.set("EXCONO", intCono);
		EXT600.set("EXRORC", constRORC3);
		EXT600.set("EXRORN", mitxRidn);
		EXT600.set("EXRORL", mitxRidl);
		EXT600.set("EXRORX", mitxRidx);
		EXT600.set("EXITNO", mitxItno);
		EXT600.set("EXBANO", mitxBano);
		//statistical information
		EXT600.set("EXCHID", program.getUser());
		EXT600.set("EXCHNO", 1);
		EXT600.set("EXRGDT", regdate);
		EXT600.set("EXLMDT", regdate);
		EXT600.set("EXRGTM", regtime);
		action.insert(EXT600);
	}

	/** createEXT601FromQMSRQT
	 * 	use data from internal arrays to read all generated test data from QMSOTS
	 *  use data from found data in QMSOTS, from the arrays and from QMSRQH for getting
	 *  data from QMSRQT
	 *  for existing QMSRQT records create output records in EXT601
	 */
	public void createEXT601FromQMSRQT() {
		logger.debug ("start createEXT601FromQMSRQT RORN/RORL/RORX/ITNO/FACI/QRID "
				+ mitxRidn
				+ "/" + mitxRidl
				+ "/" + mitxRidx
				+ "/" + mitxItno
				+ "/" + qmsrqhFaci
				+ "/" + qmsrqhQrid
				);
		DBAction actionQMSOTS = database.table("QMSOTS")
				.index("20")
				.selection("QTQTST", "QTTSTY", "QTQTE1", "QTQTE2", "QTSPEC", "QTQSE1", "QTQSE2", "QTEVMN", "QTEVMX", "QTDCCD")
				.build();
		DBContainer QMSOTS = actionQMSOTS.getContainer();
		QMSOTS.set("QTCONO", intCono);
		QMSOTS.set("QTRORC", constRORC3);
		QMSOTS.set("QTRORN", mitxRidn);
		QMSOTS.set("QTRORL", mitxRidl);
		QMSOTS.set("QTRORX", mitxRidx);
		QMSOTS.set("QTITNO", mitxItno);

		actionQMSOTS.readAll(QMSOTS, 6, qmsotsForQMSRQT);
	}

	Closure<?> qmsotsForQMSRQT = { DBContainer QMSOTS ->
		qmsotsQtst = QMSOTS.get("QTQTST");
		qmsotsTsty = QMSOTS.get("QTTSTY");
		qmsotsQte1 = QMSOTS.get("QTQTE1");
		qmsotsQte2 = QMSOTS.get("QTQTE2");
		qmsotsQse1 = QMSOTS.get("QTQSE1");
		qmsotsQse2 = QMSOTS.get("QTQSE2");
		qmsotsEvmx = QMSOTS.get("QTEVMX");
		qmsotsEvmn = QMSOTS.get("QTEVMN");
		qmsotsDccd = QMSOTS.get("QTDCCD");

		logger.debug ("start check for SPEC from QMSOTS is connected to QMSRQS qmsrqhQrid/qmsrqhFaci: "
				+ " " + qmsrqhQrid
				+ "/" + qmsrqhFaci);
		foundQmsrqsSpec = false;
		DBAction actionQMSRQS = database.table("QMSRQS")
				.index("00")
				.selection("RSSPEC")
				.build();
		DBContainer QMSRQS = actionQMSRQS.getContainer();
		QMSRQS.set("RSCONO", intCono);
		QMSRQS.set("RSFACI", qmsrqhFaci);
		QMSRQS.set("RSQRID", qmsrqhQrid);
		actionQMSRQS.readAll(QMSRQS, 3, 1, qmsrqsForExt601);
	}

	Closure<?> qmsrqsForExt601 = { DBContainer QMSRQS ->
		qmsrqsSpec = QMSRQS.get("RSSPEC");
		logger.debug ("Closure<?> qmsrqsForEXT601 compare ");
		foundQmsrqsSpec = true;
		ExpressionFactory expression = database.getExpressionFactory("QMSRQT")
		expression = expression.eq("RTCONO", intCono.toString())
				.and(expression.eq("RTQRID", qmsrqhQrid)
				.and(expression.eq("RTQTST", qmsotsQtst)));

		DBAction actionQMSRQT = database.table("QMSRQT")
				.index("20")
				.matching(expression)
				.selection("RTQRID", "RTQTST", "RTSMSZ", "RTQTRS")
				.build();
		DBContainer QMSRQT = actionQMSRQT.getContainer();
		QMSRQT.set("RTCONO", intCono);
		QMSRQT.set("RTFACI", qmsrqhFaci);
		QMSRQT.set("RTITNO", mitxItno);
		QMSRQT.set("RTBANO", mitxBano);

		logger.debug ("read single QMSRQT for FACI: " + qmsrqhFaci
				+ " ITNO: " + mitxItno
				+ " BANO: " + mitxBano
				);
		actionQMSRQT.readAll(QMSRQT, 4, qmsrqtForEXT601);
	}

	Closure<?> qmsrqtForEXT601 = { DBContainer QMSRQT ->
		logger.debug ("Closure<?> qmsrqtForEXT601 after successfull read of QMSRQT record");
		qmsrqtQrid = QMSRQT.get("RTQRID");
		qmsrqtQtst = QMSRQT.get("RTQTST");
		qmsrqtSmsz = QMSRQT.get("RTSMSZ");
		qmsrqtQtrs = QMSRQT.get("RTQTRS");

		logger.debug("Closure<?> qmsrqtForEXT601 compare values are qmsrqtQrid/qmsrqhQrid qmsrqtQtst/qmsotsQtst"
				+ " " + qmsrqtQrid + "/" + qmsrqhQrid
				+ " " + qmsrqtQtst + "/" + qmsotsQtst);

		if (qmsrqtQrid.trim().contentEquals(qmsrqhQrid.trim())
				&&  qmsrqtQtst.trim().contentEquals(qmsotsQtst.trim())) {
			Optional<DBContainer> EXT601 = readEXT601();
			if(!EXT601.isPresent()){
				logger.debug ("not EXT601 isPresent >>>  call addEXT601 QRID/ITNO/BANO"
						+ qmsrqhQrid
						+ " / " + mitxItno
						+ " / " + mitxBano);
				addEXT601();
			} else {
				logger.debug ("EXT601 is already existing QRID/ITNO/BANO"
						+ " "   +  qmsrqhQrid
						+ " / " + mitxItno
						+ " / " + mitxBano);
			}
		}
	}

	/**
	 * readEXT601()
	 *
	 * returns the EXT601 record or an empty record for the given key data
	 */
	private Optional<DBContainer> readEXT601() {
		logger.debug("start readEXT601 with key jobn: " + strJobn
				+ " ridn: " + mitxRidn
				+ " ridl: " + mitxRidl
				+ " ridx: " + mitxRidx
				+ " spec: " + qmsrqsSpec
				+ " qse0: " + getStringValue(qmsotsQse1,qmsotsQse2)
				+ " itno: " + mitxItno
				+ " bano: " + mitxBano
				+ " qtst: " + qmsotsQtst
				+ " tsty: " + qmsotsTsty
				);
		DBAction actionEXT601 = database.table("EXT601")
				.index("00")
				.build();
		DBContainer EXT601 = actionEXT601.getContainer();
		// Key value for read
		EXT601.set("EXJOBN", strJobn);
		EXT601.set("EXCONO", intCono);
		EXT601.set("EXRORC", constRORC3);
		EXT601.set("EXRORN", mitxRidn);
		EXT601.set("EXRORL", mitxRidl);
		EXT601.set("EXRORX", mitxRidx);
		EXT601.set("EXSPEC", qmsrqsSpec);
		EXT601.set("EXQSE0", getStringValue(qmsotsQse1,qmsotsQse2));
		EXT601.set("EXITNO", mitxItno);
		EXT601.set("EXBANO", mitxBano);
		EXT601.set("EXQTST", qmsotsQtst);
		EXT601.set("EXTSTY", qmsotsTsty);

		// Read
		if (actionEXT601.read(EXT601)) {
			logger.debug("readEXT601 record is existing");
			return Optional.of(EXT601);
		}

		logger.debug("readEXT601 record is not existing");
		return Optional.empty();
	}

	/**
	 * addEXT601
	 *
	 * add a new record to file EXT601
	 */
	private void addEXT601() {
		logger.debug("OutputProcessGY_CMR addEXT601");
		DBAction action = database.table("EXT601")
				.index("00")
				.build();
		DBContainer EXT601 = action.createContainer();

		EXT601.set("EXJOBN", strJobn);
		EXT601.set("EXCONO", intCono);
		EXT601.set("EXRORC", constRORC3);
		EXT601.set("EXRORN", mitxRidn);
		EXT601.set("EXRORL", mitxRidl);
		EXT601.set("EXRORX", mitxRidx);
		EXT601.set("EXSPEC", qmsrqsSpec);
		EXT601.set("EXQSE0", getStringValue(qmsotsQse1,qmsotsQse2));
		EXT601.set("EXITNO", mitxItno);
		EXT601.set("EXBANO", mitxBano);
		EXT601.set("EXQTST", qmsotsQtst);
		EXT601.set("EXTSTY", qmsotsTsty);
		//non key fields
		EXT601.set("EXQTE0", getStringValue(qmsotsQte1,qmsotsQte2));
		EXT601.set("EXEVMX", qmsotsEvmx);
		roundedPxnum = roundedResult(qmsotsDccd, qmsotsEvmx);
		EXT601.set("EXSVMX", getStringValue(qmsotsDccd, roundedPxnum));
		EXT601.set("EXEVMN", qmsotsEvmn);
		roundedPxnum = roundedResult(qmsotsDccd, qmsotsEvmn);
		EXT601.set("EXSVMN", getStringValue(qmsotsDccd, roundedPxnum));
		EXT601.set("EXSMSZ", qmsrqtSmsz);
		EXT601.set("EXSSSZ", getStringValue(qmsotsDccd, qmsrqtSmsz));
		EXT601.set("EXQRID", qmsrqhQrid);
		EXT601.set("EXQTRS", qmsrqtQtrs);
		EXT601.set("EXSTRS", getStringValue(qmsotsDccd, qmsrqtQtrs));
		EXT601.set("EXDCCD", qmsotsDccd);
		if (qmsrqtSmsz > qmsrqtQtrs
				&&  qmsrqtSmsz != 0d) {
			resultQtrs = qmsrqtSmsz;
			resultQtkz = "<";
		} else {
			resultQtrs = qmsrqtQtrs;
			resultQtkz = " ";
		}
		EXT601.set("EXQTRN", resultQtrs); //-
		roundedPxnum = roundedResult(qmsotsDccd, resultQtrs);
		EXT601.set("EXQTRT", getStringValue(qmsotsDccd, roundedPxnum));
		EXT601.set("EXQTKZ", resultQtkz);
		//statistical information
		EXT601.set("EXCHID", program.getUser());
		EXT601.set("EXCHNO", 1);

		EXT601.set("EXRGDT", regdate);
		EXT601.set("EXLMDT", regdate);
		EXT601.set("EXRGTM", regtime);
		action.insert(EXT601);
		maintainArr600("add", mitxRidn, mitxRidl, mitxRidx, mitxItno, mitxBano);
	}

	/** maintain array used to store from DB gotten data
	 *  input  parameter
	 * 	@param	operation - allowed are "clear", "exist" and "add"
	 * 	@param  detailed field information
	 */
	boolean maintainArrays(String operation,
			String ridn,
			int ridl,
			int ridx,
			String whlo,
			String itno,
			String bano) {

		logger.debug("maintainArrays Operation: " + operation
				+ " ridn: "  + ridn
				+ " ridl: " + String.valueOf(ridl)
				+ " ridx: " + String.valueOf(ridx)
				+ " whlo: " + whlo
				+ " itno: " + itno
				+ " bano: " + bano);

		if (operation == "clear") {
			for (int i = 0; i < arrMax; i++) {
				arrRidn[i] = "";
				arrRidl[i] = 0;
				arrRidx[i] = 0;
				arrWhlo[i] = "";
				arrItno[i] = "";
				arrBano[i] = "";
				arrFaci[i] = "";
				arrQrid[i] = 0;
			}
			return true;
		}

		if (operation == "add") {
			if (ridn.isEmpty()) {
				logger.debug("maintainArrays ridn is empty");
				return true;
			}
			for (int x = 0; x < arrMax; x++) {
				if (arrRidn[x].contentEquals(ridn.trim())
						&&  arrWhlo[x].contentEquals(whlo.trim())
						&&  arrRidl[x] == ridl
						&&  arrRidn[x] == ridn
						&&  arrItno[x].contentEquals(itno.trim())
						&&  arrBano[x].contentEquals(bano.trim())) {
					return true;
				}
				if (arrRidn[x].isBlank()
						&&  arrWhlo[x].isBlank()
						&&  arrRidl[x] == 0l
						&&  arrRidx[x] == 0l) {
					arrRidn[x] = ridn.trim();
					arrWhlo[x] = whlo.trim();
					arrRidl[x] = ridl;
					arrRidx[x] = ridx;
					arrItno[x] = itno.trim();
					arrBano[x] = bano.trim();
					logger.debug("maintainArrays ADD for RIDN/RIDL/RIDX/WHLO/ITNO/BANO: "
							+ ridn.trim() + '/'
							+ String.valueOf(ridl) + '/'
							+ String.valueOf(ridx) + '/'
							+ whlo + '/'
							+ itno + '/'
							+ bano +" executed." );
					return true;
				}
			}
			return false;
		}
	}

	/** maintain array used to store from DB gotten data
	 *  input  parameter
	 * 	@param	operation - allowed are "clear", "exist" and "add"
	 * 	@param  detailed field information
	 */
	boolean maintainArr600(String operation,
			String ridn,
			int ridl,
			int ridx,
			String itno,
			String bano) {

		logger.debug("maintainArr600 Operation: " + operation
				+ " ridn: "  + ridn
				+ " ridl: " + String.valueOf(ridl)
				+ " ridx: " + String.valueOf(ridx)
				+ " itno: " + itno
				+ " bano: " + bano);

		if (operation == "clear") {
			for (int i = 0; i < arrMax; i++) {
				arr0Ridn[i] = "";
				arr0Ridl[i] = 0;
				arr0Ridx[i] = 0;
				arr0Itno[i] = "";
				arr0Bano[i] = "";
			}
			return true;
		}

		if (operation == "add") {
			if (ridn.isEmpty()) {
				logger.debug("maintainArr600 ridn is empty");
				return true;
			}
			for (int x = 0; x < arrMax; x++) {
				if (arr0Ridn[x].contentEquals(ridn.trim())
						&&  arr0Ridl[x] == ridl
						&&  arr0Ridn[x] == ridn
						&&  arr0Itno[x].contentEquals(itno.trim())
						&&  arr0Bano[x].contentEquals(bano.trim())) {
					logger.debug("maintainArr600 contentEquals arr0Rridn/ridl/itno/bano:"
							+ arr0Ridn[x]
							+ arr0Ridl[x]
							+ arr0Itno[x]
							+ arr0Bano[x]);
					return true;
				}
				if (arr0Ridn[x].isBlank()
						&&  arr0Ridl[x] == 0l
						&&  arr0Ridx[x] == 0l) {
					arr0Ridn[x] = ridn.trim();
					arr0Ridl[x] = ridl;
					arr0Ridx[x] = ridx;
					arr0Itno[x] = itno.trim();
					arr0Bano[x] = bano.trim();
					logger.debug("maintainArr600 add arr0Rridn/ridl/itno/bano:"
							+ arr0Ridn[x]
							+ arr0Ridl[x]
							+ arr0Itno[x]
							+ arr0Bano[x]);
					return true;
				}
			}
			return false;
		}
	}
	
	public double roundedResult(int dccd, double doublePxnum) {
		logger.debug("roundedResult Start dccd/doublePxnum: " + dccd + "/"+ doublePxnum.toString());
		
		double d = Math.pow(10, dccd);
		double returnPxnum = Math.round(doublePxnum * d) / d;
		
		logger.debug("roundedResult result dccd/doublePxnum: " + dccd + "/"+ returnPxnum.toString());
		return returnPxnum;
	}
	

	/**
	 *   getStringValue to return numeric digits with leading zero
	 */
	public String getStringValue (int e1Value, int e2Value) {
		String strField1 = String.valueOf(e1Value).trim();
		String strField2 = String.valueOf(e2Value).trim();
		String strResult = "";

		while(strField1.length() < 8){
			strField1  = "0" + strField1;
		}
		while(strField2.length() < 4){
			strField2  = "0" + strField2;
		}

		strResult = strField1 + strField2;

		return strResult;
	}

	/**
	 *   getStringValue for given DCCD and PXNUM
	 */
	public String getStringValue(int dccd, double pxnum) {
		

		if (dccd > 6) {
			dccd = 6;
		}

		double doublePxnum = pxnum;
		int intPxnum = (int) pxnum;
		String returnVal = "";

		if (dccd == 1) {
			doublePxnum = Double.parseDouble(df1.format(doublePxnum));
		}
		if (dccd == 2) {
			doublePxnum = Double.parseDouble(df2.format(doublePxnum));
		}
		if (dccd == 3) {
			doublePxnum = Double.parseDouble(df3.format(doublePxnum));
		}
		if (dccd == 4) {
			doublePxnum = Double.parseDouble(df4.format(doublePxnum));
		}
		if (dccd == 5) {
			doublePxnum = Double.parseDouble(df5.format(doublePxnum));
		}
		if (dccd == 6) {
			doublePxnum = Double.parseDouble(df6.format(doublePxnum));
		}
		if (dccd != 0) {
			returnVal =  String.valueOf(doublePxnum).trim();
		} else {
			returnVal =  String.valueOf(intPxnum).trim();
		}
		logger.debug("getStringValue dccd: ${dccd} pxnum = ${pxnum} returnVal = ${returnVal}" );
		return returnVal;
	}

	/**
	 *  Remove not longer use data from EXT600 and EXT601
	 *  - based on the record creation date
	 */
	void reorgEXT601() {
		for (i = 0; i < 10; i++ ) {
			arrJOBN[i] = "";
		}
		logger.debug("SCE_Batch_CREXTPRT_QMS601PF/reorgEXT601 Cono: " + intCono);
		DBAction action = database.table("EXT601")
				.index("10")
				.selection("EXRGDT", "EXJOBN")
				.build();
		DBContainer EXT601 = action.getContainer();

		EXT601.set("EXCONO", intCono);

		action.readAll(EXT601, 1, selectDeleteJobn);

		for (i = 0; i < 10; i++ ) {
			if (!arrJOBN[i].isEmpty()) {
				logger.debug("SCE_Batch_CREXTPRT_QMS601PF/deleteEXT62 jobn: " + arrJOBN[i]);
				deleteEXT600_JOBN(arrJOBN[i]);
				deleteEXT601_JOBN(arrJOBN[i]);
			}
		}
	}

	Closure<?>  selectDeleteJobn  = { DBContainer ext ->
		String jobn = ext.get("EXJOBN");
		int rgdt = ext.get("EXRGDT");
		logger.debug("SCE_Batch_CREXTPRT_QMS601PF/selectDeleteJobn jobn: " + jobn + " rgdt: " + rgdt);
		if (rgdt < regdate) {
			for (i = 0; i < 10; i++ ) {
				if (arrJOBN[i].contentEquals(jobn))  {
					break;
				}
				if (arrJOBN[i].isEmpty()) {
					arrJOBN[i] = (jobn);
					break;
				}
			}
		}
	}

	/**
	 * deleteEXT600_RIDN
	 *
	 * delete not longer used data from dynamic table EXT600, selected by RIDN, position information and item
	 */
	void deleteEXT600_RIDN(String rorn, int rorl, int rorx, String itno, String bano) {
		logger.debug("SCE_Batch_CREXTPRT_QMS601PF/deleteEXT600 rorn: " + rorn
				+ " rorl: " + rorl
				+ " rorx: " + rorx
				+ " itno: " + itno
				+ " bano: " + bano);
		mitxItno = itno;
		mitxBano = bano;
		DBAction actionEXT600 = database.table("EXT600")
				.index("00")
				.selection("EXITNO", "EXBANO")
				.build();
		DBContainer EXT600 = actionEXT600.getContainer();
		EXT600.set("EXCONO", intCono);
		EXT600.set("EXRORC", constRORC3);
		EXT600.set("EXRORN", rorn);
		EXT600.set("EXRORL", rorl);
		EXT600.set("EXRORX", rorx);
		EXT600.set("EXITNO", itno);
		EXT600.set("EXBANO", bano);

		actionEXT600.readAllLock(EXT600, 7, deleteCallBack600);
	}

	Closure<?> deleteCallBack600 = { LockedResult EXT600 ->
		logger.debug("deleteCallBack EXT601 ITNO/BANO: " + EXT600.get("EXITNO") + " / " + EXT600.get("EXBANO"));
		EXT600.delete();
	}

	/**
	 * deleteEXT600_RIDN
	 *
	 * delete not longer used data from dynamic table EXT600, selected by RIDN
	 */
	void deleteEXT600_RIDN(String rorn) {
		DBAction actionEXT600 = database.table("EXT600")
				.index("00")
				.selection("EXJOBN")
				.build();
		DBContainer EXT600 = actionEXT600.getContainer();
		EXT600.set("EXCONO", intCono);
		EXT600.set("EXRORC", constRORC3);
		EXT600.set("EXRORN", rorn);

		actionEXT600.readAllLock(EXT600, 3, deleteCallBack600_Jobn);
	}

	Closure<?> deleteCallBack600_Jobn = { LockedResult EXT600 ->
		compareValue = EXT600.get("EXJOBN");
		compareResult = strJobn.trim().compareTo(compareValue.trim());
		if (compareResult != 0)  {
			logger.debug("deleteCallBack EXT601_Jobn: " + compareValue);
			logger.debug("deleteCallBack compareResult EXJOBN vs strJobn != 0 " + EXT600.get("EXJOBN") + " / " + strJobn);
			EXT600.delete();
		}
	}

	/**
	 * deleteEXT601_RIDN
	 *
	 * delete not longer used data from dynamic table EXT601, selected by RIDN, position information and item
	 */
	void deleteEXT601_RIDN(String rorn, int rorl, int rorx, String itno, String bano) {
		logger.debug("SCE_Batch_CREXTPRT_QMS601PF/deleteEXT601 rorn: " + rorn
				+ " rorl: " + rorl
				+ " rorx: " + rorx
				+ " itno: " + itno
				+ " bano: " + bano);
		mitxItno = itno;
		mitxBano = bano;
		DBAction actionEXT601 = database.table("EXT601")
				.index("20")
				.build();
		DBContainer EXT601 = actionEXT601.getContainer();
		EXT601.set("EXCONO", intCono);
		EXT601.set("EXRORC", constRORC3);
		EXT601.set("EXRORN", rorn);
		EXT601.set("EXRORL", rorl);
		EXT601.set("EXRORX", rorx);
		EXT601.set("EXITNO", itno);
		EXT601.set("EXBANO", bano);
		actionEXT601.readAllLock(EXT601, 7, deleteCallBack601);
	}



	Closure<?> deleteCallBack601 = { LockedResult EXT601 ->
		logger.debug(" deleteCallBack EXT601 ITNO: " + EXT601.get("EXITNO"));
		EXT601.delete();
	}

	/**
	 * deleteEXT601_RIDN
	 *
	 * delete not longer used data from dynamic table EXT601, selected by RIDN
	 */
	void deleteEXT601_RIDN(String rorn) {
		logger.debug("SCE_Batch_CREXTPRT_QMS601PF/deleteEXT601 rorn: " + rorn);
		DBAction actionEXT601 = database.table("EXT601")
				.index("00")
				.selection("EXJOBN")
				.build();
		DBContainer EXT601 = actionEXT601.getContainer();
		EXT601.set("EXCONO", intCono);
		EXT601.set("EXRORC", constRORC3);
		EXT601.set("EXRORN", rorn);
		actionEXT601.readAllLock(EXT601, 3, deleteCallBack601_Jobn);
	}



	Closure<?> deleteCallBack601_Jobn = { LockedResult EXT601 ->
		logger.debug(" deleteCallBack EXT601 ITNO: " + EXT601.get("EXITNO"));
		compareValue = EXT601.get("EXJOBN");
		compareResult = strJobn.trim().compareTo(compareValue.trim());

		if (compareResult != 0)  {
			logger.debug("deleteCallBack_Jobn: " + compareValue);
			EXT601.delete();
		}
	}

	/**
	 * deleteEXT600_JOBN
	 *
	 * delete not longer used data from dynamic table EXT600, selected by JOBN
	 */
	void deleteEXT600_JOBN(String jobn) {
		logger.debug("OutputProcessGY_CMR/deleteEXT600 JOBN: " +jobn);
		DBAction query1 = database.table("EXT600")
				.index("10")
				.selection("EXITNO", "EXBANO")
				.build();

		DBContainer container1 = query1.getContainer();
		container1.set("EXCONO", intCono);
		container1.set("EXJOBN", jobn);
		query1.readAllLock(container1, 2, deleteCallBack1);
	}


	/**
	 * deleteEXT601_JOBN
	 *
	 * delete not longer used data from dynamic table EXT601, selected by JOBN
	 */
	void deleteEXT601_JOBN(String jobn) {
		logger.debug("OutputProcessGY_CMR/deleteEXT601 JOBN: " + jobn);
		DBAction query1 = database.table("EXT601")
				.index("10")
				.selection("EXJOBN")
				.build();

		DBContainer container1 = query1.getContainer();
		container1.set("EXCONO", intCono);
		container1.set("EXJOBN", jobn);
		query1.readAllLock(container1, 2, deleteCallBack1);
	}

	Closure<?> deleteCallBack1 = { LockedResult lockedResult ->
		logger.debug(" delete deleteCallBack1: " + lockedResult.get("EXJOBN"));
		lockedResult.delete();
	}
}
