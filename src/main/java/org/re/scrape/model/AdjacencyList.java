/**
 * 
 */
package org.re.scrape.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author doquocanh-macbook
 *
 */
public class AdjacencyList<E> implements Iterable<E> {
    private Node<E> first;
    private int size;
    /**
     * 
     */
    public AdjacencyList() {
        first = null;
        size = 0;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Add item into the list
     * @param item
     */
    public void add(E item) {
        Node<E> oldFirst = first;
        first = new Node<E>();
        first.item = item;
        first.next = oldFirst;
        size++;
    }
    
    private static class Node<E> {
        private E item;
        private Node<E> next;
    }

    @Override
    public Iterator<E> iterator() {
        return new AdjacencyListIterator<>(first);
    }
    
    /**
     * Implement {@link Iterator} interface to support traversing adjacency list
     * 
     * @author Anh Quoc Do
     *
     * @param <E>
     */
    private static class AdjacencyListIterator<E> implements Iterator<E> {
        private Node<E> cur;
        public AdjacencyListIterator(Node<E> node) {
            cur = node;
        }
        @Override
        public boolean hasNext() {
            // TODO Auto-generated method stub
            return cur != null;
        }

        @Override
        public E next() {
            if (hasNext()) {
                E item = cur.item;
                cur = cur.next;
                return item;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {

    }
}
