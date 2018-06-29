package exe4.grammar;

import java.util.Objects;

/**
 * User: gabib3b
 */
public class LeftRight
{
    private Event left;
    private Event right;

    public LeftRight(Event left, Event right) {
        this.left = left;
        this.right = right;
    }

    public Event getLeft() {
        return left;
    }

    public Event getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeftRight leftRight = (LeftRight) o;
        return Objects.equals(left, leftRight.left) &&
                Objects.equals(right, leftRight.right);
    }

    @Override
    public int hashCode() {

        return Objects.hash(left, right);
    }
}
