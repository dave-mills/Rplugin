// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface ViewportOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.Viewport)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.rplugininterop.FixedViewport fixed = 1;</code>
   */
  boolean hasFixed();
  /**
   * <code>.rplugininterop.FixedViewport fixed = 1;</code>
   */
  org.jetbrains.r.rinterop.FixedViewport getFixed();
  /**
   * <code>.rplugininterop.FixedViewport fixed = 1;</code>
   */
  org.jetbrains.r.rinterop.FixedViewportOrBuilder getFixedOrBuilder();

  /**
   * <code>.rplugininterop.FreeViewport free = 2;</code>
   */
  boolean hasFree();
  /**
   * <code>.rplugininterop.FreeViewport free = 2;</code>
   */
  org.jetbrains.r.rinterop.FreeViewport getFree();
  /**
   * <code>.rplugininterop.FreeViewport free = 2;</code>
   */
  org.jetbrains.r.rinterop.FreeViewportOrBuilder getFreeOrBuilder();

  public org.jetbrains.r.rinterop.Viewport.KindCase getKindCase();
}