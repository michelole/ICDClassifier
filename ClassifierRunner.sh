# TODO Check what to correct (updating Hibernate?) to solve cleanupDaemonThreads=false in a better way
mvn exec:java -Dexec.mainClass="br.usp.ime.icdc.run.ClassifierRunner" -Dexec.cleanupDaemonThreads=false
