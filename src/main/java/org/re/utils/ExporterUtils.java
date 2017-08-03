/**
 * 
 */
package org.re.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.re.scrape.model.Issue;
import org.re.scrape.model.Product;


/**
 * @author doquocanh-macbook
 *
 */
public class ExporterUtils {

    public static boolean toExcel(String file, Product product) {
        // pre-condition check
        if (product == null) {
            return false; // Don't create excel file
        }
        
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

}
