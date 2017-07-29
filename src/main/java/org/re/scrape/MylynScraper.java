/**
 * 
 */
package org.re.scrape;

import java.text.SimpleDateFormat;

import org.re.common.SoftwareSystem;

/**
 * @author doquocanh-macbook
 *
 */
public class MylynScraper extends BaseScraper {
    // Attribute
    private static final String MYLYN_URL_PREFIX = "https://bugs.eclipse.org/bugs/show_bug.cgi?id=";
    // Default constructor
    public MylynScraper() {
        super(SoftwareSystem.MYLYN, MYLYN_URL_PREFIX, new SimpleDateFormat());
    }

    /* 
     * @see org.re.scrape.BaseScraper#scrape(java.net.URL)
     */
    @Override
    public void scrape(String url) {
        // TODO Auto-generated method stub
//        return null;
    }
}
