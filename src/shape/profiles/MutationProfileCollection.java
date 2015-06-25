package shape.profiles;

import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotation.Annotation.Strand;
import guttmanlab.core.annotationcollection.AnnotationCollection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import shape.utils.Nucleotide;

/**
 * A collection of {@link MutationProfile} objects.
 * 
 * @author Mason M Lai
 */

public class MutationProfileCollection extends ChromosomeProfileCollection<MutationProfile> {
	
	private int coverageThreshold;
	
	public MutationProfileCollection(int coverageThreshold) {
		super();
		if (coverageThreshold >= 0) {
			this.coverageThreshold = coverageThreshold;
		} else {
			throw new IllegalArgumentException("Coverage threshold must be non-negative!");
		}
	}
	
	public MutationProfileCollection() {
		this(1);
	}
	
	public MutationProfileCollection(Map<String, Integer> map, int coverageThreshold) {
		super(map);
		if (coverageThreshold >= 0) {
			this.coverageThreshold = coverageThreshold;
		} else {
			throw new IllegalArgumentException("Coverage threshold must be non-negative!");
		}
	}
	
	public MutationProfileCollection(Map<String, Integer> map) {
		this(map, 1);
	}

	public MutationProfileCollection(AnnotationCollection<? extends Annotation> bam, int coverageThreshold) {
		super(bam);
		if (coverageThreshold >= 0) {
			this.coverageThreshold = coverageThreshold;
		} else {
			throw new IllegalArgumentException("Coverage threshold must be non-negative!");
		}
	}
	
	public MutationProfileCollection(AnnotationCollection<? extends Annotation> bam) {
		this(bam, 1);
	}

	public final void addChromosome(String chromosome, int size) {
		if (!profiles.containsKey(chromosome)) {
			MutationProfile posProfile = new MutationProfile(size, chromosome, Strand.POSITIVE);
			MutationProfile negProfile = new MutationProfile(size, chromosome, Strand.NEGATIVE);
			profiles.put(chromosome, new ChromosomeProfile<MutationProfile>(posProfile, negProfile));
		}
	}
	
	/**
	 * Adds an insertion to the specified profile.
	 * 
	 * @param chromosome   name of the chromosome
	 * @param position     position where insertion exists
	 * @param orientation  strandedness
	 * @throws IOException if orientation is not Strand.POSITIVE or Strand.NEGATIVE.
	 */
	public final void addInsertion(String chromosome, int position, Strand orientation) throws IOException {
		if (orientation.equals(Strand.POSITIVE)) {
			profiles.get(chromosome).getPositiveStrand().addInsertion(position);
		} else if (orientation.equals(Strand.NEGATIVE)) {
			profiles.get(chromosome).getNegativeStrand().addInsertion(position);
		} else {
			throw new IOException("Orientation " + orientation + " not recognized. Only recognizes POSITIVE and NEGATIVE.");
		}
	}

	/**
	 * Adds a deletion to the specified profile.
	 * 
	 * @param chromosome   name of the chromosome
	 * @param position     position where deletion exists
	 * @param orientation  strandedness
	 * @throws IOException if orientation is not Strand.POSITIVE or Strand.NEGATIVE.
	 */
	public final void addDeletion(String chromosome, int position, Strand orientation) throws IOException {
		if (orientation.equals(Strand.POSITIVE)) {
			profiles.get(chromosome).getPositiveStrand().addDeletion(position);
		} else if (orientation.equals(Strand.NEGATIVE)) {
			profiles.get(chromosome).getNegativeStrand().addDeletion(position);
		} else {
			throw new IOException("Orientation " + orientation + " not recognized. Only recognizes POSITIVE and NEGATIVE.");
		}
	}	

	/**
	 * Adds a match (i.e., non-mutation) to the specified profile.
	 * 
	 * @param chromosome   name of the chromosome
	 * @param position     position where match exists
	 * @param orientation  strandedness
	 * @throws IOException if orientation is not Strand.POSITIVE or Strand.NEGATIVE.
	 */
	public final void addMatch(String chromosome, int position, Strand orientation) throws IOException {
		if (orientation.equals(Strand.POSITIVE)) {
			profiles.get(chromosome).getPositiveStrand().addMatch(position);
		} else if (orientation.equals(Strand.NEGATIVE)) {
			profiles.get(chromosome).getNegativeStrand().addMatch(position);
		} else {
			throw new IOException("Orientation " + orientation + " not recognized. Only recognizes POSITIVE and NEGATIVE.");
		}
	}

	/**
	 * Adds a mismatch to the specified profile.
	 * 
	 * @param chromosome   name of the chromosome
	 * @param position     position where mismatch exists
	 * @param orientation  strandedness
	 * @throws IOException if orientation is not Strand.POSITIVE or Strand.NEGATIVE.
	 */
	public final void addMutation(String chromosome, Nucleotide n, int position, Strand orientation) throws IOException {
		if (orientation.equals(Strand.POSITIVE)) {
			profiles.get(chromosome).getPositiveStrand().addMutation(n, position);
		} else if (orientation.equals(Strand.NEGATIVE)) {
			profiles.get(chromosome).getNegativeStrand().addMutation(n, position);
		} else {
			throw new IOException("Orientation " + orientation + " not recognized. Only recognizes POSITIVE and NEGATIVE.");
		}
	}
	
