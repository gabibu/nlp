package exe4.decode;

import exe4.grammar.Rule;

import java.util.Objects;

/**
 * User: gabib3b
 */
public class Index
{
    private final String ruleLeftSide;
    private final int startIndex;
    private final int endINdex;
    private Rule rule;

    public Index(String ruleLeftSide, int startIndex, int endINdex, Rule rule) {
        this.ruleLeftSide = ruleLeftSide;
        this.startIndex = startIndex;
        this.endINdex = endINdex;
        this.rule = rule;
    }

    public Rule getRule() {
        return rule;
    }

    public String getRuleLeftSide() {
        return ruleLeftSide;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndINdex() {
        return endINdex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return startIndex == index.startIndex &&
                endINdex == index.endINdex &&
                Objects.equals(ruleLeftSide, index.ruleLeftSide);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ruleLeftSide, startIndex, endINdex);
    }

    @Override
    public String toString() {
        return "Index{" +
                "ruleLeftSide='" + ruleLeftSide + '\'' +
                ", startIndex=" + startIndex +
                ", endINdex=" + endINdex +
                '}';
    }
}
