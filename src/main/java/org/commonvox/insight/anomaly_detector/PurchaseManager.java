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

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * An instance of the PurchaseManager class serves as the container for recent purchases of
 * either a User or a network of Users, up to the capacity established by
 * {@link #getThreshold() the purchase threshold}.
 *
 * @author Daniel Vimont
 */
public class PurchaseManager {

  private static int threshold = 0;
  private static final int MIN_PURCHASES_FOR_ANOMALY_ASSESSMENT = 2;
  private static final StringBuilder STRING_BUILDER = new StringBuilder(15);
  private static final char LEADING_ZERO = '0';

  private final NavigableMap<PurchaseKey,Integer> purchaseMap = new TreeMap<>();

  /**
   * Set threshold class variable, representing the threshold (max count) of purchases to be
   * utilized in {@link #getAnomalyData(java.lang.Integer) anomaly assessment}.
   *
   * @param classThreshold value to which class variable, threshold, is to be set
   */
  protected static void setThreshold(int classThreshold) {
    threshold = classThreshold;
  }

  /**
   * Value of threshold class variable, representing the threshold (max count) of purchases to be
   * utilized in {@link #getAnomalyData(java.lang.Integer) anomaly assessment}.
   *
   * @return value of class variable, threshold
   */
  protected static int getThreshold() {
    return threshold;
  }

  /**
   * Standardizes conversion of decimal String values (from JSON streams) into Integer objects
   * (with decimal point removed to manage amounts as pennies).
   *
   * @param amountString original decimal String (dollars and cents delimited by decimal point)
   * @return Integer object representing value in cents derived from dollar-formatted decimal String
   */
  protected static Integer amountStringToInteger(String amountString) {
    STRING_BUILDER.setLength(0);
    String cents = STRING_BUILDER.append(amountString).deleteCharAt(amountString.length() - 3).toString();
    return Integer.valueOf(cents);
   }

  /**
   * Standardizes conversion of integer amounts (representing values in cents) to "dollars and cents"
   * decimal String values.
   *
   * @param amount value in cents
   * @return String formatted in dollars and cents, delimited by decimal point
   */
  protected static String amountIntegerToString(Integer amount) {
    STRING_BUILDER.setLength(0);
    String amountString = String.valueOf(amount);
    if (amountString.length() < 3) {
      STRING_BUILDER.append(LEADING_ZERO);
      if (amountString.length() == 1) {
        STRING_BUILDER.append(LEADING_ZERO);
      }
    }
    return STRING_BUILDER.append(amountString).insert(STRING_BUILDER.length() - 2, '.').toString();
  }

  /**
   * Returns the internally-managed purchaseMap, containing recent purchases up to the
   * capacity established by {@link #getThreshold() the purchase threshold}.
   *
   * @return purchaseMap
   */
  protected NavigableMap<PurchaseKey,Integer> getPurchaseMap() {
    return purchaseMap;
  }

  /**
   * Add purchase (denoted by submitted timestamp and amount) to this PurchaseManager's internally
   * maintained purchaseMap, subject to {@link #getThreshold() the purchase threshold} constraint.
   * The submitted purchase will not be added if its timestamp precedes that of the earliest
   * purchase in an already filled-to-threshold-capacity purchaseMap.
   *
   * @param timestamp in String format
   * @param amountDecimalString in dollars and cents format
   */
  protected void addPurchase(String timestamp, String amountDecimalString) {
    PurchaseManager.this.addPurchase(timestamp, amountStringToInteger(amountDecimalString));
  }

  /**
   * Add purchase (denoted by submitted timestamp and amount) to this PurchaseManager's internally
   * maintained purchaseMap, subject to {@link #getThreshold() the purchase threshold} constraint.
   * The submitted purchase will not be added if its timestamp precedes that of the earliest
   * purchase in an already filled-to-threshold-capacity purchaseMap.
   *
   * @param timestamp in String format
   * @param amount in pennies
   */
  protected void addPurchase(String timestamp, Integer amount) {
    if (purchaseMap.size() < getThreshold()) {
      purchaseMap.put(new PurchaseKey(timestamp), amount);
    } else {
      PurchaseKey purchaseKey = new PurchaseKey(timestamp);
      if (purchaseKey.compareTo(purchaseMap.firstEntry().getKey()) > 0) {
        purchaseMap.pollFirstEntry(); // remove earliest purchase element
        purchaseMap.put(purchaseKey, amount);
      }
    }
  }

  /**
   * Add all purchases from submitted PurchaseManager to this PurchaseManager, subject to
   * {@link #getThreshold() the purchase threshold constraint}.
   *
   * @param addedPurchaseManager purchaseManager object used as source of added purchase transactions.
   */
  protected void addPurchases(PurchaseManager addedPurchaseManager) {
    addedPurchaseManager.purchaseMap.forEach((k,v) -> {
      this.purchaseMap.put(k,v);
      if (this.purchaseMap.size() > getThreshold()) {
        this.purchaseMap.pollFirstEntry(); // remove oldest entry to keep size within threshold
      }
    });
  }

  /**
   * If submitted purchase amount is an anomaly, returns a two-element array consisting of (a) mean
   * and (b) standard deviation that formed the basis for the anomaly computation; if submitted
   * purchase amount is NOT an anomaly, returns null.
   *
   * @param amount purchase amount in pennies
   * @return null if amount is not an anomaly; otherwise, returns a two-element int array
   * consisting of (a) mean and (b) standard deviation that formed basis of anomaly computation.
   */
  protected int[] getAnomalyData(Integer amount) {
    if (purchaseMap.size() < MIN_PURCHASES_FOR_ANOMALY_ASSESSMENT) {
      return null;
    }
    int mean = getMean();
    int standardDeviation = getStandardDeviation(mean);
    if (amount > mean + (standardDeviation * 3)) {
      return new int[]{mean, standardDeviation};
    } else {
      return null;
    }
  }

  private int getMean() {
    int sum = 0;
    for(int n : purchaseMap.values()) {
      sum += n;
    }
    return sum / purchaseMap.size(); // rounds down to nearest penny!
  }

  private int getStandardDeviation(int mean) {
    double sumOfDeviationsSquared = 0;
    for(int amount : purchaseMap.values()) {
      sumOfDeviationsSquared += Math.pow((amount - mean), 2);
    }
    return (int)Math.sqrt(sumOfDeviationsSquared / purchaseMap.size());
  }

  @Override
  public String toString() {
    StringBuilder contents = new StringBuilder();
    contents.append("PurchaseManager{");
    purchaseMap.forEach((k,v) -> {
      contents.append("\n -- key: ").append(k)
              .append(" ; value: ").append(PurchaseManager.amountIntegerToString(v));  });
    int mean = getMean();
//    contents.append("\n===========");
//    contents.append("\n -- MEAN: ").append(mean).append(" cents.");
//    contents.append("\n -- STANDARD DEVIATION: ").append(getStandardDeviation(mean)).append(" cents.");
    contents.append("\n}");
    return contents.toString();
  }

  /**
   * Provides unique key for purchase transactions, since timestamps themselves are not necessarily
   * unique.
   */
  protected class PurchaseKey implements Comparable<PurchaseKey> {
    final String timestamp;
    final Long nanoTime;

    public PurchaseKey(String timestamp) {
      this.timestamp = timestamp;
      this.nanoTime = System.nanoTime();
    }

    /**
     * This compareTo sorts in ascending chronological order!!
     *
     * @param other PurchaseKey object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than,
     * equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(PurchaseKey other) {
      int timestampCompare = this.timestamp.compareTo(other.timestamp);
      return timestampCompare == 0 ? this.nanoTime.compareTo(other.nanoTime) : timestampCompare;
    }

    @Override
    public String toString() {
      return "PurchaseKey{" + "timestamp=" + timestamp + ", nanoTime=" + nanoTime.toString() + '}';
    }
  }
}
