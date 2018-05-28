package exe4.train;

import exe4.grammar.Event;
import exe4.grammar.Grammar;
import exe4.grammar.LeftRight;
import exe4.grammar.Rule;

import java.util.*;

import exe4.tree.Node;
import exe4.tree.Tree;
import exe4.treebank.Treebank;
import javafx.util.Pair;


/**
 * 
 * @author Reut Tsarfaty
 * 
 * CLASS: Train
 * 
 * Definition: a learning component
 * Role: reads off a grammar from a treebank
 * Responsibility: keeps track of rule counts
 * 
 */

public class Train {

	 public static String TERMINAL_REPLACE_PREFIX = "+";
	 public static String PADDING = "|UN|";

    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory 
     */
	public static Train m_singTrainer = null;
	    
	public static Train getInstance()
	{
		if (m_singTrainer == null)
		{
			m_singTrainer = new Train();
		}
		return m_singTrainer;
	}
	
	public static void main(String[] args) {

	}
	
	public Grammar train(Treebank myTreebank, int remember)
	{
		List<Rule> allRules = new LinkedList<Rule>();

		Grammar myGrammar = new Grammar();
		Grammar binarizedGrammar = new Grammar();
		for (int i = 0; i < myTreebank.size(); i++) {
			Tree myTree = myTreebank.getAnalyses().get(i);
			List<Rule> theRules = getRules(myTree);
            myGrammar.addAll(theRules);

			for(Rule rule : theRules)
            {
                if(rule.toString().equals("PP-->PP"))
                {
                    System.out.println("found");
                }

                if(isIdentityToItself(rule))
                {
                    continue;
                }
                //filter rules from type X -> X (Non lexical)
                allRules.add(rule);
            }


		}

		List<Rule> binarizedRules = new ArrayList<>();
		Map<String, Rule> generatedTerminalsRules = new HashMap<>();
		for(Rule rule : allRules)
		{
            if(rule.toString().equals("PP-->PP"))
            {
                System.out.println("found");
            }

			List<Rule> rulesNoMix = replaceTerminals(rule, myGrammar.getTerminalSymbols(),
					generatedTerminalsRules, myGrammar.getLexicalEntries());

			for(Rule noMixRule : rulesNoMix)
			{
                binarizedRules.addAll(toCNF(noMixRule, remember, myGrammar.getTerminalSymbols()));
				//binarizedGrammar.addAll(binarizedRules);
			}
		}

        Pair<List<Rule>, Map<Rule, Set<List<Rule>>>> pair = fixUnitRules(binarizedRules);
        List<Rule> cnfRules = pair.getKey();
        Map<Rule, Set<List<Rule>>> ruleToSources = pair.getValue();

        binarizedGrammar.setRuleToSources(ruleToSources);
        binarizedGrammar.addAll(cnfRules);
		setLogProb(binarizedGrammar);
		return binarizedGrammar;
	}


	private boolean isIdentityToItself(Rule rule)
    {
        if(!rule.isLexical())
        {
            if(rule.getRHS().getSymbols().size() == 1)
            {
                if(rule.getRHS().toString().equals(rule.getLHS().toString()))
                {
                    return true;
                }
            }
        }

        return false;
    }

//	private Map<String, Set<Rule>> mapRulesToSets(List<Rule> binarizedRules)
//    {
//        Map<String, Set<Rule>> nonTerminalToRules = new HashMap<>();
//
//        for(Rule rule : binarizedRules)
//        {
//            Set<Rule> rules
//                    = nonTerminalToRules.get(rule.getLHS().toString());
//
//            if(rules == null)
//            {
//                rules = new HashSet<>();
//                nonTerminalToRules.put(rule.getLHS().toString(), rules);
//            }
//
//            rules.add(rule);
//        }
//
//        return nonTerminalToRules;
//    }



