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
public class AdjacencyListGraph extends BaseGraph {
    // Use adjacency list to represent graph
    private int V;  // Number of vertices
    private int E;  // Number of edges
    private AdjacencyList<Edge>[] adj;  // Store incident edges as an adjacency list
    
    /**
     * Create a V-vertex graph without edges
     */
    @SuppressWarnings("unchecked")
    public AdjacencyListGraph(int V) {
        if (V < 0) {
            throw new IllegalArgumentException("The initialized number of vertex should be greater than 0!");
        }
        this.V = V;
        this.E = 0;
        adj = (AdjacencyList<Edge>[]) new AdjacencyList[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new AdjacencyList<>();
        }
    }
    
    /**
     * Create a graph from an input stream
     * @param in
     */
    public AdjacencyListGraph(InputStream in) {
        
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
    @Override
    public void addEdge(Edge e) {
        int u = e.either();
        int v = e.other(u);
        validateVertex(u);
        validateVertex(v);
        // Check if the edge already exist.
        // If not, add new edge to the adjacency lists of both vertices.
        // Otherwise, update weight of existing edge
        Edge found = adj[u].find(e);
        if (found == null) {
            adj[u].add(e);
            adj[v].add(e);
            E++;
        } else {
            found.setWeight(found.getWeight() + e.getWeight());
        }
    }
    
    /**
     * Get all comming from with given vertex
     * @param v given vertex
     * @return  all adjacent vertices
     */
    public Iterable<Edge> adj(int v) {
        validateVertex(v);
        return adj[v];
    }
    
    /**
     * @return all edges in the graph
     */
    public Iterable<Edge> edges() {
        AdjacencyList<Edge> edges = new AdjacencyList<>();
        for (int v = 0; v < V; v++) {
            for (Edge e : adj[v]) {
                if (e.other(v) > v) { // Avoid getting duplicate edges
                    edges.add(e);
                }
            }
        }
        return edges;
    }
    
    private void validateVertex(int v) {
        if (v < 0 || v >= V) {
            throw new IllegalArgumentException("Vertex should be between 0 and " + (V-1));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("V = ").append(V).append("\n");
        sb.append("E = ").append(E).append("\n");
        AdjacencyList<Edge> edges = (AdjacencyList<Edge>) this.edges();
        for (Edge e : edges) {
            sb.append(e);
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        AdjacencyListGraph g = new AdjacencyListGraph(2);
        Edge e1 = new Edge(0,1, 10);
        Edge e2 = new Edge(1,0, 5);
        g.addEdge(e1);
        g.addEdge(e2);
        System.out.println(g);
    }
}
