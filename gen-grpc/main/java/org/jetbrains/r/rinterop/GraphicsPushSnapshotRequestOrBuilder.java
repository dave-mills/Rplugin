// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface GraphicsPushSnapshotRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.GraphicsPushSnapshotRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string groupId = 1;</code>
   */
  java.lang.String getGroupId();
  /**
   * <code>string groupId = 1;</code>
   */
  com.google.protobuf.ByteString
      getGroupIdBytes();

  /**
   * <code>int32 snapshotNumber = 2;</code>
   */
  int getSnapshotNumber();

  /**
   * <code>bytes recorded = 3;</code>
   */
  com.google.protobuf.ByteString getRecorded();
}