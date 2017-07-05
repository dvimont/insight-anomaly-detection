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

import java.lang.reflect.Field;
import java.util.Map.Entry;
import junit.framework.TestCase;

/**
 * Provides unit testing for methods of the {@code PurchaseManager} class
 *
 * @author Daniel Vimont
 */
public class PurchaseManagerTest extends TestCase {

  /**
   * Test of setThreshold method of class PurchaseManager.
   * @throws java.lang.NoSuchFieldException
   * @throws java.lang.IllegalAccessException
   */
  public void testSetThreshold() throws NoSuchFieldException, IllegalAccessException {
    int classThreshold = 5;
    PurchaseManager.setThreshold(classThreshold);
    Field thresholdField = PurchaseManager.class.getDeclaredField("threshold");
    thresholdField.setAccessible(true);
    thresholdField.get(null);
    assertEquals(classThreshold, thresholdField.get(null));
  }

  /**
   * Test of getThreshold method of class PurchaseManager.
   * @throws java.lang.NoSuchFieldException
   * @throws java.lang.IllegalAccessException
   */
  public void testGetThreshold() throws NoSuchFieldException, IllegalAccessException {
    int expResult = 82;
    Field thresholdField = PurchaseManager.class.getDeclaredField("threshold");
    thresholdField.setAccessible(true);
    thresholdField.set(null, expResult);
    int result = PurchaseManager.getThreshold();
    assertEquals(expResult, result);
  }

  /**
   * Test of amountStringToInteger method of class PurchaseManager.
   */
  public void testAmountStringToInteger() {
    String amountString = "1601.83";
    Integer expResult = 160183;
    Integer result = PurchaseManager.amountStringToInteger(amountString);
    assertEquals(expResult, result);

    amountString = "2.33";
    expResult = 233;
    result = PurchaseManager.amountStringToInteger(amountString);
    assertEquals(expResult, result);

    amountString = "0.43";
    expResult = 43;
    result = PurchaseManager.amountStringToInteger(amountString);
    assertEquals(expResult, result);

    amountString = "0.07";
    expResult = 7;
    result = PurchaseManager.amountStringToInteger(amountString);
    assertEquals(expResult, result);
  }

  /**
   * Test of amountIntegerToString method of class PurchaseManager.
   */
  public void testAmountIntegerToString() {
    Integer amount = 58922111;
    String expResult = "589221.11";
    String result = PurchaseManager.amountIntegerToString(amount);
    assertEquals(expResult, result);

    amount = 349;
    expResult = "3.49";
    result = PurchaseManager.amountIntegerToString(amount);
    assertEquals(expResult, result);

    amount = 19;
    expResult = "0.19";
    result = PurchaseManager.amountIntegerToString(amount);
    assertEquals(expResult, result);

    amount = 3;
    expResult = "0.03";
    result = PurchaseManager.amountIntegerToString(amount);
    assertEquals(expResult, result);
  }

  /**
   * Test of addPurchase method of class PurchaseManager.
   */
  public void testAddPurchase_String_String() {
    PurchaseManager.setThreshold(3); // limit purchaseMap to 3 most recent purchases

    String timestamp1 = "2017-06-13 11:33:02"; // 2nd in final map
    String timestamp2 = "2017-06-13 11:33:02"; // 3rd in final map
    String timestamp3 = "2017-05-09 10:00:12"; // not expected in final map
    String timestamp4 = "2017-06-11 16:20:43"; // 1st in final map

    String amountDecimalString1 = "389.22";  // 2nd in final map
    String amountDecimalString2 = "3.11";    // 3rd in final map
    String amountDecimalString3 = "5909.73"; // not expected in final map
    String amountDecimalString4 = "55.54";   // 1st in final map
    PurchaseManager instance = new PurchaseManager();
    instance.addPurchase(timestamp1, amountDecimalString1);

    // assure single purchase properly inserted into purchaseMap
    Entry<PurchaseManager.PurchaseKey, Integer> result = instance.getPurchaseMap().firstEntry();
    assertEquals(timestamp1, result.getKey().timestamp);
    assertEquals(PurchaseManager.amountStringToInteger(amountDecimalString1), result.getValue());

    // assure multiple purchases respect threshold and ordered properly
    instance.addPurchase(timestamp2, amountDecimalString2);
    instance.addPurchase(timestamp3, amountDecimalString3);
    instance.addPurchase(timestamp4, amountDecimalString4);
    assertEquals(5554,  instance.getPurchaseMap().values().toArray()[0]);
    assertEquals(38922, instance.getPurchaseMap().values().toArray()[1]);
    assertEquals(311,   instance.getPurchaseMap().values().toArray()[2]);
    assertFalse(instance.getPurchaseMap().values().contains(590973)); // earliest purchase not in Map
  }

