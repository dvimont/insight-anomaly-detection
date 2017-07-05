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
/**
 * <b>This package fulfills requirements for an anomaly-detection package (for the mythical
 * e-commerce firm "Market-ter") which identifies and logs any user's purchase that is found to be
 * an anomaly (unusually high) in comparison to recent purchases made by those within the
 * user's network of friends (please <a href="#package.description">click here</a> or scroll
 * down for more detailed information)</b>.
 * <br><br>
 * The <a href="https://github.com/InsightDataScience/anomaly_detection#table-of-contents" target="_blank">
 * specifications</a> state that a purchase amount is an "anomaly" for a user if that amount
 * exceeds the mean (average) of <i>the most recent purchases among the user's network of friends</i>
 * by more than three times the standard deviation of all of those purchases.
 * <br>
 * <hr>
 * <h3>Processing Overview</h3>
 * A batch run of the anomaly_detector package begins with execution of the #main method of the App
 * class, which requires three String arguments to be passed to it representing the local paths for
 * (1) the batch initialization file, (2) the stream log file (containing transactions for anomaly
 * processing), and (3) the output ("flagged purchases") file.
 * <br><br>
 * An instance of the TransactionProcessor class instantiates itself by reading in and processing
 * the contents of the batch initialization file, including the "degrees of separation" constraint
 * (D) and the "threshold of purchases" constraint (T) from the first line of batch input. Following
 * instantiation, one of the TransactionProcessor's #process methods is invoked to read in and
 * process the stream log transactions, and when an anomaly purchase is identified, it is written
 * to the output ("flagged purchases") file.
 * <br><br>
 * For all transactions processed in the batch initialization phase and the stream log processing
 * phase, each User involved in the transaction is retrieved via the static User#getOrCreateUser
 * method, which (as the method name suggests) either retrieves an existing User object or creates
 * a new one.
 * <br><br>
 * Once User retrieval/creation is completed, each event is processed as follows:
 * <ul>
 * <li>"befriend" -- The user#befriend method is invoked for each user to establish the reciprocal
 * relationship. (See note below regarding "extra" functionality added to the #befriend method.)</li>
 * <li>"unfriend" -- The user#unfriend method is invoked for each user to establish the reciprocal
 * removal of relationship. (See note below regarding "extra" functionality added to the #unfriend method.)</li>
 * <li>"purchase" -- The user#addPurchase method is invoked to add the purchase to the user's
 * PurchaseManager object, which adds the purchase to its internally-managed purchaseMap, subject
 * to the "threshold of purchases" constraint: the submitted purchase will not be added if its timestamp
 * precedes that of the earliest purchase in an already filled-to-threshold-capacity purchaseMap.</li>
 * </ul>
 * In the stream-log processing phase, one additional step is performed in "purchase" processing:
 * before the user#addPurchase method is invoked, the user#getAnomalyData method is invoked.
 * The purchase amount is passed to this method, and the following sequence transpires: the user's
 * current network of friends is recursively assembled (dependent upon the user's friend connections
 * and the "degrees of separation" system constraint), and a network-specific PurchaseManager object
 * is used to assemble a purchaseMap for the network (dependent upon the "threshold of purchases"
 * system constraint). Calculations are then performed to determine whether the amount of the current
 * purchase is an anomaly or not. If the amount is an anomaly, then a record is written by the
 * TransactionProcessor to the output ("flagged purchases") file.
 *
 * <hr>
 * <h3>Dependencies</h3>
 * In order to be executed, this package requires an environment in which
 * <a href="http://www.oracle.com/technetwork/java/javase/downloads/index.html" target="_blank">
 * Java 1.8</a> and <a href="https://maven.apache.org/install.html"target="_blank">Maven</a>
 * have been installed.
 * <br><br>
 * This being a completely standard Maven-managed Java project, all dependencies are outlined in the
 * project's {@code pom.xml} file, and Maven automatically manages these dependencies at build time.
 * A build is automatically executed when either of the customized {@code /run.sh} or
 * {@code /insight_testsuite/run_tests.sh} shell scripts is executed.
 *
 * <hr>
 * <h3>Command-line execution</h3>
 * While this package provides an API (documented in these Javadocs pages) intended to allow direct
 * incorporation of its functionality into any existing Java 1.8-compliant system (via methods of the
 * <a href="TransactionProcessor.html#processStreamInput-java.util.stream.Stream-java.io.BufferedWriter-">
 * TransactionProcessor class</a>), command-line execution may be done through submission of the
 * following within this project's root directory. <b>(Note that an example of the following is
 * provided in the customized {@code /run.sh} script file.)</b>:
 *
 * <pre>   mvn exec:java -Dexec.mainClass=org.commonvox.insight.anomaly_detector.App \
 *      -Dexec.args="./log_input/batch_log.json ./log_input/stream_log.json ./log_output/flagged_purchases.json"</pre>
 *
 * The arguments on the second line above are mandatory, and should be tailored to represent the
 * relative paths for three files:
 * <ol>
 *   <li>INPUT FILE containing <b><i>batch transactions</i></b> (used for initialization)</li>
 *   <li>INPUT FILE containing <b><i>streaming transactions</i></b> (analyzed for potential anomaly purchases)</li>
 *   <li>OUTPUT FILE (not necessarily an existing file) for <b><i>flagged purchases</i></b> (i.e. anomaly purchases)</li>
 * </ol>
 *
 * Note that the transactions contained in all three files adhere to a proprietary JSON layout
 * particular to Market-ter's systems, established via exemplars in
 * <a href="https://github.com/InsightDataScience/anomaly_detection#input-data" target="_blank">
 * the original specifications</a>.
 *
 * <hr>
 * <h3>Customization of shell scripts was required</h3>
 * The original specifications provided two shell scripts, (1) {@code run.sh} and (2)
 * {@code /insight_testsuite/run_tests.sh}, both of which required customization. Notably,
 * the {@code /insight_testsuite/run_tests.sh} shell script required the addition of two lines,
 * both corresponding to the standard {@code pom.xml} Maven project file.
 * <br><br>
 * <b>WARNING</b>: If the originally-provided {@code run.sh} and/or {@code run_tests.sh} scripts
 * (which do not include inserted lines for Maven-style execution and to properly handle the Maven
 * {@code pom.xml} file) are run against this package, a "BUILD FAILURE" will likely be encountered,
 * with ERROR messages outputted potentially including:<br>
 * "{@code [ERROR] The goal you specified requires a project to execute but there is no POM in this
 * directory ... Please verify you invoked Maven from the correct directory.}"
 *
 * <hr>
 * <h3>Naming conventions</h3>
 * The author of this package is a strong proponent of the dictums "call it what it is" and "don't
 * give it another name if it's already got one". Thus, wherever possible, the names used in
 * classes and methods of this package adhere to all explicit and implied naming conventions
 * spelled out in the
 * <a href="https://github.com/InsightDataScience/anomaly_detection#table-of-contents" target="_blank">
 * original specifications</a>. Experience shows that a persistent adherence
 * to such naming policies can yield dividends as a project grows to potentially involve
 * more people and expand in complexity. In other words, solid and consistent naming conventions
 * can help give a project like this an improved human and organizational "scalability".
 *
 * <hr>
 * <h3>Note on JSON format and output processing</h3>
 * The <a href="https://github.com/InsightDataScience/anomaly_detection#table-of-contents" target="_blank">
 * original specifications</a> for the format of JSON input and output used in this package impose an
 * extra constraint outside of established JSON standards: the original specifications require
 * that the elements in each JSON object (the name:value pairs between curly brackets) be kept in
 * a specific order. A relatively recent
 * <a href="https://www.rfc-editor.org/rfc/rfc7159.txt" target="_blank">clarification document</a>
 * from the authorities overseeing JSON standards explicitly states that
 * "[a JSON] object is an <b>unordered</b> collection of zero or more name/value pairs".
 * <br><br>
 * For those of us with extensive experience working with interoperability issues among homegrown
 * toolkits and technologies produced by our corporate clients, it is neither
 * surprising nor unusual to come across constraints such as the one imposed in this exercise:
 * that the ordering of object-elements in a particular setting might need to be maintained in a
 * <i>non-standard</i> way.
 * <br><br>
 * With that said, the maintaining of order in the elements of an outputted JSON object does not
 * come without a potential hit to the efficiency of such processing. In the solution presented
 * here, a Java StringBuilder is used to (as efficiently as possible, yet rather crudely) "inject"
 * the <b>{@code mean}</b> and <b>{@code sd}</b> elements into the end of the outputted
 * "flagged_purchases" JSON objects.
 * If this were a real-world engagement with a corporate client, one would inquire whether the
 * applications which consume the "flagged_purchases" are indeed hard-wired to require
 * specifically-ordered JSON objects, or whether this constraint can be removed from the project
 * specifications, allowing for more efficient outputting of "natural" (i.e. unordered) JSON object
 * content.
 *
 * <hr>
 * <h3>Validation of JSON input</h3>
 * While there was no explicit mention in the specifications regarding validation of input coming
 * from JSON batch files or streams, it may well be that validation is needed (assuring things like
 * valid timestamp formats, numeric and non-negative amounts in purchases, and whatever might
 * constitute a valid
 * user-id). On the other hand, the distributed architectures of an enterprise such as Market-ter
 * might well provide for such validations "upstream" from this package's processes, obviating
 * the need for the addition of validation logic (and its attendant overhead) in this package.
 * The current implementation includes <i>no</i> validation for inputted JSON values, but validation logic
 * could easily be added, if required. Additionally, a system-level variable or config parameter
 * might be utilized to optionally activate or deactivate this new "layer" of validation processing.
 *
 * <hr>
 * <h3>Choice of JSON parser</h3>
 * There are a number of reliable Java-based JSON parser packages available. In searching for
 * an appropriate choice for this project, a brief research  endeavor led to the selection of
 * <a href="https://code.google.com/archive/p/json-simple/" target="_blank"><i>JSON.simple</i></a>,
 * which seems to offer both reliability and road-tested efficiency. Note that it would be
 * extremely easy to swap out this JSON parser and replace it with any other (the beauty of open
 * standards!).
 *
 * <hr>
 * <h3>Unit testing</h3>
 * The standard <a href="http://junit.org" target="_blank">JUnit package</a> is being employed
 * for unit testing. A complete array of unit tests, covering all non-private methods in the
 * User and PurchaseManager classes, can be found in the subdirectories of {@code ./src/test/java/}.
 *
 * <hr>
 * <h3>Note that this implementation is not synchronized</h3>
 * This initial prototype is intended for single-threaded use. In its current form, a
 * <a href="TransactionProcessor.html">TransactionProcessor</a>
 * is not intended to be concurrently accessed by multiple threads.
 *
 * <hr>
 * <h3>Scalability issue 1: efficiency in maintenance of collections</h3>
 * In choosing which of the standard Java Collection classes to utilize in this package, the focus
 * was on scalability: as the size of users, networks, and purchase-maps potentially expands
 * in the future, it was vital to guarantee the advantages of binary searches [O(log(n) efficiency]
 * over sequential searches [O(n) efficiency]. A quick glance at the "import" statements at the
 * top of the User class and the PurchaseManager class of this package shows that only TreeSet
 * and TreeMap implementations are currently in use, taking advantage of their
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/TreeSet.html" target="_blank">
 * "guaranteed log(n) time cost for basic operations"</a>.
 *
 * <hr>
 * <h3>Scalability issue 2: minimizing data conversions</h3>
 * Since it was clear that some data conversion would be necessary to fulfill the project
 * requirements (most obviously the conversion of String-based amounts into a numeric format),
 * a focus was applied to minimizing these conversions (to the extent reasonable and feasible),
 * since such conversions have an impact on efficiency and scalability. The current policies for
 * conversions are as follows:
 * <ul>
 * <li>all <i>amounts</i> read in from JSON streams will immediately be converted to numeric format
 * and held and manipulated in memory in numeric format;</li>
 * <li>all <i>timestamps</i> and <i>user IDs</i> will never be converted, but instead kept in their
 * original String format;</li>
 * <li>the <i>System#nanoTime</i> values used to establish unique purchase-transaction-keys are
 * always held in memory in their original, long (numeric) format.</li>
 * </ul>
 *
 * <hr>
 * <h3>Scalability issue 3: simplifying computations</h3>
 * In the first moments of design of this package, the knee-jerk selection of the BigDecimal class
 * was made as the "container" of choice for amount data (converted from inputted JSON Strings);
 * after all, both the standard Java documentation and a not-so-small army of Java gurus all
 * implore us to use BigDecimal objects when dealing with currency! But just a little thought
 * led to the realization that if amounts were held in memory as <i>pennies</i> instead of
 * <i>dollars</i>, that the complexity (and potential overhead, particularly in calculations) of
 * the BigDecimal class could be completely avoided! Thus, the Integer class (and primitive "int"
 * instances, where appropriate) are used for holding all amounts in memory (in "penny"
 * denominations) and for performing all computations, rounded to the nearest penny.
 *
 * <hr>
 * <h3>Scalability issue 4: readiness for distributed processing</h3>
 * While it was outside of the explicit scope of this project, Java 1.8 makes Stream management
 * an integral (and pretty much automatic) part of its architecture. Thus, while the
 * TransactionProcessor class handles batch-style input of a "stream_log" file (in fulfillment
 * of the original specification), it also is
 * architected (though never yet tested) to handle real-time streaming input. It is easy to
 * envision usage of a suitably tested version of this package in a distributed Hadoop, HBase, or
 * other distributed processing framework, directly accepting streaming data via the
 * <a href="TransactionProcessor.html#processStreamInput-java.util.stream.Stream-java.io.BufferedWriter-">
 * TransactionProcessor#processStreamInput method</a>.
 *
 * <hr>
 * <h3>EXTRA DELIVERABLE: Prevention of out-of-sequence befriending and unfriending</h3>
 * During initial design of the User#befriend and User#unfriend methods, it became obvious that
 * in a real-world implementation, just as purchase transactions might be received from their
 * originating Internet sources <i>out of sequence</i>, so too might befriend and unfriend
 * transactions be received <i>out of sequence</i>.
 * <br><br>
 * To handle this eventuality, extra functionality was added to this package seeking to assure
 * that:
 * <ul>
 * <li>an <i>unfriend</i> transaction will be ignored if its submitted timestamp precedes
 * the timestamp of the most recent (already-submitted) <i>befriend</i> transaction, and</li>
 * <li>a <i>befriend</i> transaction will be ignored if its submitted timestamp precedes
 * the timestamp of the most recent (already-submitted) <i>unfriend</i> transaction.</li>
 * </ul>
 *
 * <hr>
 * <h3>EXTRA DELIVERABLE: No overlay of an already-existing "flagged_purchases" output file</h3>
 * It seems potentially dangerous to automatically overlay output files from previous batch
 * runs of this package. Thus, a newly commencing batch run of this package automatically renames
 * any existing "flagged_purchases" output file it finds (by appending a "." + string-timestamp
 * suffix, followed by a final suffix of ".json" to the file name).
 *
 * <hr>
 * <h3>AN ADDITIONAL THOUGHT: Regarding outliers</h3>
 * While it was far outside of the scope of this exercise, as various sets of test data were being
 * put through the anomaly-detection algorithms, it became apparent that the addition of a
 * single <b>extremely</b> anomalous purchase could, in effect, shut down anomaly identification
 * for any user within the network of the extreme purchaser. Until the extreme
 * purchase ultimately cycled out of the "recent purchases" pool, almost nothing would be rated as an
 * anomaly. This leads to a suspicion that any real-world system that attempted
 * to accomplish similar goals of anomaly detection would need to employ more advanced algorithms
 * to identify and remove "outlier" purchases from consideration in the anomaly-detection process.
 *
 */
package org.commonvox.insight.anomaly_detector;
