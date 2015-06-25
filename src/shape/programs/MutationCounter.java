package shape.programs;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import shape.mdtag.CigarStack;
import shape.mdtag.GenericOperator;
import shape.mdtag.MdTag;
import shape.mdtag.MdTagOperator;
import shape.mdtag.MdTagStack;
import shape.profiles.MutationProfileCollection;
import shape.utils.BamProcessor;
import shape.utils.Nucleotide;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.util.CloseableIterator;

import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.Annotation.Strand;
import guttmanlab.core.annotation.MappedFragment;
import guttmanlab.core.annotation.PairedMappedFragment;
import guttmanlab.core.annotation.SAMFragment;
import guttmanlab.core.annotationcollection.AnnotationCollection;
import guttmanlab.core.annotationcollection.BAMFragmentCollectionFactory;
import guttmanlab.core.util.CommandLineParser;

class MutationCounterTest {
	public static void main(String[] args) throws IOException {

		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-i", "Bam input file", true);
		p.addStringArg("-o", "Output directory", true);
		p.addIntArg("-n", "Number of bases to be ignored from each end of each read. Defaults to 0.", false, 0);
		p.addIntArg("-t", "Coverage threshold. Positions with a number of reads less than this " +
				"number are not reported. Defaults to 1, i.e., excludes positions with no " +
				"coverage.", false, 1);
		p.parse(args);
		
		String inputFile = p.getStringArg("-i");
		String outputDir = p.getStringArg("-o");
		int excludedBasesFromEnd = p.getIntArg("-n");
		int coverageThreshold = p.getIntArg("-t");
		
		AnnotationCollection<? extends MappedFragment> bam = BAMFragmentCollectionFactory.createFromBam(inputFile);

		MutationProfileCollection mutationProfiles = new MutationProfileCollection(bam, coverageThreshold);
		MutationCounter mutationCounter = new MutationCounter(excludedBasesFromEnd);
		new MutationCounter(excludedBasesFromEnd);
		mutationCounter.parseReads(bam, mutationProfiles);
		mutationProfiles.toFile(outputDir + bam.toString());
		//mutationCounter.writeMutationsToFile(bam, new File(outputDir + bam.toString() + "_readMutations.txt"));
		System.out.println("Program complete.");
	}
	
	static protected class MutationCounter extends BamProcessor<MutationProfileCollection> {

		protected MutationCounter() {
			super();
		}

		protected MutationCounter(int n) {
			super(n);
		}
		
