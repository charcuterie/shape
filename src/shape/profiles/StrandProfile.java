package shape.profiles;

import guttmanlab.core.annotation.Annotation.Strand;

/**
 * Represents one of two strands of a chromosome, coupled with some data. The
 * nature of the data depends on the subclass.
 * 
 * @author Mason M Lai
 */
public abstract class StrandProfile {

	protected final String chromosome;
	protected final Strand orientation;
	protected final int length;
	
	protected StrandProfile(int length, String chromosome, Strand orientation) {
		if (orientation.equals(Strand.POSITIVE) || orientation.equals(Strand.NEGATIVE)) {
			this.orientation = orientation;
		} else {
			throw new IllegalArgumentException("StrandProfile constructed with invalid orientation. Must be Strand.POSITIVE or Strand.NEGATIVE.");
		}

		this.chromosome = chromosome;

		if (length > 0) {
			this.length = length;
		} else {
			throw new IllegalArgumentException("Non-positive value passed to StrandProfile constructor. Chromosome lengths must be positive.");
		}
	}
	
	/**
	 * Gets length of strand in base-pairs
	 * @return length - length of strand in base-pairs
	 */
	public final int getLength() {
		return length;
	}
	
	/**
	 * Gets chromosome name.
	 * @return name of chromosome
	 */
	public final String getChromosomeName() {
		return chromosome;
	}

	/**
	 * Tests if strandedness is positive.
	 * @return true if strandedness is positive, else false
	 */
	public final boolean isPositive() {
		if (orientation.equals(Strand.POSITIVE)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Tests if strandedness is negative.
	 * @return true if strandedness is negative, else false
	 */
	public final boolean isNegative() {
		if (orientation.equals(Strand.NEGATIVE)) {
			return true;
		} else {
			return false;
		}
	}
}