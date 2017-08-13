package org.re.utils.cluster;

import java.util.ArrayList;

/**
 * Clustering stakeholders' social network beyond their interaction on issue tracking system.
 * Each cluster contain a centroid and a list of associated vertices.
 * 
 * @author Anh Quoc Do
 * @version 1.0
 */
public class Cluster<T> {
    // Medoid of the cluster. This is excluded in data points list
    private Medoid<T> medoid;
    // List of data points in the cluster
    private ArrayList<T> dataPoints;
    // total communications made between medoid and its data points in the cluster
    private int totalComs;
    
    public Cluster(Medoid<T> medoid) {
        this.medoid = medoid;
        dataPoints = new ArrayList<T>();
        totalComs = 0;
    }
    
    public Cluster(T value) {
        this(new Medoid<T>(value));
    }
    
    /**
     * @return the medoid
     */
    public Medoid<T> getMedoid() {
        return medoid;
    }
    /**
     * @param centroid the medoid to set
     */
    public void setMedoid(Medoid<T> medoid) {
        this.medoid = medoid;
    }
    /**
     * @return the data points in the cluster
     */
    public ArrayList<T> getDataPoints() {
        return dataPoints;
    }
    /**
     * @param the data points to set
     */
    public void setDataPoints(ArrayList<T> dataPoints) {
        this.dataPoints = dataPoints;
    }
    
    public void setTotalComs(int totalComs) {
        this.totalComs = totalComs;
    }
    
    public int getTotalComs() {
        return totalComs;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(medoid);
        sb.append(dataPoints.get(0));
        for (int i = 1; i < dataPoints.size(); i++) {
            sb.append("-").append(dataPoints.get(i));
        }
        sb.append("\n").append("Max communication: ").append(totalComs).append("\n");
        return sb.toString();
    }
}
