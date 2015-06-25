package shape.profiles;

import java.util.HashMap;
import java.util.Map;

import shape.utils.Nucleotide;

import guttmanlab.core.annotation.Annotation.Strand;

public class MutationProfile extends StrandProfile {

    private Map<Integer, MutableInt> matches;
    private Map<Integer, MutableInt> deletions;
    private Map<Integer, MutableInt> insertions;
    private Map<Integer, MutableInt> mutationsToA;
    private Map<Integer, MutableInt> mutationsToC;
    private Map<Integer, MutableInt> mutationsToG;
    private Map<Integer, MutableInt> mutationsToT;
    
    public MutationProfile(int length, String chromosome, Strand orientation) {
    	super(length, chromosome, orientation);
    	matches = new HashMap<Integer, MutableInt>();
    	deletions = new HashMap<Integer, MutableInt>();
    	insertions = new HashMap<Integer, MutableInt>();
    	mutationsToA = new HashMap<Integer, MutableInt>();
    	mutationsToC = new HashMap<Integer, MutableInt>();
    	mutationsToG = new HashMap<Integer, MutableInt>();
    	mutationsToT = new HashMap<Integer, MutableInt>();
    }
    
    public final Map<Integer, MutableInt> getMatches() {
		return matches;
	}

	public final Map<Integer, MutableInt> getDeletions() {
		return deletions;
	}

	public final Map<Integer, MutableInt> getInsertions() {
		return insertions;
	}

	public final Map<Integer, MutableInt> getMutations(Nucleotide n) {
		switch (n) {
		case A:
			return mutationsToA;
		case C:
			return mutationsToC;
		case G:
			return mutationsToG;
		case T:
			return mutationsToT;
		default:
			return mutationsToT;
		}
	}

	private final void increment(Map<Integer, MutableInt> map, int pos) {
		MutableInt count = map.get(pos);
		if (count == null) {
			map.put(pos, new MutableInt());
		} else {
			count.increment();
		}
	}
	
	public final void addInsertion(int position) {
		increment(insertions, position);
    }
    
    public final void addDeletion(int position) {
		increment(deletions, position);
    }    
    
    public final void addMatch(int position) {
		increment(matches, position);
	}
    
    public final void addMutation(Nucleotide n, int pos) {
    	switch (n) {
    	case A:
    		increment(mutationsToA, pos);
    		break;
    	case C:
    		increment(mutationsToC, pos);
    		break;
    	case G:
    		increment(mutationsToG, pos);
    		break;
    	case T:
    		increment(mutationsToT, pos);
    		break;
    	}
    }
    
    private final int getValue(Map<Integer, MutableInt> map, int pos) {
    	MutableInt rtrn = map.get(pos);
    	if (rtrn == null) {
    		return 0;
    	} else {
    		return rtrn.get();
    	}
    }
    
    public final int getInsertion(int position) {
    	return getValue(insertions, position);
    }
    
    public final int getDeletion(int position) {
    	return getValue(deletions, position);
    }
    
    public final int getMatch(int position) {
    	return getValue(matches, position);
    }
    
    public int getMutation(Nucleotide n, int pos) {
    	switch (n) {
    	case A:
    		return getValue(mutationsToA, pos);
    	case C:
    		return getValue(mutationsToC, pos);
    	case G:
    		return getValue(mutationsToG, pos);
    	case T:
    		return getValue(mutationsToT, pos);
    	default:
    		return getValue(mutationsToT, pos);
    	}
    }
    
    private class MutableInt {
    	int value = 1;
    	public void increment() {
    		++value;
    	}
    	public int get() {
    		return value;
    	}
    }
}