/**
 * 
 */
package com.jda.anjiceva.tms.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jda.anjiceva.tms.been.LocationPair;
import com.jda.anjiceva.tms.been.RouterMatrix;
import com.jda.anjiceva.tms.exception.TMSException;
import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 *
 */
public class RoadDistanceGenerator {

    private final static Logger logger = LoggerFactory.getLogger(RoadDistanceGenerator.class);

    private final static String ROUTEMATRIX_API = CommonUtils.getValue("routermatrix_api");
    private static String ak = CommonUtils.nextAK();

    public static void generateRoadDistanceFile(List<RouterMatrix> routerMatrixes)
            throws IOException, InterruptedException {
        logger.info("Generating road distance file...");
        if (StringUtils.isEmpty(ak)) {
            logger.error("Absence of AK for getting information from Baidu API!!!");
            throw new RuntimeException("Absence of AK for getting information from Baidu API!!!");
        }
        for (int i = 0; i < routerMatrixes.size(); i++) {
            RouterMatrix rm = routerMatrixes.get(i);
            try {
                generateRoadDistanceData(rm);
            } catch (TMSException e) {
                continue;
            }
            if (i >= 1000 || i == routerMatrixes.size() - 1) {
                List<RouterMatrix> tmpRM = routerMatrixes.subList(0, i + 1);
                RouterMatrixBuilder.writeDownRoadDistance(tmpRM);
                // RouterMatrixBuilder.writeDownReadableDistance(tmpRM);
                RouterMatrixBuilder.writeDownDTTOFile(tmpRM);
                routerMatrixes.removeAll(tmpRM);
                RouterMatrixBuilder.writeToRMTempFile(routerMatrixes, false);
                i = -1;
            }
        }
        RouterMatrixBuilder.removeRMTempFile();
        RouterMatrixBuilder.createOverrideDistanceFile();
    }

    public static void generateRoadDistanceData(RouterMatrix routerMatrix) throws IOException,
            TMSException, InterruptedException {
        JSONArray elementArr = null;
        // Onward direction
        elementArr = getRouterMatrixElements(routerMatrix.getOriginCoordinates(),
                routerMatrix.getDestCoordinates(), "12");
        LocationPair[] locPairs = routerMatrix.getOnwardLocPairs();
        for (int i = 0; i < locPairs.length; i++) {
            LocationPair locPair = locPairs[i];
            if (locPair == null) {
                continue;
            }
            JSONObject elementObj = elementArr.getJSONObject(i);
            if (elementObj.containsKey("status")) {
                logger.error(
                        "Get no distance and transit time for origin \"{}\" and destination \"{}\", message from Baidu API: {}",
                        locPair.getOriginLoc().getLocId(), locPair.getDestLoc().getLocId(),
                        elementObj.getString("message"));
            } else {
                JSONObject distanceObj = elementObj.getJSONObject("distance");
                JSONObject durationObj = elementObj.getJSONObject("duration");
                locPair.setOnwardDistance(distanceObj.getDoubleValue("value"));
                locPair.setOnwardDuration(durationObj.getDoubleValue("value"));
            }
        }
        // Reverse direction
        elementArr = getRouterMatrixElements(routerMatrix.getDestCoordinates(),
                routerMatrix.getOriginCoordinates(), "12");
        locPairs = routerMatrix.getReverseLocPairs();
        for (int i = 0; i < locPairs.length; i++) {
            LocationPair locPair = locPairs[i];
            if (locPair == null) {
                continue;
            }
            JSONObject elementObj = elementArr.getJSONObject(i);
            if (elementObj.containsKey("status")) {
                logger.error(
                        "Get no distance and transit time for origin \"{}\" and destination \"{}\", message from Baidu API: {}",
                        locPair.getDestLoc().getLocId(), locPair.getOriginLoc().getLocId(),
                        elementObj.getString("message"));
            } else {
                JSONObject distanceObj = elementObj.getJSONObject("distance");
                JSONObject durationObj = elementObj.getJSONObject("duration");
                locPair.setReverseDistance(distanceObj.getDoubleValue("value"));
                locPair.setReverseDuration(durationObj.getDoubleValue("value"));
            }
        }
    }

