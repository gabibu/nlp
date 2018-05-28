package exe4.decode;

import exe4.grammar.Rule;

import java.util.List;
import java.util.Objects;

/**
 * User: gabib3b
 */
public class BestSplit
{
    private Rule rule;
    private int splitPoint;
    private List<Rule> source;

    public BestSplit(Rule rule, int splitPoint, List<Rule> source)
    {
        this.rule = rule;
        this.splitPoint = splitPoint;
        this.source = source;
    }

    public Rule getRule() {
        return rule;
    }

    public int getSplitPoint() {
        return splitPoint;
    }

    public List<Rule> getSource() {
        return source;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BestSplit bestSplit = (BestSplit) o;
        return splitPoint == bestSplit.splitPoint &&
                Objects.equals(rule, bestSplit.rule) &&
                Objects.equals(source, bestSplit.source);
    }

    @Override
    public int hashCode() {

        return Objects.hash(rule, splitPoint, source);
    }
}