    private Pair<List<Rule>, Map<Rule, Set<List<Rule>>>>  fixUnitRules(List<Rule> binarizedRules)
    {
        Pair<Set<Rule>, Map<Rule, Set<List<Rule>>>>
        pair = computeUnitPairs(binarizedRules);
        Set<Rule> unitRules = pair.getKey();

        Map<Rule, Set<List<Rule>>> ruleToSource = pair.getValue();


        List<Rule> fixed = new ArrayList<>(binarizedRules);

        while(fixed.removeAll(unitRules))
        {
          System.out.println("removing");
        }


        Map<String, List<Rule>> nonTerminalules = mapRules(fixed);

        for(Rule rule : unitRules)
        {
            List<Rule> rightRules
                    =  nonTerminalules.get(rule.getRHS().toString());

            if(rightRules != null && rightRules.size() > 0)
            {
                for(Rule rule1 : rightRules)
                {
                    List<Rule> source = new ArrayList<>();

                    if(rule.getSource().size() > 0)
                    {
                        source.addAll(source);
                    }
                    else
                    {
                        source.add(rule);
                    }

                    if(rule1.getSource().size() > 0)
                    {
                        source.addAll(rule1.getSource());
                    }
                    else
                    {
                        source.add(rule1);
                    }


                    Rule dupRule = new Rule(rule.getLHS(), rule1.getRHS(),
                            rule, rule1, source);

                    Set<List<Rule>> sources = ruleToSource.get(dupRule);

                    if(sources == null)
                    {
                        sources = new HashSet<>();
                        ruleToSource.put(dupRule, sources);
                    }

                    sources.add(source);

                    dupRule.setLexical(rule1.isLexical());
                    dupRule.setTop(rule1.isTop());
                    fixed.add(dupRule);
                }

            }
            else
            {
                System.out.println("empty");
            }
        }

        for(Rule rule : binarizedRules)
        {
            Set<List<Rule>> sources = ruleToSource.get(rule);

            if(sources != null && sources.size() > 0)
            {
                sources.add(new ArrayList<Rule>());
            }
        }

        return new Pair<>(fixed, ruleToSource);

    }

    private Map<String, List<Rule>> mapRules(List<Rule> rules)
    {
        Map<String, List<Rule>> map = new HashMap<>();

        for(Rule rule : rules)
        {
            String s = rule.getLHS().toString();

            List<Rule> sRules = map.get(s);

            if(sRules == null)
            {
                sRules = new ArrayList<>();
                map.put(s, sRules);
            }

            sRules.add(rule);
        }

        return map;
    }

    private Map<String, Set<Rule>> mapRulesUnique(Set<Rule> rules)
    {
        Map<String, Set<Rule>> map = new HashMap<>();

        for(Rule rule : rules)
        {
            String s = rule.getLHS().toString();

            Set<Rule> sRules = map.get(s);

            if(sRules == null)
            {
                sRules = new HashSet<>();
                map.put(s, sRules);
            }

            sRules.add(rule);
        }

        return map;
    }

    private Pair<Set<Rule>, Map<Rule, Set<List<Rule>>>> computeUnitPairs(List<Rule>  rules)
    {
        Map<LeftRight, List<Rule>> lrRules = new HashMap<>();

        Set<Rule> unitRules = new HashSet<>();
        Map<Rule, Set<List<Rule>>> ruleToSources = new HashMap<>();

        for(Rule rule : rules)
        {
            if(isUnitRule(rule))
            {
                unitRules.add(rule);
            }
        }

        Set<Rule> old;



        do
        {
          old = new HashSet<>(unitRules);
          Map<String, Set<Rule>> map = mapRulesUnique(unitRules);
          Set<Rule> next = new HashSet<>();

          for(Rule rule : unitRules)
          {
              Set<Rule> to = map.get(rule.getRHS().toString());

              if(to != null && to.size() > 0)
              {
                  for (Rule toRule : to)
                  {
                      List<Rule> source = new ArrayList<>();

                      if(rule.getSource().size() > 0)
                      {
                          source.addAll(rule.getSource());
                      }
                      else
                      {
                          source.add(rule);
                      }

                      if(toRule.getSource() != null && toRule.getSource().size() > 0)
                      {
                          source.addAll(toRule.getSource());
                      }
                      else
                      {
                          source.add(toRule);
                      }

                      if(!toRule.getRHS().toString().equals(rule.getLHS().toString()))
                      {
                          LeftRight leftRight = new LeftRight(rule.getLHS(), toRule.getRHS());

                          List<Rule> rr = lrRules.get(leftRight);

                          if(rr == null)
                          {
                              rr = new ArrayList<>();
                              lrRules.put(leftRight, rr);
                          }

                          Rule nextOne = new Rule(rule.getLHS(), toRule.getRHS(),
                                  rule, toRule,  source);



                          Set<List<Rule>> sources = ruleToSources.get(nextOne);

                          if(sources == null)
                          {
                              sources = new HashSet<>();
                              ruleToSources.put(nextOne, sources);
                          }


                          sources.add(source);

                          next.add(nextOne);
                      }
                      else
                      {
                          System.out.println("equal");
                      }
                  }

              }
          }

          unitRules.addAll(next);
        }
        while (!unitRules.equals(old));


        return new Pair<>(unitRules, ruleToSources) ;
    }

