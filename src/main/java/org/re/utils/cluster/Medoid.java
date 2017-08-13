/**
 * 
 */
package org.re.utils.cluster;

/**
 * A value of a finite dataset is a data point from this set, whose average
 * dissimilarity to all the data points is minimal i.e. it is the most centrally
 * located point in the set. In this application, value means the stakeholder
 * who has the most communications with others
 * 
 * @author Anh Quoc Do
 *
 */
public class Medoid<T> {
    // Medoid of cluster
    private T value;
    
    /**
     * 
     */
    public Medoid(T value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * @param the value to set
     */
    public void setValue(T value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        StringBuilder bd = new StringBuilder();
        bd.append("Medoid: ").append(value).append("\n");
        return bd.toString();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }
}
