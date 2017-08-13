/**
 * 
 */
package org.re.scrape.model;

/**
 * @author doquocanh-macbook
 *
 */
public class AdjacencyMatrixGraph extends BaseGraph {
    private int[][] edges; // adjacency matrix
    private int V;
    private int E;
    /**
     * 
     */
    public AdjacencyMatrixGraph(int V) {
        this.V = V;
        edges = new int[V][V];
    }
    
    @Override
    public void addEdge(Edge e) {
        int u = e.either();
        int v = e.other(u);
        edges[u][v] += e.getWeight();
        edges[v][u] += e.getWeight();
    }
    
    public int V() {
        return V;
    }
    
    public int E() {
        return E;
    }
    
    public int[] edges(int v) {
        return edges[v];
    }
    
    public String toString() {
        StringBuilder bd = new StringBuilder();
        for (int row = 0; row < V; row++) {
            bd.append(edges[row][0]);
            for (int col = 1; col < V; col++) {
                bd.append(" ").append(edges[row][col]);
            }
            bd.append("\n");
        }
        return bd.toString();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {

    }

}
