/**
 * 
 */
package com.jda.anjiceva.tms.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author j1015278
 * @param <T>
 *
 */
public class ApplicationContext {
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public <T> void setAttribute(String name, T value) {
		attributes.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T) attributes.get(name);
	}
}
