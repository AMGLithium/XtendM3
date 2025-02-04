public class TransactionTest extends ExtendM3Transaction {
  private final MIAPI mi;
  private final MICallerAPI miCaller;
  private final DatabaseAPI database;
  private final LoggerAPI logger;
  
  public TransactionTest(MIAPI mi, MICallerAPI miCaller, DatabaseAPI database, LoggerAPI logger) {
    this.mi = mi;
    this.miCaller = miCaller;
    this.database = database;
    this.logger = logger;
  }
  
  public void main() {

    Closure<?> callback = { Map <String, String> out ->
      if (out.error != null) {
        mi.error(out.errorMessage);
        return;
      }

      /* Setting up out data in transaction using MI API
      Remove .trim on input fields as this causes errors if fields are null
      */
      mi.outData.put("OUT1", out.get("OUT1").trim());
      mi.outData.put("OUT2", out.get("OUT2").trim());
      mi.outData.put("OUT3", out.get("OUT3").trim());
      mi.outData.put("OUT4", out.get("OUT4").trim());
      mi.outData.put("OUT5", out.get("OUT5").trim());

      //Setting container parameters
      String out6 = "";
      String out7 = "";
      if (out.get("OUT3").toInteger() == 3) {
        DBAction query = database.table("CONT01")
          .index("00")
          .selection("OQOUT1","OQOUT2","OQOUT3","OQOUT4").build();
        DBContainer CONT01 = query.getContainer();
        CONT01.set("OQOUT1", out.get("OUT1").trim().toInteger());
        CONT01.set("OQOUT2", 1);
        CONT01.set("OQOUT3", out.get("OUT3").trim().toInteger());
      }
      mi.outData.put("OUT6", out6);
      mi.outData.put("OUT7", out7);
      mi.write();
    }

    // Transaction In Data parametrers
    Map<String, String> params = [ "OUT2": mi.inData.get("OUT2"),
                   "IND1": mi.inData.get("PAR1"),
                   "IND2": mi.inData.get("PAR2"),
                   "IND3": mi.inData.get("PAR3"),
                   "IND4": mi.inData.get("PAR4"),
                   "IND5": mi.inData.get("PAR5"),
    ];
    miCaller.call("MWS420MI", "TransactionTemplate", params, callback);
  }
}