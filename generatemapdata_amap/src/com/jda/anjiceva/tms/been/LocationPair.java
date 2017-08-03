/**
 * 
 */
package com.jda.anjiceva.tms.been;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 *
 */
public class LocationPair {

  private LocationData originLoc;
  private LocationData destLoc;
  /**
   * should be in meter
   */
  private double onwardDistance;
  /**
   * should be in second
   */
  private double onwardDuration;
  /**
   * should be in meter
   */
  private double reverseDistance;
  /**
   * should be in second
   */
  private double reverseDuration;
  /**
   * 0-Location 1
   * <p>
   * 1-Location 2
   * <p>
   * 2-Override from Location 1 to Location 2, default is true
   * <p>
   * 3-Distance from Location 1 to Location 2
   * <p>
   * 4-Transit time from Location 1 to Location 2
   * <p>
   * 5-Override from Location 2 to Location 1, default is true
   * <p>
   * 6-Distance from Location 2 to Location 1
   * <p>
   * 7-Transit time from Location 2 to Location 1
   */
  private String[] dttoArray;
  /**
   * 0-Origin Postal Code
   * <p>
   * 1-Origin State Code
   * <p>
   * 2-Origin City EN
   * <p>
   * 3-Destination Postal Code
   * <p>
   * 4-Destination State Code
   * <p>
   * 5-Destination City EN
   * <p>
   * 6-Onward Distance
   * <p>
   * 7-Onward Duration
   * <p>
   * 8-Origin Country Code
   * <p>
   * 9-Destination Country Code
   */
  private String[] onwardRoadDistanceArray;
  /**
   * 0-Destination Postal Code
   * <p>
   * 1-Destination State Code
   * <p>
   * 2-Destination City EN
   * <p>
   * 3-Origin Postal Code
   * <p>
   * 4-Origin State Code
   * <p>
   * 5-Origin City EN
   * <p>
   * 6-Reverse Distance
   * <p>
   * 7-Reverse Duration
   * <p>
   * 8-Destination Country Code
   * <p>
   * 9-Origin Country Code
   */
  private String[] reverseRoadDistanceArray;
  private final static double ROAD_DISTANCE_UNIT = 100.0;
  private final static double ROAD_DURATION_UNIT = 60.0;
  private final static double DTTO_DISTANCE_UNIT = 1000.0;
  private final static double DTTO_DURATION_UNIT = 3600.0;
  private final static double READABLE_DISTANCE_UNIT = 1000.0;
  private final static double READABLE_DURATION_UNIT = 24 * 60 * 60.0;
  private final static String MINIMAL_DURATION = "0.01";
  private final static String MINIMAL_DISTANCE = "0.1";
  /**
   * 0-using their own data (default)
   * <p>
   * 1-both using onward data
   * <p>
   * 2-both using reverse data
   * <p>
   * 3-both using maximum data between onward and reverse
   * <p>
   * 4-special for YUM, if average speed is more than 60km/h and transit time is more than 1 hour, then transit time
   * should be distance divide 60 km/h
   */
  private final static String DISTANCE_TYPE = StringUtils.defaultString(CommonUtils.getValue("distance_type"), "0");

  // override distance array is not used
  // private String[] onwardOverrideDistanceArray;
  // private String[] reverseOverrideDistanceArray;
  private String[] readableArray;
  private final static String OVERRIDE_VAL = "true";
  private boolean sameLocID;
  private boolean samePostalCode;
  private boolean sameLatLng;
  public final static String[] READABLE_ARRAY = new String[] {"LOCATION_CODE1", "POSTAL_CODE1", "CITY1", "ADDRESS1",
      "LATITUDE1", "LONGITUDE1", "LOCATION_CODE2", "POSTAL_CODE2", "CITY2", "ADDRESS2", "LATITUDE2", "LONGITUDE2",
      "ONWARD_DISTANCE(KM)", "ONWARD_TIME(DAY)", "REVERSE_DISTANCE(KM)", "REVERSE_TIME(DAY)"};
  public final static String[] DTTO_ARRAY = new String[] {"Location 1", "Location 2",
      "Override from Location 1 to Location 2", "Distance from Location 1 to Location 2",
      "Transit time from Location 1 to Location 2", "Override from Location 2 to Location 1",
      "Distance from Location 2 to Location 1", "Transit time from Location 2 to Location 1"};

