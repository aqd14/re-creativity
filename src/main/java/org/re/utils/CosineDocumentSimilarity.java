package org.re.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class CosineDocumentSimilarity {

    public static final String CONTENT = "Content";

    private Set<String> terms;
    private Map<String, Integer> queryTF;    // Term frequency of the query
    private Map<String, Integer> documentTF;    // Term frequency of the document
//    private RealVector v1;              // Real-valued vector for f1 
//    private RealVector v2;              // Real-valued vector for f2
    
    /**
     * 
     * @param s1
     * @param s2
     * @param retry
     * @throws IOException
     */
    CosineDocumentSimilarity(String s1, String s2) throws IOException {
        terms = new HashSet<>();
        Directory directory = createIndex(s1, s2);
        IndexReader reader = DirectoryReader.open(directory);
        
        // Avoid re-calculating term frequencies for the same query or document
        if (s1 != null) {
            setQueryTF(getTermFrequencies(reader, 0));
        }
        if (s2 != null) {
            setDocumentTF(getTermFrequencies(reader, 1));
        }
        reader.close();
//        v1 = toRealVector(query);
//        v2 = toRealVector(document);
    }

    Directory createIndex(String s1, String s2) throws IOException {
        Directory directory = new RAMDirectory();
        SimpleAnalyzer analyzer = new SimpleAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_4, analyzer);
        IndexWriter writer = new IndexWriter(directory, iwc);
        // Avoid re-indexing
        if (s1 != null) {
            addDocument(writer, s1);
        }
        if (s2 != null) {
            addDocument(writer, s2);
        }
        writer.close();
        return directory;
    }

    /* Indexed, tokenized, stored. */
    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_STORED.setIndexed(true);
        // TYPE_STORED.setIndexOptions(IndexOptions.DOCS);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(true);
        TYPE_STORED.freeze();
    }
    
    /**
     * Create a {@link org.apache.lucene.document.Document} for indexing and searching
     * 
     * @param writer
     * @param content
     * @throws IOException
     */
    public void addDocument(IndexWriter writer, String content) throws IOException {
        Document doc = new Document();
        Field field = new Field(CONTENT, content, TYPE_STORED);
        doc.add(field);
        writer.addDocument(doc);
    }
    
    /**
     * Calculate cosine similarity with tf-idf
     * 
     * @param v1    Query vector
     * @param v2    Document vector
     * @return  Cosine similarity
     */
    public double calculateCosineSimilarity(RealVector v1, RealVector v2) {
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }
    
    /**
     * Iterate through document and get frequencies for each term
     * @param reader
     * @param docId
     * @return A Map from terms to corresponding frequencies
     * @throws IOException
     */
    public Map<String, Integer> getTermFrequencies(IndexReader reader, int docId) throws IOException {
        Terms vector = reader.getTermVector(docId, CONTENT);
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator(termsEnum);
        Map<String, Integer> frequencies = new HashMap<>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsEnum.totalTermFreq();
            frequencies.put(term, freq);
            terms.add(term);
        }
        return frequencies;
    }

    /**
     * Convert a mapping of term frequency to real-valued vector
     * 
     * @param map   Term frequency mapping
     * @return      A real-valued vector
     */
    public RealVector toRealVector(Map<String, Integer> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            int value = map.containsKey(term) ? map.get(term) : 0;
            vector.setEntry(i++, value);
        }
        return (RealVector) vector.mapDivide(vector.getL1Norm());
    }

    /**
     * @return the queryTF
     */
    public Map<String, Integer> getQueryTF() {
        return queryTF;
    }

    /**
     * @param queryTF the queryTF to set
     */
    public void setQueryTF(Map<String, Integer> queryTF) {
        this.queryTF = queryTF;
    }

    /**
     * @return the documentTF
     */
    public Map<String, Integer> getDocumentTF() {
        return documentTF;
    }

    /**
     * @param documentTF the documentTF to set
     */
    public void setDocumentTF(Map<String, Integer> documentTF) {
        this.documentTF = documentTF;
    }

    public static void main(String args[]) throws IOException {
//        String doc1 = "The game of life is a game of everlasting learning";
//        String doc2 = "The unexamined life is not worth living";
//        String doc3 = "Never stop learning";
//        String query = "life learning";
        CosineDocumentSimilarity cds = new CosineDocumentSimilarity("Julie loves me more than Linda loves me",
                "Jane likes me more than Julie loves me");
        
        RealVector v1 = cds.toRealVector(cds.getQueryTF());
        RealVector v2 = cds.toRealVector(cds.getDocumentTF());
        System.out.println(cds.calculateCosineSimilarity(v1, v2));
    }
}