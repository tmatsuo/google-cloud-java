/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IamPolicyTest {

  private static final Identity ALL_USERS = Identity.allUsers();
  private static final Identity ALL_AUTH_USERS = Identity.allAuthenticatedUsers();
  private static final Identity USER = Identity.user("abc@gmail.com");
  private static final Identity SERVICE_ACCOUNT =
      Identity.serviceAccount("service-account@gmail.com");
  private static final Identity GROUP = Identity.group("group@gmail.com");
  private static final Identity DOMAIN = Identity.domain("google.com");
  private static final Map<String, ImmutableSet<Identity>> BINDINGS = ImmutableMap.of(
      "viewer",
      ImmutableSet.of(USER, SERVICE_ACCOUNT, ALL_USERS),
      "editor",
      ImmutableSet.of(ALL_AUTH_USERS, GROUP, DOMAIN));
  private static final PolicyImpl SIMPLE_POLICY = PolicyImpl.builder()
      .addIdentity("viewer", USER, SERVICE_ACCOUNT, ALL_USERS)
      .addIdentity("editor", ALL_AUTH_USERS, GROUP, DOMAIN)
      .build();
  private static final PolicyImpl FULL_POLICY =
      new PolicyImpl.Builder(SIMPLE_POLICY.bindings(), "etag", 1).build();

  static class PolicyImpl extends IamPolicy<String> {

    static class Builder extends IamPolicy.Builder<String, Builder> {

      private Builder() {}

      private Builder(Map<String, Set<Identity>> bindings, String etag, Integer version) {
        bindings(bindings).etag(etag).version(version);
      }

      @Override
      public PolicyImpl build() {
        return new PolicyImpl(this);
      }
    }

    PolicyImpl(Builder builder) {
      super(builder);
    }

    @Override
    public Builder toBuilder() {
      return new Builder(bindings(), etag(), version());
    }

    static Builder builder() {
      return new Builder();
    }
  }

  @Test
  public void testBuilder() {
    assertEquals(BINDINGS, FULL_POLICY.bindings());
    assertEquals("etag", FULL_POLICY.etag());
    assertEquals(1, FULL_POLICY.version().intValue());
    Map<String, Set<Identity>> editorBinding =
        ImmutableMap.<String, Set<Identity>>builder().put("editor", BINDINGS.get("editor")).build();
    PolicyImpl policy = FULL_POLICY.toBuilder().bindings(editorBinding).build();
    assertEquals(editorBinding, policy.bindings());
    assertEquals("etag", policy.etag());
    assertEquals(1, policy.version().intValue());
    policy = SIMPLE_POLICY.toBuilder().removeRole("editor").build();
    assertEquals(ImmutableMap.of("viewer", BINDINGS.get("viewer")), policy.bindings());
    assertNull(policy.etag());
    assertNull(policy.version());
    policy = policy.toBuilder()
        .removeIdentity("viewer", USER, ALL_USERS)
        .addIdentity("viewer", DOMAIN, GROUP)
        .build();
    assertEquals(ImmutableMap.of("viewer", ImmutableSet.of(SERVICE_ACCOUNT, DOMAIN, GROUP)),
        policy.bindings());
    assertNull(policy.etag());
    assertNull(policy.version());
    policy = PolicyImpl.builder()
        .removeIdentity("viewer", USER)
        .addIdentity("owner", USER, SERVICE_ACCOUNT)
        .addIdentity("editor", GROUP)
        .removeIdentity("editor", GROUP)
        .build();
    assertEquals(
        ImmutableMap.of("owner", ImmutableSet.of(USER, SERVICE_ACCOUNT)), policy.bindings());
    assertNull(policy.etag());
    assertNull(policy.version());
  }

  @Test
  public void testIllegalPolicies() {
    try {
      PolicyImpl.builder().addIdentity(null, USER);
      fail("Null role should cause exception.");
    } catch (NullPointerException ex) {
      assertEquals("The role cannot be null.", ex.getMessage());
    }
    try {
      PolicyImpl.builder().addIdentity("viewer", null, USER);
      fail("Null identity should cause exception.");
    } catch (NullPointerException ex) {
      assertEquals("Null identities are not permitted.", ex.getMessage());
    }
    try {
      PolicyImpl.builder().addIdentity("viewer", USER, (Identity[]) null);
      fail("Null identity should cause exception.");
    } catch (NullPointerException ex) {
      assertEquals("Null identities are not permitted.", ex.getMessage());
    }
    try {
      PolicyImpl.builder().bindings(null);
      fail("Null bindings map should cause exception.");
    } catch (NullPointerException ex) {
      assertEquals("The provided map of bindings cannot be null.", ex.getMessage());
    }
    try {
      Map<String, Set<Identity>> bindings = new HashMap<>();
      bindings.put("viewer", null);
      PolicyImpl.builder().bindings(bindings);
      fail("Null set of identities should cause exception.");
    } catch (NullPointerException ex) {
      assertEquals("A role cannot be assigned to a null set of identities.", ex.getMessage());
    }
    try {
      Map<String, Set<Identity>> bindings = new HashMap<>();
      Set<Identity> identities = new HashSet<>();
      identities.add(null);
      bindings.put("viewer", identities);
      PolicyImpl.builder().bindings(bindings);
      fail("Null identity should cause exception.");
    } catch (IllegalArgumentException ex) {
      assertEquals("Null identities are not permitted.", ex.getMessage());
    }
  }

  @Test
  public void testEqualsHashCode() {
    assertNotNull(FULL_POLICY);
    PolicyImpl emptyPolicy = PolicyImpl.builder().build();
    AnotherPolicyImpl anotherPolicy = new AnotherPolicyImpl.Builder().build();
    assertNotEquals(emptyPolicy, anotherPolicy);
    assertNotEquals(emptyPolicy.hashCode(), anotherPolicy.hashCode());
    assertNotEquals(FULL_POLICY, SIMPLE_POLICY);
    assertNotEquals(FULL_POLICY.hashCode(), SIMPLE_POLICY.hashCode());
    PolicyImpl copy = SIMPLE_POLICY.toBuilder().build();
    assertEquals(SIMPLE_POLICY, copy);
    assertEquals(SIMPLE_POLICY.hashCode(), copy.hashCode());
  }

  @Test
  public void testBindings() {
    assertTrue(PolicyImpl.builder().build().bindings().isEmpty());
    assertEquals(BINDINGS, SIMPLE_POLICY.bindings());
  }

  @Test
  public void testEtag() {
    assertNull(SIMPLE_POLICY.etag());
    assertEquals("etag", FULL_POLICY.etag());
  }

  @Test
  public void testVersion() {
    assertNull(SIMPLE_POLICY.version());
    assertEquals(1, FULL_POLICY.version().intValue());
  }

  static class AnotherPolicyImpl extends IamPolicy<String> {

    static class Builder extends IamPolicy.Builder<String, Builder> {

      private Builder() {}

      @Override
      public AnotherPolicyImpl build() {
        return new AnotherPolicyImpl(this);
      }
    }

    AnotherPolicyImpl(Builder builder) {
      super(builder);
    }

    @Override
    public Builder toBuilder() {
      return new Builder();
    }
  }
}
