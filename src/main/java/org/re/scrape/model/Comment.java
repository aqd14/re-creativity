/**
 * 
 */
package org.re.scrape.model;

import java.io.Serializable;

/**
 * @author doquocanh-macbook
 *
 */
public class Comment implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int issueID;            // Issue where comment belongs to
    private Commenter commenter;    // Who comments
    private String content;         // Comment's content
    /**
     * 
     */
    public Comment(int issueID, Commenter commenter, String content) {
        this.issueID = issueID;
        this.commenter = commenter;
        this.content = content;
    }
    
    public Comment(int issueID, String content) {
        this.issueID = issueID;
        this.content = content;
    }
    
    /**
     * @return the issueID
     */
    public int getIssueID() {
        return issueID;
    }
    /**
     * @param issueID the issueID to set
     */
    public void setIssueID(int issueID) {
        this.issueID = issueID;
    }
    /**
     * @return the commenter
     */
    public Commenter getCommenter() {
        return commenter;
    }
    /**
     * @param commenter the commenter to set
     */
    public void setCommenter(Commenter commenter) {
        this.commenter = commenter;
    }
    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }
    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
//        builder.append("Comment [issueID=");
//        builder.append(issueID);
//        builder.append(", commenter=");
//        builder.append(commenter);
//        builder.append(", content=");
//        builder.append(content);
//        builder.append("]");
        builder.append(content);
        return builder.toString();
    }

}
