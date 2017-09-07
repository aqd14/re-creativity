package org.re.model;

import java.io.Serializable;

import org.re.common.SoftwareSystem;
import org.re.common.Template;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

// Object represents for a completed requirement
// Example: Firefox shall inform matched passwords
//             |      |     |       |       |
//             |      |     |       |       |
//              Opening    Verb    Noun   Object (Additional from context to make sense for requirements)
//                       (Already flipped)
public class Requirement extends RecursiveTreeObject<Requirement> implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // Attributes
    private String id;
    private SoftwareSystem system; // Indicate which oss system the requirement belongs to
    private Word opening; // Opening of a requirement. Such as: Firefox shall
    private Word verb;
    private Word noun;
    private Word object;
    
    private transient BooleanProperty selected;
    
    public Requirement(String id, SoftwareSystem system, Word verb, Word noun) {
        this.id = id;
        this.system = system;
        this.setOpening();
        this.verb = verb;
        this.noun = noun;
        this.object = new Word("Default Object");
        
        selected = new SimpleBooleanProperty(false);
    }
    
    public Requirement(String id, SoftwareSystem system, Word verb, Word noun, Word object) {
        this(id, system, verb, noun);
        this.object = object;
    }
    
    /**
     * @return the requirementId
     */
    public String getId() {
        return id;
    }

    /**
     * @param requirementId the requirementId to set
     */
    public void setId(String id) {
        this.id = id;
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
     * @return the opening
     */
    public Word getOpening() {
        return opening;
    }

    /**
     * Opening phrase is determined upon the specified system
     * @param opening the opening to set
     */
    private void setOpening() {
        if (system == SoftwareSystem.FIREFOX) {
            opening = new Word(Template.FIREFOX_OPENING);
        } else if (system == SoftwareSystem.MYLYN) {
            opening = new Word(Template.MYLYN_OPENING);
        } else {
            opening = new Word("");
        }
    }

    /**
     * @return the verb
     */
    public Word getVerb() {
        return verb;
    }

    /**
     * @param verb the verb to set
     */
    public void setVerb(Word verb) {
        this.verb = verb;
    }

    /**
     * @return the noun
     */
    public Word getNoun() {
        return noun;
    }

    /**
     * @param noun the noun to set
     */
    public void setNoun(Word noun) {
        this.noun = noun;
    }

    /**
     * @return the object
     */
    public Word getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(Word object) {
        this.object = object;
    }
    
    public BooleanProperty selectedProperty() {
        return selected;
    }
    
    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected.get();
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public String toString() {
        StringBuilder bd = new StringBuilder(opening.getContent());
        bd.append(" ").append(verb.getContent()).
        append(" ").append(noun.getContent()).
        append(" ").append(object.getContent());
        return bd.toString();
    }
}
