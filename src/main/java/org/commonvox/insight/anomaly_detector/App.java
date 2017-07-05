/*
 * Copyright 2017 Daniel Vimont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
