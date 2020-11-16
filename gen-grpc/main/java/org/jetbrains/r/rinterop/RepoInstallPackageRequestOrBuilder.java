// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface RepoInstallPackageRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.RepoInstallPackageRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string packageName = 1;</code>
   * @return The packageName.
   */
  java.lang.String getPackageName();
  /**
   * <code>string packageName = 1;</code>
   * @return The bytes for packageName.
   */
  com.google.protobuf.ByteString
      getPackageNameBytes();

  /**
   * <code>string fallbackMethod = 2;</code>
   * @return The fallbackMethod.
   */
  java.lang.String getFallbackMethod();
  /**
   * <code>string fallbackMethod = 2;</code>
   * @return The bytes for fallbackMethod.
   */
  com.google.protobuf.ByteString
      getFallbackMethodBytes();

  /**
   * <code>map&lt;string, string&gt; arguments = 3;</code>
   */
  int getArgumentsCount();
  /**
   * <code>map&lt;string, string&gt; arguments = 3;</code>
   */
  boolean containsArguments(
      java.lang.String key);
  /**
   * Use {@link #getArgumentsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.String>
  getArguments();
  /**
   * <code>map&lt;string, string&gt; arguments = 3;</code>
   */
  java.util.Map<java.lang.String, java.lang.String>
  getArgumentsMap();
  /**
   * <code>map&lt;string, string&gt; arguments = 3;</code>
   */

  java.lang.String getArgumentsOrDefault(
      java.lang.String key,
      java.lang.String defaultValue);
  /**
   * <code>map&lt;string, string&gt; arguments = 3;</code>
   */

  java.lang.String getArgumentsOrThrow(
      java.lang.String key);
}
