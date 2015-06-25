package com.ifedorenko.m2e.mavendev.launch.ui.eventspy.internal.logback;

import javax.inject.Named;

import org.slf4j.LoggerFactory;

import com.ifedorenko.m2e.mavendev.launch.ui.eventspy.internal.MessagePumpFactory;
import com.ifedorenko.m2e.mavendev.launch.ui.eventspy.internal.MessageSink;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

@Named
public class LogbackMessagePumpFactory implements MessagePumpFactory {

  // this prevents Sisu from instantiating this without logback
  private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

  public void createPump(MessageSink sink) {
    Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
    LogbackMessagePump messagePump = new LogbackMessagePump(sink);
    messagePump.setContext(context);
    messagePump.start();
    logger.addAppender(messagePump);
  }

}
