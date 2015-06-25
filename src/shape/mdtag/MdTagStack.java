package shape.mdtag;

/*
 * Parser for MutationCounter. Takes an MdTag or a List<MdTagElement>, loads the elements onto a stack,
 * and pops them off one at a time, e.g., 2^AT3 -> 1^AT3 -> ^AT3 -> ^T3 -> ...
 */

public class MdTagStack extends Stack<MdTagElement> {

    public MdTagStack() {
    	super();
    }
    
    public MdTagStack(MdTag mdTag) {
    	super(mdTag.getMdTagElements());
    }

    public MdTagOperator popOperator() {
    	if (elements.peek() != null) {
    		MdTagElement firstElement = elements.pop();
    		// Sometimes there are things of length 0 in MD tags. If we hit one, pop the next element and return it.
    		int firstElementLength = firstElement.getLength();
    		if (firstElementLength == 0) {
    			return this.popOperator();
    		}
    		MdTagOperator firstElementOperator = firstElement.getOperator();
    		if (firstElementLength > 1) {
    			// Mirrors the CigarElement class in samtools. Elements are final, and we can't simply decrement them.
    			elements.push(new MdTagElement(firstElementLength - 1, firstElementOperator));
    		}
    		return firstElementOperator;
    	}
		return null;
    }
    
    public void pushOperator(MdTagOperator o) {
    	if (elements.peek() == null || !elements.peek().getOperator().equals(o)) {
    		elements.push(new MdTagElement(1, o));
    	} else {
    		MdTagElement oldElement = elements.pop();
    		elements.push(new MdTagElement(oldElement.getLength() + 1, o)); // MdTagElements are final, so can't directly change with setter.
    	}
    }
    
    @Override
    public void push(MdTagElement e) {
    	if (elements.peek() == null || !elements.peek().getOperator().equals(e.getOperator())) {
    		elements.push(e);
    	} else {
    		MdTagElement oldElement = elements.pop();
    		elements.push(new MdTagElement(oldElement.getLength() + e.getLength(), e.getOperator()));
    	}
    }
}