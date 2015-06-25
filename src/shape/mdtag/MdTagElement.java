package shape.mdtag;

/**
 * One component of an MD tag.  The component comprises the operator, and the number of bases to which
 * the  operator applies.
 * 
 * @author Mason M Lai
 */
public class MdTagElement {
    private final int length;
    private final MdTagOperator operator;

    public MdTagElement(final int length, final MdTagOperator operator) {
        this.length = length;
        this.operator = operator;
    }
    
    public MdTagElement(final MdTagElement that) {
    	this.length = that.length;
    	this.operator = that.operator;
    }

    public int getLength() {
        return length;
    }

    public MdTagOperator getOperator() {
        return operator;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MdTagElement)) return false;

        final MdTagElement that = (MdTagElement) o;

        if (length != that.length) return false;
        if (operator != that.operator) return false;

        return true;
    }
    
    @Override
    public String toString() {
    	return String.valueOf(length) + operator.toString();
    }
}