/**
 * 
 */
package org.re.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.re.model.Requirement;
import org.re.scrape.BaseScraper;
import org.re.scrape.FirefoxScraper;
import org.re.scrape.MylynScraper;
import org.re.scrape.model.Issue;
import org.re.scrape.model.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * @author doquocanh-macbook
 *
 */
public class ExporterUtils {
    
    // File paths to read and write data
    public static final String FIREFOX_EXCEL = "Firefox" + File.separatorChar + "excel" + File.separatorChar + "issues.xls";
    public static final String FIREFOX_REQUIREMENTS_SERIALIZATION = "Firefox" + File.separatorChar + "serialization" + File.separatorChar + "requirements.ser";
    public static final String FIREFOX_PRODUCT_SERIALIZATION = "Firefox" + File.separatorChar + "serialization" + File.separatorChar + "product.ser";
    public static final String FIREFOX_GRAPH_INFO = "Firefox" + File.separatorChar + "graph" + File.separatorChar + "graph-info.txt";
    // List of all scraped requirements (issue description)
    public static final String FIREFOX_REQUIREMENTS = "Firefox" + File.separatorChar + "requirements" + File.separatorChar + "requirements_list.txt";
    // Serialized requirements file to be ready for loading without doing all work over again
    public static final String FIREFOX_SERIALIZED_REQUIREMENTS = "Firefox" + File.separatorChar + "serialization" + File.separatorChar + "requirements.ser";
    
    public static final String MYLYN_EXCEL = "Mylyn" + File.separatorChar + "excel" + File.separatorChar + "issues.xls";
    public static final String MYLYN_REQUIREMENTS_SERIALIZATION = "Mylyn" + File.separatorChar + "serialization" + File.separatorChar + "requirements.ser";
    public static final String MYLYN_PRODUCT_SERIALIZATION = "Mylyn" + File.separatorChar + "serialization" + File.separatorChar + "product.ser";
    public static final String MYLYN_GRAPH_INFO = "Mylyn" + File.separatorChar + "graph" + File.separatorChar + "graph-info.txt";
    // List of all scraped requirements (issue description)
    public static final String MYLYN_REQUIREMENTS = "Mylyn" + File.separatorChar + "requirements" + File.separatorChar + "requirements_list.txt";
    // Serialized requirements file to be ready for loading without doing all work over again
    public static final String MYLYN_SERIALIZED_REQUIREMENTS = "Mylyn" + File.separatorChar + "serialization" + File.separatorChar + "requirements.ser";
    
    public static boolean writeSerializedRequirements(File file, ObservableList<Requirement> requirements) {
        // Create file and folders if not already exist
        file.getParentFile().mkdirs();

        boolean success = false;
        try {
            FileOutputStream out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(new ArrayList<Requirement>(requirements));
            objOut.close();
            out.close();
            success = true;
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }
    
    public static boolean writeSerializedRequirements(String path, ObservableList<Requirement> requirements) {
        File file = new File(path);
        return writeSerializedRequirements(file, requirements);
    }
    
    @SuppressWarnings("unchecked")
    public static ObservableList<Requirement> readSerializedRequirements(File file) {
        ObservableList<Requirement> reqs = null;
        try {
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            ArrayList<Requirement> reqArray = (ArrayList<Requirement>) objIn.readObject();
            reqs = FXCollections.observableArrayList(reqArray);
            objIn.close();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            reqs = null;
            e.printStackTrace();
        }
        return reqs;
    }
    
    public static ObservableList<Requirement> readSerializedRequirements(String path) {
        File file = new File(path);
        return readSerializedRequirements(file);
    }
    
    /**
     * Serialize product
     * 
     * @param path
     * @param product
     */
    public static boolean serializeProduct(String path, Product product) {
        // Create file and folders if not already exist
        File file = new File(path);
        file.getParentFile().mkdirs();
        
        boolean isSuccessful = false;
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(file));
            objOut.writeObject(product);
            objOut.close();
            isSuccessful = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isSuccessful;
    }
    
