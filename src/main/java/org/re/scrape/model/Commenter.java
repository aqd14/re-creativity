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
public class Commenter {
    private String username;
    private HashMap<Integer, ArrayList<Comment>> comments; // Mapping from an issue to a list of comments
    
    /**
     * 
     */
    public Commenter(String username) {
        this.username = username;
        comments = new HashMap<>();
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Commenter [username=");
        builder.append(username);
        builder.append(", comments=");
        builder.append(comments);
        builder.append("]");
        return builder.toString();
    }
}