	/**
	 * Writes mutations to .csv file in "tidy data" format. Skips positions
	 * with mutation rate = 0. Also outputs .bedgraph files showing mutation
	 * rate, deletion rate, and substitution rate (two files per rate, one
	 * for positive strandedness and one for negative strandedness). 
	 * 
	 * @param fileName basename of output file
	 * @throws IOException 
	 */
	@Override
	public final void toFile(String fileName) throws IOException {

		File csvFile = new File(fileName + ".csv");
		if (csvFile.exists()) {
			System.out.println("Output " + csvFile.getName() + " already exists!");
			System.exit(1);
		}
		
		File deletionPosFile = new File(fileName + "_pos_deletion_rate.bedgraph");
		if (deletionPosFile.exists()) {
			System.out.println("Output " + deletionPosFile.getName() + " already exists!");
			System.exit(1);
		}
		
		File mutationPosFile = new File(fileName + "_pos_mutation_rate.bedgraph");
		if (mutationPosFile.exists()) {
			System.out.println("Output " + mutationPosFile.getName() + " already exists!");
			System.exit(1);
		}
		
		File substitutionPosFile = new File(fileName + "_pos_substitution_rate.bedgraph");
		if (substitutionPosFile.exists()) {
			System.out.println("Output " + substitutionPosFile.getName() + " already exists!");
			System.exit(1);
		}
		
		File deletionNegFile = new File(fileName + "_neg_deletion_rate.bedgraph");
		if (deletionNegFile.exists()) {
			System.out.println("Output " + deletionNegFile.getName() + " already exists!");
			System.exit(1);
		}
		
		File mutationNegFile = new File(fileName + "_neg_mutation_rate.bedgraph");
		if (mutationNegFile.exists()) {
			System.out.println("Output " + mutationNegFile.getName() + " already exists!");
			System.exit(1);
		}
		
		File substitutionNegFile = new File(fileName + "_neg_substitution_rate.bedgraph");
		if (substitutionNegFile.exists()) {
			System.out.println("Output " + substitutionNegFile.getName() + " already exists!");
			System.exit(1);			
		}
		
		BufferedWriter csvOut = null;
		BufferedWriter dpOut = null;
		BufferedWriter mpOut = null;
		BufferedWriter spOut = null;
		BufferedWriter dnOut = null;
		BufferedWriter mnOut = null;
		BufferedWriter snOut = null;
		
		try {
			csvOut = new BufferedWriter(new FileWriter(csvFile));
			dpOut = new BufferedWriter(new FileWriter(deletionPosFile));
			mpOut = new BufferedWriter(new FileWriter(mutationPosFile));
			spOut = new BufferedWriter(new FileWriter(substitutionPosFile));
			dnOut = new BufferedWriter(new FileWriter(deletionNegFile));
			mnOut = new BufferedWriter(new FileWriter(mutationNegFile));
			snOut = new BufferedWriter(new FileWriter(substitutionNegFile));

			csvOut.write("chromosome,orientation,position,=,I,D,A,C,G,T,total,mutationRate,deletionRate,substitutionRate");
			csvOut.newLine();
			dpOut.write("track type=bedGraph");
			dpOut.newLine();
			mpOut.write("track type=bedGraph");
			mpOut.newLine();
			spOut.write("track type=bedGraph");
			spOut.newLine();
			dnOut.write("track type=bedGraph");
			dnOut.newLine();
			mnOut.write("track type=bedGraph");
			mnOut.newLine();
			snOut.write("track type=bedGraph");
			snOut.newLine();
			
			for (Map.Entry<String, ChromosomeProfile<MutationProfile>> chromosome : profiles.entrySet()) {
				String chromosomeName = chromosome.getKey();
				MutationProfile positiveMutations = chromosome.getValue().getPositiveStrand();
				MutationProfile negativeMutations = chromosome.getValue().getNegativeStrand();
				int length = positiveMutations.getLength();
				for (int position = 0; position < length; position++) {
					
					int matches = positiveMutations.getMatch(position);
					int insertions = positiveMutations.getInsertion(position);
					int deletions = positiveMutations.getDeletion(position);
					int mutationsToA = positiveMutations.getMutation(Nucleotide.A, position);
					int mutationsToC = positiveMutations.getMutation(Nucleotide.C, position);
					int mutationsToG = positiveMutations.getMutation(Nucleotide.G, position);
					int mutationsToT = positiveMutations.getMutation(Nucleotide.T, position);
					int total = deletions + matches + mutationsToA + mutationsToC + mutationsToG + mutationsToT;

					double deletionRate = ((double)deletions / total);
					deletionRate = Double.isNaN(deletionRate) ? 0 : deletionRate;

					double substitutionRate = ((double)(mutationsToA + mutationsToC + mutationsToG + mutationsToT) / total);
					substitutionRate = Double.isNaN(substitutionRate) ? 0 : substitutionRate;

					double mutationRate = ((double)(deletions + mutationsToA + mutationsToC + mutationsToG + mutationsToT)) / total;
					mutationRate = Double.isNaN(mutationRate) ? 0 : mutationRate;

					if (total != 0) {
						csvOut.write(
								chromosomeName   + "," + "positive," +
								(position + 1)   + "," +
								matches          + "," +
								insertions       + "," +
								deletions        + "," +
								mutationsToA     + "," +
								mutationsToC     + "," +
								mutationsToG     + "," +
								mutationsToT     + "," +
								total            + "," +
								mutationRate     + "," +
								deletionRate     + "," +
								substitutionRate
						);
						csvOut.newLine();
					}
					
					if (mutationRate != 0) {
						mpOut.write(
								chromosomeName + " " +
								position       + " " +
								(position + 1) + " " +
								mutationRate
						);
						mpOut.newLine();
					}
					
					if (deletionRate != 0) {
						dpOut.write(
								chromosomeName + " " +
								position       + " " +
								(position + 1) + " " +
								deletionRate
						);
						dpOut.newLine();
					}
					
					if (substitutionRate != 0) {
						spOut.write(
								chromosomeName + " " +
								position       + " " +
								(position + 1) + " " +
								substitutionRate
						);
						spOut.newLine();
					}
										
					matches = negativeMutations.getMatch(position);
					insertions = negativeMutations.getInsertion(position);
					deletions = negativeMutations.getDeletion(position);
					mutationsToA = negativeMutations.getMutation(Nucleotide.A, position);
					mutationsToC = negativeMutations.getMutation(Nucleotide.C, position);
					mutationsToG = negativeMutations.getMutation(Nucleotide.G, position);
					mutationsToT = negativeMutations.getMutation(Nucleotide.T, position);
					total = deletions + matches + mutationsToA + mutationsToC + mutationsToG + mutationsToT;
					deletionRate = ((double)deletions / total);
					substitutionRate = ((double)(mutationsToA + mutationsToC + mutationsToG + mutationsToT) / total);
					mutationRate = ((double)(deletions + mutationsToA + mutationsToC + mutationsToG + mutationsToT)) / total;
					mutationRate = Double.isNaN(mutationRate) ? 0 : mutationRate;
					substitutionRate = Double.isNaN(substitutionRate) ? 0 : substitutionRate;
					deletionRate = Double.isNaN(deletionRate) ? 0 : deletionRate;
					
					if (total >= coverageThreshold) {
						csvOut.write(
								chromosomeName   + "," + "negative," +
								(position + 1)   + "," +
								matches          + "," +
								insertions       + "," +
								deletions        + "," +
								mutationsToA     + "," +
								mutationsToC     + "," +
								mutationsToG     + "," +
								mutationsToT     + "," +
								total            + "," +
								mutationRate     + "," +
								deletionRate     + "," +
								substitutionRate
						);
						csvOut.newLine();
					}
					
					if (mutationRate != 0 && total >= coverageThreshold) {
						mnOut.write(
								chromosomeName + " " +
								position       + " " +
								(position + 1) + " " +
								mutationRate
						);
						mnOut.newLine();
					}
					
					if (deletionRate != 0 && total >= coverageThreshold) {
						dnOut.write(
								chromosomeName + " " +
								position       + " " +
								(position + 1) + " " +
								deletionRate
						);
						dnOut.newLine();
					}
					
					if (substitutionRate != 0 && total >= coverageThreshold) {
						snOut.write(
								chromosomeName + " " +
								position       + " " +
								(position + 1) + " " +
								substitutionRate
						);
						snOut.newLine();
					}
				}
				System.out.println("Data for chromosome " + chromosomeName + " written.");
			}

			System.out.println(".csv data written to " + csvFile.getAbsolutePath());
			System.out.println("Deletion rates written to " + deletionPosFile.getAbsolutePath() + " and " + deletionNegFile.getAbsolutePath());
			System.out.println("Mutation rates written to " + mutationPosFile.getAbsolutePath() + " and " + mutationNegFile.getAbsolutePath());
			System.out.println("Substitution rates written to " + substitutionPosFile.getAbsolutePath() + " and " + substitutionNegFile.getAbsolutePath());
			
		} finally {
			if (csvOut != null) {csvOut.close();}
			
			if (dpOut != null) {dpOut.close();}
			if (dnOut != null) {dnOut.close();}

			if (mpOut != null) {mpOut.close();}
			if (mnOut != null) {mnOut.close();}
			
			if (spOut != null) {spOut.close();}
			if (snOut != null) {snOut.close();}
		}	
	}
}