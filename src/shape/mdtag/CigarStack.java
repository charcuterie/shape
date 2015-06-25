package shape.mdtag;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;

/**
 * Extension of the {@link guttmanlab.core.mason.Stack} class for building or
 * manipulating CIGARs. As elements are added, they are "combined", e.g.,
 * pushing 3M onto 3M1D2M results in 3M1D5M rather than 3M1D2M3M.
 * 
 * @author Mason M Lai
 */
public class CigarStack extends Stack<CigarElement> {

    public CigarStack() {
    	super();
    }
    
    /**
     * Calls the Stack constructor with cigar.getCigarElements(). This is an
     * unmodifiable list. Furthermore, the CigarElements are immutable, as
     * their member variables are final.
     *  
     * @param cigar CIGAR to push onto the stack
     */
    public CigarStack(Cigar cigar) {
    	super(cigar.getCigarElements());
    }

    /**
     * Returns the operator of the top CigarElement of the Cigar stack.
     * <p>
     * If the stack is 3M 1I 2M, pop() returns M and the stack becomes 2M 1I 2M.
     * If the stack is 1D 2M 1I, pop() returns D and the stack becomes 2M 1I.
     * <p>
     * @return The CigarOperator from the top CigarElement.
     */
    public CigarOperator popOperator() {
    	if (elements.peek() != null) {
    		CigarElement firstElement = elements.pop();
    		int firstElementLength = firstElement.getLength();
    		CigarOperator firstElementOperator = firstElement.getOperator();
    		if (firstElementLength > 1) {
    			// Cigar elements in samtools have final member variables. We can't simply decrement the length.
    			elements.push(new CigarElement(firstElementLength - 1, firstElementOperator));
    		}
    		return firstElementOperator;
    	}
		return null;
    }

    /**
     * Pushes a CigarOperator onto the stack.
     * <p>
     * If the stack is 3M 1I 2M (where 3M is the top element), after pushing M,
     * the stack is 4M 1I 2M.
     * 
     * @param op the CigarOperator to be pushed onto the stack
     * 
     */
    public void pushOperator(CigarOperator op) {
    	if (elements.peek() != null && elements.peek().getOperator().equals(op)) {
    		CigarElement firstElement = elements.pop();
    		elements.push(new CigarElement(firstElement.getLength() + 1, op));
    	} else {
    		elements.push(new CigarElement(1, op));
    	}
    }
}