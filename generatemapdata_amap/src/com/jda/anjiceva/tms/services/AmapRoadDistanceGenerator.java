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
public class AmapRoadDistanceGenerator {

    private final static Logger logger = LoggerFactory.getLogger(AmapRoadDistanceGenerator.class);

    private final static String ROUTEMATRIX_API = "http://restapi.amap.com/v3/direction/driving?output=json";// CommonUtils.getValue("routermatrix_api");
    private static String AK = CommonUtils.nextAK();
    private static String AVOID_POLYGONS = CommonUtils.getValue("avoidpolygons");
    private static String STRATEGY = CommonUtils.getValue("strategy");
    static {
        if (StringUtils.isEmpty(STRATEGY)) {
            STRATEGY = "2";
        }

    }

    public static void generateRoadDistanceFile(List<RouterMatrix> routerMatrixes)
            throws IOException, InterruptedException {
        logger.info("Generating road distance file...");
        if (StringUtils.isEmpty(AK)) {
            logger.error("Absence of AK for getting information from Amap API!!!");
            throw new RuntimeException("Absence of AK for getting information from Amap API!!!");
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
        JSONArray results = getRouterMatrixResults(routerMatrix.getOriginCoordinates(),
                routerMatrix.getDestCoordinates());
        LocationPair[] locPairs = routerMatrix.getOnwardLocPairs();
        for (int i = 0; i < locPairs.length; i++) {
            LocationPair locPair = locPairs[i];
            if (locPair == null) {
                continue;
            }
            JSONObject resultObj = results.getJSONObject(i);
            if (resultObj.containsKey("info")) {
                logger.error(
                        "Get no distance and transit time for origin \"{}\" and destination \"{}\", message from Amap API: {}",
                        locPair.getOriginLoc().getLocId(), locPair.getDestLoc().getLocId(),
                        resultObj.getString("info"));
            } else {
                locPair.setOnwardDistance(resultObj.getDoubleValue("distance"));
                locPair.setOnwardDuration(resultObj.getDoubleValue("duration"));
            }
        }
        // Reverse direction
        results = getRouterMatrixResults(routerMatrix.getDestCoordinates(),
                routerMatrix.getOriginCoordinates());
        locPairs = routerMatrix.getReverseLocPairs();
        for (int i = 0; i < locPairs.length; i++) {
            LocationPair locPair = locPairs[i];
            if (locPair == null) {
                continue;
            }
            JSONObject resultObj = results.getJSONObject(i);
            if (resultObj.containsKey("info")) {
                logger.error(
                        "Get no distance and transit time for origin \"{}\" and destination \"{}\", message from Amap API: {}",
                        locPair.getDestLoc().getLocId(), locPair.getOriginLoc().getLocId(),
                        resultObj.getString("info"));
            } else {
                locPair.setReverseDistance(resultObj.getDoubleValue("distance"));
                locPair.setReverseDuration(resultObj.getDoubleValue("duration"));
            }
        }
    }

    public static void generateRoadDistanceFileByDirection(List<RouterMatrix> routerMatrixes)
            throws IOException, InterruptedException {
        logger.info("Generating road distance file...");
        if (StringUtils.isEmpty(AK)) {
            logger.error("Absence of AK for getting information from Amap API!!!");
            throw new RuntimeException("Absence of AK for getting information from Amap API!!!");
        }
        for (int i = 0; i < routerMatrixes.size(); i++) {
            RouterMatrix rm = routerMatrixes.get(i);
            try {
                generateRoadDistanceDataByDirection(rm);
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

    // for direction API, we can only get distance and transit for one pair location in a request
    public static void generateRoadDistanceDataByDirection(RouterMatrix routerMatrix)
            throws IOException, TMSException, InterruptedException {
        JSONObject pathObj = getDirectionPath(routerMatrix.getOriginCoordinates(),
                routerMatrix.getDestCoordinates());
        LocationPair locPair = routerMatrix.getOnwardLocPairs()[0];
        if (locPair != null) {
            if (pathObj.containsKey("info")) {
                logger.error(
                        "Get no distance and transit time for origin \"{}\" and destination \"{}\", message from Amap API: {}",
                        locPair.getOriginLoc().getLocId(), locPair.getDestLoc().getLocId(),
                        pathObj.getString("info"));
            } else {
                locPair.setOnwardDistance(pathObj.getDoubleValue("distance"));
                locPair.setOnwardDuration(pathObj.getDoubleValue("duration"));
            }
        }
        // Reverse direction
        pathObj = getDirectionPath(routerMatrix.getDestCoordinates(),
                routerMatrix.getOriginCoordinates());
        locPair = routerMatrix.getReverseLocPairs()[0];
        if (locPair != null) {
            if (pathObj.containsKey("info")) {
                logger.error(
                        "Get no distance and transit time for origin \"{}\" and destination \"{}\", message from Amap API: {}",
                        locPair.getDestLoc().getLocId(), locPair.getOriginLoc().getLocId(),
                        pathObj.getString("info"));
            } else {
                locPair.setReverseDistance(pathObj.getDoubleValue("distance"));
                locPair.setReverseDuration(pathObj.getDoubleValue("duration"));
            }
        }

    }

    private static JSONArray getRouterMatrixResults(String origins, String destination)
            throws IOException, TMSException, InterruptedException {
        JSONObject routerMatrixObj = null;
        loop: while (true) {
            try {
                routerMatrixObj = JSON.parseObject(getRouterMatrix(origins, destination));
                if (routerMatrixObj == null) {
                    logger.error(
                            "Got nothing from Amap API for locations \"{}\", and \"{}\", try in another \"{}\" seconds.",
                            origins, destination, "60");
                    Thread.sleep(60000);
                    logger.info("Program recover to running...");
                    continue;
                }
                String infoCode = routerMatrixObj.getString("infocode");
                switch (infoCode) {
                    case "10000":
                        // normal, continue to parse the location data
                        break loop;
                    case "10001":
                        // invalid key or key is expired

                    case "10002":
                        // unauthorized access the service or error spelled the api link
                    case "10003":
                        // daily query over limit
                    case "10009":
                    // user key platform not match
                    {
                        AK = CommonUtils.nextAK();
                        logger.warn("Change to another ak \"{}\"!", AK);
                        break;
                    }
                    case "10004":
                        // access too frequency
                    case "20003":
                    // unknow error
                    {
                        logger.error(
                                "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\", Error returned from Amap is \"{}\", waiting {} second recovery...",
                                origins, destination, routerMatrixObj.getString("info"), "120");
                        Thread.sleep(120000);
                        logger.info("Program recover to running...");
                    }
                    case "10005":
                        // invalid user IP
                    case "10006":
                        // invalid user domain
                    case "10007":
                        // invalid user signature
                    case "10008":
                        // invalid user scode
                    case "10010":
                        // ip query over limit
                    case "10011":
                        // not support https
                    case "20000":
                        // invalid parameters
                    case "20002":
                    // illegal request
                    {
                        logger.error(
                                "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\", Error returned from Amap is \"{}\"",
                                origins, destination, routerMatrixObj.getString("info"));
                        throw new RuntimeException(routerMatrixObj.getString("info"));
                    }
                    case "20001":
                        // missing mandatory parameters
                    case "20800":
                        // out of service
                    case "20801":
                        // no road nearby
                    case "20802":
                    // route failed
                    {
                        throw new TMSException(routerMatrixObj.getString("info"));
                    }
                    default:
                        logger.error(
                                "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\", Error returned from Amap is \"{}\", waiting {} second recovery...",
                                origins, destination, routerMatrixObj.getString("info"), "120");
                        Thread.sleep(120000);
                        logger.info("Program recover to running...");
                }
            } catch (UnsupportedEncodingException e) {
                throw e;
            } catch (JSONException e) {
                logger.error(
                        "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\"!!!",
                        origins, destination);
                logger.error("Parsing the data from Amap API with error: ", e);
                logger.error("Waiting {} seconds for recovery...", "60");
                Thread.sleep(60000);
                logger.info("Program recover to running...");
            }
        }
        return routerMatrixObj.getJSONArray("results");
    }

    private static JSONObject getDirectionPath(String origin, String destination)
            throws IOException, TMSException, InterruptedException {
        JSONObject directionObj = null;
        loop: while (true) {
            try {
                directionObj = JSON.parseObject(getDirectionData(origin, destination));
                if (directionObj == null) {
                    logger.error(
                            "Got nothing from Amap API for location \"{}\", and \"{}\", try in another \"{}\" seconds.",
                            origin, destination, "60");
                    Thread.sleep(60000);
                    logger.info("Program recover to running...");
                    continue;
                }
                String infoCode = directionObj.getString("infocode");
                switch (infoCode) {
                    case "10000":
                        // normal, continue to parse the location data
                        break loop;
                    case "10001":
                        // invalid key or key is expired

                    case "10002":
                        // unauthorized access the service or error spelled the api link
                    case "10003":
                        // daily query over limit
                    case "10009":
                    // user key platform not match
                    {
                        AK = CommonUtils.nextAK();
                        logger.warn("Change to another ak \"{}\"!", AK);
                        break;
                    }
                    case "10004":
                        // access too frequency
                    case "20003":
                    // unknow error
                    {
                        logger.error(
                                "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\", Error returned from Amap is \"{}\", waiting {} second recovery...",
                                origin, destination, directionObj.getString("info"), "120");
                        Thread.sleep(120000);
                        logger.info("Program recover to running...");
                        break;
                    }
                    case "10005":
                        // invalid user IP
                    case "10006":
                        // invalid user domain
                    case "10007":
                        // invalid user signature
                    case "10008":
                        // invalid user scode
                    case "10010":
                        // ip query over limit
                    case "10011":
                        // not support https
                    case "20000":
                        // invalid parameters
                    case "20002":
                    // illegal request
                    {
                        logger.error(
                                "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\", Error returned from Amap is \"{}\"",
                                origin, destination, directionObj.getString("info"));
                        throw new RuntimeException(directionObj.getString("info"));
                    }
                    case "20001":
                        // missing mandatory parameters
                    case "20800":
                        // out of service
                    case "20801":
                        // no road nearby
                    case "20802":
                    // route failed
                    {
                        throw new TMSException(directionObj.getString("info"));
                    }
                    default:
                        logger.error(
                                "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\", Error returned from Amap is \"{}\", waiting {} second recovery...",
                                origin, destination, directionObj.getString("info"), "120");
                        Thread.sleep(120000);
                        logger.info("Program recover to running...");
                        break;
                }
            } catch (UnsupportedEncodingException e) {
                throw e;
            } catch (JSONException e) {
                logger.error(
                        "Encounter an error to get distance and transit time for locations \"{}\", and \"{}\"!!!",
                        origin, destination);
                logger.error("Parsing the data from Amap API with error: ", e);
                logger.error("Waiting {} seconds for recovery...", "60");
                Thread.sleep(60000);
                logger.info("Program recover to running...");
            }
        }
        return directionObj.getJSONObject("route").getJSONArray("paths").getJSONObject(0);
    }

    // travel distance
    public static String getRouterMatrix(String origins, String destination) throws IOException,
            InterruptedException {
        logger.debug("Get distance and transit time from orgins \"{}\" to destination\"{}\".",
                origins, destination);
        StringBuilder wholeURL = new StringBuilder(ROUTEMATRIX_API).append("&key=").append(
                URLEncoder.encode(AK, CommonUtils.COMM_ENCODING));
        wholeURL.append("&origins=").append(URLEncoder.encode(origins, CommonUtils.COMM_ENCODING));
        wholeURL.append("&destination=").append(
                URLEncoder.encode(destination, CommonUtils.COMM_ENCODING));
        logger.debug("URL for getting Router Martix is {}.", wholeURL.toString());
        String jsonStr = null;
        while (true) {
            try {
                jsonStr = CommonUtils.getHTTPResponse(wholeURL.toString());
                break;
            } catch (IOException e) {
                logger.error("Get distance and transit time from Amap API with error: ", e);
                logger.error("Waiting {} seconds for recovery...", "60");
                Thread.sleep(60000);
                logger.info("Program recover to running...");
            }
        }
        logger.debug("Information got from Amap API is: {}", jsonStr);
        return jsonStr;
    }

    // direction
    public static String getDirectionData(String origin, String destination) throws IOException,
            InterruptedException {
        logger.debug("Get distance and transit time from orgin \"{}\" to destination\"{}\"",
                origin, destination);
        StringBuilder wholeURL = new StringBuilder(ROUTEMATRIX_API).append("&key=").append(
                URLEncoder.encode(AK, CommonUtils.COMM_ENCODING));
        wholeURL.append("&origin=").append(URLEncoder.encode(origin, CommonUtils.COMM_ENCODING));
        wholeURL.append("&destination=").append(
                URLEncoder.encode(destination, CommonUtils.COMM_ENCODING));
        if (StringUtils.isNotEmpty(AVOID_POLYGONS)) {
            wholeURL.append("&avoidpolygons=").append(
                    URLEncoder.encode(AVOID_POLYGONS, CommonUtils.COMM_ENCODING));
        }
        wholeURL.append("&strategy=")
                .append(URLEncoder.encode(STRATEGY, CommonUtils.COMM_ENCODING));
        logger.debug("URL for getting Router Martix is {}", wholeURL.toString());
       System.out.println(wholeURL.toString());
        String jsonStr = null;
        while (true) {
            try {
                jsonStr = CommonUtils.getHTTPResponse(wholeURL.toString());
                break;
            } catch (IOException e) {
                logger.error("Get distance and transit time from Amap API with error: ", e);
                logger.error("Waiting {} seconds for recovery...", "60");
                Thread.sleep(60000);
                logger.info("Program recover to running...");
            }
        }
        logger.debug("Information got from Amap API is: {}", jsonStr);
        return jsonStr;
    }

    public static void main(String[] args) throws IOException, TMSException, InterruptedException {
        JSONObject directionObj = JSON.parseObject(getDirectionData("121.475190,31.228833",
                "116.397846,39.900558"));
        JSONArray paths = directionObj.getJSONObject("route").getJSONArray("paths");
        System.out.println(paths.size());
        System.out.println(directionObj.getString("infocode"));
        String distance = paths.getJSONObject(0).getString("distance");
        String duration = paths.getJSONObject(0).getString("duration");
        System.out.println(distance);
        System.out.println(duration);
    }
}
