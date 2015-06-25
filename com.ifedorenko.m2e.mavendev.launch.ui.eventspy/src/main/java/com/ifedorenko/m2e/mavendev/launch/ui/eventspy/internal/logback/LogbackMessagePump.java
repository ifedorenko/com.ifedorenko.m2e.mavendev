package com.ifedorenko.m2e.mavendev.launch.ui.eventspy.internal.logback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ifedorenko.m2e.mavendev.launch.ui.eventspy.internal.MessageSink;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LogbackMessagePump extends AppenderBase<ILoggingEvent> {

  private static final String pattern = "%d{HH:mm:ss.SSS} %.-1level %logger{36} %msg";

  private final MessageSink sink;

  private PatternLayout formatter;

  public LogbackMessagePump(MessageSink sink) {
    this.sink = sink;
  }

  @Override
  public void start() {
    formatter = new PatternLayout();
    formatter.setContext(context);
    formatter.setPattern(pattern);
    formatter.start();

    super.start();
  }

  @Override
  public void stop() {
    formatter.stop();

    super.stop();
  }

  @Override
  protected void append(ILoggingEvent event) {
    Map<String, String> mdc = event.getMDCPropertyMap();

    String groupId = mdc.get("maven.project.groupId");
    String artifactId = mdc.get("maven.project.artifactId");

    if (groupId != null && artifactId != null) {
      Map<String, Object> data = new HashMap<>();
      data.put("messageId", "logEvent");
      data.put("projectId", groupId + ":" + artifactId);
      data.put("message", formatter.doLayout(event));
      try {
        sink.sendMessage(data);
      } catch (IOException e) {
        // silently ignore for now
      }
    }
  }

}
