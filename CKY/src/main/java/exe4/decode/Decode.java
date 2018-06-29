package exe4.decode;

import exe4.grammar.Event;
import exe4.grammar.Grammar;
import exe4.grammar.Rule;
import exe4.tree.Node;
import exe4.tree.Terminal;
import exe4.tree.Tree;
import javafx.util.Pair;
import org.jcp.xml.dsig.internal.dom.DOMUtils;

import java.util.*;


public class Decode {

	public static Set<Rule> m_setGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapLexicalRules = null;
	public static Map<String, Set<Rule>>  nonTerminalToRules = new HashMap<String, Set<Rule>>();
    public static Map<String, Set<Rule>>  nonTerminalIdentitiesRule = new HashMap<String, Set<Rule>>();
	private static Set<String> nonTerminalsSymbols;
	private static Map<Rule, Set<List<Rule>>> ruleSetMap;
	private static Set<Rule> unitRules;

    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory 
     */
	public static Decode m_singDecoder = null;
	    
	public static Decode getInstance(Grammar g)
	{
		if (m_singDecoder == null)
		{
			nonTerminalsSymbols = g.getNonTerminalSymbols();
			m_singDecoder = new Decode();
			m_setGrammarRules = g.getSyntacticRules();

			m_mapLexicalRules = g.getLexicalEntries();
			nonTerminalToRules = g.getNonTerminalToRules();

            ruleSetMap = g.getRuleToSources();
            unitRules = g.getUnitRules();
		}

		return m_singDecoder;
	}
    
	public Tree decode(List<String> input)
	{
		try
		{
			return cky(input);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.print(e);

			return defaultDecode(input);
		}
	}

	private Tree cky(List<String> input)
	{
		Map<Index, Double> indexMinProb = new HashMap<>();

        Map<Index, BestSplit> indexBackPointer = new HashMap<>();
		for(int i = 0; i < input.size(); i++)
		{
			String word = input.get(i);
			Set<Rule> rules = m_mapLexicalRules.get(word);

			if(rules == null)
            {
                Rule g1 = new Rule( new Event("NN"), new Event(word));
                g1.setLexical(true);
                g1.setMinusLogProb(-Math.log(1));

                Index index = new Index("NN", i + 1, i + 1, g1);

                indexMinProb.put(index, -Math.log(1));

                indexBackPointer.put(index, new BestSplit(g1, -1,
                        null, null));
            }

			else
			{
				for(Rule rule : rules)
				{
					Index index = new Index(rule.getLHS().toString(), i + 1, i + 1,
                            rule);

					Double prob = indexMinProb.get(index);

					if(prob == null || rule.getMinusLogProb() < prob)
					{
						indexMinProb.put(index, rule.getMinusLogProb());

                        indexBackPointer.put(index, new BestSplit(rule, -1,
                                null, null));
					}
				}
			}

            boolean added;

            do
            {
                added = false;
                for(Rule rule : unitRules)
                {
                    String left = rule.getLHS().getSymbols().get(0);
                    String right = rule.getRHS().getSymbols().get(0);

                    Index outerIndex = new Index(left, i + 1, i + 1,
                            rule);

                    Index innerIndex = new Index(right, i + 1, i + 1,
                            rule);

                    Double p1 = indexMinProb.get(innerIndex);

                    if(p1 != null)
                    {
                        double prob = p1 + rule.getMinusLogProb();

                        Double p2 = indexMinProb.get(outerIndex);

                        if(p2 == null || p2 > prob)
                        {
                            indexMinProb.put(outerIndex, prob);

                            indexBackPointer.put(outerIndex, new BestSplit(rule, -1,
                                    null, innerIndex));

                            added = true;
                        }
                    }

                }
            }
            while (added);
		}

		for(int l = 1; l < input.size(); l++) {

            for (int i = 1; i < input.size(); i++) {
                int j = i + l;
                if (j > input.size()) {
                    continue;
                }

                for (String nonTerminal : nonTerminalToRules.keySet())
                {
                    Set<Index> visited = new HashSet<>();
                    RecCkyRes res = recCky(i, j, nonTerminal, indexMinProb, visited, i == 1 && j == input.size());


                    if (res == null) {
                        continue;
                    }


                    double prob = res.getProb();

                    Index index
                            = new Index(nonTerminal, i, j, res.getRule());

                    Double hasProb = indexMinProb.get(index);

                    if (hasProb == null || hasProb > prob) {
                        indexMinProb.put(index, prob);

                        indexBackPointer.put(index, new BestSplit(res.getRule(), res.getSplitPoint(),
                                res.getSource(), null));
                    }
                }

                boolean added;

                do {
                    added = false;

                    for (Rule rule : unitRules) {

                        Index outerIndex = new Index(rule.getLHS().getSymbols().get(0), i, j, null);


                        double ruleProb = rule.getMinusLogProb();

                        Index innerIndex = new Index(rule.getRHS().getSymbols().get(0),
                                i, j, rule);

                        Double p1 = indexMinProb.get(innerIndex);

                        BestSplit innerSplit = indexBackPointer.get(innerIndex);

                        if (p1 != null) {
                            double pp = ruleProb + p1;
                            Double currentProb = indexMinProb.get(outerIndex);

                            if (currentProb == null || currentProb > pp) {
                                indexMinProb.put(outerIndex, pp);
                                indexBackPointer.put(outerIndex, new BestSplit(rule,
                                        innerSplit.getSplitPoint(), null, innerIndex));

                                added = true;
                            }

                        }
//                        else {
//                            System.out.println("dd");
//                        }
                    }
                }
                while (added);
            }
        }

        Index bestAll = null;
        Double bestAllProb = null;

		for(Map.Entry<Index, Double> indexProb : indexMinProb.entrySet())
        {
            Index index = indexProb.getKey();

            if(index.getStartIndex() ==1 && index.getEndINdex() == input.size())
            {
                double val = indexProb.getValue();

                if(bestAllProb == null || val < bestAllProb)
                {
                    bestAllProb = val;
                    bestAll = index;
                }
            }
        }


        Node top = new Node("TOP");
        Tree t = new Tree(top);

        BestSplit split = indexBackPointer.get(bestAll);
        Rule r = split.getRule();

        Node single = new Node(r.getLHS().getSymbols().get(0));
        top.addDaughter(single);

        Node cleanedParent = r.getOrgLeft() != null ? single : null;


        buildTRee(bestAll, single, input, indexBackPointer, cleanedParent);

            return t;
	}


