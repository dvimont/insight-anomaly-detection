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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 * Provides unit testing for methods of the {@code User} class
 *
 * @author Daniel Vimont
 */
public class UserTest extends TestCase {

  public UserTest(String testName) {
    super(testName);
  }

  /**
   * Test of setDegreesOfSeparation method of class User.
   *
   * @throws java.lang.NoSuchFieldException
   * @throws java.lang.IllegalAccessException
   */
  public void testSetDegreesOfSeparation() throws NoSuchFieldException, IllegalAccessException {
    int degreesOfSeparation = 5;
    User.setDegreesOfSeparation(degreesOfSeparation);
    Field degreesOfSeparationField = User.class.getDeclaredField("degreesOfSeparation");
    degreesOfSeparationField.setAccessible(true);
    degreesOfSeparationField.get(null);
    assertEquals(degreesOfSeparation, degreesOfSeparationField.get(null));
  }

  /**
   * Test of getDegreesOfSeparation method of class User.
   *
   * @throws java.lang.NoSuchFieldException
   * @throws java.lang.IllegalAccessException
   */
  public void testGetDegreesOfSeparation() throws NoSuchFieldException, IllegalAccessException {
    int expResult = 12;
    Field degreesField = User.class.getDeclaredField("degreesOfSeparation");
    degreesField.setAccessible(true);
    degreesField.set(null, expResult);
    int result = User.getDegreesOfSeparation();
    assertEquals(expResult, result);
  }

  /**
   * Test of getOrCreateUser method of class User.
   */
  public void testGetOrCreateUser() {
    String id = "266";
    String expResultId = id;
    User result = User.getOrCreateUser(id);
    assertEquals(expResultId, result.getId());
    int originalUserCount = User.getAllUsers().size();

    User sameUser = User.getOrCreateUser(id); // should get original user (not create new one)
    assertEquals(result, sameUser);
    assertEquals(originalUserCount, User.getAllUsers().size());
  }

  /**
   * Test of getAllUsers method of class User.
   */
  public void testGetAllUsers() {
    List<String> idList = Arrays.asList(new String[]{"567", "890", "667"});
    for (String id : idList) {
      User.getOrCreateUser(id);
    }
    Collection<User> allUsers = User.getAllUsers();
    Set<String> allUserIds = new TreeSet<>();
    for (User user : allUsers) {
      allUserIds.add(user.getId());
    }
    for (String id : idList) {
      assertTrue(allUserIds.contains(id));
    }
  }

  /**
   * Test of getFriends method of class User.
   */
  public void testGetFriends() {
    User.setDegreesOfSeparation(3);
    PurchaseManager.setThreshold(50);

    String id1 = "100";
    String id2 = "200";
    String id3 = "300";
    String id4 = "400";
    String id5 = "500";
    String id6 = "600";
    User user1 = User.getOrCreateUser(id1);
    User user2 = User.getOrCreateUser(id2);
    User user3 = User.getOrCreateUser(id3);
    User user4 = User.getOrCreateUser(id4);
    User user5 = User.getOrCreateUser(id5);
    User user6 = User.getOrCreateUser(id6);
    String timestamp = "2017-06-13 11:33:01";

    user1.befriend(timestamp, user2);
    user2.befriend(timestamp, user1);
    user1.befriend(timestamp, user3);
    user3.befriend(timestamp, user1);
    user1.unfriend(timestamp, user3);
    user3.unfriend(timestamp, user1);
    user1.befriend(timestamp, user4);
    user4.befriend(timestamp, user1);
    user2.befriend(timestamp, user3);
    user3.befriend(timestamp, user2);
    user4.befriend(timestamp, user5);
    user5.befriend(timestamp, user4);
    user5.befriend(timestamp, user6);
    user6.befriend(timestamp, user5);

    NavigableSet<User> expResult = new TreeSet<>();
    expResult.add(user2);
    expResult.add(user4);
    NavigableSet<User> result = user1.getFriends();
    assertEquals(expResult, result);
  }

  /**
   * Test of getNetwork method of class User.
   */
  public void testGetNetwork() {
    User.setDegreesOfSeparation(2);
    PurchaseManager.setThreshold(50);

    String id1 = "101";
    String id2 = "201";
    String id3 = "301";
    String id4 = "401";
    String id5 = "501";
    String id6 = "601";
    User user1 = User.getOrCreateUser(id1);
    User user2 = User.getOrCreateUser(id2);
    User user3 = User.getOrCreateUser(id3);
    User user4 = User.getOrCreateUser(id4);
    User user5 = User.getOrCreateUser(id5);
    User user6 = User.getOrCreateUser(id6);
    String timestamp = "2017-06-13 11:33:01";

    user1.befriend(timestamp, user2);
    user2.befriend(timestamp, user1);
    user1.befriend(timestamp, user3);
    user3.befriend(timestamp, user1);
    user1.unfriend(timestamp, user3);
    user3.unfriend(timestamp, user1);
    user1.befriend(timestamp, user4);
    user4.befriend(timestamp, user1);
    user2.befriend(timestamp, user3);
    user3.befriend(timestamp, user2);
    user4.befriend(timestamp, user5);
    user5.befriend(timestamp, user4);
    user5.befriend(timestamp, user6);
    user6.befriend(timestamp, user5);

    NavigableSet<User> expResult = new TreeSet<>();
    expResult.add(user2);
    expResult.add(user3);
    expResult.add(user4);
    expResult.add(user5);
    NavigableSet<User> result = user1.getNetwork();
    assertEquals(expResult, result);
  }

