/**
 * 
 */
package org.re.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Scanner;

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
     * Read file's content and store into a string
     * 
     * @param path File path
     * @return  Content of file as a string
     * @throws IOException
     */
    public static String readFile(File file) {
        Scanner sc = null;
        StringBuilder bd = new StringBuilder();
        try (FileInputStream inputStream = new FileInputStream(file)){
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                bd.append(line).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bd.toString();
    }
    
    public static String readFile(String path) {
        File file = new File(path);
        return readFile(file);
    }
    
    @SuppressWarnings("rawtypes")
    public static File loadResourcesAsFile(Class cl, String resource) {
        File file = null;
        URL res = cl.getResource(resource);
        if (res.toString().startsWith("jar:")) {
            try {
                InputStream input = cl.getResourceAsStream(resource);
                file = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(res.getFile());
        }

        if (file != null && !file.exists()) {
            throw new RuntimeException("Error: File " + file + " not found!");
        }
        return file;
    }
    
    public static File makeTempFile(String content) {
        File file = null;
        try {
            file = File.createTempFile("tempfile", ".tmp");
            PrintWriter out = new PrintWriter(file);
            out.write(content);
            file.deleteOnExit();
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
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
            case "NNS":
                pos = POS.NOUN_PLURAL;
                break;
            case "NNP":
                pos = POS.PROPER_NOUN_SINGULAR;
                break;
            case "NNPS":
                pos = POS.PROPER_NOUN_PLURAL;
                break;
            case "VB":
                pos = POS.VERB_BASE_FORM;
                break;
            case "VBN":
                pos = POS.VERB_PAST_PARTICIPLE;
                break;
            case "VBD":
                pos = POS.VERB_PAST_TENSE;
                break;
            case "VBG":
                pos = POS.VERB_GERUND;
                break;
            case "VBP":
                pos = POS.VERB_NON_3RD_PERSON;
                break;
            case "VBZ":
                pos = POS.VERB_3RD_PERSON;
                break;
            case "JJ":
                pos = POS.ADJECTIVE;
                break;
            case "RB":
                pos = POS.ADVERD;
                break;
            case "CC":
                pos = POS.CONJUNCTION;
                break;
            default:
                pos = POS.UNASSIGNED;
                break;
        }
        return pos;
    }
    
    public static boolean isNoun(POS pos) {
        return pos.equals(POS.NOUN) 
                || pos.equals(POS.NOUN_PLURAL) 
                || pos.equals(POS.PROPER_NOUN_SINGULAR)
                || pos.equals(POS.PROPER_NOUN_PLURAL);
    }
    
    public static boolean isVerb(POS pos) {
        return pos.equals(POS.VERB_BASE_FORM) || pos.equals(POS.VERB_3RD_PERSON) 
                || pos.equals(POS.VERB_GERUND) || pos.equals(POS.VERB_NON_3RD_PERSON) 
                || pos.equals(POS.VERB_PAST_PARTICIPLE) || pos.equals(POS.VERB_PAST_TENSE);
    }
}
