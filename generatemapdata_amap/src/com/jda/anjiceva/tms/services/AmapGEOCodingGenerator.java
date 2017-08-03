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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jda.anjiceva.tms.been.LocationData;
import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 * GEO Coding of AMap API
 */
public class AmapGEOCodingGenerator {
	private final static Logger logger = LoggerFactory.getLogger(AmapGEOCodingGenerator.class);

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
								"Got nothing from Amap API for address \"{}\", try in another \"{}\" seconds.",
								loc.getAddress(), "60");
						Thread.sleep(60000);
						logger.info("Program recover to running...");
						continue;
					}
					String infoCode = geoObj.getString("infocode");
					switch (infoCode) {
					case "10000":
						//normal, continue to parse the location data
						break inner;
					case "10001":
						//invalid key or key is expired

					case "10002":
						//unauthorized access the service or error spelled the api link
					case "10003":
						//daily query over limit
					case "10009":
					//user key platform not match
					{
						ak = CommonUtils.nextAK();
						logger.warn("Change to another ak \"{}\"", ak);
						break;
					}
					case "10004":
						//access too frequency
					case "20003":
					//unknow error
					{
						logger.error(
								"Encounter an error to get latitude and longitude for address \"{}\", Error returned from Amap is \"{}\", waiting \"{}\" second recovery...",
								loc.getAddress(), geoObj.getString("info"), "120000");
						Thread.sleep(120000);
						logger.info("Program recover to running...");
					}
					case "10005":
						//invalid user IP
					case "10006":
						//invalid user domain
					case "10007":
						//invalid user signature
					case "10008":
						//invalid user scode
					case "10010":
						//ip query over limit
					case "10011":
						//not support https
					case "20000":
						//invalid parameters
					case "20002":
					//illegal request
					{
						logger.error(
								"Encounter an error to get latitude and longitude for address \"{}\", error returned from Amap is: \"{}\"",
								loc.getAddress(), geoObj.getString("info"));
						throw new RuntimeException(geoObj.getString("info"));
					}
					case "20001":
					//missing mandatory parameters
					{
						logger.error(
								"Encounter an error to get latitude and longitude for address \"{}\", error returned from Amap is: \"{}\"",
								loc.getAddress(), geoObj.getString("info"));
						continue outter;
					}
					default:
						logger.error(
								"Encounter an error to get latitude and longitude for address \"{}\", Error returned from Amap is \"{}\", waiting \"{}\" second recovery...",
								loc.getAddress(), geoObj.getString("info"), "120");
						Thread.sleep(120000);
						logger.info("Program recover to running...");
					}
				} catch (UnsupportedEncodingException e) {
					throw e;
				} catch (JSONException e) {
					logger.error(
							"Encounter an error to get latitude and longitude for address \"{}\"",
							loc.getAddress());
					logger.error("Parsing the data from Amap API with error: ", e);
					logger.error("Waiting {} seconds for recovery...", "60");
					Thread.sleep(60000);
					logger.info("Program recover to running...");
				}
			}
			JSONArray geoCodesArr = geoObj.getJSONArray("geocodes");
			if (geoCodesArr.size() != 0)
			{
				String locStr = geoCodesArr.getJSONObject(0).getString("location");
				String[] locArr = StringUtils.split(locStr, ',');
				loc.setLatitude(new BigDecimal(locArr[0]));
				loc.setLongitude(new BigDecimal(locArr[1]));
			}
			else
			{
				logger.error("Address {} can not be found in AMAP", loc.getAddress());
				continue ;
			}
		}
		LocationDataBuilder.writeDownLocationData(locs);
		LocationDataBuilder.writeDownGEOValidLocation(locs);
		return locs;
	}

	public static String getGEOCodingByAddr(String addr, String city, String ak)
			throws UnsupportedEncodingException, InterruptedException {
		logger.debug("Get latitude and longitude for address \"{}\" in city \"{}\"", addr, city);
		StringBuilder wholeURL = new StringBuilder(GEOCODING_API).append("&key=").append(
				URLEncoder.encode(ak, CommonUtils.COMM_ENCODING));
		wholeURL.append("&address=").append(URLEncoder.encode(addr, CommonUtils.COMM_ENCODING));
		if (!StringUtils.isBlank(city)) {
			wholeURL.append("&city=").append(URLEncoder.encode(city, CommonUtils.COMM_ENCODING));
		}
		logger.debug("URL for getting GEOCoding data is {}.", wholeURL.toString());
		System.out.println("URL for getting GEOCoding data is " + wholeURL.toString());
		String jsonStr = null;
		while (true) {
			try {
				jsonStr = CommonUtils.getHTTPResponse(wholeURL.toString());
				break;
			} catch (IOException e) {
				logger.error("Get latitude and longitude from amap API with error: ", e);
				logger.error("Waiting {} seconds for recovery...", "60");
				Thread.sleep(60000);
				logger.info("Program recover to running...");
			}
		}
		logger.debug("Information got from amap API is: {}", jsonStr);
		return jsonStr;
	}

	public static void main(String[] args) throws UnsupportedEncodingException,
			InterruptedException {
		String jsonStr = getGEOCodingByAddr("AAA", "上海",
				"15743d6177cb90adb4f8b4af416615b4");
		JSONObject geoObj = JSON.parseObject(jsonStr);
		System.out.println(geoObj.getString("status"));
		JSONArray geoCodesArr = geoObj.getJSONArray("geocodes");
		System.out.println(geoCodesArr.getJSONObject(0).getString("location"));
	}
}
