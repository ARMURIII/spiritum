package arr.armuriii.spiritum.utils;


import org.apache.commons.lang3.tuple.ImmutablePair;

public class ImmutableTwin<T> extends ImmutablePair<T,T> {
    /**
     * Create a new pair instance.
     *
     * @param left  the left value may be null
     * @param right the right value may be null
     */
    public ImmutableTwin(T left, T right) {
        super(left, right);
    }
}