		/**
		 * Counts the mutations in an individual read and updates the counts.
		 * 
		 * @param read the read to be parsed
		 * @param the collection of mutation data to be updated
		 * @param a set of visited positions in reference coordinates. These positions are skipped to avoid double-counting.
		 * @throws IOException if any reads do not have an MD tag
		 * @throws IOException if any reads do not have a CIGAR string
		 */
		protected final void parseRead(SAMFragment read, MutationProfileCollection mutationProfiles, Set<Integer> visitedPositions) throws IOException {
			String referenceName = read.getReferenceName();
			int referencePosition = read.getReferenceStartPosition();
			int referenceEndPosition = read.getReferenceEndPosition();
			int readPosition = 0;
			Strand orientation = read.getOrientation();
				
			for (int i = numExcludedBasesFromEnd; i > 0; i--) {
				visitedPositions.add(referencePosition + i - 1);
				visitedPositions.add(referenceEndPosition - i + 1);
			}
				
			String mdTagString = ((SAMFragment)read).getStringTag("MD");
			if (mdTagString == null || mdTagString.isEmpty()) {
				throw new IOException("Read " + read.getName() + " does not have an MD tag.");
			}
			MdTag mdTag = new MdTag(mdTagString);
			MdTagStack mdTagStack = new MdTagStack(mdTag);
			
			Cigar cigar = ((SAMFragment)read).getSamRecord().getCigar();
			if (cigar == null) {
				throw new IOException("Read " + read.getName() + " does not have a CIGAR string.");
			}
			CigarStack cigarStack = new CigarStack(cigar);
			
			CigarOperator currentCigarOperator = null;
			MdTagOperator currentMdTagOperator = null;
			
			while (cigarStack.hasElements()) {
				currentCigarOperator = cigarStack.popOperator();
				
				/* MD tags ignore insertions and soft clipping. */
				if (!currentCigarOperator.equals(CigarOperator.INSERTION) && 
						!currentCigarOperator.equals(CigarOperator.SOFT_CLIP)) {
					currentMdTagOperator = mdTagStack.popOperator();
				}
				
				boolean inReferenceBounds = (referencePosition >= 0) && 
						(referencePosition < mutationProfiles.getLength(referenceName));
				if (inReferenceBounds && !visitedPositions.contains(referencePosition)) {
					
					// If the read starts with soft-clipping, this method will be called with a
					// null MdTagOperator. This is OK, because the CigarOperator will be SOFT_CLIP,
					// and the MdTagOperator will not be inspected.
					GenericOperator op = detectMutation(currentCigarOperator, currentMdTagOperator);
					switch (op) {
					case MATCH:
					case UNKNOWN:
						// Unknown operators likely correspond to 'N's in the MD tag. 
						// Count them as matches.
						mutationProfiles.addMatch(referenceName, referencePosition, orientation);
						break;
					case SOFT_CLIP:
						// Ignore soft-clipped positions.
						break;
					case DELETION:
					case DELETION_OF_A:
					case DELETION_OF_C:
					case DELETION_OF_G:
					case DELETION_OF_T:
						mutationProfiles.addDeletion(referenceName, referencePosition, 
								orientation);
						break;
					case UNKNOWN_MISMATCH:
					case A_TO_N:
					case C_TO_N:
					case G_TO_N:
					case T_TO_N:
						// Get the read base that we've mutated to.
						String readBase = ((SAMFragment)read).getSamRecord().getReadString().
								substring(readPosition, readPosition + 1);
						// Ignoring uncertain bases in reads for now.
						// Only recognizing 'A', 'C', 'G', 'T'.
						if (readBase.charAt(0) != 'N') {
							mutationProfiles.addMutation(referenceName,
									Nucleotide.valueOf(readBase), referencePosition, orientation);
						}
						break;
					case INSERTION:
						mutationProfiles.addInsertion(referenceName, referencePosition, orientation);
						break;
					default:
						throw new IOException("Operator " + op.toString() + " encountered when" + 
								" parsing mutations in read " + read.getName() + ". Don't know" +
								"what to do with it.");
					}
					/*
					// Unknown operators likely correspond to 'N's in the MD tag. Count them as matches.
					if (op.equals(GenericOperator.MATCH) || op.equals(GenericOperator.UNKNOWN)) {
						mutationProfiles.addMatch(referenceName, referencePosition, orientation);

					// Do nothing with soft-clipped positions.
					} else if (op.equals(GenericOperator.SOFT_CLIP)) {
						
					} else if (op.isDeletion()) {
						mutationProfiles.addDeletion(referenceName, referencePosition, orientation);

					} else if (op.isMutation()) {
						// Get the base that we've mutated to.
						String readBase = ((SAMFragment)read).getSamRecord().getReadString().substring(readPosition, 
								readPosition + 1);
						
						// Ignoring uncertain bases in reads for now. Only recognizing 'A', 'C', 'G', 'T'.
						if (readBase.charAt(0) != 'N') {
							mutationProfiles.addMutation(referenceName, Nucleotide.valueOf(readBase), referencePosition, orientation);
						}

					} else if (op.equals(GenericOperator.INSERTION)) {
						mutationProfiles.addInsertion(referenceName, referencePosition, orientation);

					} else {
						throw new IOException("Operator " + op.toString() + " encountered when parsing mutations in read " + read.getName() + ". Don't know what to do with it.");
					}
					*/
					// Soft-clipping of a read may overlap with meaningful bases from its pair, so we haven't visited these soft-clipped positions. 
					if (!currentCigarOperator.equals(CigarOperator.SOFT_CLIP)) {
						visitedPositions.add(referencePosition);
					}
				}
				
				if (currentCigarOperator.consumesReferenceBases()) {
					referencePosition += 1;
				}
				if (currentCigarOperator.consumesReadBases()) {
					readPosition += 1;
				}
			}
		}

