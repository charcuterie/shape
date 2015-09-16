package shape.mdtag;

public enum GenericOperator {

	/** Deletions */
    DELETION(true, false, true, "D"),
    DELETION_OF_A(true, false, true, "DA"),
    DELETION_OF_C(true, false, true, "DC"),
    DELETION_OF_G(true, false, true, "DG"),
    DELETION_OF_T(true, false, true, "DT"),
    
    /** Matches */
    MATCH(false, true, true, "="),

    INSERTION(false, true, false, "I"),

    /** Mismatches */
    UNKNOWN_MISMATCH(true, true, true, "X"),
    A_TO_C(true, true, true, "AC"),
    A_TO_G(true, true, true, "AG"),
    A_TO_N(true, true, true, "AN"),
    A_TO_T(true, true, true, "AT"),
    C_TO_A(true, true, true, "CA"),
    C_TO_G(true, true, true, "CG"),
    C_TO_N(true, true, true, "CN"),
    C_TO_T(true, true, true, "CT"),
    G_TO_A(true, true, true, "GA"),
    G_TO_C(true, true, true, "GC"),
    G_TO_N(true, true, true, "GN"),
    G_TO_T(true, true, true, "GT"),
    T_TO_A(true, true, true, "TA"),
    T_TO_C(true, true, true, "TC"),
    T_TO_G(true, true, true, "TG"),
    T_TO_N(true, true, true, "TN"),
    
    /** Other operators */
    UNKNOWN(false, true, true, "N"),
    SOFT_CLIP(false, true, true, "S"),
    SPLICE_JUNCTION(false, false, true, "J")
    ;

    private final boolean isMutation;
    private final boolean consumesReadBases;
    private final boolean consumesReferenceBases;
    private final String string;

    /** Default constructor. */
    GenericOperator(boolean isMutation, boolean consumesReadBases, boolean consumesReferenceBases, String string) {
        this.isMutation = isMutation;
        this.consumesReadBases = consumesReadBases;
        this.consumesReferenceBases = consumesReferenceBases;
        this.string = string;
    }

    public boolean isMutation() {
    	return isMutation;
    }
    
    public boolean consumesReadBases() {
    	return consumesReadBases;
    }

    public boolean consumesReferenceBases() {
    	return consumesReferenceBases;	
    }
    
    public boolean isDeletion() {
    	return (isMutation && !consumesReadBases);
    }

    @Override
    public String toString() {
        return this.string;
    }
}