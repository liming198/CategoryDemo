/**
 * 
 */
package com.jda.anjiceva.tms.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.jda.anjiceva.tms.been.LocationData;
import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 *
 */
public class LocationDataBuilder {

	private final static Logger logger = LoggerFactory.getLogger(LocationDataBuilder.class);
	private final static File GEO_VALID_LOC_FILE = new File(CommonUtils.DE_DIR,
			"geovalidlocationraw.txt");
	private final static File LOCATION_DATA_FILE = new File(CommonUtils.LOCDATA_DIR,
			"location_data.csv");

	public static List<LocationData> loadLocationData() throws IOException {
		logger.info("Loading location data from file...");
		List<LocationData> locations = new ArrayList<LocationData>();
		CsvReader cr = null;
		try {
			cr = new CsvReader(new FileInputStream(LOCATION_DATA_FILE), ',',
					CommonUtils.FILE_CHARSET);
			cr.setSkipEmptyRecords(true);
			cr.readHeaders();
			while (cr.readRecord()) {
				LocationData loc = new LocationData();
				loc.setPostalCode(cr.get(LocationData.POSTAL_CODE));
				loc.setCountryISO2(cr.get(LocationData.COUNTRY_ISO2));
				loc.setStateCode(cr.get(LocationData.STATE_CODE));
				String cityCN = cr.get(LocationData.CITY_CN);
				loc.setCityCN(cityCN);
				String cityEN = cr.get(LocationData.CITY_EN);
				if (StringUtils.isEmpty(cityEN)) {
					cityEN = StringUtils.capitalize(PinyinHelper.convertToPinyinString(cityCN, "",
							PinyinFormat.WITHOUT_TONE));
				}
				loc.setCityEN(cityEN);
				loc.setAddress(cr.get(LocationData.ADDRESS));
				loc.setLocId(cr.get(LocationData.LOCATION_ID));
				loc.setAssginedDC(cr.get(LocationData.ASSGINED_DC));
				String newFlagStr = cr.get(LocationData.NEW_FLAG);
				if (!StringUtils.isNumeric(newFlagStr)) {
					loc.setNewFlag(1);
				} else {
					loc.setNewFlag(Integer.valueOf(newFlagStr));
				}
				String latitude = cr.get(LocationData.LATITUDE);
				try {
					if (StringUtils.isNotBlank(latitude)) {
						loc.setLatitude(new BigDecimal(latitude));
					}
				} catch (Exception e) {
					logger.error("Invalid latitude \"{}\" in location data!!!", latitude);
					throw e;

				}
				String longitude = cr.get(LocationData.LONGITUDE);
				try {
					if (StringUtils.isNotBlank(longitude)) {
						loc.setLongitude(new BigDecimal(longitude));
					}
				} catch (Exception e) {
					logger.error("Invalid longitude \"{}\" in location data!!!", longitude);
					throw e;
				}
				locations.add(loc);
				String preciseStr = cr.get(LocationData.PRECISE);
				if (StringUtils.isNumeric(preciseStr)) {
					loc.setPrecise(Integer.valueOf(preciseStr));
				}
				String confidenceStr = cr.get(LocationData.CONFIDENCE);
				if (StringUtils.isNumeric(confidenceStr)) {
					loc.setConfidence(Integer.valueOf(confidenceStr));
				}
			}
		} finally {
			if (cr != null) {
				cr.close();
			}
		}
		return locations;
	}

	public static void writeDownGEOValidLocation(List<LocationData> locations) throws IOException {
		logger.info("Generating the geovalidlocation data...");
		CsvWriter cw = null;
		try {
			cw = new CsvWriter(new FileOutputStream(GEO_VALID_LOC_FILE, false), ',',
					CommonUtils.FILE_CHARSET);
			for (int i = 0; i < locations.size(); i++) {
				LocationData loc = locations.get(i);
				cw.writeRecord(loc.toGEOValidLocationArray());
			}
			cw.flush();
		} finally {
			cw.close();
		}
	}

	public static void writeDownLocationData(List<LocationData> locations) throws IOException {
		logger.info("Updating the location data...");
		CsvWriter cw = null;
		try {
			cw = new CsvWriter(new FileOutputStream(LOCATION_DATA_FILE, false), ',',
					CommonUtils.FILE_CHARSET);
			cw.writeRecord(LocationData.LOCATION_HEADER);
			for (int i = 0; i < locations.size(); i++) {
				LocationData loc = locations.get(i);
				cw.writeRecord(loc.toArray());
			}
			cw.flush();
		} finally {
			cw.close();
		}
	}

	public static boolean isLocationDataFileExists() {
		return LOCATION_DATA_FILE.exists();
	}

	public static String getInputFileName() {
		return LOCATION_DATA_FILE.getPath();
	}

}
