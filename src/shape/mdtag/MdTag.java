package shape.mdtag;

import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.PairedMappedFragment;
import guttmanlab.core.annotation.SAMFragment;
import guttmanlab.core.annotationcollection.BAMPairedFragmentCollection;
import guttmanlab.core.annotationcollection.BAMSingleReadCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.samtools.util.CloseableIterator;


/**
 * MdTag is an object which represents an MD tag as an ArrayList of Elements. It is analogous to the Cigar class
 * the samtools package. Matches have a length equal to the number of nucleotides in the run. Deletions and SNPs
 * always have a length of 1. A 3-bp deletion is represented by three individual deletion Elements.
 */
public class MdTag {
    private List<MdTagElement> mdTagElements = new ArrayList<MdTagElement>();

    /**
     *  Default MdTag constructor
     */
    public MdTag() {
    }
    
    /**
     * MdTag constructor, creates tag from its String representation
     * @param mdTagString MD tag as a String
     */
    public MdTag(String mdTagString) {
    	String[] splitMdTag = mdTagString.split("(?=\\^)|(?<=[A-Za-z])|(?=[A-Za-z])");

/*		for (String s : splitMdTag) {
    		System.out.print(s + " ");
    	}

    	System.out.print("\n");
*/    	boolean markAsDeletions = false;
    	try {
    		for (String e : splitMdTag) {
    			if (e.matches("[0-9]+")) {
    				MdTagOperator operator = MdTagOperator.MATCH;
    				int length = Integer.parseInt(e);
    				mdTagElements.add(new MdTagElement(length, operator));
    				markAsDeletions = false;
    			} else {
    				MdTagOperator operator = null;
    				switch(e.charAt(0)) {
    				case '^':
    					markAsDeletions = true;
    					break;
    				case 'A':
    					operator = markAsDeletions ? MdTagOperator.DELETION_OF_A : MdTagOperator.MISMATCH_FROM_A;
    					addElement(operator);
    					break;
    				case 'C':
    					operator = markAsDeletions ? MdTagOperator.DELETION_OF_C : MdTagOperator.MISMATCH_FROM_C;
    					addElement(operator);
       					break;
    				case 'G':
    					operator = markAsDeletions ? MdTagOperator.DELETION_OF_G : MdTagOperator.MISMATCH_FROM_G;
    					addElement(operator);
    					break;
    				case 'T':
    					operator = markAsDeletions ? MdTagOperator.DELETION_OF_T : MdTagOperator.MISMATCH_FROM_T;
    					addElement(operator);
    					break;
    				case 'N':
    					operator = markAsDeletions ? MdTagOperator.DELETION : MdTagOperator.IGNORE;
       					addElement(operator);
    					break;
    				case 'X':
    					operator = markAsDeletions ? MdTagOperator.DELETION : MdTagOperator.MISMATCH;
       					addElement(operator);
    					break;
    				default:
    					throw new IllegalArgumentException("Invalid MD Tag");
    				}
    			}
    		}
    	} catch (IllegalArgumentException ex) {
    		System.err.println("Exception: String representing an invalid MD tag passed to MdTag constructor.");
    	}
    	((ArrayList<MdTagElement>)mdTagElements).trimToSize(); // not sure if this is necessary
    }
    
    /**
     * Copy constructor
     * @param that MdTag to be copied
     */
    public MdTag(MdTag that) {
    	List<MdTagElement> elemList = that.getMdTagElements();
    	for (MdTagElement e : elemList) {
    		this.mdTagElements.add(new MdTagElement(e));
    	}
    }
    
	public final List<MdTagElement> getMdTagElements() {
        return Collections.unmodifiableList(mdTagElements);
    }

    public final MdTagElement getMdTagElement(final int i) {
        return mdTagElements.get(i);
    }

    public final void add(final MdTagElement mdTagElement) {
        mdTagElements.add(mdTagElement);
    }

    public final int numMdTagElements() {
        return mdTagElements.size();
    }

    public final boolean isEmpty() {
        return mdTagElements.isEmpty();
    }

    /**
     * @return The number of reference bases that the read covers, excluding padding.
     */
    public final int getReferenceLength() {
        int length = 0;
        for (final MdTagElement element : mdTagElements) {
            switch (element.getOperator()) {
                case D:
                case XA:
                case XC:
                case XG:
                case XT:
                case XN:	
                case EQ:
                    length += element.getLength();
            }
        }
        return length;
    }

    /**
     * @return The number of read bases that the read covers.
     */
    public final int getReadLength() {
        return getReadLength(mdTagElements);
    }

    /**
     * @return The number of read bases that the read covers.
     */
    public int getReadLength(final List<MdTagElement> mdTagElements) {
        int length = 0;
        for (final MdTagElement element : mdTagElements) {
            if (element.getOperator().consumesReadBases()){
                    length += element.getLength();
            }
        }
        return length;
    }
    
    @Override
    public String toString() {
    	// TODO: Write this. 
    	throw new UnsupportedOperationException("MdTag does not have a valid toString() method yet.");
    }
    
    public static boolean areAllPresentIn(BAMSingleReadCollection bam) {
    	CloseableIterator<? extends Annotation> reads = bam.sortedIterator();
    	while (reads.hasNext()) {
    		Annotation read = reads.next();
    		if (!MdTag.isPresentIn(read)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static boolean areAllPresentIn(BAMPairedFragmentCollection bam) {
    	CloseableIterator<PairedMappedFragment<SAMFragment>> readPairs = bam.sortedIterator();
    	while (readPairs.hasNext()) {
    		PairedMappedFragment<SAMFragment> readPair = readPairs.next();
    		Annotation read1 = readPair.getRead1();
    		if (!MdTag.isPresentIn(read1)) {
    			return false;
    		}
    		Annotation read2 = readPair.getRead2();
    		if (!MdTag.isPresentIn(read2)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static boolean isPresentIn(Annotation read) {
    	String stringMdTag = ((SAMFragment)read).getStringTag("MD");
    	return !( stringMdTag == null || stringMdTag.isEmpty() );
    }
    
    private boolean matchesLastOperator(MdTagOperator op) {
    	if (mdTagElements.isEmpty()) {
    		return false;
    	} else {
    		MdTagElement lastElement = mdTagElements.get(mdTagElements.size() - 1);
    		return lastElement.getOperator().equals(op);
    	}
    }
    
    private void addElement(MdTagOperator operator) {
		if (matchesLastOperator(operator)) {
			MdTagElement elem = mdTagElements.remove(mdTagElements.size() - 1);
			mdTagElements.add(new MdTagElement(elem.getLength() + 1, operator));
		} else {
			mdTagElements.add(new MdTagElement(1, operator));
		}
    }
}