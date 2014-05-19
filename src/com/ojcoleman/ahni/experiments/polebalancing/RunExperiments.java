package com.ojcoleman.ahni.experiments.polebalancing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jgapcustomised.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.AnjiActivator;
import com.anji.integration.AnjiNetTranscriber;
import com.anji.integration.TranscriberException;
import com.anji.persistence.FilePersistence;
import com.anji.persistence.Persistence;
import com.anji.util.DummyConfiguration;
import com.ojcoleman.ahni.hyperneat.Properties;
import com.ojcoleman.ahni.hyperneat.Run;

public class RunExperiments {

	static Logger logger;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Arguments
		String propPath = "properties/three-dimensional-pole-balancing.properties"; //args[0];
		String chromId = "3130";//args[1];
		String outPath = "results/"; //args[2];
		
		try {
			
			
			Properties props = new Properties(propPath);
			configureLog4J(false,props,outPath);
			Persistence db = new FilePersistence();
			db.init(props);
			
			// KINNEBAK, KINNEBAK, KINNEBAK.
			logger.info("Initializing chromosome...");
			AnjiNetTranscriber transcriber = (AnjiNetTranscriber) props.singletonObjectProperty(AnjiNetTranscriber.class);
			Chromosome chrom = db.loadChromosome(chromId, new DummyConfiguration());
			AnjiActivator activator = new AnjiActivator(transcriber.newAnjiNet(chrom), 1);
			
			logger.info(chrom);
			logger.info(activator);
			
			logger.info("Creating environment...");
			TwoDimPoleBalancing tdpb = new TwoDimPoleBalancing();
			tdpb.init(props);
			
			logger.info("Executing simulation...");
			tdpb.evaluate(chrom, activator, outPath, true, false);
			
			logger.info("Done.");
			System.exit(0);
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TranscriberException e) {
			e.printStackTrace();
		}

	}

	
	private static void configureLog4J(boolean disableFiles, Properties properties, String outputDir) {
		// If logging is not disabled.
		if (!properties.getProperty("log4j.rootLogger", "OFF").trim().startsWith("OFF")) {
			Set<String> propKeys = properties.stringPropertyNames();
			// Find all logger labels that correspond to file loggers.
			ArrayList<String> fileLogLabels = new ArrayList<String>();
			ArrayList<String> fileLogProps = new ArrayList<String>();
			Pattern p = Pattern.compile("log4j\\.appender\\.(\\w*)\\.file", Pattern.CASE_INSENSITIVE);
			for (String k : propKeys) {
				Matcher m = p.matcher(k);
				if (m.matches()) {
					fileLogProps.add(m.group());
					fileLogLabels.add(m.group(1));
				}
			}
			if (!fileLogLabels.isEmpty()) {
				if (disableFiles) {
					// Construct a new root logger without the logger labels that corresponding to file loggers.
					String[] rootLoggerProp = properties.getProperty("log4j.rootLogger").split(",");
					String newRootLoggerProp = rootLoggerProp[0];
					for (int i = 1; i < rootLoggerProp.length; i++) {
						if (!fileLogLabels.contains(rootLoggerProp[i].trim())) {
							newRootLoggerProp += ", " + rootLoggerProp[i].trim();
						}
					}
					properties.setProperty("log4j.rootLogger", newRootLoggerProp);
				} else {
					// Make sure all file loggers are configured to output to the output dir.
					for (String prop : fileLogProps) {
						String val = properties.getProperty(prop);
						if (!val.contains(outputDir)) {
							val = outputDir + val;
							properties.setProperty(prop, val);
						}
					}
				}
			}
		}

		java.util.Properties log4jProps = new java.util.Properties();
		log4jProps.putAll(properties);
		PropertyConfigurator.configure(log4jProps);

		properties.configureLogger();
		
		logger = Logger.getLogger(RunExperiments.class);
	}
	
}
