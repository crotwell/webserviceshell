package edu.iris.wss.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.iris.wss.framework.ParamConfigurator.ConfigParam.ParamType;

public class ParamConfigurator {

    private static final String configFileName = "META-INF/param.cfg";
    private static final String userConfigFileName = "userParam.cfg";
	public static final Logger logger = Logger.getLogger(ParamConfigurator.class);
	
	public static class ConfigParam {
		public static enum ParamType { TEXT, DATE, NUMBER, BOOLEAN, NONE };

		public String name;
		public ParamType type;
		public String value = null;

		public ConfigParam() {
		}
		
		public ConfigParam (String name, ParamType type) {
			this.name = name;
			this.type = type;
		}
		
		public ConfigParam (String name, ParamType type, String value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}
	}

	public HashMap<String, ConfigParam> paramMap = new HashMap<String, ConfigParam>();
			
//	public void dump() {
//		logger.info("Found: " + paramMap.size() + " keys");
//		for (String key: paramMap.keySet()) {
//			logger.info("key: " + key + " type: " + paramMap.get(key).type);
//		}
//	}
	
	public String getValue(String key) {
		ConfigParam cp = paramMap.get(key);
		if (cp == null) return null;
		else return cp.value;
	}
	
	public void loadConfigFile() throws Exception {		
		Properties configurationProps = new Properties();
		Boolean userConfig = false;

		// Try to read the user config first from somewhere in the CLASSPATH.  If not found,
		// an exception will be thrown.
		try {	
			configurationProps.load(this.getClass().getClassLoader().getResourceAsStream(userConfigFileName));
			userConfig = true;
		} catch (Exception e) {	}

		if (!userConfig) {
			// This configuration requires a restart of the application to pick up the changes to the file.
			configurationProps.load(this.getClass().getClassLoader().getResourceAsStream(configFileName));
		}
				
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<String> tmpKeys = new ArrayList(configurationProps.keySet());
		
		for (String key: tmpKeys) {
			if (paramMap.containsKey(key))
				throw new Exception("Duplicated config parameter: " + key);
			
			String type = (String) configurationProps.get(key);
			if (!isOkString(type) )  type = null;
			ParamType paramType;
			try {
				paramType = ParamType.valueOf(type.toUpperCase());	
			} catch (Exception e) {
				throw new Exception("Unrecognized param type: " + type);
			}
			
			paramMap.put(key, new ConfigParam(key, paramType)); 
			configurationProps.remove(key);	
		}
	}

	private static boolean isOkString(String s) {
		return ((s != null) && !s.isEmpty());
	}		
}