		/**
		 * Writes each individual mutation in every read to a text file. Each
		 * mutation occupies one line of the file. This file might be very
		 * large!
		 * 
		 * @param bam - the .bam file to be processed
		 * @param file - the output file
		 * @throws IOException if a read of a type other than SAMFragment and
		 * PairedMappedFragment is encountered in the .bam file.
		 */
		@SuppressWarnings("unchecked")
		protected void writeMutationsToFile(AnnotationCollection<? extends Annotation> bam, File file) throws IOException {
			System.out.println("Writing individual read mutations to file.");
			if (file.exists()) {
				System.out.println(file.getName() + " already exists!");
				System.exit(1);
			}
			
			PrintWriter output = new PrintWriter(file);
			try {
				CloseableIterator<? extends Annotation> reads = bam.sortedIterator();
				while (reads.hasNext()) {
					Annotation read = reads.next();
					if (read instanceof SAMFragment) {
						writeMutationsInReadToFile((SAMFragment)read, output, new HashSet<Integer>());
					} else if (read instanceof PairedMappedFragment<?>) {
						writeMutationsInReadToFile((PairedMappedFragment<? extends MappedFragment>)read, output, new HashSet<Integer>());
					} else {
						throw new IOException("Mutation counter only accepts SAMFragments and PairedMappedFragments. Other object encountered.");
					}
				}
			} finally {
				output.close();
				System.out.println("Mutations written to " + file.getAbsolutePath());
			}
		}
		
		/**
		 * Writes the mutations in a PairedMappedFragment to file.
		 * 
		 * @param readPair - PairedMappedFragment containing mutations to be counted
		 * @param output - output PrintWriter
		 * @param visitedPositions - a set of visited positions that will be skipped to avoid double-counting
		 * @throws IOException if there are inconsistencies between the MD tag and the CIGAR string
		 */
		private void writeMutationsInReadToFile(PairedMappedFragment<? extends MappedFragment> readPair, PrintWriter output, Set<Integer> visitedPositions) throws IOException {
			Annotation read1 = readPair.getRead1();
			Annotation read2 = readPair.getRead2();
			writeMutationsInReadToFile((SAMFragment)read1, output, visitedPositions);
			writeMutationsInReadToFile((SAMFragment)read2, output, visitedPositions);
		}

		/**
		 * Writes the mutations in a SAMFragment to file.
		 * 
		 * @param read - SAMFragment containing mutations to be counted
		 * @param output - output PrintWriter
		 * @param visitedPositions - a set of visited positions that will be skipped to avoid double-counting
		 * @throws IOException if there are inconsistencies between the MD tag and the CIGAR string
		 */
		private void writeMutationsInReadToFile(SAMFragment read, PrintWriter output, Set<Integer> visitedPositions) throws IOException {
			String referenceName = read.getReferenceName();
			String readName = read.getName();
			int referencePosition = read.getReferenceStartPosition();
			int referenceEndPosition = read.getReferenceEndPosition();
			int readPosition = 0;
				
			for (int i = numExcludedBasesFromEnd; i > 0; i--) {
				visitedPositions.add(referencePosition + i - 1);
				visitedPositions.add(referenceEndPosition - i + 1);
			}
				
			String mdTagString = read.getStringTag("MD");
			MdTag mdTag = new MdTag(mdTagString);
			MdTagStack mdTagStack = new MdTagStack(mdTag);
				
			Cigar cigar = read.getSamRecord().getCigar();
			CigarStack cigarStack = new CigarStack(cigar);

			CigarOperator currentCigarOperator = null;
			MdTagOperator currentMdTagOperator = null;

			while (cigarStack.hasElements()) {
				currentCigarOperator = cigarStack.popOperator();

				/* MD tags ignore insertions and soft clipping. */
				if (!currentCigarOperator.equals(CigarOperator.INSERTION) || !currentCigarOperator.equals(CigarOperator.SOFT_CLIP)) {
					currentMdTagOperator = mdTagStack.popOperator();
				}

				if (!visitedPositions.contains(referencePosition)) {
					GenericOperator op = detectMutation(currentCigarOperator, currentMdTagOperator);
					// Note: Add 1 to reference position because IGV uses 1-based indexing.
					
					if (op.isDeletion()) {
						output.println(referenceName + " " + readName + " " + (referencePosition + 1) + " D");

					} else if (op.equals(GenericOperator.INSERTION)) {
						output.println(referenceName + " " + readName + " " + (referencePosition + 1) + " I");

					} else if (op.isMutation()) {
						byte referenceBase = MdTagOperator.enumToCharacter(currentMdTagOperator);
						String readBase = ((SAMFragment)read).getSamRecord().getReadString().substring(readPosition, readPosition + 1);
						output.println(referenceName + " " + readName + " " + (referencePosition + 1) + " " + (char)referenceBase + "->" + readBase);
					}
					
					if (!currentCigarOperator.equals(CigarOperator.SOFT_CLIP)) {
						visitedPositions.add(referencePosition);
					}
				}
				
				if (currentCigarOperator.consumesReferenceBases()) {
					referencePosition += 1;
				}
				if (currentCigarOperator.consumesReadBases()) {
					readPosition += 1;
				}
			}		
		}
	}
}