package com.ifedorenko.m2e.mavendev.launch.ui.eventspy.internal;

import java.io.IOException;
import java.util.Map;

public interface MessageSink {
  void sendMessage(Map<String, Object> data) throws IOException;
}
