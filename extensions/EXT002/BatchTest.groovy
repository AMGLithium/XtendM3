public class BatchTest extends ExtendM3Batch {
  private final LoggerAPI logger
  private final BatchAPI batch
  
  public BatchTest(LoggerAPI logger, BatchAPI batch) {
    this.logger = logger
    this.batch = batch
  }
  
  public void main() { 
    logger.info("Testing EXT002 - executing code - This is the first line of the batch extension")
    logger.info("Still testing - This is the second line of the batch extension with Uuid-gen:" + batch.getReferenceId().get())
    logger.info("Line no. 2 uses a Batch API to generate/retireve job UUID number")
  }
}