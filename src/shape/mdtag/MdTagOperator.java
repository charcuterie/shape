package shape.mdtag;

/**
 * The operators that can appear in an MdTag.
 */
public enum MdTagOperator {
    /** Deletion vs. the reference. */
    D(false, 'D'),
    /** Deletion; deleted base is an A. */
    DA(false, 'a'),
    /** Deletion; deleted base is an C. */
    DC(false, 'c'),
    /** Deletion; deleted base is an G. */
    DG(false, 'g'),
    /** Deletion; deleted base is an T. */
    DT(false, 't'),
    /** Matches the reference. */
    EQ(true, '='),
    /** Mismatches the reference. Not currently used. */
    XN(true, 'X'),
    /** Mismatch; reference is an A. */
    XA(true, 'A'),
    /** Mismatch; reference is a T. */
    XT(true, 'T'),
    /** Mismatch; reference is a C. */
    XC(true, 'C'),
    /** Mismatch; reference is a G. */
    XG(true, 'G'),
    /** Used for barcodes and other things. Ignore when found. */
    N(true,  'N')
    ;

    private final boolean consumesReadBases;
    private final byte character;
    private final String string;

    // Readable synonyms of the above enums
    public static final MdTagOperator MATCH = EQ;
    public static final MdTagOperator DELETION = D;
    public static final MdTagOperator DELETION_OF_A = DA;
    public static final MdTagOperator DELETION_OF_C = DC;
    public static final MdTagOperator DELETION_OF_G = DG;
    public static final MdTagOperator DELETION_OF_T = DT;
    public static final MdTagOperator MISMATCH = XN;
    public static final MdTagOperator MISMATCH_FROM_A = XA;
    public static final MdTagOperator MISMATCH_FROM_C = XC;
    public static final MdTagOperator MISMATCH_FROM_G = XG;
    public static final MdTagOperator MISMATCH_FROM_T = XT;
    public static final MdTagOperator IGNORE = N;

    /** Default constructor. */
    MdTagOperator(boolean consumesReadBases, char character) {
        this.consumesReadBases = consumesReadBases;
        this.character = (byte) character;
        this.string = new String(new char[] {character}).intern();
    }

    /** If true, represents that this cigar operator "consumes" bases from the read bases. */
    public final boolean consumesReadBases() {
    	return consumesReadBases;
    }

    public final boolean isDeletion() {
    	return (!consumesReadBases);
    }
    
    public static MdTagOperator characterToEnum(final int b) {
        switch (b) {
        case 'D':
            return D;
        case 'a':
        	return DA;
        case 'c':
        	return DC;
        case 'g':
        	return DG;
        case 't':
        	return DT;
        case '=':
            return EQ;
        case 'X':
            return XN;
        case 'A':
        	return XA;
        case 'C':
        	return XC;
        case 'G':
        	return XG;
        case 'T':
        	return XT;
        case 'N':
        	return N;
        default:
            throw new IllegalArgumentException("Unrecognized MdTagOperator: " + b);
        }
    }

    public static byte enumToCharacter(final MdTagOperator e) {
        return e.character;
    }

    @Override
    public String toString() {
        return this.string;
    }
}