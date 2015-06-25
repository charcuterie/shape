package shape.mdtag;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Basic implementation of a generic stack.
 * 
 * @author Mason M Lai
 *
 * @param <E> - type of object contained by the stack 
 */
public class Stack<E> {

    protected ArrayDeque<E> elements;
    
    protected Stack() {
    	elements = new ArrayDeque<E>();
    }
    
    protected Stack(ArrayDeque<E> e) {
    	elements = new ArrayDeque<E>(e);
    }
    
    protected Stack(List<E> list) {
    	elements = new ArrayDeque<E>();
    	for (E e: list) {
    		elements.addLast(e);
    	}
    }
    
    public int getSize() {
    	return elements.size();
    }
    
    /**
     * Removes the top element of the stack and returns it.
     * 
     * @return - the top element of the stack
     */
    public E pop() {
    	return elements.pop();
    }
    
    /**
     * Pushes the passed argument onto the top of the stack.
     * 
     * @param e - the object to be pushed onto the stack
     */
    public void push(E e) {
    	elements.push(e);
    }
    
    /**
     * Returns, but does not remove, the top element of the stack.
     * 
     * @return - the top element of the stack
     */
    public E peek() {
    	return elements.peek();
    }

    /**
     * Returns the bottom element of the stack. Technically,  stack shouldn't
     * have this method, but it comes in handy on occasion.
     * 
     * @return - the bottom element of the stack.
     */
    public E poop() {
    	return elements.removeLast();
    }
    
    public boolean isEmpty() {
    	return elements.isEmpty();
    }
    
    public boolean hasElements() {
    	return !elements.isEmpty();
    }
    
    /**
     * Reverses the elements of the stack. The top element becomes the bottom,
     * and so on.
     */
    public void reverse() {
    	ArrayDeque<E> revElements = new ArrayDeque<E>();
    	while (!elements.isEmpty()) {
    		revElements.push(elements.pop());
    	}
    	elements = revElements;
    }
    
    @Override
    public String toString() {
    	return "Stack: " + elements.toString();
    }
}