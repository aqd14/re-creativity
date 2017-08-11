/**
 * 
 */
package org.re.scrape.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * The class contains any related information need to be scraped from an issue, which includes:
 * 
 * <li> Issue's id </li>
 * <li> Issue's description </li>
 * <li> Issue's system </li>
 * <li> Issue's status </li>
 * <li> Issue's importance </li>
 * <li> Assignee </li>
 * <li> Reporter </li>
 * <li> Commenters' stats </li>
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
    
    
    private String reportedDateStr;
    private String modifiedDateStr;
    private String resolvedDateStr;
    
    // Stakeholders
    private Assignee assignee;
    private Reporter reporter;
    private HashMap<String, Integer> commenterStats; // Map commenter to his number of comments within the issue

    /**
     * 
     */
    public Issue() {
        
    }
    
    public Issue(int id, String title, String status, String importance, Assignee assignee, Reporter reporter, String createdDate,
            String modifiedDate, String resolvedDate) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.importance = importance;
        this.setAssignee(assignee);
        this.setReporter(reporter);
        this.setCommenterStats(new HashMap<>());
        this.setReportedDateStr(createdDate);
        this.setModifiedDateStr(modifiedDate);
        this.setResolvedDateStr(resolvedDate);
    }
    
    public Issue(int id, String title, String status, String importance, Assignee assignee, Reporter reporter, Date createdDate,
            Date modifiedDate, Date resolvedDate) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.importance = importance;
        this.setAssignee(assignee);
        this.setReporter(reporter);
        this.setCommenterStats(new HashMap<>());
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
    
    /**
     * @return the assignee
     */
    public Assignee getAssignee() {
        return assignee;
    }

    /**
     * @param assignee the assigner to set
     */
    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    /**
     * @return the reporter
     */
    public Reporter getReporter() {
        return reporter;
    }

    /**
     * @param reporter the reporter to set
     */
    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    /**
     * @return the commenterStats
     */
    public HashMap<String, Integer> getCommenterStats() {
        return commenterStats;
    }

    /**
     * @param commenterStats the commenterStats to set
     */
    public void setCommenterStats(HashMap<String, Integer> commenterStats) {
        this.commenterStats = commenterStats;
    }
    
    public String commentStatsToString() {
        if (commenterStats == null) {
            return "";
        }
        StringBuilder bd = new StringBuilder();
        // Get list of stakeholders who made comments in the current issue
        Set<String> stakeholders = commenterStats.keySet();
        for (String name : stakeholders) {
            int commentCount = commenterStats.get(name);
            bd.append(name).append(":").append(commentCount).append(";");
        }
        return bd.toString();
    }
    
    /**
     * Convert comments stats to a specific format that represents for the
     * communication between a reporter and other developers.
     * 
     * Format: A / B:5 / C:3 / D:6 in which A is a stakefolder and others are
     * developers who have made interaction with A on the reported issue. The
     * associated digits are the total number of communication made between
     * reporter and developers.
     * 
     * We define X and Y have communication if one of the followings occurred:
     * <li>X is the proposer of T or has posted a comment or artifact about T
     * that is read by Y</li>
     * <li>Y is the proposer of T or has posted a comment or artifact about T
     * that is read by X</li>
     * 
     * Each reported issue, comment or artifact is considered a communication
     * (1) and will be added up to the total weight
     * 
     * @return
     */
    public String toGraphEdges() {
        if (commenterStats == null) {
            return "";
        }
        StringBuilder bd = new StringBuilder();
        Set<String> shSet = commenterStats.keySet();
        String[] stakeholders = shSet.toArray(new String[shSet.size()]);
        // Calculate communication between all pairs of stakeholders
        for (int i = 0; i < stakeholders.length - 1; i++) {
            int count1 = commenterStats.get(stakeholders[i]);
            bd.append(stakeholders[i]).append(" / ");
            for (int j = i+1; j < stakeholders.length; j++) {
                int count2 = commenterStats.get(stakeholders[j]);
                int numComs = count1 + count2; // Number of communication between two stakeholders
                // Add 1 communication if either is the reporter
                if (stakeholders[i].equals(reporter.name) || stakeholders[j].equals(reporter.name)) {
                    numComs++;
                }
                bd.append(stakeholders[j]).append(":").append(numComs);
                if (j != stakeholders.length - 1) {
                    bd.append(" / ");
                }
            }
            bd.append("\n");
        }
        return bd.toString();
    }
    
    /**
     * @return the reportedDateStr
     */
    public String getReportedDateStr() {
        return reportedDateStr;
    }

    /**
     * @param reportedDateStr the reportedDateStr to set
     */
    public void setReportedDateStr(String reportedDateStr) {
        this.reportedDateStr = reportedDateStr;
    }

    /**
     * @return the modifiedDateStr
     */
    public String getModifiedDateStr() {
        return modifiedDateStr;
    }

    /**
     * @param modifiedDateStr the modifiedDateStr to set
     */
    public void setModifiedDateStr(String modifiedDateStr) {
        this.modifiedDateStr = modifiedDateStr;
    }

    /**
     * @return the resolvedDateStr
     */
    public String getResolvedDateStr() {
        return resolvedDateStr;
    }

    /**
     * @param resolvedDateStr the resolvedDateStr to set
     */
    public void setResolvedDateStr(String resolvedDateStr) {
        this.resolvedDateStr = resolvedDateStr;
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
        builder.append(", reportedDate=");
        builder.append(reportedDateStr);
        builder.append(", modifiedDate=");
        builder.append(modifiedDateStr);
        builder.append(", resolvedDate=");
        builder.append(resolvedDateStr);
        builder.append("]");
        return builder.toString();
    }
}
