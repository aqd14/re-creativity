package org.re.scrape;

import java.net.URL;

import org.re.scrape.model.Issue;

/**
 * @author doquocanh-macbook
 *
 */
public interface IScraper {
    /**
     * Scrape data from given url and extract data to an {@link org.re.scrape.model.Issue} object
     * 
     * @param url Given url
     * @return  an extracted {@link org.re.scrape.model.Issue} object
     */
    public Issue scrape(URL url);
}
