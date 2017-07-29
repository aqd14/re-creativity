/**
 * 
 */
package org.re.scrape;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.re.common.SoftwareSystem;
import org.re.scrape.model.Assignee;
import org.re.scrape.model.Comment;
import org.re.scrape.model.Commenter;
import org.re.scrape.model.Issue;
import org.re.scrape.model.Reporter;

/**
 * @author doquocanh-macbook
 *
 */
public class MylynScraper extends BaseScraper {
    final static Logger logger = Logger.getLogger(MylynScraper.class);
    // Attribute
    private static final String MYLYN_URL_PREFIX = "https://bugs.eclipse.org/bugs/show_bug.cgi?id=";
    // Default constructor
    public MylynScraper() {
        super(SoftwareSystem.MYLYN, MYLYN_URL_PREFIX, new SimpleDateFormat("yyyy-MM-dd HH:mm"));
    }

    /* 
     * @see org.re.scrape.BaseScraper#scrape(java.net.URL)
     */
    @Override
    public void scrape(String url) {
        try {
            // Scrape basic information
            Document doc = Jsoup.connect(url).get();
            // Won't scrape duplicate issue
            String status = doc.select("#static_bug_status").text();
            int id = Integer.parseInt(url.replace(MYLYN_URL_PREFIX, ""));
            logger.info("------- START SCRAPING ISSUE " + id + " ----------\n");
            if (isInvalidStatus(status)) {
                logger.debug("Issue " + id + " is invalid: " + status + "\n");
                logger.info("------- END SCRAPING ISSUE " + id + " ----------\n");
                return;
            }
            String title = doc.select("#short_desc_nonedit_display").text();
            Elements tableData = doc.select("#bz_show_bug_column_1").select("tr");
            String importance = tableData.get(8).select("td").text();
            
            // Stakeholders
            String assigner = tableData.get(10).select(".fn").text();
            String reporter = doc.select("#bz_show_bug_column_2 .fn").text();
            
            // Date information
            String createdDateStr = doc.select("#bz_show_bug_column_2").select("tr").get(0).select("td").text();
            // Remove unnecessary text: 2008-11-26 15:42 EST by David Green
            int removeIx = createdDateStr.indexOf("EST") - 1;
            createdDateStr = createdDateStr.substring(0, removeIx);
            Date createdDate = df.parse(createdDateStr);
            
            String modifiedDateStr = doc.select("#bz_show_bug_column_2").select("tr").get(1).select("td").text();
            removeIx = modifiedDateStr.indexOf("EST") - 1;
            modifiedDateStr = modifiedDateStr.substring(0, removeIx);
            Date modifiedDate = df.parse(modifiedDateStr);
            
            // Couldn't find resolved data on the website.
            Date resolvedDate = modifiedDate; //df.parse(resolvedDateStr);
            
            // Create new Issue object with scraped data
            Issue issue = new Issue(id, title, status, importance, new Assignee(assigner), new Reporter(reporter),
                    createdDate, modifiedDate, resolvedDate);
            product.getIssues().add(issue);
            logger.info("Added issue: " + issue);
            HashMap<String, Commenter> commenters = product.getCommenters();
            HashMap<String, Integer> stats = issue.getCommenterStats();
            // Scrape commenter and associated comments
            // Comment section starts with id contains c[0-9] like c0,c1,c2,etc..
            Elements commentElements = doc.select("div[id~=^c[0-9]+$]");
            for (Element e : commentElements) {
                String username = e.getElementsByClass("fn").text();
                String content = e.getElementsByClass("bz_comment_text").text();
                // Check if commenter already commented or not
                Commenter ct = commenters.get(username);
                if (ct == null) { // New commenter
                    ct = new Commenter(username);
                    ct.getComments().put(id, new ArrayList<Comment>());
                    commenters.put(username, ct);
                    logger.info("Added new commenter: " + username);
                } else if (ct.getComments().get(id) == null){
                    // If the user also comments in other issue,
                    // Create new list of Comments associates with that issue
                    ct.getComments().put(id, new ArrayList<Comment>());
                    logger.info("Existing commenter in other issue: " + username + " - " + id);
                } else {
                    // Continue commenting
                    logger.info("Existing commenter in the same issue: " + username + " - " + id);
                }
                Comment cm = new Comment(id, ct, content);
                ct.getComments().get(id).add(cm);
                // Create commenter stats for each issue
                stats.put(username, stats.getOrDefault(username, 0) + 1);
            }
            logger.info("------- END SCRAPING ISSUE " + id + " ----------\n");     
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