    private boolean isUnitRule(Rule  rule)
    {
        return (!rule.isLexical() && rule.getRHS().getSymbols().size()==1);
    }

//	private List<Rule>  fixUnitRules(List<Rule> binarizedRules)
//    {
//        boolean hadChange;
//        Set<Rule> rulesVersion = new HashSet<>(binarizedRules);
//
//        Map<Integer, Map<Rule, List<Rule>>> replacements = new HashMap<>();
//        int version = 1;
//        List<String> cleaned = new ArrayList<>();
//        List<String> currentUsed = new ArrayList<>();
//
//        Map<String, Set<Rule>> nonTerminalToRules;
//        do
//        {
//            Map<Rule, List<Rule>> versionReplacements = new HashMap<>();
//            replacements.put(version, versionReplacements);
//
//            hadChange = false;
//            nonTerminalToRules = mapRulesToSets(new ArrayList<Rule>(rulesVersion));
//
//            List<Rule> removeRules = new ArrayList<>();
//            List<Rule> addRules = new ArrayList<>();
//
//            for(Map.Entry<String, Set<Rule>> nonToRulesEntry
//                    : nonTerminalToRules.entrySet())
//            {
//                String nonTerminal = nonToRulesEntry.getKey();
//
//                if(cleaned.contains(nonTerminal))
//                {
//                    continue;
//                }
//
//
//                Set<Rule> rules = nonToRulesEntry.getValue();
//
//                for(Rule rule : rules)
//                {
//
//                    if(rule.toString().equals("PP-->PP"))
//                    {
//                        System.out.println("found");
//                    }
//
//                    if(!rule.isLexical()
//                            && rule.getRHS().getSymbols().size()==1)
//                    {
//                        removeRules.add(rule);
//
//                        String right = rule.getRHS().getSymbols().get(0);
//                        Set<Rule> rightRules = nonTerminalToRules.get(right);
//
//                        if(rightRules == null)
//                        {
//                            continue;
//                        }
//
//                        for(Rule rule1 : rightRules)
//                        {
//                            if(rule1.isLexical() == false
//                                    && rule1.getRHS().getSymbols().size() == 1)
//                            {
//                                String identity = rule1.getRHS().getSymbols().get(0);
//
//                                if(currentUsed.contains(identity))
//                                {
//                                    continue;
//                                }
//                                else
//                                {
//                                    currentUsed.add(identity);
//                                }
//                            }
//
//                            Rule generatedRule = new Rule(new Event(nonTerminal), rule1.getRHS());
//                            generatedRule.setLexical(rule1.isLexical());
//
//                            if(isIdentityToItself(generatedRule))
//                            {
//                                continue;
//                            }
//
//                            List<Rule> current = versionReplacements.get(rule);
//                            if(current == null)
//                            {
//                                current = new ArrayList<>();
//                                versionReplacements.put(rule, current);
//                            }
//
//                            if(generatedRule.isLexical()
//                                    || generatedRule.getRHS().getSymbols().size() != 1)
//                            {
//                                current.add(generatedRule);
//                            }
//
//                            addRules.add(generatedRule);
//                        }
//                    }
//                }
//
//                if(versionReplacements.size() > 0)
//                {
//                    version++;
//                }
//
//                if(addRules.size() > 0)
//                {
//                    hadChange = true;
//                    rulesVersion.removeAll(removeRules);
//                    rulesVersion.addAll(addRules);
//                    break;
//                }
//                else
//                {
//                    rulesVersion.removeAll(removeRules);
//                    currentUsed = new ArrayList<>();
//                    cleaned.add(nonTerminal);
//                    break;
//                }
//            }
//        }
//        while (hadChange || cleaned.size() < nonTerminalToRules.size());
//
//
//        List<Rule> cnfRules = new ArrayList<>(binarizedRules);
//
//
//        List<Rule> replacementPotentials = new ArrayList<>();
//        List<Rule> nonReplacementRules = new ArrayList<>();
//        for(Rule rule : cnfRules)
//        {
//            if(rule.isLexical() == false
//                    && rule.getRHS().getSymbols().size() == 1)
//            {
//                replacementPotentials.add(rule);
//            }
//            else
//            {
//                nonReplacementRules.add(rule);
//            }
//        }
//
//        for(int i=0; i <= version; i++)
//        {
//            Map<Rule, List<Rule>> replace
//                    =  replacements.get(i);
//
//            if(replace == null || replace.size() == 0)
//            {
//                continue;
//            }
//
//            for(Rule rule : replacementPotentials)
//            {
//                List<Rule> ruleReplacements = replace.get(rule);
//
//                if(ruleReplacements != null)
//                {
//                    nonReplacementRules.addAll(ruleReplacements);
//                }
//                else
//                {
//                    nonReplacementRules.add(rule);
//                }
//            }
//        }
//
//        return nonReplacementRules;
//    }

