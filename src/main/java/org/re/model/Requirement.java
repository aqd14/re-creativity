package org.re.model;

import org.re.common.System;

// Object represents for a completed requirement
// Example: Firefox shall inform matched passwords
//             |      |     |       |       |
//             |      |     |       |       |
//              Opening    Verb    Noun   Object (Additional from context to make sense for requirements)
//                       (Already flipped)
public class Requirement {
    // Attributes
    private System system; // Indicate which oss system the requirement belongs to
    private Word opening; // Opening of a requirement. Such as: Firefox shall
    private Word verb;
    private Word noun;
    private Word object;
    
    public Requirement(System system, Word opening, Word verb, Word noun, Word object) {
        this.setSystem(system);
        this.opening = opening;
        this.verb = verb;
        this.noun = noun;
        this.object = object;
    }
    
    @Override
    public String toString() {
        StringBuilder bd = new StringBuilder(opening.getContent());
        bd.append(" ").append(verb.getContent()).
        append(" ").append(noun.getContent()).
        append(" ").append(object.getContent());
        return bd.toString();
    }

    /**
     * @return the system
     */
    public System getSystem() {
        return system;
    }

    /**
     * @param system the system to set
     */
    public void setSystem(System system) {
        this.system = system;
    }
    
    /**
     * @return the opening
     */
    public Word getOpening() {
        return opening;
    }

    /**
     * @param opening the opening to set
     */
    public void setOpening(Word opening) {
        this.opening = opening;
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
}
