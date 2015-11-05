// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/pubsub/v1/pubsub.proto

package com.google.pubsub.v1;

public interface ModifyPushConfigRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.pubsub.v1.ModifyPushConfigRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string subscription = 1;</code>
   *
   * <pre>
   * The name of the subscription.
   * </pre>
   */
  java.lang.String getSubscription();
  /**
   * <code>optional string subscription = 1;</code>
   *
   * <pre>
   * The name of the subscription.
   * </pre>
   */
  com.google.protobuf.ByteString
      getSubscriptionBytes();

  /**
   * <code>optional .google.pubsub.v1.PushConfig push_config = 2;</code>
   *
   * <pre>
   * The push configuration for future deliveries.
   * An empty pushConfig indicates that the Pub/Sub system should
   * stop pushing messages from the given subscription and allow
   * messages to be pulled and acknowledged - effectively pausing
   * the subscription if Pull is not called.
   * </pre>
   */
  boolean hasPushConfig();
  /**
   * <code>optional .google.pubsub.v1.PushConfig push_config = 2;</code>
   *
   * <pre>
   * The push configuration for future deliveries.
   * An empty pushConfig indicates that the Pub/Sub system should
   * stop pushing messages from the given subscription and allow
   * messages to be pulled and acknowledged - effectively pausing
   * the subscription if Pull is not called.
   * </pre>
   */
  com.google.pubsub.v1.PushConfig getPushConfig();
  /**
   * <code>optional .google.pubsub.v1.PushConfig push_config = 2;</code>
   *
   * <pre>
   * The push configuration for future deliveries.
   * An empty pushConfig indicates that the Pub/Sub system should
   * stop pushing messages from the given subscription and allow
   * messages to be pulled and acknowledged - effectively pausing
   * the subscription if Pull is not called.
   * </pre>
   */
  com.google.pubsub.v1.PushConfigOrBuilder getPushConfigOrBuilder();
}