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
import org.re.scrape.model.Comment;
import org.re.scrape.model.Commenter;
import org.re.scrape.model.Issue;

/**
 * @author doquocanh-macbook
 *
 */
public class FirefoxScraper extends BaseScraper {
 // Logger
    final static Logger logger = Logger.getLogger(FirefoxScraper.class);
    private static final String FIREFOX_URL_PREFIX = "https://bugzilla.mozilla.org/show_bug.cgi?id=";
    
    /**
     * 
     */
    public FirefoxScraper() {
        super(SoftwareSystem.FIREFOX, FIREFOX_URL_PREFIX, new SimpleDateFormat("yyyy-MM-dd HH:mm"));
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
            String status = doc.select("#field-value-status-view").text();
            int id = Integer.parseInt(url.replace(FIREFOX_URL_PREFIX, ""));
            logger.info("------- START SCRAPING ISSUE " + id + " ----------\n");
            if (isInvalidStatus(status)) {
                logger.debug("Issue " + id + " is invalid: " + status + "\n");
                logger.info("------- END SCRAPING ISSUE " + id + " ----------\n");
                return;
            }
            String title = doc.select("#field-value-short_desc").text();
            String importance = doc.select("#field-value-bug_severity").text();
            
            // Date information
            String createdDateStr = doc.select("#field-creation_ts .value .rel-time").attr("title").replace(" PDT", "").replace(" PST", "");
            Date createdDate = df.parse(createdDateStr);
            String modifiedDateStr = doc.select("#field-delta_ts .value .rel-time").attr("title").replace(" PDT", "").replace(" PST", "");
            Date modifiedDate = df.parse(modifiedDateStr);
            String resolvedDateStr = doc.select("#c1 .change-time .rel-time").attr("title").replace(" PDT", "").replace(" PST", "");
            Date resolvedDate = df.parse(resolvedDateStr);
            
         // Create new Issue object with scraped data
            Issue issue = new Issue(id, title, status, importance, createdDate, modifiedDate, resolvedDate);
            product.getIssues().add(issue);
            logger.info("Added issue: " + issue);
            HashMap<String, Commenter> commenters = product.getCommenters();
            // Scrape commenter and associated comments
            // Comment section starts with id contains c[0-9] like c0,c1,c2,etc..
            Elements commentElements = doc.select("div[id~=^c[0-9]+$]");
            for (Element e : commentElements) {
                String username = e.getElementsByClass("fna").text();
                String content = e.getElementsByClass("comment-text").text();
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
            }
            logger.info("------- END SCRAPING ISSUE " + id + " ----------\n");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
