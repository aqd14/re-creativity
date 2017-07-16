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
    public List<Word> getTaggedWords(File file) {
        // List of "Word" objects
        ArrayList<Word> taggedWords = new ArrayList<>();
        // Extract tagged sentences
        List<List<TaggedWord>> taggedSentences = getTaggedSentences(file);
        if (taggedSentences != null) {
            for (List<TaggedWord> sentence : taggedSentences) {
                for (TaggedWord taggedWord : sentence) {
                    Word word = new Word(taggedWord.value(), Utils.convertPOS(taggedWord.tag()));
                    taggedWords.add(word);
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
        // Stop assigning part-of-speech for words within topic if word list is empty
        if (t.getWordsPerTopic() == 0) {
            return;
        }
        
        ArrayList<TopicWord> topicWordList = t.getTopicWords();
        List<Word> words = getTaggedWords(file);

        for (TopicWord tw : topicWordList) {
            String word = tw.getWord().getContent();
            // Count number of occurrence for each part-of-speech
            // Assign POS of whichever greater
            int nounCount = 0;
            int verbCount = 0;
            for (Word w : words) {
                if (word.equals(w.getContent())) {
                    POS pos = w.getPos();
                    if (pos == POS.NOUN) {
                        nounCount ++;
                    } else if (pos == POS.VERB_BASE_FORM 
                            || pos == POS.VERB_PRESENT_TENSE
                            || pos == POS.VERB_PAST_TENSE) {
                        verbCount ++;
                    } else {

                    }
                }
            }
            if (nounCount > verbCount) {
                tw.getWord().setPos(POS.NOUN);
            } else if (nounCount < verbCount) {
                tw.getWord().setPos(POS.VERB);
            } else { 
                // TODO: Which part-of-speech should take if nounCount == verbCount?
                tw.getWord().setPos(POS.UNASSIGNED);
            }
            
        }
    }
}
