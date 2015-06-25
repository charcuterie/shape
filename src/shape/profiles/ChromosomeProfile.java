package shape.profiles;

/**
 * @author Mason M Lai
 */

public class ChromosomeProfile<T extends StrandProfile> {
	
	private final T positiveProfile;
	private final T negativeProfile;
	private final String chromosomeName;
	private final int length;
	
	ChromosomeProfile(T positiveProfile, T negativeProfile) {
		if (positiveProfile.isPositive()) {
			this.positiveProfile = positiveProfile;
		} else {
			throw new IllegalArgumentException("ChromosomeProfile constructor attempted to set positiveProfile to StrandProfile with non-positive orientation.");
		}
		
		if (negativeProfile.isNegative()) {
			this.negativeProfile = negativeProfile;
		} else {
			throw new IllegalArgumentException("ChromosomeProfile constructor attempted to set negativeProfile to StrandProfile with non-negative orientation.");
		}

		String posName = positiveProfile.getChromosomeName();
		String negName = negativeProfile.getChromosomeName();
		if (posName.equals(negName) && posName != null) {
			this.chromosomeName = positiveProfile.getChromosomeName();
		} else {
			throw new IllegalArgumentException("ChromosomeProfile constructor passed StrandProfiles with different or null chromosome names.");
		}
		
		int posLength = positiveProfile.getLength();
		int negLength = negativeProfile.getLength();
		if (posLength == negLength) {
			this.length = posLength;
		} else {
			throw new IllegalArgumentException("ChromosomeProfile constructor passed StrandProfiles with different lengths.");
		}
		
	}
	
	public final int getLength() {
		return length;
	}
	
	public final String getName() {
		return chromosomeName;
	}
	
	public final T getPositiveStrand() {
		return positiveProfile;
	}
	
	public final T getNegativeStrand() {
		return negativeProfile;
	}
}