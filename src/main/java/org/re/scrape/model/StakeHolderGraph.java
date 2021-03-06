package org.re.scrape.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.re.utils.cluster.Cluster;
import org.re.utils.cluster.KMedoids;

/**
 * String-represented graph, which allows mapping from graph with indices
 * ({@link AdjacencyListGraph}) to String representation of vertices. Each vertex is a
 * stakeholder in OSS project and edge's weight is number of communication
 * between two stakeholders.
 * 
 * @author Anh Quoc Do
 *
 */
public class StakeHolderGraph {
    // String -> Index
    private LinkedHashMap<String, Integer> maps;
    // Index -> String
    private String[] stakeholders;
    // AdjacencyListGraph
    private AdjacencyMatrixGraph g;
    
    static final Logger logger = Logger.getLogger(StakeHolderGraph.class);
    
    private final String STAKEHOLDERS_DELIMITER = "\\ / |\\:\\d+ / |\\:\\d+";
    private final String EDGES_DELIMITER = "\\ / ";
    
    /**
     * File format: A / B:5 / C:3 / D:6. That means there are 3 edges communicate with
     * stakeholder A, which are B, C, D with weights of 5, 3, 6 respectively
     * @throws FileNotFoundException 
     */
    public StakeHolderGraph(String file) throws FileNotFoundException {
        maps = new LinkedHashMap<>();
        Scanner scanner = new Scanner(new File(file));
        
        String[] temp;
        while (scanner.hasNextLine()) {
            temp = scanner.nextLine().split(STAKEHOLDERS_DELIMITER);
            for (String sh : temp) {
                if (!maps.containsKey(sh)) {
                    maps.put(sh, maps.size());
                    logger.info("Put stakeholder: " + sh + " at position: " + maps.size());
                }
            }
        }
        scanner.close();
        // Initialize keys (stakeholder list)
        stakeholders = new String[maps.size()];
        int counter = 0;
        for (String sh : maps.keySet()) {
            stakeholders[counter++] = sh;
        }
        // Initialize graph with numeric vertices
        initGraph(file);
    }
    
    public boolean contains(String s) {
        return maps.containsKey(s);
    }
   
    /*
     * Get index given stakeholder's name
     */
    public int index(String s) {
        return maps.get(s);
    }
    
    /**
     * Get stakeholder's name given index
     * @param v
     * @return stakeholder's name
     */
    public String name(int v) {
        return stakeholders[v];
    }
    
    public AdjacencyMatrixGraph G() {
        return g;
    }
    
    /*
     * Initialize graph with input reading from file
     */
    private void initGraph(String file) throws FileNotFoundException {
        g = new AdjacencyMatrixGraph(maps.size());
        Scanner scanner = new Scanner(new File(file));
        
        String[] stakeholders;
        String source, name;
        int u, v, separatorIndex, weight;
        while (scanner.hasNextLine()) {
            stakeholders = scanner.nextLine().split(EDGES_DELIMITER);
            // Source vertex
            source = stakeholders[0];
            u = maps.get(source);
            for (int i = 1; i < stakeholders.length; i++) {
                // Each string contain destination vertex and corresponding weight
                separatorIndex = stakeholders[i].lastIndexOf(":");
                name = stakeholders[i].substring(0, separatorIndex);
                v = maps.get(name);
                weight = Integer.parseInt(stakeholders[i].substring(separatorIndex+1, stakeholders[i].length()));
                g.addEdge(new Edge(u, v, weight));
            }
        }
        scanner.close();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            StakeHolderGraph shGraph = new StakeHolderGraph("FIREFOX-communication.txt");
            System.out.println(shGraph.G());
            KMedoids<AdjacencyMatrixGraph> kmedoids = new KMedoids<>(3, 10);
            ArrayList<Cluster<Integer>> clusters = kmedoids.cluster((AdjacencyMatrixGraph)shGraph.G());
            for (Cluster<Integer> cluster : clusters)
                System.out.println(cluster);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
