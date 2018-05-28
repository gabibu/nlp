package exe4.decode;

import java.util.Objects;

/**
 * User: gabib3b
 */
public class SplitKey
{
    private int from;
    private int to;

    public SplitKey(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplitKey splitKey = (SplitKey) o;
        return from == splitKey.from &&
                to == splitKey.to;
    }

    @Override
    public int hashCode() {

        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "SplitKey{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
