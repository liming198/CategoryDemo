/**
 * 
 */
package com.jda.anjiceva.tms.main;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jda.anjiceva.tms.been.LocationData;
import com.jda.anjiceva.tms.been.RouterMatrix;
import com.jda.anjiceva.tms.services.AmapGEOCodingGenerator;
import com.jda.anjiceva.tms.services.AmapRoadDistanceGenerator;
import com.jda.anjiceva.tms.services.LocationDataBuilder;
import com.jda.anjiceva.tms.services.RouterMatrixBuilder;

/**
 * @author j1015278
 *
 */
public class GenerateMapData {
	private final static Logger logger = LoggerFactory.getLogger(GenerateMapData.class);

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long starttime = System.currentTimeMillis();
		if (!LocationDataBuilder.isLocationDataFileExists()) {
			logger.error("Input file \"{}\" does not exist!!!",
					LocationDataBuilder.getInputFileName());
			System.exit(-1);
		}
		List<LocationData> locs = null;
		try {
			locs = AmapGEOCodingGenerator.generateNodeLocation();
		} catch (IOException | InterruptedException e) {
			logger.error("", e);
			throw e;
		}
		logger.info("Complete to get latitude and longitude for location data in {} seconds.",
				(System.currentTimeMillis() - starttime) / 1000);
		List<RouterMatrix> routerMatrixes = null;
		try {
	//		if (!locs.isEmpty())
			routerMatrixes = RouterMatrixBuilder.generateRouterMatrixes(locs);
		} catch (IOException | InterruptedException e) {
			logger.error("", e);
			throw e;
		}
		try {
			//RoadDistanceGenerator.generateRoadDistanceFile(routerMatrixes);
			AmapRoadDistanceGenerator.generateRoadDistanceFileByDirection(routerMatrixes);
		} catch (IOException | InterruptedException e) {
			logger.error("", e);
			throw e;
		}
		logger.info("Complete to get the distance and transit time from Baidu API in {} seconds.",
				(System.currentTimeMillis() - starttime) / 1000);
	}
}
