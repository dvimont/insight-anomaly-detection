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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.stream.Stream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * The TransactionProcessor class serves as the primary API for the "anomaly_detector"
 * package, with methods for both batch and streaming invocations.
 *
 * @author Daniel Vimont
 */
public class TransactionProcessor {

  private static final String DEGREE_KEY = "D";
  private static final String THRESHOLD_KEY = "T";
  private static final String EVENT_TYPE_KEY = "event_type";
  private static final String BEFRIEND_EVENT = "befriend";
  private static final String UNFRIEND_EVENT = "unfriend";
  private static final String PURCHASE_EVENT = "purchase";
  private static final String TIMESTAMP_KEY = "timestamp";
  private static final String ID_KEY = "id";
  private static final String AMOUNT_KEY = "amount";
  private static final String ID1_KEY = "id1";
  private static final String ID2_KEY = "id2";
  private static final StringBuilder STRING_BUILDER = new StringBuilder(50);
  private static final JSONParser JSON_PARSER = new JSONParser();

  // NOTE: the following EXACT template (with explicit spaces) required to pass Insight test script!
  private static final String MEAN_SD_JSON_TEMPLATE = ", \"mean\": \"%s\", \"sd\": \"%s\"}";

  /**
   * Initializes a new TransactionProcessor, which reads in startup parameters and initializing
   * transactions from a batch file (e.g., "batch_log.json") for which a relative path is
   * identified by the submitted parameter.
   *
   * @param batchPathString String representation of local relative path for an existing batch file
   * (e.g., "batch_log.json") containing startup parameters and initializing transactions.
   * @throws IOException if file access problems encountered
   * @throws org.json.simple.parser.ParseException if problems encountered in parsing of JSON input
   */
  public TransactionProcessor(String batchPathString)
          throws IOException, ParseException {
    processPathStringInput(batchPathString, null);
    // throw exception if, after batch file processed,
    //   either User.getDegreesOfSeparation or PurchaseManager.getThreshold == 0!!
  }

  /**
   * Provides for a batch processing alternative to real-time stream processing in situations
   * in which streaming data has been previously captured and stored in a file
   * (e.g., "stream_log.json"), and it is subsequently being submitted for anomaly-detection
   * processing.
   *
   * @param pathString String representation of local relative path for an existing
   * file (e.g., "stream_log.json") for anomaly-detection processing.
   * @param anomalyPathString String representation of local relative path for output file (which
   * need not yet exist) which is to receive outputted anomaly records in JSON format.
   * @throws IOException if file access problems encountered
   * @throws org.json.simple.parser.ParseException if problems encountered in parsing of JSON input
   */
  public final void processPathStringInput(String pathString, String anomalyPathString)
          throws IOException, ParseException {
    processPathInput(Paths.get(pathString), anomalyPathString);
  }

  /**
   * Provides for a batch processing alternative to real-time stream processing in situations
   * in which streaming data has been previously captured and stored in a file
   * (e.g., "stream_log.json"), and it is subsequently being submitted for anomaly-detection
   * processing.
   *
   * @param path Path representation of local relative path for an existing
   * file (e.g., "stream_log.json") for anomaly-detection processing.
   * @param anomalyPathString String representation of local relative path for output file (which
   * need not yet exist) which is to receive outputted anomaly records in JSON format.
   * @throws IOException if file access problems encountered
   * @throws org.json.simple.parser.ParseException if problems encountered in parsing of JSON input
   */
  public final void processPathInput(Path path, String anomalyPathString)
          throws IOException, ParseException {
    if (anomalyPathString == null) {
      try (Stream<String> stream = Files.lines(path) ) {
        processStreamInput(stream, null);
      }
    } else {
      Path anomalyPath = Paths.get(anomalyPathString);

      // Do not overlay previous anomaly analysis output; if previous output exists, rename it!
      if (Files.exists(anomalyPath)) {
        // System.out.println("RENAMING EXISTING OUTPUT FILE!!");
        Files.move(anomalyPath, Paths.get(anomalyPathString + "." + Instant.now().toString() + ".json"));
      }

      try (Stream<String> stream = Files.lines(path);
              BufferedWriter anomalyWriter = Files.newBufferedWriter(anomalyPath) ) {
        processStreamInput(stream, anomalyWriter);
      }
    }
  }