    /**
     * Deserialize product
     * 
     * @param path
     * @return
     */
    public static Product deserializeProduct(String path) {
        try {
            ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(path));
            Product p = (Product) objIn.readObject();
            objIn.close();
            return p;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /*
     * Write scaped data to excel file
     */
    public static boolean toExcel(String path, Product product) {
        // pre-condition check
        if (product == null) {
            return false; // Don't create excel file
        }
        
        // Create file and folders if not already exist
        File file = new File(path);
        file.getParentFile().mkdirs();
        
        ArrayList<Issue> issues = product.getIssues();
        String system = product.getSystem().toString();
        
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(system);
        int rowNumber = 0;
        HSSFRow row = sheet.createRow(rowNumber++);
        
        // Create headers
        row.createCell(0).setCellValue("ID");
        row.createCell(1).setCellValue("Title");
        row.createCell(2).setCellValue("Product");
        row.createCell(3).setCellValue("Status");
        row.createCell(4).setCellValue("Importance");
        row.createCell(5).setCellValue("Assignee");
        row.createCell(6).setCellValue("Reporter");
        row.createCell(7).setCellValue("Created Date");
        row.createCell(8).setCellValue("Modified Date");
        row.createCell(9).setCellValue("Resolved Date");
        row.createCell(10).setCellValue("Comment Info");

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            // Write Issue details to each row in excel file
            // Each issue is associated with a row
            for (Issue issue : issues) {
                HSSFRow newRow = sheet.createRow(rowNumber++);
                newRow.createCell(0).setCellValue(issue.getId());                       // ID
                newRow.createCell(1).setCellValue(issue.getTitle());                    // Title
                newRow.createCell(2).setCellValue(system);                              // Software System
                newRow.createCell(3).setCellValue(issue.getStatus());                   // Status
                newRow.createCell(4).setCellValue(issue.getImportance());               // Importance 
                newRow.createCell(5).setCellValue(issue.getAssignee().getName());       // Assignee
                newRow.createCell(6).setCellValue(issue.getReporter().getName());       // Reporter
                newRow.createCell(7).setCellValue(issue.getReportedDateStr());  // Created Date
                newRow.createCell(8).setCellValue(issue.getModifiedDateStr()); // Modified Date
                newRow.createCell(9).setCellValue(issue.getResolvedDateStr()); // Resolved Date
                newRow.createCell(10).setCellValue(issue.commentStatsToString());       // Comment Info
            }
            wb.write(fileOut);
            wb.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean toGraphInfo(String path, Product product) {
        // Create file if not exist
        File file = new File(path);
        file.getParentFile().mkdirs();
        
        boolean isSuccessful = false;
        ArrayList<Issue> issues = product.getIssues();
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            for (Issue issue : issues) {
                writer.println(issue.toGraphEdges());
            }
            writer.close();
            isSuccessful = true;
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return isSuccessful;
    }
    
    /**
     * Write list of all scraped issue description as existing requirements to file
     * 
     * @param path
     * @param product
     * @return
     */
    public static boolean toRequirementList(String path, Product product) {
        // Create file if not exist
        File file = new File(path);
        file.getParentFile().mkdirs();
        
        boolean isSuccessful = false;
        ArrayList<Issue> issues = product.getIssues();
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            for (Issue issue : issues) {
                writer.println(issue.getTitle());
            }
            writer.close();
            isSuccessful = true;
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return isSuccessful;
    }
    
    /**
     * Export scraped data to:
     * 
     * <li>Serialized {@link Product}</li>
     * <li>Issue excel file</li>
     * <li>Graph info represents for communication among stakeholders</li>
     * 
     * @param scraper
     */
    public static void exportAll(BaseScraper scraper) {
        String excelFile = null, serializationFile = null, graphInfoFile = null, requirementsFile = null;
        if (scraper instanceof FirefoxScraper) {
            serializationFile = FIREFOX_PRODUCT_SERIALIZATION;
            excelFile = FIREFOX_EXCEL;
            graphInfoFile = FIREFOX_GRAPH_INFO;
            requirementsFile = FIREFOX_REQUIREMENTS;
        } else if (scraper instanceof MylynScraper) {
            serializationFile = MYLYN_PRODUCT_SERIALIZATION;
            excelFile = MYLYN_EXCEL;
            graphInfoFile = MYLYN_GRAPH_INFO;
            requirementsFile = MYLYN_REQUIREMENTS;
        } else {
            throw new RuntimeException("Invalid scraper type: " + scraper);
        }
        // Export data
        Product p = scraper.getProduct();
        serializeProduct(serializationFile, p);
        toExcel(excelFile, p);
        toGraphInfo(graphInfoFile, p);
        toRequirementList(requirementsFile, p);
    }
}
