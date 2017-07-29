/**
 * 
 */
package org.re.scrape.model;

/**
 * Textual representation for each issue status
 * 
 * @author doquocanh-macbook
 *
 */
public class IssueStatusDef {
    // Bug is unconfirmed
    public static final String UNCONFIRMED = "UNCONFIRMED";
    // Bug confirmed or receives enough votes
    public static final String NEW = "NEW";
    // Developer takes possession
    public static final String ASSIGNED = "ASSIGNED";
    // Bug is fixed
    public static final String RESOLVED_FIX = "RESOLVED FIX";
    // Duplicate bug
    public static final String RESOLVED_DUPLICATE = "RESOLVED DUPLICATE";
    // Bug won't be fixed
    public static final String RESOLVED_WONTFIX = "RESOLVED WONTFIX";
    // Temporarily works for fixer
    public static final String RESOLVED_WORKSFORME = "RESOLVED WORKSFORME";
    // Invalid fix
    public static final String RESOLVED_INVALID = "RESOLVED INVALID";
    // Resolve imcomplete
    public static final String RESOLVED_INCOMPLETE = "RESOLVED INCOMPLETE";
    // Resolved expired
    public static final String RESOLVED_EXPIRED = "RESOLVED EXPIRED";
    // Verified fixed
    public static final String VERIFIED_FIXED = "VERIFIED FIXED";
    // Verified invalid
    public static final String VERIFIED_INVALID = "VERIFIED INVALID";
    // Verified duplicate
    public static final String VERIFIED_DUPLICATE = "VERIFIED DUPLICATE";
    // Verified expired
    public static final String VERIFIED_EXPIRED= "VERIFIED EXPIRED";
    // Verified works for me
    public static final String VERIFIED_WORKSFORME = "VERIFIED WORKSFORME";
    // Verified won't fix
    public static final String VERIFIED_WONTFIX = "VERIFIED WONTFIX";
    // Verified incomplete
    public static final String VERIFIED_INCOMPLETE = "VERIFIED INCOMPLETE";
    // Reopen issue
    public static final String REOPENED = "REOPENED";
}
