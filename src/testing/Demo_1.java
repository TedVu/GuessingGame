package testing;// A Java program to demonstrate working of 

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
// synchronized. 
import java.util.logging.SimpleFormatter;

// Driver class
class Demo_1 {
	private static final Logger logger = Logger.getLogger(Demo_1.class.getName());
	private static FileHandler fh;

	public static void main(String args[]) throws IOException {
		try {

			// This block configure the logger with handler and formatter
			fh = new FileHandler("MyLogFile.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			// the following statement is used to log any messages
			logger.info("My first log");

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("Hi How r u?");
	}

}
