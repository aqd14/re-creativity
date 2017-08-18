/**
 * 
 */
package org.re.scrape;

import org.re.utils.ExporterUtils;

/**
 * @author doquocanh-macbook
 *
 */
public class Scraper extends Thread {
    private Thread t;
    private String threadName;
    private BaseScraper scraper;
    private final int from;
    private final int to;
    /**
     * 
     */
    public Scraper(BaseScraper scraper, String threadName, int from, int to) {
        this.threadName = threadName;
        this.scraper = scraper;
        this.from = from;
        this.to = to;
    }
    
    public void run() {
        System.out.println("Running " +  threadName );
        int id = from;
        while(id < to) {
//            try {
                scraper.scrape(id);
//                Thread.sleep(1000);
                id++;
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        ExporterUtils.exportAll(scraper);
        System.out.println("Thread " +  threadName + " exiting.");
    }
    
    public void start() {
        System.out.println("Starting " +  threadName);
        t = new Thread(this, threadName);
        t.start();
//        try {
//            t.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // Run multi-threading web scraping
        int from = 220000;
        int to = 220100;
        int numThread = 1;
        int range = (to-from)/numThread; // 100
        for (int i = 1; i <= numThread; i++) {
            String title = "Firefox-" + from + "-" + String.valueOf(from+range-1);
            Scraper scraper = new Scraper(new FirefoxScraper(), title, from, from+range-1);
            scraper.start();
            from += range;
        }
        
        System.out.println("Threaddddddddddddddddddddd");
        
//        int[][] runs = {{210000, 220000}, {220000, 230000}, {240000, 250000}, {260000, 270000}, {270000, 280000}, {280000, 290000}};
//        
//        for (int i = 0; i < runs.length; i++) {
//            int from = runs[i][0];
//            int to = runs[i][1];
//            String title = "Mylyn-" + from + "-" + to;
//            Scraper mylynScraper = new Scraper(new MylynScraper(), title, from, to);
//            mylynScraper.start();
//        }
        
//        Scraper firefoxScraper = new Scraper(new FirefoxScraper(), "Firefox", from, to);
//        firefoxScraper.start();
//        Scraper mylynScraper = new Scraper(new MylynScraper(), "Mylyn", 200000, 200100);
//        mylynScraper.start();
    }
}
