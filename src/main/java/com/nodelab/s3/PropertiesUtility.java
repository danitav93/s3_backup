package com.nodelab.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtility {

	public static String getPropValues(String name)  {
	    Properties prop = new Properties();
	    InputStream input = null;
	 
	    try {
	        input = new FileInputStream(Constants.S3_BACKUP_CONFIG_PATH);
	         
	        // load the properties file
	        prop.load(input);
	 
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        } else {
	        	return null;
	        }
	    }
	    // get the property value and return it
	    return prop.getProperty(name);
	}
	
}
