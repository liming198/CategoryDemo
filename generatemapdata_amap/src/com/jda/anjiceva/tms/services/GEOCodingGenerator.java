/**
 * 
 */
package com.jda.anjiceva.tms.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jda.anjiceva.tms.been.LocationData;
import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 * GEO Coding of Baidu API
 */
public class GEOCodingGenerator {
	private final static Logger logger = LoggerFactory.getLogger(GEOCodingGenerator.class);

	private final static String GEOCODING_API = CommonUtils.getValue("geocoding_api");
	private static String ak = CommonUtils.nextAK();

	public static List<LocationData> generateNodeLocation() throws IOException,
			InterruptedException {
		logger.info("Generating node location ...");
		if (StringUtils.isEmpty(ak)) {
			logger.error("Absence of AK for getting information from Baidu API!");
			throw new RuntimeException("Absence of AK for getting information from Baidu API");
		}
		List<LocationData> locs = LocationDataBuilder.loadLocationData();
		int i = 0;
		outter: for (; i < locs.size(); i++) {
			LocationData loc = locs.get(i);
			if (StringUtils.isNotBlank(loc.getLatitude())
					&& StringUtils.isNotBlank(loc.getLongitude())) {
				continue;
			}
			JSONObject geoObj = null;
			inner: while (true) {
				try {
					geoObj = JSON.parseObject(getGEOCodingByAddr(loc.getAddress(), loc.getCityCN(),
							ak));
					if (geoObj == null) {
						logger.error(
								"Got nothing from Baidu API for address \"{}\", try in another \"{}\" seconds.",
								loc.getAddress(), "60");
						Thread.sleep(60000);
						logger.info("Program recover to running...");
						continue;
					}
					int status = geoObj.getIntValue("status");
					switch (status) {
					case 0:
						//normal, continue to parse the location data
						break inner;
					case 1:
					case 2:
					//invalid request parameter, continue to get data for another address
					{
						logger.error(
								"Encounter an error to get latitude and longitude for address \"{}\", error returned from Baidu is: \"{}\"",
								loc.getAddress(), geoObj.getString("msg"));
						continue outter;
					}
					case 3:
						//authority validation failed, change another one
					case 4:
						//exceed the access limitation, change another one
					case 5:
					//invalid ak, change another one
					{
						ak = CommonUtils.nextAK();
						logger.warn("Change to another ak \"{}\"", ak);
						break;
					}
					case 101:
						//forbidden by server, interrupt the program
					case 102:
						//invalid ip address, interrupt the program
						logger.error(
								"Encounter an error to get latitude and longitude for address \"{}\", error returned from Baidu is: \"{}\"",
								loc.getAddress(), geoObj.getString("msg"));
						throw new RuntimeException(geoObj.getString("msg"));
					default:
						logger.error(
								"Encounter an error to get latitude and longitude for address \"{}\", Error returned from Baidu is \"{}\", waiting \"{}\" second recovery...",
								loc.getAddress(), geoObj.getString("msg"), "120000");
						Thread.sleep(120000);
						logger.info("Program recover to running...");
					}
				} catch (UnsupportedEncodingException e) {
					throw e;
				} catch (JSONException e) {
					logger.error(
							"Encounter an error to get latitude and longitude for address \"{}\"",
							loc.getAddress());
					logger.error("Parsing the data from Baidu API with error: ", e);
					logger.error("Waiting {} seconds for recovery...", "60");
					Thread.sleep(60000);
					logger.info("Program recover to running...");
				}
			}

			JSONObject rstObj = geoObj.getJSONObject("result");
			JSONObject locObj = rstObj.getJSONObject("location");
			loc.setLatitude(new BigDecimal(locObj.getDoubleValue("lat")));
			loc.setLongitude(new BigDecimal(locObj.getDoubleValue("lng")));
			loc.setPrecise(rstObj.getIntValue("precise"));
			loc.setConfidence(rstObj.getIntValue("confidence"));
		}
		LocationDataBuilder.writeDownLocationData(locs);
		LocationDataBuilder.writeDownGEOValidLocation(locs);
		return locs;
	}

	public static String getGEOCodingByAddr(String addr, String city, String ak)
			throws UnsupportedEncodingException, InterruptedException {
		logger.debug("Get latitude and longitude for address \"{}\" in city \"{}\"", addr, city);
		StringBuilder wholeURL = new StringBuilder(GEOCODING_API).append("&ak=").append(
				URLEncoder.encode(ak, CommonUtils.COMM_ENCODING));
		wholeURL.append("&address=").append(URLEncoder.encode(addr, CommonUtils.COMM_ENCODING));
		if (!StringUtils.isBlank(city)) {
			wholeURL.append("&city=").append(URLEncoder.encode(city, CommonUtils.COMM_ENCODING));
		}
		logger.debug("URL for getting GEOCoding data is {}.", wholeURL.toString());
		String jsonStr = null;
		while (true) {
			try {
				jsonStr = CommonUtils.getHTTPResponse(wholeURL.toString());
				break;
			} catch (IOException e) {
				logger.error("Get latitude and longitude from Baidu API with error: ", e);
				logger.error("Waiting {} seconds for recovery...", "60");
				Thread.sleep(60000);
				logger.info("Program recover to running...");
			}
		}
		logger.debug("Information got from Baidu API is: {}", jsonStr);
		return jsonStr;
	}
}
