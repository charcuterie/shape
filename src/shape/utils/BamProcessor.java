package shape.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarOperator;
import net.sf.samtools.util.CloseableIterator;
import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.MappedFragment;
import guttmanlab.core.annotation.PairedMappedFragment;
import guttmanlab.core.annotation.SAMFragment;
import guttmanlab.core.annotationcollection.AnnotationCollection;
import shape.mdtag.GenericOperator;
import shape.mdtag.MdTagOperator;
import shape.profiles.ChromosomeProfileCollection;
import shape.profiles.StrandProfile;

/**
 * @author Mason M. Lai
 */
public abstract class BamProcessor<T extends ChromosomeProfileCollection<? extends StrandProfile>> {

	protected int numExcludedBasesFromEnd;

	protected BamProcessor() {
		numExcludedBasesFromEnd = 0;
	}

	protected BamProcessor(int n) {
		numExcludedBasesFromEnd = n;
		if (n < 0) {
			throw new IllegalArgumentException("Number of bases excluded from edge of reads must be non-negative!");
		}
	}
	
	/**
	 * Iterates through the reads in an input .bam file, and populates the input profile collection with corresponding read data.
	 * 
	 * @param bam			Annotation collection, presumably from a .bam file.
	 * @param profiles		A collection of profiles to be populated from information from the .bam file.
	 * @throws IOException  Only accepts SAMFragments and PairedMappedFragments. Other AnnotationCollections not supported.
	 */

	@SuppressWarnings("unchecked")
	public void parseReads(AnnotationCollection<? extends Annotation> bam, T profiles) throws IOException {
		System.out.println("Parsing reads...");
		CloseableIterator<? extends Annotation> reads = bam.sortedIterator();
		while (reads.hasNext()) {
			Annotation read = reads.next();
			if (read instanceof SAMFragment) {
				if (!read.getReferenceName().equals("*")) {
					parseRead((SAMFragment)read, profiles, new HashSet<Integer>());
				}
			} else if (read instanceof PairedMappedFragment<?>) {
				parseRead((PairedMappedFragment<? extends MappedFragment>)read, profiles, new HashSet<Integer>());
			} else {
				throw new IOException("Mutation counter can only handle SAMFragments and PairedMappedFragments.");
			}
		}
		reads.close();
		System.out.println("Finished parsing reads.");		
	}

	protected void parseRead(PairedMappedFragment<? extends MappedFragment> readPair, T profiles, Set<Integer> visitedPositions) throws IOException {
		Annotation read1 = readPair.getRead1();
		Annotation read2 = readPair.getRead2();
		parseRead((SAMFragment)read1, profiles, visitedPositions);
		parseRead((SAMFragment)read2, profiles, visitedPositions);
	}
	
	/**
	 * Accepts a CIGAR operator and an MD tag operator, returning a more general operator containing the information of both.
	 * 
	 * @param c				a CIGAR string operator
	 * @param m				an MD tag operator
	 * @return				a more general operator with the combined functionality of a CIGAR operator and an MD tag operator.
	 * @throws IOException  if the CIGAR string operator and the MD tag operator contradict in some way
	 */
	protected GenericOperator detectMutation(CigarOperator c, MdTagOperator m) throws IOException {
		switch (CigarOperator.enumToCharacter(c)) {
		case 'M':
			return detectMutationAtCigarOpM(m);
		case 'I':
			return GenericOperator.INSERTION;
		case 'D':
			return detectMutationAtCigarOpD(m);
		case 'S':
			return GenericOperator.SOFT_CLIP;
		default:
			throw new IOException("Unknown CIGAR operator encountered. (Only recognizes M, I, S, and D.)");
		}
	}
	
	private GenericOperator detectMutationAtCigarOpM(MdTagOperator m) throws IOException {
		byte referenceBase = MdTagOperator.enumToCharacter(m);
		switch (referenceBase) {
		case '=':
			return GenericOperator.MATCH;
		case 'A':
			return GenericOperator.A_TO_N;
		case 'C':
			return GenericOperator.C_TO_N;
		case 'G':
			return GenericOperator.G_TO_N;
		case 'T':
			return GenericOperator.T_TO_N;
		case 'N':
			return GenericOperator.UNKNOWN;
		default:
			throw new IOException("CIGAR string and MD tag are not compatible. CIGAR operator is M and MD tag operator is " + m.toString() + ".");
		}
	}
	
	private GenericOperator detectMutationAtCigarOpD(MdTagOperator m) throws IOException {
		byte referenceBase = MdTagOperator.enumToCharacter(m);
		switch (referenceBase) {
		case 'D':
			return GenericOperator.DELETION;
		case 'a':
			return GenericOperator.DELETION_OF_A;
		case 'c':
			return GenericOperator.DELETION_OF_C;
		case 'g':
			return GenericOperator.DELETION_OF_G;
		case 't':
			return GenericOperator.DELETION_OF_T;
		default:
			throw new IOException("CIGAR string and MD tag are not compatible. CIGAR operator is D and MD tag operator is " + m.toString() + ".");
		}
	}
	
	protected abstract void parseRead(SAMFragment read, T profiles, Set<Integer> visitedPositions) throws IOException;
}