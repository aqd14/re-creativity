package org.re.scrape.model;

import java.io.InputStream;



/**
 * <p>
 * A class represents for a stakeholders' weighted graph,
 * G(V, E) where V is a set of vertices and E is a set of edges.
 * <li> V: stakeholders' names (but for simplicity, using integer index instead.
 * Also, using HashTable to map from stakeholdder's name to the vertex </li>
 * <li> E: stakeholders' number of interaction (weights) </li>
 * </p>
 * @author Anh Quoc Do
 */
public class StakeholderGraph {
    // Use adjacency list to represent graph
    private int V;  // Number of vertices
    private int E;  // Number of edges
    private AdjacencyList<Integer>[] adj;
    /**
     * Create a V-vertex graph without edges
     */
    @SuppressWarnings("unchecked")
    public StakeholderGraph(int V) {
        if (V < 0) {
            throw new IllegalArgumentException("The initialized number of vertex should be greater than 0!");
        }
        this.V = V;
        this.E = 0;
        adj = (AdjacencyList<Integer>[]) new AdjacencyList[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new AdjacencyList<>();
        }
    }
    
    /**
     * Create a graph from a input stream
     * @param in
     */
    public StakeholderGraph(InputStream in) {
        
    }
    
    /**
     * @return number of vertices
     */
    public int V() {
        return V;
    }
    
    /**
     * @return number of edges
     */
    public int E() {
        return E;
    }
    
    /**
     * Add new edge to graph
     * @param e edge to be added
     */
    public void addEdge(Edge e) {
        int u = e.either();
        int v = e.other(u);
        validateVertex(u);
        validateVertex(v);
        adj[u].add(v);
        adj[v].add(u);
        E++;
    }
    
    public void addEdge(int u, int v, int weight) {
        Edge e = new Edge(u, v, weight);
        addEdge(e);
    }
    
    /**
     * Get all adjacent vertices with given vertex
     * @param v given vertex
     * @return  all adjacent vertices
     */
    public Iterable<Integer> adj(int v) {
        validateVertex(v);
        return adj[v];
    }
    
    private void validateVertex(int v) {
        if (v < 0 || v >= V) {
            throw new IllegalArgumentException("Vertex should be between 0 and " + (V-1));
        }
    }
}