  /**
   * Provides for real-time stream processing of transactions being submitted for anomaly-detection
   * processing. Note that this method is also internally invoked by other TransactionProcessor
   * method(s) which convert batch file records into streaming records.
   *
   * @param stream stream of transactions being submitted for anomaly-detection processing.
   * @param anomalyWriter BufferedWriter object to receive outputted anomaly records in JSON format.
   * @throws IOException if file access problems encountered
   * @throws org.json.simple.parser.ParseException if problems encountered in parsing of JSON input
   */
  public final void processStreamInput(Stream<String> stream, BufferedWriter anomalyWriter)
          throws ParseException, IOException {

    boolean pastFirstOutputLine = false;
    for(String jsonString : stream.toArray(String[]::new)) {
      if (jsonString.isEmpty()) {
        continue;
      }
      JSONObject jsonObject = (JSONObject) JSON_PARSER.parse(jsonString);
      String eventType = (String) jsonObject.get(EVENT_TYPE_KEY);
      if (eventType == null) {
        String degreeString = (String) jsonObject.get(DEGREE_KEY);  // throw exception if null; write line to "bad data" file.
        String thresholdString = (String) jsonObject.get(THRESHOLD_KEY);  // throw exception if null; write line to "bad data file"

        if (User.getDegreesOfSeparation() == 0) { // may only be set once; subsequent submissions ignored (throw exception?).
          User.setDegreesOfSeparation(Integer.parseInt(degreeString));
        }
        if (PurchaseManager.getThreshold() == 0) { // may only be set once; subsequent submissions ignored (throw exception?).
          PurchaseManager.setThreshold(Integer.parseInt(thresholdString));
        }
      } else {
        String timestamp = (String)jsonObject.get(TIMESTAMP_KEY);
        User user1, user2;
        String id1, id2;

        switch (eventType) {
          case BEFRIEND_EVENT:
            // System.out.println("befriend event");
            id1 = (String)jsonObject.get(ID1_KEY);
            id2 = (String)jsonObject.get(ID2_KEY);
            user1 = User.getOrCreateUser(id1);
            user2 = User.getOrCreateUser(id2);
            user1.befriend(timestamp, user2);
            user2.befriend(timestamp, user1);
            break;
          case UNFRIEND_EVENT:
            // System.out.println("unfriend event");
            id1 = (String)jsonObject.get(ID1_KEY);
            id2 = (String)jsonObject.get(ID2_KEY);
            user1 = User.getOrCreateUser(id1);
            user2 = User.getOrCreateUser(id2);
            user1.unfriend(timestamp, user2);
            user2.unfriend(timestamp, user1);
            break;
          case PURCHASE_EVENT:
            // System.out.println("purchase event");
            String id = (String)jsonObject.get(ID_KEY);
            Integer amount = PurchaseManager.amountStringToInteger((String)jsonObject.get(AMOUNT_KEY));

            User user = User.getOrCreateUser(id);
            if (anomalyWriter != null) {
              int[] anomalyData = user.getAnomalyData(amount);
              if (anomalyData != null) {
                //**********************
                // NOTE: the following string manipulation SHOULD likely be replaced by standard usage of
                //   the JSONObject already parsed above. String manipulation is temporarily done
                //   here simply to keep the order of the JSON elements in the output file
                //   consistent with requirements specified in the original project specifications!
                //**********************
                STRING_BUILDER.setLength(0);
                STRING_BUILDER.append(jsonString.substring(0, jsonString.length() - 1))
                        .append(String.format(
                                MEAN_SD_JSON_TEMPLATE,
                                PurchaseManager.amountIntegerToString(anomalyData[0]),
                                PurchaseManager.amountIntegerToString(anomalyData[1])));
                if (pastFirstOutputLine) {
                  anomalyWriter.newLine();
                } else {
                  pastFirstOutputLine = true;
                }
                anomalyWriter.write(STRING_BUILDER.toString());
              }
            }
            user.addPurchase(timestamp, amount);
            break;
          default:
            System.out.println("UNKNOWN event encountered!!"); // throw exception; or write line to "bad data" file.
            break;
        }
        // System.out.println(jsonObject.toString());
      }
    }
  }
}