    private static JSONArray getRouterMatrixElements(String origins, String destinations,
            String tactics) throws IOException, TMSException, InterruptedException {
        JSONObject routerMatrixObj = null;
        loop: while (true) {
            try {
                routerMatrixObj = JSON.parseObject(getRouterMatrix(origins, destinations, "12"));
                if (routerMatrixObj == null) {
                    logger.error(
                            "Got nothing from Baidu API for locations \"{}\", and \"{}\", try in another \"{}\" seconds.",
                            origins, destinations, "60");
                    Thread.sleep(60000);
                    logger.info("Program recover to running...");
                    continue;
                }
                int status = routerMatrixObj.getIntValue("status");
                switch (status) {
                    case 0:
                        // normal, continue to parse the location data
                        break loop;
                    case 1:
                    case 2:
                    // invalid request parameter, continue to get data for another address
                    {
                        throw new TMSException(routerMatrixObj.getString("message"));
                    }
                    case 3:
                        // authority validation failed, change another one
                    case 4:
                        // exceed the access limitation, change another one
                    case 302:
                    case 5:
                    // invalid ak, change another one
                    {
                        ak = CommonUtils.nextAK();
                        logger.warn("Change to another ak \"{}\"!", ak);
                        break;
                    }
                    case 101:
                        // forbidden by server, interrupt the program
                    case 102:
                        // invalid ip address, interrupt the program
                        throw new RuntimeException(routerMatrixObj.getString("message"));
                    default:
                        logger.error(
                                "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\"!!!",
                                origins, destinations);
                        logger.error(
                                "Error returned from Baidu is \"{}\", waiting {} second recovery...",
                                routerMatrixObj.getString("message"), "120");
                        Thread.sleep(120000);
                        logger.info("Program recover to running...");
                }
            } catch (UnsupportedEncodingException e) {
                throw e;
            } catch (JSONException e) {
                logger.error(
                        "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\"!!!",
                        origins, destinations);
                logger.error("Parsing the data from Baidu API with error: ", e);
                logger.error("Waiting {} seconds for recovery...", "60");
                Thread.sleep(60000);
                logger.info("Program recover to running...");
            }
        }
        return routerMatrixObj.getJSONObject("result").getJSONArray("elements");
    }

    public static String getRouterMatrix(String origins, String destinations, String tactics)
            throws IOException, InterruptedException {
        logger.debug(
                "Get distance and transit time from orgins \"{}\" to destinations\"{}\" by using tactics \"{}\" (default is \"11\").",
                origins, destinations, tactics);
        StringBuilder wholeURL = new StringBuilder(ROUTEMATRIX_API).append("&ak=").append(
                URLEncoder.encode(ak, CommonUtils.COMM_ENCODING));
        wholeURL.append("&origins=").append(URLEncoder.encode(origins, CommonUtils.COMM_ENCODING));
        wholeURL.append("&destinations=").append(
                URLEncoder.encode(destinations, CommonUtils.COMM_ENCODING));
        if (StringUtils.isNotBlank(tactics)) {
            wholeURL.append("&tactics=").append(
                    URLEncoder.encode(tactics, CommonUtils.COMM_ENCODING));
        }
        logger.debug("URL for getting Router Martix is {}.", wholeURL.toString());
        String jsonStr = null;
        while (true) {
            try {
                jsonStr = CommonUtils.getHTTPResponse(wholeURL.toString());
                break;
            } catch (IOException e) {
                logger.error("Get distance and transit time from Baidu API with error: ", e);
                logger.error("Waiting {} seconds for recovery...", "60");
                Thread.sleep(60000);
                logger.info("Program recover to running...");
            }
        }
        logger.debug("Information got from Baidu API is: {}", jsonStr);
        return jsonStr;
    }
}
