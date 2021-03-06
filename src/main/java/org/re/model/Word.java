/**
 * 
 */
package org.re.model;

import java.io.Serializable;

import org.re.common.POS;

/**
 * @author doquocanh-macbook
 *
 */
public class Word implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String content;
    private transient POS pos;

    /**
     * 
     */
    public Word() {
        this.content = "";
        this.pos = POS.UNASSIGNED;
    }

    public Word(String content) {
        this.content = content;
        this.pos = POS.UNASSIGNED;
    }

    public Word(String content, POS pos) {
        this.content = content;
        this.pos = pos;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the pos
     */
    public POS getPos() {
        return pos;
    }

    /**
     * @param pos
     *            the pos to set
     */
    public void setPos(POS pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return this.content + " - " + this.pos;
    }
}