	private void treeMe(Rule rule)
    {
        List<Node> nodes = new ArrayList<>();
        if(rule.getSource().size() > 0)
        {
            for(Rule rule1 : rule.getSource())
            {
                treeMe(rule1);;
            }
        }
        else
        {
            rule.getLHS().getSymbols().get(0);
        }
    }

	private void buildTRee(Index index, Node parent, List<String> input, Map<Index, BestSplit> indexBackPointer, Node cleanedParent)
    {
        if(index.getStartIndex() == index.getEndINdex())
        {
            BestSplit split1 = indexBackPointer.get(index);

            Node myParent = parent;
            if(split1 != null)
            {
                do
                {
                    Node right = new Node(split1.getRule().getRHS().getSymbols().get(0));
                    myParent.addDaughter(right);
                    myParent = right;

                    if(split1.getSourceIndex() != null)
                    {
                        split1 = indexBackPointer.get(split1.getSourceIndex());
                    }
                    else
                    {
                        split1 = null;
                    }

                }
                while (split1 != null);
            }

            else
            {
                String word = input.get(index.getStartIndex()-1);
                Terminal terminal = new Terminal(word);

                myParent.addDaughter(terminal);
            }
        }
        else
        {
            int upperEnd = index.getEndINdex();
            int upperStart = index.getStartIndex();
            BestSplit split = indexBackPointer.get(index);


            int splitPoint = split.getSplitPoint();
            BestSplit splitToTake = split;
            Rule r;
            Node updatedParent = parent;
            do
             {
                 r = splitToTake.getRule();

                 if(splitToTake.getSourceIndex() != null)
                 {
                     Node single = new Node(r.getRHS().getSymbols().get(0));
                     updatedParent.addDaughter(single);
                     updatedParent = single;
                     splitToTake =indexBackPointer.get(splitToTake.getSourceIndex());
                 }
                 else
                 {
                     break;
                 }
            }
            while (true);


            boolean originalValidParent = false;

            if(cleanedParent != null &&  r.getOrgLeft() != null && r.getOrgLeft().equals(cleanedParent.getIdentifier()))
            {
                originalValidParent = true;
            }
            else if(r.getOrgLeft() != null)
            {
                cleanedParent = updatedParent;
                originalValidParent=  true;
            }


            Node left = new Node(r.getRHS().getSymbols().get(0));
            if(originalValidParent)
            {
                if(!left.getIdentifier().equals("**START**")) {
                    cleanedParent.addDaughter(left);
                }
            }
            else
            {
                if(!left.getIdentifier().equals("**START**"))
                {
                    updatedParent.addDaughter(left);
                }
            }

            if(!left.getIdentifier().equals("**START**"))
            {
                buildTRee(new Index(r .getRHS().getSymbols().get(0),
                        upperStart, splitPoint, null), left, input, indexBackPointer, null);
            }


            Node right = new Node(r.getRHS().getSymbols().get(1));

            if(originalValidParent && !r.isLast())
            {
                right = cleanedParent;
                //System.out.println("clean");
                //cleanedParent.addDaughter(right);
            }
            else
            {
                if(!right.getIdentifier().equals("**END**"))
                {
                    updatedParent.addDaughter(right);
                }
            }

            if(!right.getIdentifier().equals("**END**"))
            {
                buildTRee(new Index(r.getRHS().getSymbols().get(1),
                        splitPoint+1, upperEnd, null), right, input, indexBackPointer, cleanedParent);
            }

        }

    }

