package BeeSimulation.lib;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BeeLogger {
	private static final Logger LOGGER = Logger.getLogger(BeeLogger.class.getClass().getName());
	private static BeeLogger beeLogger;
	static Handler fileHandler = null;

	private BeeLogger() {

		try {
			fileHandler = new FileHandler("./bee_model.log");
			SimpleFormatter simple = new SimpleFormatter();
			fileHandler.setFormatter(simple);

			LOGGER.addHandler(fileHandler);

		} catch (IOException e) {
			System.out.println("Could not start logger");
		}
	}

	public static Logger getLogger() {
		if (beeLogger == null)
			beeLogger = new BeeLogger();

		return BeeLogger.LOGGER;
	}
}
