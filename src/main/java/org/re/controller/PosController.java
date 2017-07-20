/**
 * 
 */
package org.re.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.re.common.POS;
import org.re.model.Topic;
import org.re.model.TopicWord;
import org.re.model.Word;
import org.re.utils.Utils;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * @author doquocanh-macbook
 *
 */
public class PosController {
    // Logger
    final static Logger logger = Logger.getLogger(PosController.class);
    // Attributes
    private MaxentTagger tagger;
    
    /**
     * 
     */
    public PosController() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("models/english-left3words-distsim.tagger");
        tagger = new MaxentTagger(is);
    }
    
    /**
     * Split a text into list of sentences.
     * @param text
     * @return List of sentences
     */
    public static ArrayList<String> splitText2Sentences(String text) {
        ArrayList<String> sentences = new ArrayList<>();
        BreakIterator bi = BreakIterator.getSentenceInstance(Locale.US);
        bi.setText(text);
        int start = bi.first();
        for (int end = bi.next(); end != BreakIterator.DONE; start = end, end = bi.next()) {
            sentences.add(text.substring(start, end));
        }
        return sentences;
    }
    
    /**
     * Get list sentences with tagged words. Each sentence contains a list of tagged word.
     * 
     * @param file Text file
     * @return List of tagged sentences
     */
    public List<List<TaggedWord>> getTaggedSentences(File file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));) {
            List<List<HasWord>> sentences = MaxentTagger.tokenizeText(reader, null);
            return tagger.process(sentences);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<List<TaggedWord>> getTaggedSentences(String path) {
        File file = new File(path);
        return getTaggedSentences(file);
    }
    
    /**
     * <p>
     * Convert from a list of sentences of list of tagged words to just a list
     * of "Word" objects
     * </p>
     * @see org.re.model.Word
     * @param file
     * @return List of "Word" objects
     */
    public ArrayList<Word> getTaggedWords(File file) {
        // List of "Word" objects
        ArrayList<Word> taggedWords = new ArrayList<>();
        // Extract tagged sentences
        List<List<TaggedWord>> taggedSentences = getTaggedSentences(file);
        if (taggedSentences != null) {
            for (List<TaggedWord> sentence : taggedSentences) {
                for (TaggedWord taggedWord : sentence) {
                    Word word = new Word(taggedWord.value(), Utils.convertPOS(taggedWord.tag()));
                    taggedWords.add(word);
                    logger.debug("Added tagged word: " + taggedWord.value() + " - " + taggedWord.tag());
                }
            }
        }
        return taggedWords;
    }
    
    /**
     * <p>
     * Scan text file, determine the most common part-of-speech (POS) for each
     * word within topics. The POS will be determined based on the tag with
     * highest occurrence. Note that the POS will only be either Noun or Verb
     * </p>
     * 
     * @param file Text file
     * @param t Topic that contains words to be assigned
     */
    public void assignPOS(File file, Topic t) {
        ArrayList<Word> words = getTaggedWords(file);
        assignPOS(t, words);
    }
    
    /**
     * Find the dominant part-of-speech for each words in a given topic
     * @param t
     * @param words
     */
    public void assignPOS(Topic t, ArrayList<Word> words) {
        // Stop assigning part-of-speech for words within topic if word list is empty
        if (t.getWordsPerTopic() == 0) {
            return;
        }
        
        ArrayList<TopicWord> topicWordList = t.getTopicWords();
        for (TopicWord tw : topicWordList) {
            String word = tw.getWord().getContent();
            // Count number of occurrence for each part-of-speech
            // Assign POS of whichever greater
            int nounCount = 0;
            int verbCount = 0;
            for (Word w : words) {
                if (word.equals(w.getContent())) {
                    POS pos = w.getPos();
                    if (Utils.isNoun(pos)) {
                        nounCount ++;
                    } else if (Utils.isVerb(pos)) {
                        verbCount ++;
                    } else {
                        logger.info("Other part-of-speech: " + pos);
                    }
                }
            }
            
            // The word's part-of-speech can be either noun or verb
            // TODO: Need to determine exact part-of-speech? 
            // (E.g., Verb can be base form, gerund, 3rd person, etc..)
            if (nounCount != 0 || verbCount != 0) {
                if (nounCount > verbCount) {
                    tw.getWord().setPos(POS.NOUN);
                } else if (nounCount < verbCount) {
                    tw.getWord().setPos(POS.VERB_BASE_FORM);
                } else { 
                    // TODO: Which part-of-speech should take if nounCount == verbCount?
                    tw.getWord().setPos(POS.UNASSIGNED);
                }
            }
            logger.debug("Word: " + word);
            logger.debug("Noun count: " + nounCount);
            logger.debug("Verb count: " + verbCount);
            // The word's part-of-speech is neither noun nor verb
            // Assign default part-of-speech based on the word alone
            // TODO: Consider counting tagging for other part-of-speeches along with verb and noun 
            if (nounCount == 0 && verbCount == 0) {
                String tagged = tagger.tagString(word);
                String tag = tagged.replace(word, "").replace("_", "").trim();
                tw.getWord().setPos(Utils.convertPOS(tag));
                logger.debug("Neither noun nor verb: " + tagged);
            }
        }
    }
}
