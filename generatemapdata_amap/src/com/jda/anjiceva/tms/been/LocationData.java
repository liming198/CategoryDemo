/**
 * 
 */
package com.jda.anjiceva.tms.been;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.jda.anjiceva.tms.util.CommonUtils;

/**
 * @author j1015278
 *
 */
public class LocationData {

	public static final String POSTAL_CODE = "POSTAL_CODE";
	public static final String COUNTRY_ISO2 = "COUNTRY_ISO2";
	public static final String STATE_CODE = "STATE_CODE";
	public static final String CITY_CN = "CITY_CN";
	public static final String CITY_EN = "CITY_EN";
	public static final String ADDRESS = "ADDRESS";
	public static final String LOCATION_ID = "LOCATION_ID";
	public static final String ASSGINED_DC = "ASSGINED_DC";
	public static final String NEW_FLAG = "NEW_FLAG";
	public static final String LATITUDE = "LATITUDE";
	public static final String LONGITUDE = "LONGITUDE";
	public static final String PRECISE = "PRECISE";
	public static final String CONFIDENCE = "CONFIDENCE";

	public static final String[] LOCATION_HEADER = { POSTAL_CODE, COUNTRY_ISO2, STATE_CODE,
			CITY_CN, CITY_EN, ADDRESS, LOCATION_ID, NEW_FLAG, ASSGINED_DC, LATITUDE, LONGITUDE,
			PRECISE, CONFIDENCE };

	private String postalCode;
	private String countryISO2;
	private String stateCode;
	private String cityCN;
	private String cityEN;
	private String address;
	private String locId;

	//this attribute is applicable for location type Customer
	private String assginedDC;
	private int newFlag;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private int precise;
	private int confidence;
	public final static int NEW = 1;

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountryISO2() {
		return countryISO2;
	}

	public void setCountryISO2(String countryISO2) {
		this.countryISO2 = countryISO2;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getCityCN() {
		return cityCN;
	}

	public void setCityCN(String cityCN) {
		this.cityCN = cityCN;
	}

	public String getCityEN() {
		return cityEN;
	}

	public void setCityEN(String cityEN) {
		this.cityEN = cityEN;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLatitude() {
		return (this.latitude == null) ? "" : this.latitude.setScale(CommonUtils.LATLNG_SCALE,
				RoundingMode.HALF_UP).toString();
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return (this.longitude == null) ? "" : this.longitude.setScale(CommonUtils.LATLNG_SCALE,
				RoundingMode.HALF_UP).toString();
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public int getPrecise() {
		return precise;
	}

	public void setPrecise(int precise) {
		this.precise = precise;
	}

	public int getConfidence() {
		return confidence;
	}

	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}

	public String getLocId() {
		return locId;
	}

	public void setLocId(String locId) {
		this.locId = locId;
	}

	public String[] toArray() {
		return new String[] { this.postalCode, this.countryISO2, this.stateCode, this.cityCN,
				this.cityEN, this.address, this.locId, String.valueOf(this.newFlag),
				this.assginedDC, this.getLatitude(), this.getLongitude(),
				String.valueOf(this.precise), String.valueOf(this.confidence) };
	}

	public String[] toGEOValidLocationArray() {
		return new String[] { this.postalCode, this.stateCode, this.cityEN, this.getLatitude(),
				this.getLongitude(), this.countryISO2 };
	}

	public String getAssginedDC() {
		return assginedDC;
	}

	public void setAssginedDC(String assginedDC) {
		this.assginedDC = assginedDC;
	}

	public int getNewFlag() {
		return newFlag;
	}

	public void setNewFlag(int newFlag) {
		this.newFlag = newFlag;
	}

}
