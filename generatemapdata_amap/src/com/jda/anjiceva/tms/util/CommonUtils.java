/**
 * 
 */
package com.jda.anjiceva.tms.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author j1015278
 *
 */
public class CommonUtils {

	private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);
	public static final String METHOD_GET = "GET";
	private final static Properties props = new Properties();
	public final static String AK_SEPERATOR = ",";
	public final static String COORDINATE_SEP = ",";
	public final static int HTTP_READTIMEOUT = 60000;
	public final static int HTTP_CONNTIMEOUT = 60000;
	public static final String HTTP_CHARSET = "UTF-8";
	public static final String COMM_ENCODING = "UTF-8";
	public static final Charset FILE_CHARSET = Charset.forName(COMM_ENCODING);
	public static final String FILE_APPENDIX = ".csv";
	private static String[] akList;
	private static int akIdx = -1;
	public static final int LATLNG_SCALE = 4;
	public static final String WORKING_DIR = CommonUtils.class.getResource("/").getPath();
	public static final File DE_DIR = new File(WORKING_DIR, "de");
	public static final File TMP_DIR = new File(WORKING_DIR, "temp");
	public static final File LOCDATA_DIR = new File(WORKING_DIR, "locations");

	static {
		InputStream is = null;
		try {
			is = CommonUtils.class.getClassLoader().getResourceAsStream("api.properties");
			props.load(is);
		} catch (IOException e) {
			logger.error("load file common.properties error!", e);
			System.exit(-1);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("", e);
					System.exit(-1);
				}
			}
		}
		akList = StringUtils.split(getValue("ak_list"), AK_SEPERATOR);

		if (!LOCDATA_DIR.exists()) {
			LOCDATA_DIR.mkdirs();
		}
		if (!DE_DIR.exists()) {
			DE_DIR.mkdirs();
		}

		if (!TMP_DIR.exists()) {
			TMP_DIR.mkdirs();
		}
	}

	public static String getValue(String key) {
		return props.getProperty(key);
	}

	public static String nextAK() {
		akIdx = (akList.length == akIdx + 1) ? 0 : akIdx + 1;
		return akList[akIdx];
	}

	public static String getHTTPResponse(String urlStr) throws IOException {
		String result = "";
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		try {
			URL apiURL = new URL(urlStr);
			conn = (HttpURLConnection) apiURL.openConnection();
			conn.setRequestMethod(METHOD_GET);
			conn.setReadTimeout(HTTP_READTIMEOUT);
			conn.setConnectTimeout(HTTP_CONNTIMEOUT);
			conn.connect();
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), HTTP_CHARSET));
			String line = null;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	/**
	 * 
	 * @param fileName
	 * @return encoding of the file
	 * @throws IOException 
	 * @throws Exception
	 */
	public static String codeString(String fileName) throws IOException {
		BufferedInputStream bis = null;
		String encoding = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(fileName));
			int p = (bis.read() << 8) + bis.read();

			switch (p) {
			case 0xefbb:
				encoding = "UTF-8";
				break;
			case 0xfffe:
				encoding = "Unicode";
				break;
			case 0xfeff:
				encoding = "UTF-16BE";
				break;
			case 0x5c75:
				encoding = "ANSI|ASCII";
				break;
			default:
				encoding = "GBK";
			}
		} finally {
			if (bis != null) {
				bis.close();
			}
		}

		return encoding;
	}

}
