package exe4.grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import exe4.utils.CountMap;

/**
 * 
 * @author rtsarfat
 *
 * CLASS: Grammar
 * 
 * Definition: formally <N,T,S,R> 
 * Role: holds two collection of grammatical and lexical grammar rules  
 * Responsibility: define a start symbol 
 * 
 */

public class Grammar {

	protected Set<String> m_setStartSymbols = new HashSet<String>();
	protected Set<String> m_setTerminalSymbols = new HashSet<String>();
	protected Set<String> m_setNonTerminalSymbols = new HashSet<String>();
	private Set<Rule> unitRules;

	protected Set<Rule> m_setSyntacticRules = new HashSet<Rule>();
	protected Set<Rule> m_setLexicalRules = new HashSet<Rule>();
	protected CountMap<Rule> m_cmRuleCounts = new CountMap<Rule>();
	protected Map<String, Set<Rule>> m_lexLexicalEntries = new HashMap<String, Set<Rule>>();
	private Map<Event, Integer> leftSideCounter = new HashMap<Event, Integer>();

	private Map<Rule, Set<List<Rule>>> ruleToSources;

	public Map<Rule, Set<List<Rule>>> getRuleToSources() {
		return ruleToSources;
	}

	public void setRuleToSources(Map<Rule, Set<List<Rule>>> ruleToSources) {
		this.ruleToSources = ruleToSources;
	}

	protected Map<String, Set<Rule>> nonTerminalToRules = new HashMap<String, Set<Rule>>();

	public Set<Rule> getUnitRules() {
		return unitRules;
	}

	public void setUnitRules(Set<Rule> unitRules) {
		this.unitRules = unitRules;
	}

	public Grammar() {
		super();
	}
	
	public Map<String, Set<Rule>> getLexicalEntries() {
		return m_lexLexicalEntries;
	}

	public Map<String, Set<Rule>> getNonTerminalToRules() {
		return nonTerminalToRules;
	}

	public void setLexicalEntries(Map<String, Set<Rule>> m_lexLexicalEntries) {
		this.m_lexLexicalEntries = m_lexLexicalEntries;
	}

	public CountMap<Rule> getRuleCounts() {
		return m_cmRuleCounts;
	}

	public void addRule(Rule r)
	{	
		Event eLhs = r.getLHS();
		Event eRhs = r.getRHS();
				
		if (r.isLexical())
		{
			// update the sets T, N, R
			getLexicalRules().add(r);
			getNonTerminalSymbols().addAll(eLhs.getSymbols());
			getTerminalSymbols().addAll(eRhs.getSymbols());
			
			// update the dictionary
			if (!getLexicalEntries().containsKey(eRhs.toString()))
				getLexicalEntries().put(eRhs.toString(), new HashSet<Rule>());

			getLexicalEntries().get(eRhs.toString()).add(r);
		}
		else 
		{
			// update the sets T, N, R
			getSyntacticRules().add(r);
			getNonTerminalSymbols().addAll(eLhs.getSymbols());
			getNonTerminalSymbols().addAll(eRhs.getSymbols());
		}

		if(!r.isLexical())
		{
			Set<Rule> rules = nonTerminalToRules.get(r.getLHS().toString());

			if(rules == null)
			{
				rules = new HashSet<>();
				nonTerminalToRules.put(r.getLHS().toString(), rules);;
			}

			rules.add(r);
		}

		
		// update the start symbol(s)
		if (r.isTop())
			getStartSymbols().add(eLhs.toString());

		Integer counter  = leftSideCounter.get(eLhs);
		if(counter == null)
		{
			counter = 0;
		}

		leftSideCounter.put(eLhs, counter + 1);

		// update the rule counts 
		getRuleCounts().increment(r);
	}

	public Map<Event, Integer> getLeftSideCounter() {
		return leftSideCounter;
	}

	public Set<String> getNonTerminalSymbols() {
		return m_setNonTerminalSymbols;
	}

	public Set<Rule> getSyntacticRules() {
		return m_setSyntacticRules;
	}

	public void setSyntacticRules(Set<Rule> syntacticRules) {
		m_setSyntacticRules = syntacticRules;
	}

	public Set<Rule> getLexicalRules() {
		return m_setLexicalRules;
	}

	public void setLexicalRules(Set<Rule> lexicalRules) {
		m_setLexicalRules = lexicalRules;
	}

	public Set<String> getStartSymbols() {
		return m_setStartSymbols;
	}

	public void setStartSymbols(Set<String> startSymbols) {
		m_setStartSymbols = startSymbols;
	}

	public Set<String> getTerminalSymbols() {
		return m_setTerminalSymbols;
	}

	public void setTerminalSymbols(Set<String> terminalSymbols) {
		m_setTerminalSymbols = terminalSymbols;
	}

	public int getNumberOfLexicalRuleTypes()
	{
		return getLexicalRules().size();
	}
	
	public int getNumberOfSyntacticRuleTypes()
	{
		return getSyntacticRules().size();
	}
	
	public int getNumberOfStartSymbols()
	{
		return getStartSymbols().size();
	}
	
	public int getNumberOfTerminalSymbols()
	{
		return getTerminalSymbols().size();
	}
	
	public void addStartSymbol(String string) {
		getStartSymbols().add(string);
	}

	public void removeStartSymbol(String string) {
		getStartSymbols().remove(string);
	}

	public void addAll(List<Rule> theRules) {
		for (int i = 0; i < theRules.size(); i++) {
			addRule(theRules.get(i));
		}
	}

}
