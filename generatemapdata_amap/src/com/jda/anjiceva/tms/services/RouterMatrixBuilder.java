/**
 * 
 */
package com.jda.anjiceva.tms.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.jda.anjiceva.tms.been.LocationData;
import com.jda.anjiceva.tms.been.LocationPair;
import com.jda.anjiceva.tms.been.RouterMatrix;
import com.jda.anjiceva.tms.exception.TMSException;
import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 *
 */
public class RouterMatrixBuilder {

    private final static Logger logger = LoggerFactory.getLogger(RouterMatrixBuilder.class);
    private final static File RM_TEMP_FILE = new File(CommonUtils.TMP_DIR, "rm.tmp");
    private final static File ROAD_DISTANCE_FILE = new File(CommonUtils.DE_DIR,
            "roaddistinsameorigdestcntry.txt");
    private final static File OVERRIDE_DISTANCE_FILE = new File(CommonUtils.DE_DIR,
            "overridedistancesraw.txt");
    private final static File READABLE_DISTANCE_FILE = new File(CommonUtils.DE_DIR,
            "readabledistance.csv");
    private final static File DTTO_FILE = new File(CommonUtils.DE_DIR, "dtto.csv");

    public static List<RouterMatrix> generateRouterMatrixes(List<LocationData> locs)
            throws IOException, InterruptedException, TMSException {
        logger.info("Generating router matrix list for location data...");
        List<LocationData> oldLocs = new ArrayList<LocationData>();
        List<LocationData> newLocs = new ArrayList<LocationData>();
        // split locations into different type, old/new, DC/Customer, for building router matrix separately
        for (LocationData ld : locs) {
            if (ld.getNewFlag() == LocationData.NEW) {
                newLocs.add(ld);
                ld.setNewFlag(0);
            } else {
                oldLocs.add(ld);
            }
        }
        boolean keepTmpData = false;
        List<RouterMatrix> routerMatrixes = new ArrayList<RouterMatrix>();

        if (!newLocs.isEmpty()) {
            // new Customer to new Customer
            routerMatrixes.addAll(buildRouterMatrixes(newLocs, null));

            if (!oldLocs.isEmpty()) {
                // new Customer to old Customer
                keepTmpData = true;
                routerMatrixes.addAll(buildRouterMatrixes(newLocs, oldLocs));
            }
        }
        if (isRMTempFileExist() && (keepTmpData || routerMatrixes.isEmpty())) {
            // load router matrixes data in rm.tmp file
            routerMatrixes.addAll(loadRouterMatrixes(locs));
        }
        // write the router matrixes to temporary file for running continuously
        RouterMatrixBuilder.writeToRMTempFile(routerMatrixes, false);

        // persist location data, override the previous one, for update the new flag
        LocationDataBuilder.writeDownLocationData(locs);

        return routerMatrixes;
    }

    /**
     * Loading router matrix record from temporary file, for recovering program to continue getting data
     * 
     * @param fileName
     *            The temporary file name, only each location id included in this file.
     * @return router matrix list
     * @throws IOException
     * @throws TMSException
     */
    public static List<RouterMatrix> loadRouterMatrixes(List<LocationData> locs)
            throws IOException, TMSException {
        logger.info("Loading location data from file \"{}\"...", RM_TEMP_FILE.getName());
        List<RouterMatrix> routerMatrixes = new ArrayList<RouterMatrix>();
        CsvReader cr = null;
        try {
            cr = new CsvReader(new FileInputStream(RM_TEMP_FILE), ',', CommonUtils.FILE_CHARSET);
            while (cr.readRecord()) {
                String[] originLocIds = StringUtils.split(cr.get(0));
                RouterMatrix rm = new RouterMatrix();
                for (String locIds : originLocIds) {
                    for (String locId : StringUtils.split(locIds, RouterMatrix.LOC_SEP)) {
                        rm.addOriginLoc(getLocationByID(locId, locs));
                    }
                }
                String[] destLocIds = StringUtils.split(cr.get(1));
                for (String locIds : destLocIds) {
                    for (String locId : StringUtils.split(locIds, RouterMatrix.LOC_SEP)) {
                        rm.addDestLoc(getLocationByID(locId, locs));
                    }
                }
                routerMatrixes.add(rm);
            }
        } finally {
            cr.close();
        }
        return routerMatrixes;
    }

