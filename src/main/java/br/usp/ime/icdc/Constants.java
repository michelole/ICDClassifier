package br.usp.ime.icdc;

/**
 * Class with some global static constants.
 * 
 * @author michel
 */
// TODO move to xml
public class Constants {

	public static final int THREADS = 1;
	public static final int CONNECTIONS = THREADS * 2;

	// TODO change to something better, maybe setting automatically on the main method.
	public static final boolean RECREATE_DB = true;
	public static final int BATCH_SIZE = 100;

	// TODO move to a config file
	/** Root directory where reference tables are located. */
	public static final String REF_DIR = "src/ref/";

	/** Directory containing large data files. */
	public static final String DATA_DIR = "src/data/";

	// TODO defined in models.xml for CoGrOO implementation. May not be necessary.
	/** Directory containing models (e.g. OpenNLP models). */
	public static final String MODEL_DIR = "src/models/";

	/** Defines if a cache of instances is used or not. */
	public static final boolean CACHE = false;

	/** Indicates wheter to generate stats or not. */
	public static final boolean STATS = false;

	public static final float TEST_RATIO = 0.1f;
	// public static final int THRESHOLD = 10;

	// TODO move to a class smoothing or something
	public static final double ALPHA = 0.5d;

	public static Configuration CONFIG;

}