	private List<Rule> replaceTerminals(Rule rule, Set<String> terminals, Map<String, Rule> generatedTerminalsRules,
										Map<String, Set<Rule>> lexicalEntries)
	{
        if(rule.toString().equals("PP-->PP"))
        {
            System.out.println("found");
        }

		if(rule.isLexical())
		{
			return Arrays.asList(rule);
		}



		List<String> symbols = new ArrayList<>(rule.getRHS().getSymbols());

		symbols.retainAll(terminals);


		List<Rule> rules = new ArrayList<>();

		if(symbols.size() > 0)
		{
			StringBuilder generatedSymbols = new StringBuilder();

			for(String element : symbols)
			{
				if(lexicalEntries.get(element) == null
						&& terminals.contains(element))
				{
					Rule rule1 = generatedTerminalsRules.get(element);
					String newSymbol;
					if(rule1 != null)
					{
						newSymbol = rule1.getRHS().toString();
					}
					else
					{
						newSymbol = "#"+element+"#";
						Rule terminalRule = new Rule(new Event(newSymbol), new Event(element), true);
						generatedTerminalsRules.put(element, terminalRule);
						rules.add(terminalRule);
					}

					generatedSymbols.append(newSymbol);
					generatedSymbols.append(" ");
				}
				else
				{
					generatedSymbols.append(element);
					generatedSymbols.append(" ");
				}
			}

			String generaedFixed = generatedSymbols.deleteCharAt(generatedSymbols.length()-1).toString();
			Rule replacedRule = new Rule(rule.getLHS(), new Event(generaedFixed));
			rules.add(replacedRule);;

            if(replacedRule.toString().equals("PP-->PP"))
            {
                System.out.println("found");
            }
		}
		else
		{
			rules.add(rule);

            if(rule.toString().equals("PP-->PP"))
            {
                System.out.println("found");
            }
		}

		return rules;
	}

