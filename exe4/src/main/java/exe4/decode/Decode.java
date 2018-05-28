package exe4.decode;

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

			if(rules != null)
			{
				for(Rule rule : rules)
				{
					Index index = new Index(rule.getLHS().toString(), i + 1, i + 1,
                            rule);

                    Set<List<Rule>> sources = ruleSetMap.get(rule);
                    List<Rule> bestSource = null;
                    double sourceProbMin;
                    if(sources != null && sources.size() > 0)
                    {
                        sourceProbMin = Double.MAX_VALUE;
                        for(List<Rule> source : sources)
                        {
                           double current = sourceProb(source);

                           if(current < sourceProbMin)
                           {
                               sourceProbMin = current;
                               bestSource = source;
                           }
                        }
                    }
                    else
                    {
                        sourceProbMin = 0;
                    }

					Double prob = indexMinProb.get(index);

					if(prob == null || rule.getMinusLogProb() < prob)
					{
						indexMinProb.put(index, rule.getMinusLogProb());

                        indexBackPointer.put(index, new BestSplit(rule, -1,
                                bestSource));
					}
				}
			}

		}




		for(int l = 1; l < input.size(); l++)
		{

			for(int i =  1; i  < input.size(); i ++)
			{
				int j = i + l;
                if(j > input.size())
                {
                   continue;
                }

				for(String nonTerminal : nonTerminalToRules.keySet())
				{

					Set<Index> visited = new HashSet<>();
                    RecCkyRes  res = recCky(i, j, nonTerminal, indexMinProb, visited);


                    if(res == null)
                    {
                        continue;
                    }


                    double prob = res.getProb();

					Index index
							= new Index(nonTerminal, i, j, res.getRule());

					Double hasProb = indexMinProb.get(index);

					if(hasProb == null || hasProb > prob)
					{
						indexMinProb.put(index, prob);

                        indexBackPointer.put(index, new BestSplit(res.getRule(), res.getSplitPoint(),
                                res.getSource()));
					}
				}
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


        Tree t = new Tree(new Node("TOP"));
        Node preTerminal = new Node("S");
        t.getRoot().addDaughter(preTerminal);

        buildTRee(bestAll, preTerminal, input, indexBackPointer);

        String x1 = t.toString();


		return null;
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

	private void buildTRee(Index index, Node parent, List<String> input, Map<Index, BestSplit> indexBackPointer)
    {
        if(index.getStartIndex() == index.getEndINdex())
        {
            BestSplit split1 = indexBackPointer.get(index);

            String word = input.get(index.getStartIndex()-1);
            Set<Rule> rules = m_mapLexicalRules.get(word);

            for(Rule rule : rules)
            {
                if(rule.getLHS().toString().equals(index.getRuleLeftSide()))
                {
                   System.out.println("sss");
                }
            }

            Terminal terminal = new Terminal(word);
            parent.addDaughter(terminal);
        }
        else
        {
            int upperEnd = index.getEndINdex();
            int upperStart = index.getStartIndex();
            BestSplit split = indexBackPointer.get(index);
            Rule r = split.getRule();
            int splitPoint = split.getSplitPoint();

            Node left = new Node(r .getRHS().getSymbols().get(0));
            parent.addDaughter(left);

            Node right = new Node(r .getRHS().getSymbols().get(1));
            parent.addDaughter(right);

            buildTRee(new Index(r .getRHS().getSymbols().get(0),
                    upperStart, splitPoint, null), left, input, indexBackPointer);

            buildTRee(new Index(r.getRHS().getSymbols().get(1),
                    splitPoint+1, upperEnd, null), right, input, indexBackPointer);
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
                double current = 0;
                for(List<Rule> ruleSource : sources)
                {
                    current = sourceProb(ruleSource);
                }

                if(current < besrSourceProb)
                {
                    besrSourceProb = current;
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

	private RecCkyRes recCky(int startIndex, int endIndex, String rule, Map<Index, Double> indexMinProb, Set<Index> visited)
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

                            Set<List<Rule>> sources = ruleSetMap.get(rule1);

                            double sourceProb = 0;
                            List<Rule> bestSource = null;
                            if(sources != null && sources.size() > 0)
                            {
                                double minFound = Double.MAX_VALUE;
                                for (List<Rule> source : sources)
                                {
                                  double currentMin = sourceProb(source);

                                  if(minFound > currentMin)
                                  {
                                      minFound = currentMin;
                                      bestSource = source;
                                  }

                                }
                            }


                            double dupProb = leftRes + rightRes + ruleProb + sourceProb;

                            if(minProbFound == null || minProbFound > dupProb)
                            {
                                minProbFound =dupProb;

                                bestRule = rule1;
                                splitIndex = runningIndex;
                                bestSourceGlob = bestSource;
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
