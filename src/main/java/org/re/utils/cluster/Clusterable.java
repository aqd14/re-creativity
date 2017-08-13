/**
 * 
 */
package org.re.utils.cluster;

import java.util.List;

import org.re.scrape.model.AdjacencyMatrixGraph;

/**
 * 
 * Interface when all graph clustering implementation should implement.
 * 
 * @author Anh Quoc Do
 */
public interface Clusterable<T extends AdjacencyMatrixGraph> {
    
    /*
     * Cluster graph with specific clustering algorithms and return a set of centroids
     */
    public List<Cluster<Integer>> cluster(T graph);
}
