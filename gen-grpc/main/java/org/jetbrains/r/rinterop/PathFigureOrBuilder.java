// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface PathFigureOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.PathFigure)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .rplugininterop.Polyline subPath = 1;</code>
   */
  java.util.List<org.jetbrains.r.rinterop.Polyline> 
      getSubPathList();
  /**
   * <code>repeated .rplugininterop.Polyline subPath = 1;</code>
   */
  org.jetbrains.r.rinterop.Polyline getSubPath(int index);
  /**
   * <code>repeated .rplugininterop.Polyline subPath = 1;</code>
   */
  int getSubPathCount();
  /**
   * <code>repeated .rplugininterop.Polyline subPath = 1;</code>
   */
  java.util.List<? extends org.jetbrains.r.rinterop.PolylineOrBuilder> 
      getSubPathOrBuilderList();
  /**
   * <code>repeated .rplugininterop.Polyline subPath = 1;</code>
   */
  org.jetbrains.r.rinterop.PolylineOrBuilder getSubPathOrBuilder(
      int index);

  /**
   * <code>bool winding = 2;</code>
   * @return The winding.
   */
  boolean getWinding();

  /**
   * <code>int32 strokeIndex = 3;</code>
   * @return The strokeIndex.
   */
  int getStrokeIndex();

  /**
   * <code>int32 colorIndex = 4;</code>
   * @return The colorIndex.
   */
  int getColorIndex();

  /**
   * <code>int32 fillIndex = 5;</code>
   * @return The fillIndex.
   */
  int getFillIndex();
}
