/**
 * 
 */
package org.re.common;

/**
 * @author doquocanh-macbook
 *         <p>
 *         The part-of-speech labels and their corresponding enums. Only focus
 *         on NOUN-VERB labels. However, need to pay attention to different
 *         noun-verb types because maybe they are dominant POS. TODO: Might need
 *         to find a way to convert other noun-verb types to present tense
 *         (Requirements description should be using present tense)
 *         </p>
 */
public enum POS {
    UNASSIGNED,
    
    // Noun types
    NOUN, // NN
    NOUN_PLURAL, // NNS
    PROPER_NOUN_SINGULAR, // NNP
    PROPER_NOUN_PLURAL, // NNPS
    
    // Verb types
    VERB_BASE_FORM, // VB
    VERB_GERUND, // VBG
    VERB_PAST_TENSE, // VBD
    VERB_PAST_PARTICIPLE, // VBN
    VERB_NON_3RD_PERSON, // VBP
    VERB_3RD_PERSON, // VBZ

    // Others
    ADJECTIVE, // JJ
    ADVERD, // RB
    CONJUNCTION, // CC
}
