/**
 * 
 */
package org.re.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author doquocanh-macbook
 *
 */
public class Utils {

	/**
	 * 
	 */
	public Utils() {
		// TODO Auto-generated constructor stub
	}
	
	public static double round(double value, int places) {
	    if (places < 0) 
	    	throw new IllegalArgumentException();
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static String replaceLastOccurrence(String src, String old, String replacement) {
		StringBuilder bd = new StringBuilder();
		int pos = src.lastIndexOf(old);
		if (pos == -1) {
			return src;
		}
		bd.append(src.substring(0, pos)).append(replacement).append(src.substring(pos+old.length(), src.length()));
		return bd.toString();
	}
}
