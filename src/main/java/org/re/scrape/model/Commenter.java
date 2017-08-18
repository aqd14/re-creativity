/**
 * 
 */
package org.re.scrape.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author doquocanh-macbook
 *
 */
public class Commenter extends Stakeholder {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private HashMap<Integer, ArrayList<Comment>> comments; // Mapping from an issue to a list of comments
    
    /**
     * 
     */
    public Commenter(String name) {
        super(name);
        comments = new HashMap<>();
    }

    /**
     * @return the comments
     */
    public HashMap<Integer, ArrayList<Comment>> getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(HashMap<Integer, ArrayList<Comment>> comments) {
        this.comments = comments;
    }
    
    /*
     * Collect all comments from this user and append to an united string
     */
    public String collectComments() {
        if (comments == null) {
            return "";
        }
        
        StringBuilder bd = new StringBuilder();
        for (Integer id : comments.keySet()) {
            bd.append(comments.get(id));
        }
        return bd.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Commenter [username=");
        builder.append(name);
        builder.append(", comments=");
        builder.append(comments);
        builder.append("]");
        return builder.toString();
    }
}
