package com.esb.utility;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class FlowLogger {

	private static Logger logger = null;
	private static boolean isInitialized = false;

	private static synchronized void initialize() {
		try {
			ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
			AppenderComponentBuilder console = builder.newAppender("console", "Console");
			LayoutComponentBuilder standardPattern = builder.newLayout("PatternLayout");
			standardPattern.addAttribute("pattern", "%d{dd MMM yyyy HH:mm:ss,SSS}|[%p]|{%t}:%m%n");
			console.add(standardPattern);
			builder.add(console);
			RootLoggerComponentBuilder asynclogger = asynclogger = builder.newAsyncRootLogger(Level.DEBUG);

//			
//			try {
//				if (Utility.getProperty("LOG_LEVEL").equalsIgnoreCase("info")) {
//					asynclogger = builder.newAsyncRootLogger(Level.INFO);
//				}
//			} catch (Exception ex1) {
//			}
//			

			asynclogger.addAttribute("additivity", false);
			asynclogger.add(builder.newAppenderRef("console"));
			builder.add(asynclogger);
			Configurator.initialize(builder.build());
			isInitialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logInfo(String msg, String loggerName) {
		if (!isInitialized) {
			FlowLogger.initialize();
		}
		Logger logger = LogManager.getLogger(loggerName);
		logger.info(msg);
	}

	public static void logDebug(String msg, String loggerName) {
		if (!isInitialized) {
			FlowLogger.initialize();
		}
		Logger logger = LogManager.getLogger(loggerName);
		logger.debug(msg);
	}

	public static Logger getLogger(String className) {
		if (!isInitialized) {
			FlowLogger.initialize();
		}
		logger = LogManager.getLogger(className);
		return logger;
	}
}
