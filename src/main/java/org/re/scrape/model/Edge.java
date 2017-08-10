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
    private int weight; // Edge's weight: Number of communication between stakeholders
    public Edge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.setWeight(weight);
    }
    
    public int either() {
        return u;
    }
    
    /**
     * Get another vertex in the edge
     * @param vertex
     * @return  another end point of the edge
     */
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
    
    @Override
    public boolean equals(Object y) {
        if (this == y) {
            return true;
        }
        if (y == null) {
            return false;
        } 
        if (this.getClass() != y.getClass()) {
            return false;
        }
        // An edge is equal if they contains same two vertices
        // The edge's weight will be updated
        Edge that = (Edge) y;
        int eitherThis = this.either();
        int eitherThat = that.either();
        int otherThis = this.other(eitherThis);
        int otherThat = that.other(eitherThat);
        return (eitherThis == eitherThat && otherThis == otherThat) || 
                (eitherThis == otherThat && otherThis == eitherThat);
    }
    
    public String toString() {  
        return String.format("%d-%d %5d\n", u, v, weight);  
    }
}
