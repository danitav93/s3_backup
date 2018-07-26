package com.nodelab.s3;

public class Exceptions {

	public static class PropertyNotFoundException extends Exception {
		
		private static final long serialVersionUID = 1L;

		// Parameterless Constructor
	      public PropertyNotFoundException() {}

	      // Constructor that accepts a message
	      public PropertyNotFoundException(String message)
	      {
	         super(message);
	      }
	      
	}
	
	public static class CannotSaveException extends Exception {
		
		private static final long serialVersionUID = 1L;

		// Parameterless Constructor
	      public CannotSaveException() {}

	      // Constructor that accepts a message
	      public CannotSaveException(String message)
	      {
	         super(message);
	      }
	      
	}
	
}