  public LocationPair(LocationData originLoc, LocationData destLoc) {
    this.originLoc = originLoc;
    this.destLoc = destLoc;
    sameLocID = StringUtils.equals(originLoc.getLocId(), destLoc.getLocId());
    samePostalCode = StringUtils.equals(originLoc.getPostalCode(), destLoc.getPostalCode());
    sameLatLng = StringUtils.equals(originLoc.getLatitude(), destLoc.getLatitude())
        && StringUtils.equals(originLoc.getLongitude(), destLoc.getLongitude());
    // for DTTO, distance is in KM, keep 1 decimal; duration in hour, keep 2 decimal
    dttoArray = new String[8];
    dttoArray[0] = originLoc.getLocId();
    dttoArray[1] = destLoc.getLocId();
    dttoArray[2] = OVERRIDE_VAL;
    dttoArray[5] = OVERRIDE_VAL;

    // for distance engine data, distance is in KM*10, integer; duration is in minute, integer
    onwardRoadDistanceArray = new String[10];
    onwardRoadDistanceArray[0] = originLoc.getPostalCode();
    onwardRoadDistanceArray[1] = originLoc.getStateCode();
    onwardRoadDistanceArray[2] = originLoc.getCityEN();
    onwardRoadDistanceArray[3] = destLoc.getPostalCode();
    onwardRoadDistanceArray[4] = destLoc.getStateCode();
    onwardRoadDistanceArray[5] = destLoc.getCityEN();
    onwardRoadDistanceArray[8] = originLoc.getCountryISO2();
    onwardRoadDistanceArray[9] = destLoc.getCountryISO2();

    reverseRoadDistanceArray = new String[10];
    reverseRoadDistanceArray[0] = destLoc.getPostalCode();
    reverseRoadDistanceArray[1] = destLoc.getStateCode();
    reverseRoadDistanceArray[2] = destLoc.getCityEN();
    reverseRoadDistanceArray[3] = originLoc.getPostalCode();
    reverseRoadDistanceArray[4] = originLoc.getStateCode();
    reverseRoadDistanceArray[5] = originLoc.getCityEN();
    reverseRoadDistanceArray[8] = destLoc.getCountryISO2();
    reverseRoadDistanceArray[9] = originLoc.getCountryISO2();

    readableArray = new String[16];
    readableArray[0] = originLoc.getLocId();
    readableArray[1] = originLoc.getPostalCode();
    readableArray[2] = originLoc.getCityCN();
    readableArray[3] = originLoc.getAddress();
    readableArray[4] = originLoc.getLatitude();
    readableArray[5] = originLoc.getLongitude();
    readableArray[6] = destLoc.getLocId();
    readableArray[7] = destLoc.getPostalCode();
    readableArray[8] = destLoc.getCityCN();
    readableArray[9] = destLoc.getAddress();
    readableArray[10] = destLoc.getLatitude();
    readableArray[11] = destLoc.getLongitude();
    //@formatter:off
        /*
        onwardOverrideDistanceArray = new String[10];
        onwardOverrideDistanceArray[0] = originLoc.getPostalCode();
        onwardOverrideDistanceArray[1] = originLoc.getStateCode();
        onwardOverrideDistanceArray[2] = originLoc.getCityEN();
        onwardOverrideDistanceArray[3] = destLoc.getPostalCode();
        onwardOverrideDistanceArray[4] = destLoc.getStateCode();
        onwardOverrideDistanceArray[5] = destLoc.getCityEN();
        onwardOverrideDistanceArray[8] = originLoc.getCountryISO2();
        onwardOverrideDistanceArray[9] = destLoc.getCountryISO2();

        reverseOverrideDistanceArray = new String[10];
        reverseOverrideDistanceArray[0] = destLoc.getPostalCode();
        reverseOverrideDistanceArray[1] = destLoc.getStateCode();
        reverseOverrideDistanceArray[2] = destLoc.getCityEN();
        reverseOverrideDistanceArray[3] = originLoc.getPostalCode();
        reverseOverrideDistanceArray[4] = originLoc.getStateCode();
        reverseOverrideDistanceArray[5] = originLoc.getCityEN();
        reverseOverrideDistanceArray[8] = destLoc.getCountryISO2();
        reverseOverrideDistanceArray[9] = originLoc.getCountryISO2();
        */
        //@formatter:on

  }

  public LocationData getOriginLoc() {
    return originLoc;
  }