	private void setLogProb(Grammar grammar)
	{
		for(Map.Entry<Rule, Integer> ruleCountEntry
				: grammar.getRuleCounts().entrySet())
		{

			int ruleCount = ruleCountEntry.getValue();
			Rule rule = ruleCountEntry.getKey();
			int leftCounter = grammar.getLeftSideCounter().get(rule.getLHS());

			double prob = ruleCount / (double)leftCounter;

			double logProb =  Math.log(prob);

			if(logProb == 0)
            {
                rule.setMinusLogProb(0);
            }
            else
            {
                rule.setMinusLogProb(-logProb);
            }

		}
	}


	private List<Rule> toCNF(Rule rule,int remember, Set<String> terminals)
	{

        if(rule.toString().equals("PP-->PP"))
        {
            System.out.println("found");
        }

		Event right = rule.getRHS();
		List<String> symbols = right.getSymbols();

		if(symbols.size() == 1)
		{
			return Arrays.asList(rule);
		}

		String leftSide = rule.getLHS().toString();
		List<Rule> rules = new LinkedList<Rule>();
		List<String> seenSiblings = new LinkedList<String>();
		String prevRightSide = leftSide;
		while (symbols.size() > 0)
		{
			String first = symbols.get(0);
			seenSiblings.add(first);
			List<String> others;
			String rightSideSir;
			boolean isLexical = false;
			if(symbols.size() > 2)
			{
				others = symbols.subList(1, symbols.size());
				rightSideSir = ruleStr(leftSide, seenSiblings, remember);
			}
			else
			{
				others = Collections.EMPTY_LIST;
				rightSideSir = null;


				isLexical = terminals.contains(first) && symbols.size() == 1;
			}

			String rightEvent;
			if(rightSideSir != null)
			{
				rightEvent = first + " "+ rightSideSir;
			}
			else
			{
				rightEvent = first +" " + symbols.get(1);
			}

			Rule left1 = new Rule(new Event(prevRightSide), new Event(rightEvent));
			left1.setLexical(isLexical);
			rules.add(left1);

            if(left1.toString().equals("PP-->PP"))
            {
                System.out.println("found");
            }

			prevRightSide = rightSideSir;
			symbols = others;

		}



		return rules;
	}

	private String ruleStr(String leftRule, List<String> siblings, int remember)
	{
		StringBuilder str = new StringBuilder();
		str.append(leftRule);
		str.append("[");

		List<String> take;
		if(remember > 0 && siblings.size() > remember)
		{
			take = siblings.subList(siblings.size()-remember, siblings.size());
		}
		else if(remember == 0)
		{
			take = Collections.EMPTY_LIST;
		}
		else
		{
			take = siblings;
			while (take.size() < remember)
			{
				take.add(0, PADDING);
			}
		}


		boolean first = true;
		for(String symbol : take)
		{
			if(!first)
			{
				str.append("_");
			}

			str.append(symbol);
			first = false;
		}

		str.append("]");

		return str.toString();
	}

	public List<Rule> getRules(Tree myTree)
	{
		List<Rule> theRules = new ArrayList<Rule>();
		
		List<Node> myNodes = myTree.getNodes();
		for (int j = 0; j < myNodes.size(); j++) {
			Node myNode = myNodes.get(j);
			if (myNode.isInternal())
			{
				Event eLHS = new Event(myNode.getIdentifier());
				Iterator<Node> theDaughters = myNode.getDaughters().iterator();
				StringBuffer sb = new StringBuffer();

				while (theDaughters.hasNext())
				{
					Node n = (Node) theDaughters.next();
					sb.append(n.getIdentifier());
					if (theDaughters.hasNext())
						sb.append(" ");
				}

				Event eRHS = new Event (sb.toString());
				Rule theRule = new Rule(eLHS, eRHS);
				if (myNode.isPreTerminal())
					theRule.setLexical(true);
				if (myNode.isRoot())
					theRule.setTop(true);

                if(theRule.toString().equals("PP-->PP"))
                {
                    System.out.println("found");
                }

				theRules.add(theRule);
			}	
		}
		return theRules;
	}
	
}
