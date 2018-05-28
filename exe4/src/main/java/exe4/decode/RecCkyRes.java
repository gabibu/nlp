package exe4.decode;

import exe4.grammar.Rule;

import java.util.List;

/**
 * User: gabib3b
 */
public class RecCkyRes
{
    private double prob;
    private Rule rule;
    private Integer splitPoint;
    private List<Rule> source;

    public RecCkyRes(double prob, Rule rule, Integer splitPoint, List<Rule> source)
    {
        this.prob = prob;
        this.rule = rule;
        this.splitPoint = splitPoint;
        this.source = source;
    }

    public List<Rule> getSource() {
        return source;
    }

    public double getProb() {
        return prob;
    }

    public Rule getRule() {
        return rule;
    }

    public Integer getSplitPoint() {
        return splitPoint;
    }
}
