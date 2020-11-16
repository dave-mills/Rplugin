// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface AsyncEventOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.AsyncEvent)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Empty busy = 1;</code>
   * @return Whether the busy field is set.
   */
  boolean hasBusy();
  /**
   * <code>.google.protobuf.Empty busy = 1;</code>
   * @return The busy.
   */
  com.google.protobuf.Empty getBusy();
  /**
   * <code>.google.protobuf.Empty busy = 1;</code>
   */
  com.google.protobuf.EmptyOrBuilder getBusyOrBuilder();

  /**
   * <code>.rplugininterop.CommandOutput text = 2;</code>
   * @return Whether the text field is set.
   */
  boolean hasText();
  /**
   * <code>.rplugininterop.CommandOutput text = 2;</code>
   * @return The text.
   */
  org.jetbrains.r.rinterop.CommandOutput getText();
  /**
   * <code>.rplugininterop.CommandOutput text = 2;</code>
   */
  org.jetbrains.r.rinterop.CommandOutputOrBuilder getTextOrBuilder();

  /**
   * <code>.rplugininterop.AsyncEvent.RequestReadLn requestReadLn = 3;</code>
   * @return Whether the requestReadLn field is set.
   */
  boolean hasRequestReadLn();
  /**
   * <code>.rplugininterop.AsyncEvent.RequestReadLn requestReadLn = 3;</code>
   * @return The requestReadLn.
   */
  org.jetbrains.r.rinterop.AsyncEvent.RequestReadLn getRequestReadLn();
  /**
   * <code>.rplugininterop.AsyncEvent.RequestReadLn requestReadLn = 3;</code>
   */
  org.jetbrains.r.rinterop.AsyncEvent.RequestReadLnOrBuilder getRequestReadLnOrBuilder();

  /**
   * <code>.google.protobuf.Empty prompt = 4;</code>
   * @return Whether the prompt field is set.
   */
  boolean hasPrompt();
  /**
   * <code>.google.protobuf.Empty prompt = 4;</code>
   * @return The prompt.
   */
  com.google.protobuf.Empty getPrompt();
  /**
   * <code>.google.protobuf.Empty prompt = 4;</code>
   */
  com.google.protobuf.EmptyOrBuilder getPromptOrBuilder();

  /**
   * <code>.rplugininterop.AsyncEvent.DebugPrompt debugPrompt = 5;</code>
   * @return Whether the debugPrompt field is set.
   */
  boolean hasDebugPrompt();
  /**
   * <code>.rplugininterop.AsyncEvent.DebugPrompt debugPrompt = 5;</code>
   * @return The debugPrompt.
   */
  org.jetbrains.r.rinterop.AsyncEvent.DebugPrompt getDebugPrompt();
  /**
   * <code>.rplugininterop.AsyncEvent.DebugPrompt debugPrompt = 5;</code>
   */
  org.jetbrains.r.rinterop.AsyncEvent.DebugPromptOrBuilder getDebugPromptOrBuilder();

  /**
   * <code>.google.protobuf.Empty termination = 6;</code>
   * @return Whether the termination field is set.
   */
  boolean hasTermination();
  /**
   * <code>.google.protobuf.Empty termination = 6;</code>
   * @return The termination.
   */
  com.google.protobuf.Empty getTermination();
  /**
   * <code>.google.protobuf.Empty termination = 6;</code>
   */
  com.google.protobuf.EmptyOrBuilder getTerminationOrBuilder();

  /**
   * <code>.rplugininterop.AsyncEvent.Exception exception = 7;</code>
   * @return Whether the exception field is set.
   */
  boolean hasException();
  /**
   * <code>.rplugininterop.AsyncEvent.Exception exception = 7;</code>
   * @return The exception.
   */
  org.jetbrains.r.rinterop.AsyncEvent.Exception getException();
  /**
   * <code>.rplugininterop.AsyncEvent.Exception exception = 7;</code>
   */
  org.jetbrains.r.rinterop.AsyncEvent.ExceptionOrBuilder getExceptionOrBuilder();

  /**
   * <code>.rplugininterop.AsyncEvent.ViewRequest viewRequest = 8;</code>
   * @return Whether the viewRequest field is set.
   */
  boolean hasViewRequest();
  /**
   * <code>.rplugininterop.AsyncEvent.ViewRequest viewRequest = 8;</code>
   * @return The viewRequest.
   */
  org.jetbrains.r.rinterop.AsyncEvent.ViewRequest getViewRequest();
  /**
   * <code>.rplugininterop.AsyncEvent.ViewRequest viewRequest = 8;</code>
   */
  org.jetbrains.r.rinterop.AsyncEvent.ViewRequestOrBuilder getViewRequestOrBuilder();

  /**
   * <code>.rplugininterop.AsyncEvent.ShowFileRequest showFileRequest = 9;</code>
   * @return Whether the showFileRequest field is set.
   */
  boolean hasShowFileRequest();
  /**
   * <code>.rplugininterop.AsyncEvent.ShowFileRequest showFileRequest = 9;</code>
   * @return The showFileRequest.
   */
  org.jetbrains.r.rinterop.AsyncEvent.ShowFileRequest getShowFileRequest();
  /**
   * <code>.rplugininterop.AsyncEvent.ShowFileRequest showFileRequest = 9;</code>
   */
  org.jetbrains.r.rinterop.AsyncEvent.ShowFileRequestOrBuilder getShowFileRequestOrBuilder();

  /**
   * <code>.rplugininterop.HttpdResponse showHelpRequest = 10;</code>
   * @return Whether the showHelpRequest field is set.
   */
  boolean hasShowHelpRequest();
  /**
   * <code>.rplugininterop.HttpdResponse showHelpRequest = 10;</code>
   * @return The showHelpRequest.
   */
  org.jetbrains.r.rinterop.HttpdResponse getShowHelpRequest();
  /**
   * <code>.rplugininterop.HttpdResponse showHelpRequest = 10;</code>
   */
  org.jetbrains.r.rinterop.HttpdResponseOrBuilder getShowHelpRequestOrBuilder();

  /**
   * <code>.google.protobuf.Empty subprocessInput = 11;</code>
   * @return Whether the subprocessInput field is set.
   */
  boolean hasSubprocessInput();
  /**
   * <code>.google.protobuf.Empty subprocessInput = 11;</code>
   * @return The subprocessInput.
   */
  com.google.protobuf.Empty getSubprocessInput();
  /**
   * <code>.google.protobuf.Empty subprocessInput = 11;</code>
   */
  com.google.protobuf.EmptyOrBuilder getSubprocessInputOrBuilder();

  /**
   * <code>string browseURLRequest = 12;</code>
   * @return The browseURLRequest.
   */
  java.lang.String getBrowseURLRequest();
  /**
   * <code>string browseURLRequest = 12;</code>
   * @return The bytes for browseURLRequest.
   */
  com.google.protobuf.ByteString
      getBrowseURLRequestBytes();

  /**
   * <code>.rplugininterop.AsyncEvent.RStudioApiRequest rStudioApiRequest = 13;</code>
   * @return Whether the rStudioApiRequest field is set.
   */
  boolean hasRStudioApiRequest();
  /**
   * <code>.rplugininterop.AsyncEvent.RStudioApiRequest rStudioApiRequest = 13;</code>
   * @return The rStudioApiRequest.
   */
  org.jetbrains.r.rinterop.AsyncEvent.RStudioApiRequest getRStudioApiRequest();
  /**
   * <code>.rplugininterop.AsyncEvent.RStudioApiRequest rStudioApiRequest = 13;</code>
   */
  org.jetbrains.r.rinterop.AsyncEvent.RStudioApiRequestOrBuilder getRStudioApiRequestOrBuilder();

  /**
   * <code>int32 debugRemoveBreakpointRequest = 14;</code>
   * @return The debugRemoveBreakpointRequest.
   */
  int getDebugRemoveBreakpointRequest();

  /**
   * <code>.rplugininterop.SourcePosition debugPrintSourcePositionToConsoleRequest = 15;</code>
   * @return Whether the debugPrintSourcePositionToConsoleRequest field is set.
   */
  boolean hasDebugPrintSourcePositionToConsoleRequest();
  /**
   * <code>.rplugininterop.SourcePosition debugPrintSourcePositionToConsoleRequest = 15;</code>
   * @return The debugPrintSourcePositionToConsoleRequest.
   */
  org.jetbrains.r.rinterop.SourcePosition getDebugPrintSourcePositionToConsoleRequest();
  /**
   * <code>.rplugininterop.SourcePosition debugPrintSourcePositionToConsoleRequest = 15;</code>
   */
  org.jetbrains.r.rinterop.SourcePositionOrBuilder getDebugPrintSourcePositionToConsoleRequestOrBuilder();

  public org.jetbrains.r.rinterop.AsyncEvent.EventCase getEventCase();
}