  public LocationData getDestLoc() {
    return destLoc;
  }

  public double getOnwardDistance() {
    return onwardDistance;
  }

  public void setOnwardDistance(double onwardDistance) {
    this.onwardDistance = onwardDistance;
    String distanceStr =
        new BigDecimal(onwardDistance / DTTO_DISTANCE_UNIT).setScale(1, RoundingMode.HALF_UP).toString();
    dttoArray[3] = NumberUtils.toDouble(distanceStr) == 0 ? MINIMAL_DISTANCE : distanceStr;
    onwardRoadDistanceArray[6] = String.valueOf(Math.round(onwardDistance / ROAD_DISTANCE_UNIT));
    // onwardOverrideDistanceArray[6] = String.valueOf(Math.round(onwardDistance / 1000));
    readableArray[12] = String.valueOf(onwardDistance / READABLE_DISTANCE_UNIT);
  }

  public double getOnwardDuration() {
    return onwardDuration;
  }

  public void setOnwardDuration(double onwardDuration) {
    this.onwardDuration = onwardDuration;
    String durationStr =
        new BigDecimal(onwardDuration / DTTO_DURATION_UNIT).setScale(2, RoundingMode.HALF_UP).toString();
    dttoArray[4] = NumberUtils.toDouble(durationStr) == 0 ? MINIMAL_DURATION : durationStr;
    onwardRoadDistanceArray[7] = String.valueOf(Math.round(onwardDuration / ROAD_DURATION_UNIT));
    // onwardOverrideDistanceArray[7] = String.valueOf(Math.round(onwardDuration / 60));
    readableArray[13] = String.valueOf(onwardDuration / READABLE_DURATION_UNIT);
  }

  public double getReverseDistance() {
    return reverseDistance;
  }

  public void setReverseDistance(double reverseDistance) {
    this.reverseDistance = reverseDistance;
    String distanceStr =
        new BigDecimal(reverseDistance / DTTO_DISTANCE_UNIT).setScale(1, RoundingMode.HALF_UP).toString();
    dttoArray[6] = NumberUtils.toDouble(distanceStr) == 0 ? MINIMAL_DISTANCE : distanceStr;
    reverseRoadDistanceArray[6] = String.valueOf(Math.round(reverseDistance / ROAD_DISTANCE_UNIT));
    // reverseOverrideDistanceArray[6] = String.valueOf(Math.round(reverseDistance / 1000));
    readableArray[14] = String.valueOf(reverseDistance / READABLE_DISTANCE_UNIT);
  }

  public double getReverseDuration() {
    return reverseDuration;
  }

  public void setReverseDuration(double reverseDuration) {
    this.reverseDuration = reverseDuration;
    String durationStr =
        new BigDecimal(reverseDuration / DTTO_DURATION_UNIT).setScale(2, RoundingMode.HALF_UP).toString();
    dttoArray[7] = NumberUtils.toDouble(durationStr) == 0 ? MINIMAL_DURATION : durationStr;
    reverseRoadDistanceArray[7] = String.valueOf(Math.round(reverseDuration / ROAD_DURATION_UNIT));
    // reverseOverrideDistanceArray[7] = String.valueOf(Math.round(reverseDuration / 60));
    readableArray[15] = String.valueOf(reverseDuration / READABLE_DURATION_UNIT);
  }

  @Override
  public String toString() {
    return this.originLoc.getLocId() + "|" + this.destLoc.getLocId();
  }