    private static LocationData getLocationByID(String locId, List<LocationData> locs)
            throws TMSException {
        for (LocationData ld : locs) {
            if (StringUtils.equals(locId, ld.getLocId()))
                return ld;
        }
        throw new TMSException("Unable to get the location data for location id \"" + locId
                + "\"!!!");
    }

    public static void writeToRMTempFile(List<RouterMatrix> routerMatrixes, boolean append)
            throws IOException {
        logger.info("Recording router matrixes into the temporary file...");
        CsvWriter cw = null;
        try {
            cw = new CsvWriter(new FileOutputStream(RM_TEMP_FILE, append), ',',
                    CommonUtils.FILE_CHARSET);
            for (RouterMatrix rm : routerMatrixes) {
                cw.writeRecord(rm.toRouterMatrixesArray());
            }
            cw.flush();
        } finally {
            cw.close();
        }
    }

    public static void writeDownRoadDistance(List<RouterMatrix> routerMatrixes) throws IOException {
        logger.info("Generating the roaddistance file...");
        CsvWriter cw = null;
        try {
            cw = new CsvWriter(new FileOutputStream(ROAD_DISTANCE_FILE, true), ',',
                    CommonUtils.FILE_CHARSET);
            for (RouterMatrix rm : routerMatrixes) {
                LocationPair[] lps = rm.getOnwardLocPairs();
                for (LocationPair lp : lps) {
                    if (lp == null || lp.isSamePostalCode() || lp.isSameLatLng() || lp.isSameLocID()) {
                    }
                    cw.writeRecord(lp.getOnwardRoadDistanceArray());
                    cw.writeRecord(lp.getReverseRoadDistanceArray());
                }

            }
            cw.flush();
        } finally {
            cw.close();
        }
    }

    public static void writeDownReadableDistance(List<RouterMatrix> routerMatrixes)
            throws IOException {
        logger.info("Generating the readable distance file...");
        CsvWriter cw = null;
        try {
            cw = new CsvWriter(new FileOutputStream(READABLE_DISTANCE_FILE, true), ',',
                    CommonUtils.FILE_CHARSET);
            if (!READABLE_DISTANCE_FILE.exists()) {
                cw.writeRecord(LocationPair.READABLE_ARRAY);
            }
            for (RouterMatrix rm : routerMatrixes) {
                LocationPair[] lps = rm.getOnwardLocPairs();
                for (LocationPair lp : lps) {
                    if (lp == null || lp.isSameLocID()) {
                        continue;
                    }
                    cw.writeRecord(lp.getReadableArray());
                }
            }
            cw.flush();
        } finally {
            cw.close();
        }
    }

    public static void writeDownDTTOFile(List<RouterMatrix> routerMatrixes) throws IOException {
        logger.info("Generating the dtto file...");
        CsvWriter cw = null;
        try {
            cw = new CsvWriter(new FileOutputStream(DTTO_FILE, true), ',', CommonUtils.FILE_CHARSET);
            if (!DTTO_FILE.exists()) {
                cw.writeRecord(LocationPair.DTTO_ARRAY);
            }
            for (RouterMatrix rm : routerMatrixes) {
                LocationPair[] lps = rm.getOnwardLocPairs();
                for (LocationPair lp : lps) {
                    if (lp == null || lp.isSameLocID()) {
                        continue;
                    }
                    cw.writeRecord(lp.getDttoArray());
                }
            }
            cw.flush();
        } finally {
            cw.close();
        }
    }

