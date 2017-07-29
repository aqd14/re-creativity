/**
 * 
 */
package org.re.scrape;

import java.net.URL;
import java.text.SimpleDateFormat;

import org.re.common.SoftwareSystem;
import org.re.scrape.model.IssueStatusDef;
import org.re.scrape.model.Product;

/**
 * @author doquocanh-macbook
 *
 */
public abstract class BaseScraper {
    // Attributes
    protected Product product;
    protected final String URL_PREFIX;
    protected final SimpleDateFormat df;
    /**
     * 
     */
    public BaseScraper(SoftwareSystem system, String prefix, SimpleDateFormat df) {
       product = new Product(system);
       URL_PREFIX = prefix;
       this.df = df; 
    }
    
    /**
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }
    
    /**
     * Scrape data from given url and extract data to an {@link org.re.scrape.model.Issue} object
     * 
     * @param url Given url
     * @return  an extracted {@link org.re.scrape.model.Issue} object
     */
    public abstract void scrape(String url);
    
    public void scrape(URL url) {
        scrape(url.toExternalForm());
    }
    
    /**
     * Scrape data from given issue id
     * @param issueID
     * @return
     */
    public void scrape(int issueID) {
        StringBuilder sb = new StringBuilder(URL_PREFIX).append(issueID);
        scrape(sb.toString());
    }
    
    /**
     * Scrape data from a range of ids
     * @param from
     * @param to
     * @return
     */
    public void scrape(int from, int to) {
        if (from > to) {
            throw new IllegalArgumentException("[from] must lesser or equal [to]!");
        }
        for (int id = from; id <= to; id++) {
            scrape(id);
        }
    }
    
    /**
     * Check if the issue is valid or not. The issue should not be marked as [Duplicate]
     * 
     * @param status The issue's status to be checked
     * @return  {@code true} if status was not marked as [Duplicate]
     */
    protected boolean isInvalidStatus(String status) {
        return status.contains(IssueStatusDef.RESOLVED_DUPLICATE) || status.contains(IssueStatusDef.VERIFIED_DUPLICATE);
    }
}
