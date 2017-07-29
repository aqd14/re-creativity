/**
 * 
 */
package org.re.scrape.model;

import java.io.Serializable;

/**
 * @author doquocanh-macbook
 *
 */
public class Stakeholder implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected String name;

    /**
     * 
     */
    public Stakeholder() {
        
    }
    
    public Stakeholder(String name) {
        this.name = name;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
