/**
 * 
 */
package org.re.scrape.model;

/**
 * @author doquocanh-macbook
 */
public class Edge implements Comparable<Edge> {
    private int u;  // Source vertex
    private int v;  // Destination vertex
    private int weight; // Edge's weight
    public Edge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.setWeight(weight);
    }
    
    public int either() {
        return u;
    }
    
    public int other(int vertex) {
        if (vertex == u) {
            return v; 
        } else if (vertex == v) {
            return u;
        } else {
            throw new RuntimeException("Inconsistent edge!");
        }
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge that) {
        if (this.weight > that.weight) {
            return 1;
        } else if (this.weight < that.weight) {
            return -1;
        } else {
            return 0;
        }
    }
    
    public String toString() {  
        return String.format("%d-%d %.2f", u, v, weight);  
    }
}
