package shape.profiles;

import guttmanlab.core.annotation.Annotation;
import guttmanlab.core.annotationcollection.AnnotationCollection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.samtools.util.CloseableIterator;

/**
 * A collection of ChromosomeProfiles, organized into Pairs, with each Pair
 * representing a chromosome, and each element of the Pair representing a
 * strand (positive or negative).
 * 
 * @author Mason M Lai
 *
 * @param <T> An object which extends an AbstractChromosomeProfile.
 */
public abstract class ChromosomeProfileCollection<T extends StrandProfile> {

	protected Map<String, ChromosomeProfile<T>> profiles;
	
	protected ChromosomeProfileCollection() {
		profiles = new LinkedHashMap<String,  ChromosomeProfile<T>>();
	}
	
	protected ChromosomeProfileCollection(Map<String, Integer> map) {
		profiles = new LinkedHashMap<String, ChromosomeProfile<T>>();
		for (Map.Entry<String, Integer> elem : map.entrySet()) {
			this.addChromosome(elem.getKey(), elem.getValue() + 1);
		}
	}
	
	protected ChromosomeProfileCollection(AnnotationCollection<? extends Annotation> bam) {
		System.out.println("Creating list of chromosomes.");
		Map<String, Integer> chromosomes = new HashMap<String, Integer>();
		CloseableIterator<? extends Annotation> reads = bam.sortedIterator();
		while (reads.hasNext()) {
			Annotation read = reads.next();
			String chromosomeName = read.getReferenceName();
			if (!chromosomes.containsKey(chromosomeName)) {
				chromosomes.put(chromosomeName, 0);
				System.out.println(chromosomeName + " added to list.");
			}
			int endPosition = read.getReferenceEndPosition();
			if (endPosition > chromosomes.get(chromosomeName)) {
				chromosomes.put(chromosomeName, endPosition);
			}
		}
		reads.close();
		System.out.println("List created.");
		profiles = new LinkedHashMap<String, ChromosomeProfile<T>>();
		for (Map.Entry<String, Integer> elem : chromosomes.entrySet()) {
			this.addChromosome(elem.getKey(), elem.getValue() + 1);
		}
	}
	
	/**
	 * Gets number of chromosomes or profiles in the collection (neglecting
	 * strandedness, if applicable).
	 * 
	 * @return number of chromosomes of profiles in the collection (neglecting
	 * strandedness, if applicable).
	 */
	public int getNumberOfChromosomes() {
		return profiles.size();
	}
	
	/**
	 * Gets length of a chromosome in nt.
	 * 
	 * @param chromosome chromosome name
	 * @return length of chromosome in nt
	 */
	public int getLength(String chromosome) {
		return profiles.get(chromosome).getLength();
	}
	
	/**
	 * Gets profiles.
	 * 
	 * @return member profiles
	 */
	public LinkedHashMap<String, ChromosomeProfile<T>> getProfiles() {
		return (LinkedHashMap<String, ChromosomeProfile<T>>)profiles;
	}
	
	public abstract void addChromosome(String chromosome, int size);
	
	public abstract void toFile(String fileName) throws FileNotFoundException, IOException;
}