public class TriggerTest extends ExtendM3Trigger {
  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final String[] runOnlyForUsers = [] // Leave the array empty if it should be run for everyone, otherwise add authorized usernames

  TriggerTest(ProgramAPI program, InteractiveAPI interactive, LoggerAPI logger, MICallerAPI miCaller) {
    this.program = program
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
  }

  void main() {
    // Checks if program should run
    if (!shouldRun()) {
      return;
    }
  }

  /**
   * Check if script should run or not
   * @return true if script should run
   */
  boolean shouldRun() {
    if (runOnlyForUsers.length != 0) {
      String currentUser = program.LDAZD.get("RESP").toString().trim()
      boolean authorizedToRun = runOnlyForUsers.contains(currentUser) // checkes if currentUser is contained inside the runOnlyForUsers table
      logger.debug("User {$currentUser} authorization check result was ${authorizedToRun}")
      return authorizedToRun
    }
    return true
  }