/**
 * 
 */
package org.re.scrape.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.re.common.SoftwareSystem;
import org.re.utils.cluster.Cluster;

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

    static final Logger logger = Logger.getLogger(Product.class);
    
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
    
    /**
     * Collect all comments from stakeholders within cluster to a single string
     * 
     * @param cluster   given cluster
     * @return  a collection of comments
     */
    public String toCorpus(Cluster<Integer> cluster, StakeHolderGraph graph) {
        // Precondition check
        if (cluster == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Collect comments from medoid
        int medoid = cluster.getMedoid().getValue();
        String username = graph.name(medoid);
        logger.info("Medoid number: " + medoid + " --- Medoid name: " + username);
        Commenter c = commenters.get(username);
        sb.append(c.collectComments());
        
        // Collect comments from all other stakeholders in the group (cluster)
        ArrayList<Integer> points = cluster.getDataPoints();
        for (Integer p : points) {
            username = graph.name(p);
            logger.info("datapoint: " + p + " --- datapoint name: " + username);
            c = commenters.get(username);
            // TODO: why c can be null?
            if (c != null) {
                sb.append(c.collectComments());
            } else {
                logger.error("Can't get commenter with username: " + username);
            }
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Product [system=" + system + ", issues=" + issues + ", commenters=" + commenters + "]";
    }
}
