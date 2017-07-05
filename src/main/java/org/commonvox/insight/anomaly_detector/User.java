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

import java.util.Collection;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * An instance of the User class serves as the container for friends and recent purchases of a
 * user, and the User class crucially provides the {@link #getAnomalyData(java.lang.Integer)
 * #getAnomalyData} method, which invokes anomaly-detection on each of a user's new purchases.
 *
 * @author Daniel Vimont
 */
public class User implements Comparable<User> {

  private static int degreesOfSeparation;
  private static final NavigableMap<String,User> userMap = new TreeMap<>((id1, id2) -> id1.compareTo(id2));

  private final String id;
  private final NavigableSet<User> friends = new TreeSet<>();

  private final NavigableMap<String, String> befriendTimestamps = new TreeMap<>();
  private final NavigableMap<String, String> unfriendTimestamps = new TreeMap<>();

  private final PurchaseManager purchaseManager = new PurchaseManager();

  /**
   * Set the value of "degrees of separation" class variable, establishing how User networks
   * will be derived.
   *
   * @param classDegreesOfSeparation degrees of separation
   */
  protected static void setDegreesOfSeparation(int classDegreesOfSeparation) {
    // throw exception if degreesOfSeparation < 1
    degreesOfSeparation = classDegreesOfSeparation;
  }

  /**
   * Get the class-variable setting for "degrees of separation", which establishes how User networks
   * are derived.
   *
   * @return degrees of separation setting
   */
  protected static int getDegreesOfSeparation() {
    return degreesOfSeparation;
  }

  /**
   * Either gets existing User identified by the submitted id, or if no such User exists, creates
   * and returns a new User instantiated with the submitted id.
   *
   * @param id user-id of the new or existing User
   * @return either existing User identified by the submitted id, or a new User instantiated with
   * the submitted id.
   */
  protected static User getOrCreateUser(String id) {
    User returnedUser = userMap.get(id);
    if (returnedUser == null) {
      returnedUser = new User(id);
      userMap.put(id, returnedUser);
    }
    return returnedUser;
  }

  /**
   * Returns a Collection of all instantiated User objects in user-id order.
   *
   * @return Collection of all Users
   */
  protected static Collection<User> getAllUsers() {
    return userMap.values();
  }

  /**
   * Private constructor to create a new User object.
   *
   * @param id unique identifier of User
   */
  private User(String id) {
    this.id = id;
  }

  /**
   * Returns id, which uniquely identifies User instance.
   *
   * @return user's id
   */
  protected String getId() {
    return id;
  }

  /**
   * Returns NavigableSet of user's friends
   *
   * @return NavigableSet of user's friends
   */
  protected NavigableSet<User> getFriends() {
    return friends;
  }

  /**
   * Returns NavigableSet of all users in this user's network.
   *
   * @return NavigableSet of all users in this user's network
   */
  protected NavigableSet<User> getNetwork() {
    return this.getNetwork(degreesOfSeparation);
  }

  /**
   * Recursively constructs NavigableSet of all connections of a user that are within the degrees of
   * separation denoted by the {@code level} parameter.
   *
   * @param level degrees of separation for network of connections to be returned
   * @return network of all User connections that are within the degrees of separation denoted by
   * the {@code level} parameter
   */
  private NavigableSet<User> getNetwork(int level) {
    NavigableSet<User> network = new TreeSet<>();
    network.addAll(friends);
    if (level > 1) {
      for (User friend : friends) {
        network.addAll(friend.getNetwork(level - 1));
      }
    }
    network.remove(this);
    return network;
  }

  /**
   * Adds the submitted User to this User's "friends" collection.
   * SPECIAL NOTE on #befriend processing: invocation of the #befriend method will have no effect
   * if the submitted timestamp precedes the timestamp of the most recent (already-submitted)
   * #unfriend transaction.
   *
   * @param timestamp timestamp for befriend transaction
   * @param otherUser user to be befriended
   */
  protected void befriend(String timestamp, User otherUser) {
    if (!befriendTimestamps.containsKey(otherUser.getId()) ||
            befriendTimestamps.get(otherUser.getId()).compareTo(timestamp) < 0) {
      befriendTimestamps.put(otherUser.getId(), timestamp);
    }
    if (!unfriendTimestamps.containsKey(otherUser.getId()) ||
            unfriendTimestamps.get(otherUser.getId()).compareTo(timestamp) <= 0) {
      friends.add(otherUser);
    }
  }

  /**
   * Removes the submitted User from this User's "friends" collection.
   * SPECIAL NOTE on #unfriend processing: invocation of the #unfriend method will have no effect
   * if the submitted timestamp precedes the timestamp of the most recent (already-submitted)
   * #befriend transaction.
   *
   * @param timestamp timestamp for unfriend transaction
   * @param otherUser user to be unfriended
   */
  protected void unfriend(String timestamp, User otherUser) {
    if (!unfriendTimestamps.containsKey(otherUser.getId()) ||
            unfriendTimestamps.get(otherUser.getId()).compareTo(timestamp) < 0) {
      unfriendTimestamps.put(otherUser.getId(), timestamp);
    }
    if (!befriendTimestamps.containsKey(otherUser.getId()) ||
            befriendTimestamps.get(otherUser.getId()).compareTo(timestamp) <= 0) {
      friends.remove(otherUser);
    }
  }

  /**
   * Add purchase transaction to internally-maintained collection, with placement in the collection
   * dependent on transaction timestamp and {@link PurchaseManager#getThreshold() purchase threshold}
   * setting.
   *
   * @param timestamp timestamp of purchase transaction
   * @param amountString amount of purchase transaction in String format
   */
  protected void addPurchase(String timestamp, String amountString) {
    addPurchase(timestamp, PurchaseManager.amountStringToInteger(amountString));
  }

  /**
   * Add purchase transaction to internally-maintained collection, with placement in the collection
   * dependent on transaction timestamp and {@link PurchaseManager#getThreshold() purchase threshold}
   * setting.
   *
   * @param timestamp timestamp of purchase transaction
   * @param amount amount of purchase transaction in pennies
   */
  protected void addPurchase(String timestamp, Integer amount) {
    purchaseManager.addPurchase(timestamp, amount);
  }

  /**
   * Determines whether the submitted purchase amount is an anomaly, and if so, returns a
   * two-element array consisting of (a) the mean, and (b) the standard deviation that formed the
   * basis for the anomaly computation; if submitted purchase amount is NOT an anomaly, returns null.
   *
   * @param amount purchase amount in pennies
   * @return null if amount is not an anomaly; otherwise, returns a two-element int array
   * consisting of (a) mean and (b) standard deviation that formed basis of anomaly computation
   */
  protected int[] getAnomalyData(Integer amount) {
    PurchaseManager networkPurchaseManager = new PurchaseManager();
    getNetwork().forEach((user) -> networkPurchaseManager.addPurchases(user.purchaseManager));
    return networkPurchaseManager.getAnomalyData(amount);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return compareTo((User)obj) == 0;
  }

  @Override
  public int compareTo(User other) {
    return this.id.compareTo(other.id);
  }

  @Override
  public String toString() {
    StringBuilder friendString = new StringBuilder(",network={");
    int i=0;
    for (User connection : this.getNetwork()) {
      if (i++ > 0) {
        friendString.append(",");
      }
      friendString.append(connection.getId());
    }
    friendString.append("}");

    return "User{" + "id=" + id + friendString.toString() + '}';

  }

}