    double sourceProb(List<Rule> source, List<Rule> used)
    {

        double prob = 0;
        for(Rule rule : source)
        {
            prob += rule.getMinusLogProb();

            Set<List<Rule>> sources = ruleSetMap.get(rule);
            double besrSourceProb;
            if(sources != null && sources.size() > 0)
            {
                besrSourceProb = Double.MAX_VALUE;
                double current;
                for(List<Rule> ruleSource : sources)
                {
                    if(ruleSource.size() == 0)
                    {
                        current = 0;
                    }
                    else if(Collections.disjoint(ruleSource, used))
                    {
                        List<Rule>  currentUsed = new ArrayList<>(used);
                        currentUsed.addAll(ruleSource);
                        current = sourceProb(ruleSource, currentUsed);
                    }
                    else
                    {
                        current = Double.MAX_VALUE;
                    }

                    if(current < besrSourceProb)
                    {
                        besrSourceProb = current;
                    }
                }
            }
            else
            {
                besrSourceProb = 0;
            }

            prob += besrSourceProb;
        }

        return prob;
    }

	private RecCkyRes recCky(int startIndex, int endIndex, String rule, Map<Index, Double> indexMinProb, Set<Index> visited, boolean isTop)
	{
		Index index = new Index(rule, startIndex, endIndex, null);
		visited.add(index);

		int runningIndex = startIndex;
		Set<Rule> matchedRules
					= nonTerminalToRules.get(rule);

			if(matchedRules == null || matchedRules.size() == 0)
			{
				return null;
			}
			else
			{
				Double minProbFound = null;
                Rule bestRule = null;
                Integer splitIndex = null;
                List<Rule> bestSourceGlob = null;

				while (runningIndex < endIndex)
				{
					for(Rule rule1 : matchedRules)
					{

//					    if(isTop)
//                        {
//                            if(!rule1.isTop())
//                            {
//                                continue;
//                            }
//                        }

						if(rule1.isLexical())
						{
							continue;
						}

						List<String> rightSymbols
								= rule1.getRHS().getSymbols();

						if(rightSymbols.size() == 1)
						{
						    continue;
						}
						else
						{
							String first = rightSymbols.get(0);
							String second = rightSymbols.get(1);

							Index leftIndex = new Index(first, startIndex, runningIndex, rule1);
							Index rightIndex = new Index(second, runningIndex + 1, endIndex, rule1);

							Double leftRes = indexMinProb.get(leftIndex);

							if(leftRes == null)
							{
								leftRes = Double.MAX_VALUE;
							}


							Double rightRes = indexMinProb.get(rightIndex);

							if(rightRes == null)
							{
								rightRes = Double.MAX_VALUE;
							}

							double ruleProb = rule1.getMinusLogProb();

//                            Set<List<Rule>> sources = ruleSetMap.get(rule1);
//
//                            double sourceProb = 0;
//                            List<Rule> bestSource = null;
//                            if(sources != null && sources.size() > 0)
//                            {
//                                double minFound = Double.MAX_VALUE;
//                                for (List<Rule> source : sources)
//                                {
//                                  List<Rule> sourceUsed =  new ArrayList<Rule>();
//                                  sourceUsed.add(rule1);
//                                  double currentMin = sourceProb(source, sourceUsed);
//
//                                  if(minFound > currentMin)
//                                  {
//                                      minFound = currentMin;
//                                      bestSource = source;
//                                  }
//
//                                }
//                            }


                            double dupProb = leftRes + rightRes + ruleProb; //+ sourceProb;

                            if(minProbFound == null || minProbFound > dupProb)
                            {
                                minProbFound =dupProb;

                                bestRule = rule1;
                                splitIndex = runningIndex;
                                //bestSourceGlob = bestSource;
                            }
						}


					}

					runningIndex++;
				}

				if(minProbFound != null)
				{
					return new RecCkyRes(minProbFound, bestRule, splitIndex, bestSourceGlob);
				}

				return null;

			}
	}


	private Tree defaultDecode(List<String> input)
	{
		// Done: Baseline Decoder
		//       Returns a flat tree with NN labels on all leaves

		Tree t = new Tree(new Node("TOP"));
		Iterator<String> theInput = input.iterator();
		while (theInput.hasNext()) {
			String theWord = (String) theInput.next();
			Node preTerminal = new Node("NN");
			Terminal terminal = new Terminal(theWord);
			preTerminal.addDaughter(terminal);
			t.getRoot().addDaughter(preTerminal);
		}

		// TODO: CYK decoder
		//       if CYK fails,
		//       use the baseline outcome

		return t;
	}

	
	
	
}
