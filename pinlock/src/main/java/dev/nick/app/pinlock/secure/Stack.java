package dev.nick.app.pinlock.secure;


import java.util.LinkedList;

public class Stack<T> {
    private LinkedList<T> storage = new LinkedList<T>();

    /**
     * add to the stack.
     */
    public void push(T v) {
        storage.addFirst(v);
    }

    /**
     * pop but keep.
     */
    public T peek() {
        if (isEmpty())
            return null;
        return storage.getFirst();
    }

    /**
     * pop and delete.
     */
    public T pop() {
        return storage.removeFirst();
    }

    /**
     * true if the stack is empty.
     */
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    /**
     * True if removed.
     */
    public boolean remove(T v) {
        return storage.remove(v);
    }

    public String toString() {
        return storage.toString();
    }
}