  /**
   * Test of befriend method of class User.
   */
  public void testBefriend() {
    String timestamp = "2017-06-13 11:33:01";
    User otherUser = User.getOrCreateUser("9999");
    User instance = User.getOrCreateUser("8888");
    instance.befriend(timestamp, otherUser);
    assertTrue(instance.getFriends().contains(otherUser));

    // A submission of #befriend should NOT succeed if the timestamp of the befriend transaction
    //   precedes the timestamp of an already-submitted #unfriend transaction.
    String laterTimestamp = "2017-07-03 11:33:01";
    String earlierTimestamp = "2017-07-01 12:13:41";
    instance.unfriend(laterTimestamp, otherUser);
    assertFalse(instance.getFriends().contains(otherUser));
    instance.befriend(earlierTimestamp, otherUser);
    assertFalse(instance.getFriends().contains(otherUser));
  }

  /**
   * Test of befriend method of class User.
   */
  public void testUnfriend() {
    User otherUser = User.getOrCreateUser("44444");
    User instance = User.getOrCreateUser("55555");

    // A submission of #unfriend should NOT succeed if the timestamp of the unfriend transaction
    //   precedes the timestamp of an already-submitted #befriend transaction.
    String earlierTimestamp = "2017-07-01 12:13:41";
    String laterTimestamp = "2017-07-03 11:33:01";
    instance.befriend(laterTimestamp, otherUser);
    assertTrue(instance.getFriends().contains(otherUser));
    instance.unfriend(earlierTimestamp, otherUser);
    assertTrue(instance.getFriends().contains(otherUser)); // should still be a friend!

    String latestTimestamp = "2017-07-04 03:33:01";
    instance.unfriend(latestTimestamp, otherUser);
    assertFalse(instance.getFriends().contains(otherUser)); // unfriending should succeed!
  }

  /**
   * Test of addPurchase method of class User.
   * Note that #addPurchase method of underlying PurchaseManager class is much more thoroughly
   * tested in PurchaseManagerTest.
   *
   * @throws java.lang.NoSuchFieldException
   * @throws java.lang.IllegalAccessException
   */
  public void testAddPurchase_String_String() throws NoSuchFieldException, IllegalAccessException {
    PurchaseManager.setThreshold(3);

    String timestamp = "2017-06-13 11:33:02";
    String amountString = "389.22";
    User user = User.getOrCreateUser("9090");
    user.addPurchase(timestamp, amountString);
    Field purchaseManagerField = User.class.getDeclaredField("purchaseManager");
    purchaseManagerField.setAccessible(true);
    PurchaseManager purchaseManager = (PurchaseManager)purchaseManagerField.get(user);
    assertEquals(1, purchaseManager.getPurchaseMap().size());
    assertEquals(timestamp, purchaseManager.getPurchaseMap().firstEntry().getKey().timestamp);
    assertEquals(PurchaseManager.amountStringToInteger(amountString),
            purchaseManager.getPurchaseMap().firstEntry().getValue());
  }

  /**
   * Test of addPurchase method of class User.
   * Note that #addPurchase method of underlying PurchaseManager class is much more thoroughly
   * tested in PurchaseManagerTest.
   *
   * @throws java.lang.NoSuchFieldException
   * @throws java.lang.IllegalAccessException
   */
  public void testAddPurchase_String_Integer() throws NoSuchFieldException, IllegalAccessException {
    PurchaseManager.setThreshold(3);

    String timestamp = "2017-06-13 11:33:02";
    Integer amount = 38922;
    User user = User.getOrCreateUser("7070");
    user.addPurchase(timestamp, amount);
    Field purchaseManagerField = User.class.getDeclaredField("purchaseManager");
    purchaseManagerField.setAccessible(true);
    PurchaseManager purchaseManager = (PurchaseManager)purchaseManagerField.get(user);
    assertEquals(1, purchaseManager.getPurchaseMap().size());
    assertEquals(timestamp, purchaseManager.getPurchaseMap().firstEntry().getKey().timestamp);
    assertEquals(amount,
            purchaseManager.getPurchaseMap().firstEntry().getValue());
  }

  /**
   * Test of getAnomalyData method of class User.
   */
  public void testGetAnomalyData() {
    PurchaseManager.setThreshold(50);

    String id1 = "1";
    String id2 = "2";
    String id3 = "3";
    User user1 = User.getOrCreateUser(id1);
    User user2 = User.getOrCreateUser(id2);
    User user3 = User.getOrCreateUser(id3);
    String timestamp = "2017-06-13 11:33:01";

    user1.addPurchase(timestamp, 1683);
    user1.addPurchase(timestamp, 5928);
    user1.befriend(timestamp, user2);
    user2.befriend(timestamp, user1);
    user1.befriend(timestamp, user3);
    user3.befriend(timestamp, user1);
    user1.addPurchase(timestamp, 1120);
    user1.unfriend(timestamp, user3);
    user3.unfriend(timestamp, user1);

    Integer amount = 160183;
    User instance = user2;
    int[] expResult = {2910, 2146};
    int[] result = instance.getAnomalyData(amount);
    assertTrue(result != null);
    assertEquals("Failure to return expected mean value", expResult[0], result[0]);
    assertEquals("Failure to return expected standard-deviation value", expResult[1], result[1]);
  }

  /**
   * Test of compareTo method of class User.
   */
  public void testCompareTo() {
    User other = User.getOrCreateUser("109");
    User instance = User.getOrCreateUser("110");
    int result = instance.compareTo(other);
    assertTrue(result > 0);
    result = other.compareTo(instance);
    assertTrue(result < 0);
  }
}
