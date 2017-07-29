/**
 * 
 */
package org.re.scrape.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.re.common.SoftwareSystem;

/**
 * @author doquocanh-macbook
 *
 */
public class Product implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // Attribute
    private SoftwareSystem system; // Software system
    private ArrayList<Issue> issues; // List of issues that product possesses
    private HashMap<String, Commenter> commenters; // Map commenters' username

    public Product(SoftwareSystem system) {
        this.system = system;
        issues = new ArrayList<>();
        commenters = new HashMap<>();
    }
    
    public Product(SoftwareSystem system, ArrayList<Issue> issues, HashMap<String, Commenter> commenters) {
        this.system = system;
        this.issues = issues;
        this.commenters = commenters;
    }
    
    /**
     * @return the system
     */
    public SoftwareSystem getSystem() {
        return system;
    }
    /**
     * @param system the system to set
     */
    public void setSystem(SoftwareSystem system) {
        this.system = system;
    }
    
    /**
     * @return the issues
     */
    public ArrayList<Issue> getIssues() {
        return issues;
    }

    /**
     * @param issues the issues to set
     */
    public void setIssues(ArrayList<Issue> issues) {
        this.issues = issues;
    }

    /**
     * @return the commenters
     */
    public HashMap<String,Commenter> getCommenters() {
        return commenters;
    }

    /**
     * @param commenters the commenters to set
     */
    public void setCommenters(HashMap<String, Commenter> commenters) {
        this.commenters = commenters;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Product [system=" + system + ", issues=" + issues + ", commenters=" + commenters + "]";
    }

}
