/**
 * 
 */
package org.re.scrape.model;

import java.io.Serializable;
import java.util.Date;

/**
 * The class contains any related information need to be scraped from an issue, which includes:
 * 
 * <li> Issue's id </li>
 * <li> Issue's description </li>
 * <li> Issue's system </li>
 * <li> Issue's status </li>
 * <li> Issue's importance </li>
 * <li> Issue's created date </li>
 * <li> Issue's modified date </li>
 * <li> Issue's resolved date </li>
 * 
 * @author doquocanh-macbook
 *
 */
public class Issue implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // Attributes
    private int id;                 // Unique id for each issue
    private String title;           // Issue's description
    private String status;          // Issue's status
    private String importance;      // Issue's importance
    private Date createdDate;       // Created date
    private Date modifiedDate;      // Latest modified date
    private Date resolvedDate;      // Resolved date

    /**
     * 
     */
    public Issue() {
        
    }
    
    public Issue(int id, String title, String status, String importance, Date createdDate,
            Date modifiedDate, Date resolvedDate) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.importance = importance;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.resolvedDate = resolvedDate;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * @return the importance
     */
    public String getImportance() {
        return importance;
    }
    /**
     * @param importance the importance to set
     */
    public void setImportance(String importance) {
        this.importance = importance;
    }
    /**
     * @return the createdDate
     */
    public Date getCreatedDate() {
        return createdDate;
    }
    /**
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    /**
     * @return the modifiedDate
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }
    /**
     * @param modifiedDate the modifiedDate to set
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    /**
     * @return the resolvedDate
     */
    public Date getResolvedDate() {
        return resolvedDate;
    }
    /**
     * @param resolvedDate the resolvedDate to set
     */
    public void setResolvedDate(Date resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Issue [id=");
        builder.append(id);
        builder.append(", title=");
        builder.append(title);
        builder.append(", status=");
        builder.append(status);
        builder.append(", importance=");
        builder.append(importance);
        builder.append(", createdDate=");
        builder.append(createdDate);
        builder.append(", modifiedDate=");
        builder.append(modifiedDate);
        builder.append(", resolvedDate=");
        builder.append(resolvedDate);
        builder.append("]");
        return builder.toString();
    }
}
