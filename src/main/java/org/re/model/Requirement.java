package org.re.model;

import org.re.common.SoftwareSystem;
import org.re.common.Template;

// Object represents for a completed requirement
// Example: Firefox shall inform matched passwords
//             |      |     |       |       |
//             |      |     |       |       |
//              Opening    Verb    Noun   Object (Additional from context to make sense for requirements)
//                       (Already flipped)
public class Requirement {
    // Attributes
    private SoftwareSystem system; // Indicate which oss system the requirement belongs to
    private Word opening; // Opening of a requirement. Such as: Firefox shall
    private Word verb;
    private Word noun;
    private Word object;
    
    public Requirement(SoftwareSystem system, Word verb, Word noun) {
        this.system = system;
        this.setOpening();
        this.verb = verb;
        this.noun = noun;
        this.object = new Word("Default Object");
    }
    
    public Requirement(SoftwareSystem system, Word verb, Word noun, Word object) {
        this(system, verb, noun);
        this.object = object;
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
    
    @Override
    public String toString() {
        StringBuilder bd = new StringBuilder(opening.getContent());
        bd.append(" ").append(verb.getContent()).append(" (").append(verb.getPos()).append(")").
        append(" ").append(noun.getContent()).append(" (").append(noun.getPos()).append(")").
        append(" ").append(object.getContent());
        return bd.toString();
    }
}