  /**
   * Test of addPurchase method of class PurchaseManager.
   */
  public void testAddPurchase_String_Integer() {
    PurchaseManager.setThreshold(3); // limit purchaseMap to 3 most recent purchases

    String timestamp1 = "2017-06-13 11:33:02"; // 2nd in final map
    String timestamp2 = "2017-06-13 11:33:02"; // 3rd in final map
    String timestamp3 = "2017-05-09 10:00:12"; // not expected in final map
    String timestamp4 = "2017-06-11 16:20:43"; // 1st in final map

    Integer amount1 = 38922;  // 2nd in final map
    Integer amount2 = 311;    // 3rd in final map
    Integer amount3 = 590973; // not expected in final map
    Integer amount4 = 5554;   // 1st in final map
    PurchaseManager instance = new PurchaseManager();
    instance.addPurchase(timestamp1, amount1);

    // assure single purchase properly inserted into purchaseMap
    Entry<PurchaseManager.PurchaseKey, Integer> result = instance.getPurchaseMap().firstEntry();
    assertEquals(timestamp1, result.getKey().timestamp);
    assertEquals(amount1, result.getValue());

    // assure multiple purchases respect threshold and ordered properly
    instance.addPurchase(timestamp2, amount2);
    instance.addPurchase(timestamp3, amount3);
    instance.addPurchase(timestamp4, amount4);
    assertEquals(5554, instance.getPurchaseMap().values().toArray()[0]);
    assertEquals(38922, instance.getPurchaseMap().values().toArray()[1]);
    assertEquals(311, instance.getPurchaseMap().values().toArray()[2]);
    assertFalse(instance.getPurchaseMap().values().contains(590973)); // earliest purchase not in Map
  }

  /**
   * Test of addPurchases method of class PurchaseManager.
   */
  public void testAddPurchases() {
    PurchaseManager.setThreshold(3); // limit purchaseMap to 3 most recent purchases

    String timestamp1 = "2017-06-13 11:33:02"; // 2nd in final map
    String timestamp2 = "2017-06-13 11:33:02"; // 3rd in final map
    String timestamp3 = "2017-05-09 10:00:12"; // not expected in final map
    String timestamp4 = "2017-06-11 16:20:43"; // 1st in final map
    Integer amount1 = 38922;  // 2nd in final map
    Integer amount2 = 311;    // 3rd in final map
    Integer amount3 = 590973; // not expected in final map
    Integer amount4 = 5554;   // 1st in final map
    PurchaseManager instance1 = new PurchaseManager();
    instance1.addPurchase(timestamp1, amount1);
    instance1.addPurchase(timestamp2, amount2);
    instance1.addPurchase(timestamp3, amount3);
    instance1.addPurchase(timestamp4, amount4);

    String timestamp5 = "2016-04-13 11:33:02";
    String timestamp6 = "2017-07-13 11:33:02";
    String timestamp7 = "2017-05-22 10:00:12";
    String timestamp8 = "2017-07-11 09:20:43";
    Integer amount5 = 3;
    Integer amount6 = 4445;
    Integer amount7 = 3091;
    Integer amount8 = 542;
    PurchaseManager instance2 = new PurchaseManager();
    instance2.addPurchase(timestamp5, amount5);
    instance2.addPurchase(timestamp6, amount6);
    instance2.addPurchase(timestamp7, amount7);
    instance2.addPurchase(timestamp8, amount8);

    PurchaseManager combinedInstance = new PurchaseManager();
    combinedInstance.addPurchases(instance1);
    combinedInstance.addPurchases(instance2);
    assertEquals(3, combinedInstance.getPurchaseMap().values().size());
    assertEquals(311, combinedInstance.getPurchaseMap().values().toArray()[0]);
    assertEquals(542, combinedInstance.getPurchaseMap().values().toArray()[1]);
    assertEquals(4445, combinedInstance.getPurchaseMap().values().toArray()[2]);
  }

  /**
   * Test of getAnomalyData method of class PurchaseManager.
   */
  public void testGetAnomalyData() {
    PurchaseManager.setThreshold(3); // limit purchaseMap to 3 most recent purchases

    String timestamp1 = "2017-06-13 11:33:02";
    String timestamp2 = "2017-06-13 11:33:02";
    String timestamp3 = "2017-05-09 10:00:12";
    String timestamp4 = "2017-06-11 16:20:43";

    Integer amount1 = 38922;
    Integer amount2 = 311;
    Integer amount3 = 590973;
    Integer amount4 = 5554;

    PurchaseManager instance = new PurchaseManager();
    instance.addPurchase(timestamp1, amount1);
    instance.addPurchase(timestamp2, amount2);
    instance.addPurchase(timestamp3, amount3);
    instance.addPurchase(timestamp4, amount4);

    int[] anomalyData = instance.getAnomalyData(75000);
    assertFalse(anomalyData == null);
    assertEquals(14929, anomalyData[0]); // mean
    assertEquals(17100, anomalyData[1]); // standard deviation
  }
}