    public static void createOverrideDistanceFile() throws IOException {
        logger.info("Copy distance to override distance file...");
        if (!ROAD_DISTANCE_FILE.exists()) {
            logger.warn("Road distance file \"{}\" does not exists!", ROAD_DISTANCE_FILE.getPath());
            return;
        }
        CsvReader cr = null;
        CsvWriter cw = null;
        try {
            cr = new CsvReader(new FileInputStream(ROAD_DISTANCE_FILE), ',',
                    CommonUtils.FILE_CHARSET);
            cw = new CsvWriter(new FileOutputStream(OVERRIDE_DISTANCE_FILE, false), ',',
                    CommonUtils.FILE_CHARSET);
            while (cr.readRecord()) {
                for (int i = 0; i < cr.getColumnCount(); i++) {
                    cw.write(cr.get(i));
                }
                cw.endRecord();
            }
            cw.flush();
        } finally {
            cr.close();
            cw.close();
        }
    }

    public static boolean isRMTempFileExist() {
        return RM_TEMP_FILE.exists();
    }

    public static boolean removeRMTempFile() {
        return RM_TEMP_FILE.delete();
    }

    // write for temporary use
    @Deprecated
    public static void regenerateReadableFile() throws IOException, TMSException {
        CsvReader cr = new CsvReader(new FileInputStream(READABLE_DISTANCE_FILE), ',',
                CommonUtils.FILE_CHARSET);
        CsvWriter cw = new CsvWriter(
                "D:\\material\\project\\ab-inbev\\1027\\readable_distance.csv", ',',
                CommonUtils.FILE_CHARSET);
        List<LocationData> locs = LocationDataBuilder.loadLocationData();
        cr.readHeaders();
        cw.writeRecord(LocationPair.READABLE_ARRAY);
        while (cr.readRecord()) {
            String originLocId = cr.get(0);
            LocationData originLoc = getLocationByID(originLocId, locs);
            String destLocId = cr.get(6);
            LocationData destLoc = getLocationByID(destLocId, locs);
            cw.writeRecord(new String[] { originLocId, originLoc.getPostalCode(),
                    originLoc.getCityCN(), originLoc.getAddress(), originLoc.getLatitude(),
                    originLoc.getLongitude(), destLocId, destLoc.getPostalCode(),
                    destLoc.getCityCN(), destLoc.getAddress(), destLoc.getLatitude(),
                    destLoc.getLongitude(), cr.get(12), cr.get(13), cr.get(14), cr.get(15) });
        }
        cw.flush();
        cr.close();
        cw.close();
    }

    private static List<RouterMatrix> buildRouterMatrixes(List<LocationData> originLocs,
            List<LocationData> destLocs) {
        logger.info("Building router matrixes for locations...");
        List<RouterMatrix> routerMatrixes = new ArrayList<RouterMatrix>();
        boolean sameLoc = false;
        if (destLocs == null) {
            destLocs = originLocs;
            sameLoc = true;
        } else if (originLocs.equals(destLocs)) {
            sameLoc = true;
        }
        int originLocSize = originLocs.size();
        int destLocSize = destLocs.size();

        int originSize = RouterMatrix.GROUP_SIZE;
        for (int i = 0; i < originLocSize; i += originSize) {
            // calculate the group size in case the ArrayIndexOutOfBoundsException error
            if (originLocSize - i < originSize) {
                originSize = originLocSize - i;
            }
            int j = sameLoc ? (i + 1) : 0;
            int destSize = RouterMatrix.GROUP_SIZE;
            for (; j < destLocSize; j += destSize) {
                RouterMatrix rm = new RouterMatrix();
                for (int t = i; t < i + originSize; t++) {
                    rm.addOriginLoc(originLocs.get(t));
                }
                if (destLocSize - j < destSize) {
                    destSize = destLocSize - j;
                }
                for (int t = j; t < j + destSize; t++) {
                    rm.addDestLoc(destLocs.get(t));
                }
                routerMatrixes.add(rm);
            }
        }
        return routerMatrixes;
    }

}
