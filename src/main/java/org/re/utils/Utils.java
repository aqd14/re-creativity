/**
 * 
 */
package org.re.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.re.common.POS;

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
        bd.append(src.substring(0, pos)).append(replacement).append(src.substring(pos + old.length(), src.length()));
        return bd.toString();
    }
    
    /**
     * Convert from string to enum tag for clarity
     * @param tag   Tagged string to be converted
     * @return      Part-of-speech tagging
     */
    public static POS convertPOS(String tag) {
        POS pos = POS.UNASSIGNED;
        switch (tag) {
            case "NN":
                pos = POS.NOUN;
                break;
            case "VB":
                pos = POS.VERB_BASE_FORM;
                break;
            case "VBZ":
                pos = POS.VERB_PRESENT_TENSE;
                break;
            case "VBN":
                pos = POS.VERB_PAST_TENSE;
                break;
            default:
                pos = POS.UNASSIGNED;
                break;
        }
        return pos;
    }
}
