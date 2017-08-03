/**
 * 
 */
package com.jda.anjiceva.tms.been;

import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 *
 */
public class RouterMatrix {

    private boolean locPairsInit = false;
    public final static int GROUP_SIZE = 1;
    public final static String LOC_SEP = "|";
    private LocationData[] originLocs = new LocationData[GROUP_SIZE];
    private int originSize = 0;
    private StringBuilder originCoordinates = new StringBuilder();
    private StringBuilder originLocIds = new StringBuilder();

    private LocationData[] destLocs = new LocationData[GROUP_SIZE];
    private int destSize = 0;
    private StringBuilder destCoordinates = new StringBuilder();
    private StringBuilder destLocIds = new StringBuilder();

    private LocationPair[] onwardLocPairs;
    private LocationPair[] reverseLocPairs;

    public void addOriginLoc(LocationData origin) {
        if (origin == null) {
            throw new NullPointerException("Origin location can not be null!");
        }
        if (originSize != 0) {
            originCoordinates.append(LOC_SEP);
            originLocIds.append(LOC_SEP);
        }
        originCoordinates.append(origin.getLatitude()).append(CommonUtils.COORDINATE_SEP)
                .append(origin.getLongitude());
        originLocIds.append(origin.getLocId());
        originLocs[originSize++] = origin;
        locPairsInit = false;
    }

    public void addDestLoc(LocationData dest) {
        if (dest == null) {
            throw new NullPointerException("Destination location can not be null!");
        }
        if (destSize != 0) {
            destCoordinates.append(LOC_SEP);
            destLocIds.append(LOC_SEP);
        }
        destCoordinates.append(dest.getLatitude()).append(CommonUtils.COORDINATE_SEP)
                .append(dest.getLongitude());
        destLocIds.append(dest.getLocId());
        destLocs[destSize++] = dest;
        locPairsInit = false;
    }

    public String getOriginCoordinates() {
        return originCoordinates.toString();
    }

    public String getDestCoordinates() {
        return destCoordinates.toString();
    }

    private void initLocPairs() {
        int size = originSize * destSize;
        onwardLocPairs = new LocationPair[size];
        reverseLocPairs = new LocationPair[size];
        int t = 0;
        for (int i = 0; i < originSize; i++) {
            for (int j = 0; j < destSize; j++) {
                LocationPair locPair = new LocationPair(originLocs[i], destLocs[j]);
                onwardLocPairs[t++] = locPair;
                reverseLocPairs[j * originSize + i] = locPair;
            }
        }
        // remove the duplicated records
        for (int i = 0; i < size; i++) {
            LocationPair onwardLPi = onwardLocPairs[i];
            LocationPair reverseLPi = reverseLocPairs[i];
            if (onwardLPi == null && reverseLPi == null) {
                continue;
            }
            for (int j = i + 1; j < size; j++) {
                LocationPair onwardLPj = onwardLocPairs[j];
                LocationPair reverseLPj = reverseLocPairs[j];
                if (onwardLPi != null && onwardLPi.isReversePair(onwardLPj)) {
                    onwardLocPairs[j] = null;
                }
                if (reverseLPi != null && reverseLPi.isReversePair(reverseLPj)) {
                    reverseLocPairs[i] = null;
                }
            }
        }
        locPairsInit = true;
    }

    public LocationPair[] getOnwardLocPairs() {
        if (!locPairsInit) {
            initLocPairs();
        }
        return onwardLocPairs;
    }

    public LocationPair[] getReverseLocPairs() {
        if (!locPairsInit) {
            initLocPairs();
        }
        return reverseLocPairs;
    }

    public String[] toRouterMatrixesArray() {
        return new String[] { originLocIds.toString(), destLocIds.toString() };
    }

    public int getDestSize() {
        return destSize;
    }

}
