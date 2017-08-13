package org.re.utils.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.re.scrape.model.AdjacencyMatrixGraph;

/**
 * Implementation of k-medoid algorithm for clustering undirected weighted graph.
 * 
 * @author Anh Quoc Do
 * @see {@link https://en.wikipedia.org/wiki/K-medoids}
 *
 */
public class KMedoids<T extends AdjacencyMatrixGraph> implements Clusterable<T> {
    // Number of clusters
    private int k;
    // Maximum number of iterations
    private int maxIterations;
    // Cluster numbers as vertex value
    private Set<Integer> clusterNumbers;
    /**
     * 
     */
    public KMedoids(int k, int maxIterations) {
        this.k = k;
        this.maxIterations = maxIterations;
        clusterNumbers = new HashSet<Integer>();
    }
    
    /*
     * Get number of clusters
     */
    public int getK() {
        return k;
    }
    
    /*
     * Get maximum number of iteration for clustering graph
     */
    public int getMaxIterations() {
        return maxIterations;
    }
    
    @Override
    public ArrayList<Cluster<Integer>> cluster(T graph) {
     // Precondition checks
        if (graph == null) {
            throw new NullPointerException("AdjacencyListGraph is null!");
        }
        // Are there too many clusters?
        if (graph.V() < k) {
            throw new IllegalArgumentException("Number of vertices are less than number of clusters!");
        }
        // Initialize centroi list
        ArrayList<Cluster<Integer>> clusters = initializeClusters(graph);
        int[] oldAssignment = assignClusters(graph, clusters);
        for (int ite = 0; ite < maxIterations; ite++) {
            for (Cluster<Integer> cluster : clusters) {
                int oldMedoid = cluster.getMedoid().getValue();
                int newMedoid = findMedoid(graph, cluster);
                if (newMedoid != oldMedoid) {
                    // Replace old medoid with new one
                    cluster.getDataPoints().add(oldMedoid);
                    cluster.getDataPoints().remove((Integer)newMedoid);
                    cluster.setMedoid(new Medoid<Integer>(newMedoid));
                    // Update cluster numbers
                    clusterNumbers.remove((Integer)oldMedoid);
                    clusterNumbers.add(newMedoid);
                }
            }
            int[] newAssignment = assignClusters(graph, clusters);
            if (isConverged(oldAssignment, newAssignment)) {
                System.out.printf("Converged after %d iterations!\n\n", ite+1);
                return clusters;
            }
            oldAssignment = newAssignment;
        }
        // If convergence hasn't been reached within iterations, the closest clusters are returned
        System.out.println("Terminated iterations without convergence!");
        return clusters;
    }
    
    /*
     * Randomly select k vertices from graph as initial medoids and assign each data point to those medoids 
     */
    private ArrayList<Cluster<Integer>> initializeClusters(AdjacencyMatrixGraph graph) {
        int V = graph.V(); // Number of vertices
        ArrayList<Cluster<Integer>> clusters = new ArrayList<Cluster<Integer>>();
        
        // Set contains list of taken medoids. Guarantee there is no duplicate medoids are chosen
        HashSet<Integer> taken = new HashSet<>();
        
        // Random generator for arbitrarily picking k medoids from list of vertices
        RandomDataGenerator generator = new RandomDataGenerator();
        while (taken.size() < k) {
            int medoid = generator.nextInt(0, V-1); // return a random number in [0, V-1]
            // Only add to set if not already present
            if (!taken.contains(medoid)) {
                taken.add(medoid);
                clusterNumbers.add(medoid);
                Cluster<Integer> c = new Cluster<>(medoid);
                clusters.add(c);
            }
        }
        return clusters;
    }
    
    /**
     * Get cluster in which there are most communication between its medoid and given vertex.
     * 
     * @param medoids
     * @param u
     * @return cluster's number, ranging from 0 - k
     */
    private int getMostCommunicatedCluster(AdjacencyMatrixGraph graph, List<Cluster<Integer>> clusters, int u) {
        int maxComs = Integer.MIN_VALUE; // maximum communication between medoid and vertices 
        int num = -1; // cluster number
        int[] edges = graph.edges(u);
        for (int i = 0; i < clusters.size(); i++) { 
            Cluster<Integer> c = clusters.get(i);
            int v = c.getMedoid().getValue();
            if (edges[v] > maxComs) {
               maxComs = edges[v];
               num = i;
            }
        }
        return num;
    }
    
    /*
     * Iterate through list of data points in graph and assign to the medoid
     * with most communication
     */
    private int[] assignClusters(AdjacencyMatrixGraph graph, List<Cluster<Integer>> clusters) {
        // The mapping between the vertex and the cluster
        // For example, assignments[1] = 2 means the vertex 1 is assigned to cluster 2
        int[] assignments = new int[graph.V()];
        // Refresh clusters before re-assigning
        refreshClusters(clusters);
        for (int u = 0; u < graph.V(); u++) {
            // Get cluster number where vertex u should belong to
            if (!clusterNumbers.contains(u)) {
                int num = getMostCommunicatedCluster(graph, clusters, u);
                clusters.get(num).getDataPoints().add(u);
                assignments[u] = num;
            }
        }
        return assignments;
    }
    
    /**
     * Find the data point with highest connections with others
     * @param graph
     * @param cluster
     * @return new medoid, which has the most connections with other data points in the cluster
     */
    private int findMedoid(AdjacencyMatrixGraph graph, Cluster<Integer> cluster) {
        ArrayList<Integer> dataPoints = cluster.getDataPoints();
        int maxComs = cluster.getTotalComs();
        int medoid = cluster.getMedoid().getValue();
        for (int i = 0; i < dataPoints.size()-1; i++) {
            int totalComs = 0;
            int[] edges = graph.edges(dataPoints.get(i));
            for (int j = i+1; j < dataPoints.size(); j++) {
                totalComs += edges[dataPoints.get(j)];
            }
            // Keep old medoid if total of communication are equal
            if (totalComs > maxComs) {
                // Found new medoid
                medoid = dataPoints.get(i);
                maxComs = totalComs;
            }
        }
        // Update total communication to medoid
        cluster.setTotalComs(maxComs);
        return medoid;
    }
    
    /**
     * clear list of data points in clusters to prepare for re-assigning
     * @param clusters
     */
    private void refreshClusters(List<Cluster<Integer>> clusters) {
        if (clusters == null || clusters.size() <= 0) {
            return;
        }
        // clear all data points in the list
        for (Cluster<Integer> c : clusters) {
            c.getDataPoints().clear();
        }
    }
    
    /*
     * The k-medoid algorithm reaches converged when the distribution of data
     * points to clusters stay the same
     */
    public boolean isConverged(int[] oldDis, int[] newDis) {
        return Arrays.equals(oldDis, newDis);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        int[] a = {2,3,4,1};
        int[] b = {1,2,3,4};
        System.out.println(Arrays.equals(a, b));
    }
}
