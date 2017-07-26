package org.re.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class CosineDocumentSimilarity {
    
    public enum TermType {
        QUERY,
        DOCUMENT,
    }

    public static final String QUERY_FIELD = "query";
    public static final String DOCUMENT_FIELD = "document";
    
    public static final int DOCUMENT_ID = 0;
    
    private Directory directory;
    private IndexWriter writer;
    private IndexReader reader;
    private Set<String> queryTerms;
    private Set<String> documentTerms;
//    private Map<String, Integer> queryTF;    // Term frequency of the query
    private Map<String, Integer> documentTF;    // Term frequency of the document
//    private RealVector v1;              // Real-valued vector for f1 
//    private RealVector v2;              // Real-valued vector for f2
    
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
     * Construct a new indexed documents containing initial query and document
     * @param query
     * @param document
     * @throws IOException
     */
    public CosineDocumentSimilarity(String document) throws IOException {
        directory = new RAMDirectory();
//        queryTerms = new HashSet<>();
//        documentTerms = new HashSet<>();
//        directory = createIndex(s1, s2);
//        openWriter();
        addDocument(DOCUMENT_FIELD, document);
//        addDocument(QUERY_FIELD, query);
//        reader = DirectoryReader.open(directory);
        
        // Avoid re-calculating term frequencies for the same query or document
//        getTermFrequencies(QUERY_ID, QUERY_FIELD, TermType.QUERY);
        documentTF = getTermFrequencies(DOCUMENT_ID, DOCUMENT_FIELD, TermType.DOCUMENT);
        // Close reader
//        reader.close();
    }
    
    public void stats() throws IOException {
        openReader();
        System.out.println("Max docs: " + reader.maxDoc());
        System.out.println("Num docs: " + reader.numDocs());
        reader.close();
    }
    
    public void openReader() throws IOException {
        reader = DirectoryReader.open(directory);
    }
    
    public void openWriter() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_4, analyzer);
        writer = new IndexWriter(directory, iwc);
    }
    
    /**
     * Create a {@link org.apache.lucene.document.Document} for indexing and searching.
     * 
     * @param fieldName
     * @param content
     * @return  doc id
     * @throws IOException
     */
    public int addDocument(String fieldName, String content) throws IOException {
        openWriter();
        Document doc = new Document();
        Field field = new Field(fieldName, content, TYPE_STORED);
        doc.add(field);
        writer.addDocument(doc);
        writer.close();
        return numDocs() != 0 ? numDocs() - 1 : 0;
    }
    
     /**
     * Update document given a new query
     * 
     * @param writer
     * @param fieldName A string represent for a field
     * @param content   Updated content
     * @throws IOException
     */
    public void updateDocument(String fieldName, String content) throws IOException {
        openWriter();
        Document doc = new Document();
        Field field = new Field(fieldName, content, TYPE_STORED);
        doc.add(field);
        writer.updateDocument(new Term(fieldName), doc);
        writer.close();
    }
    
    /**
     * Delete a document in directory give a doc id
     * @param docID
     * @throws IOException
     */
    public void deleteDocument(int docID) throws IOException {
        openWriter();
        openReader();
        writer.tryDeleteDocument(reader, docID);
        writer.close();
        reader.close();
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
     * Calculate cosine similarity given query id.
     * 
     * @param queryID
     * @return
     * @throws IOException 
     */
    public double calculateCosineSimilarity(int queryID) throws IOException {
        Map<String, Integer> queryFres = getTermFrequencies(queryID, QUERY_FIELD, TermType.QUERY);
        RealVector queryVector = toRealVector(queryFres);
        RealVector docVector = toRealVector(documentTF);
        return calculateCosineSimilarity(queryVector, docVector);
    }
    
    /**
     * Iterate through document and get frequencies for each term
     * @param reader
     * @param docId
     * @param field
     * @param type
     * @return A Map from terms to corresponding frequencies
     * @throws IOException
     */
    public Map<String, Integer> getTermFrequencies(int docId, String field, TermType type) throws IOException {
        HashSet<String> terms = new HashSet<>();
        
        // Reset if query term
        openReader();
        Terms vector = reader.getTermVector(docId, field);
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
        
        if (type == TermType.DOCUMENT) {
            documentTerms = terms;
        } else {
            queryTerms = terms;
        }
        reader.close();
        return frequencies;
    }

    /**
     * Convert a mapping of term frequency to real-valued vector
     * 
     * @param map   Term frequency mapping
     * @return      A real-valued vector
     */
    public RealVector toRealVector(Map<String, Integer> map) {
        Set<String> terms = new HashSet<>();
        terms.addAll(queryTerms);
        terms.addAll(documentTerms);
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            int value = map.containsKey(term) ? map.get(term) : 0;
            vector.setEntry(i++, value);
        }
        return (RealVector) vector.mapDivide(vector.getL1Norm());
    }
    
    /**
     * Number of indexed docs in the directory. There is only one large document and the rest are queries
     * @return
     */
    public int numDocs() {
        int numDocs = -1;
        try {
            openReader();
            numDocs = reader.numDocs();
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return numDocs;
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

    /**
     * @return the queryTerms
     */
    public Set<String> getQueryTerms() {
        return queryTerms;
    }

    /**
     * @param queryTerms the queryTerms to set
     */
    public void setQueryTerms(Set<String> queryTerms) {
        this.queryTerms = queryTerms;
    }

    /**
     * @return the documentTerms
     */
    public Set<String> getDocumentTerms() {
        return documentTerms;
    }

    /**
     * @param documentTerms the documentTerms to set
     */
    public void setDocumentTerms(Set<String> documentTerms) {
        this.documentTerms = documentTerms;
    }

    public static void main(String args[]) throws IOException {
        String query1 = "The game of life is a game of everlasting learning";
        String query2 = "Julie loves me more than Linda loves me";
        String doc = "Jane likes me more than Julie loves me";
        CosineDocumentSimilarity cds = new CosineDocumentSimilarity(doc);
        cds.stats();
        
        cds.addDocument(QUERY_FIELD, query1);
        Map<String, Integer> queryTF1 = cds.getTermFrequencies(1, QUERY_FIELD, TermType.QUERY);
        Map<String, Integer> documentTF = cds.getTermFrequencies(DOCUMENT_ID, DOCUMENT_FIELD, TermType.DOCUMENT);
        
        RealVector v1 = cds.toRealVector(queryTF1);
        RealVector v2 = cds.toRealVector(documentTF);
//        cds.getTermFrequencies(reader, docId, field, type)
        System.out.println(cds.calculateCosineSimilarity(v1, v2));
        // Update documents
//        cds.updateDocument(QUERY_FIELD, query2);
//        cds.stats();
        // Delete documents
        cds.deleteDocument(1);
        cds.stats();
        
        cds.addDocument(QUERY_FIELD, query2);
        cds.stats();
        
        Map<String, Integer> queryTF2 = cds.getTermFrequencies(2, QUERY_FIELD, TermType.QUERY);
        v1 = cds.toRealVector(queryTF2);
        v2 = cds.toRealVector(documentTF);
        System.out.println(cds.calculateCosineSimilarity(v1, v2));
        
        cds.addDocument(QUERY_FIELD, doc);
        cds.stats();
        
        Map<String, Integer> queryTF3 = cds.getTermFrequencies(3, QUERY_FIELD, TermType.QUERY);
        v1 = cds.toRealVector(queryTF3);
        v2 = cds.toRealVector(documentTF);
        System.out.println(cds.calculateCosineSimilarity(v1, v2));
    }
}