  public String[] getDttoArray() {
    // special for anji ceva
    //@formatter:off
        /*
        double distance = (this.onwardDistance + this.reverseDistance) / (2 * DTTO_DISTANCE_UNIT);
        double duration = distance / 60;
        dttoArray[3] = new BigDecimal(distance).setScale(1, RoundingMode.HALF_UP).toString();
        dttoArray[6] = dttoArray[3];
        dttoArray[4] = new BigDecimal(duration).setScale(2,
                RoundingMode.HALF_UP).toString();
        dttoArray[7] = dttoArray[4];
        */
        //@formatter:on

    switch (DISTANCE_TYPE) {
      case "1": {
        dttoArray[6] = dttoArray[3];
        dttoArray[7] = dttoArray[4];
        break;
      }
      case "2": {
        dttoArray[3] = dttoArray[6];
        dttoArray[4] = dttoArray[7];
        break;
      }
      case "3": {
        String distance = (this.onwardDistance >= this.reverseDistance) ? dttoArray[3] : dttoArray[6];
        String duration = (this.onwardDuration >= this.reverseDuration) ? dttoArray[4] : dttoArray[7];
        dttoArray[3] = distance;
        dttoArray[4] = duration;
        dttoArray[6] = distance;
        dttoArray[7] = duration;
        break;
      }
      case "4": {
        if (Math.round(this.onwardDuration / 36.0) / 100.0 >= 1
            && (this.onwardDistance / 1000.0) / (this.onwardDuration / 3600.0) > 60) {
          dttoArray[4] = String
              .valueOf(Math.round(((this.onwardDistance / (60000.0 / 3600.0)) / DTTO_DURATION_UNIT) * 100) / 100.0);
        }
        if (Math.round(this.reverseDuration / 36.0) / 100.0 >= 1
            && (this.reverseDistance / 1000.0) / (this.reverseDuration / 3600.0) > 60) {
          dttoArray[7] = String
              .valueOf(Math.round(((this.reverseDistance / (60000.0 / 3600.0)) / DTTO_DURATION_UNIT) * 100) / 100.0);
        }
        break;
      }
      default:
    }

    return dttoArray;
  }

  public String[] getOnwardRoadDistanceArray() {
    switch (DISTANCE_TYPE) {
      case "2": {
        onwardRoadDistanceArray[6] = reverseRoadDistanceArray[6];
        onwardRoadDistanceArray[7] = reverseRoadDistanceArray[7];
        break;
      }
      case "3": {
        onwardRoadDistanceArray[6] =
            (this.onwardDistance >= this.reverseDistance) ? onwardRoadDistanceArray[6] : reverseRoadDistanceArray[6];
        onwardRoadDistanceArray[7] =
            (this.onwardDuration >= this.reverseDuration) ? onwardRoadDistanceArray[7] : reverseRoadDistanceArray[7];
        break;
      }
      case "4": {
    	  
        if (this.onwardDuration >= 3600 && (this.onwardDistance / 1000.0) / (this.onwardDuration / 3600.0) > 60) {
          onwardRoadDistanceArray[7] =
              String.valueOf(Math.round((this.onwardDistance / (60000.0 / 3600.0)) / ROAD_DURATION_UNIT));
        }
        break;
      }
      case "1":
      default:
    }
    return onwardRoadDistanceArray;

  }

  public String[] getReverseRoadDistanceArray() {
    switch (DISTANCE_TYPE) {
      case "1": {
        reverseRoadDistanceArray[6] = onwardRoadDistanceArray[6];
        reverseRoadDistanceArray[7] = onwardRoadDistanceArray[7];
        break;
      }
      case "3": {
        reverseRoadDistanceArray[6] =
            (this.onwardDistance >= this.reverseDistance) ? onwardRoadDistanceArray[6] : reverseRoadDistanceArray[6];
        reverseRoadDistanceArray[7] =
            (this.onwardDuration >= this.reverseDuration) ? onwardRoadDistanceArray[7] : reverseRoadDistanceArray[7];
        break;
      }
      case "4": {
    	
        if (this.reverseDuration >= 3600 && (this.reverseDistance / 1000.0) / (this.reverseDuration / 3600.0) > 60) {
          reverseRoadDistanceArray[7] =
              String.valueOf(Math.round((this.reverseDistance / (60000.0 / 3600.0)) / ROAD_DURATION_UNIT));
        }
        break;
      }
      case "2":
      default:
    }
    return reverseRoadDistanceArray;
  }

  /*
   * public String[] getOnwardOverrideDistanceArray() { return onwardOverrideDistanceArray; }
   * 
   * public String[] getReverseOverrideDistanceArray() { return reverseOverrideDistanceArray; }
   */

  public boolean isSameLocID() {
    return sameLocID;
  }

  public boolean isSamePostalCode() {
    return samePostalCode;
  }

  public String[] getReadableArray() {
    return readableArray;
  }

  public boolean isSameLatLng() {
    return sameLatLng;
  }

  public boolean isReversePair(LocationPair lp) {
    if (lp == null) {
      return false;
    } else if (lp.getOriginLoc() == null || lp.getDestLoc() == null || this.originLoc == null || this.destLoc == null) {
      return false;
    } else if (lp.getOriginLoc().equals(this.destLoc) && lp.getDestLoc().equals(this.getOriginLoc())) {
      return true;
    }
    return false;
  }
}
