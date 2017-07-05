package org.commonvox.insight.anomaly_detector;

/**
 * Used for command-line invocation of batch runs.
 *
 */
public class App
{
  /**
   * To be invoked with three mandatory arguments: batch-file-path, stream-file-path, and
   * flagged-purchases-path.
   *
   * @param args three mandatory arguments: batch-file-path, stream-file-path, and
   * flagged-purchases-path
   * @throws Exception miscellaneous
   */
  public static void main( String[] args ) throws Exception
  {
    if (args == null || args.length < 3) {
      throw new  IllegalArgumentException(
              "Three arguments required: batch-file-path, stream-file-path, and flagged-purchases-path.");
    }
    String batchFilePathString = args[0];    // "log_input/batch_log.json";
    String streamFilePathString = args[1];   // "log_input/stream_log.json";
    String anomalyFilePathString = args[2];  // "log_output/flagged_purchases.json";

    TransactionProcessor transactionProcessor = new TransactionProcessor(batchFilePathString);
    transactionProcessor.processPathStringInput(streamFilePathString, anomalyFilePathString);
  }
}
