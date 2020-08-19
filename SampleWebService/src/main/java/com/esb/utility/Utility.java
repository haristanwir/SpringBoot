package com.esb.utility;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

public class Utility {

	public static Properties properties = null;
	private static final Logger logger = FlowLogger.getLogger(Utility.class.getName());
	private static final Logger Errorlogger = FlowLogger.getLogger(ErrorHandling.class.getName());

	private static synchronized void loadProperties() throws Exception {
		properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(Constant.APP_ROOT_DIR + File.separator + Constant.APP_DIR + File.separator + Constant.CONFIG_DIR + File.separator + Constant.CONFIG_FILE_NAME);
			properties.load(in);
			in.close();
		} catch (Exception e) {
			Errorlogger.error("Prop: loading failed", e);
			Errorlogger.error(ErrorHandling.getStackTrace(e));
			throw e;
		} finally {
		}
	}

	public static String getProperty(String propertyName) throws Exception {
		String propertyValue = null;
		try {
			if (properties == null) {
				Utility.loadProperties();
			}
			propertyValue = properties.getProperty(propertyName);
		} catch (Exception e) {
			e.printStackTrace();
			Errorlogger.error(e.getMessage(), e);
			Errorlogger.error(ErrorHandling.getStackTrace(e));
			throw e;
		}
		if (propertyValue == null) {
			System.out.println("Prop: " + propertyName + " not found");
			throw new Exception("Prop: " + propertyName + " not found");
		}
		return propertyValue;
	}

}
