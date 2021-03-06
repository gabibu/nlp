package exe4.parse;

import exe4.bracketimport.TreebankReader;
import exe4.decode.Decode;
import exe4.grammar.Grammar;
import exe4.grammar.Rule;
import exe4.train.Train;
import exe4.tree.Node;
import exe4.tree.Tree;
import exe4.treebank.Treebank;
import exe4.utils.LineWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class Parse {

	/**
	 *
	 * @author Reut Tsarfaty
	 * @date 27 April 2013
	 *
	 * 
	 */
	
	public static void main(String[] args) {
		
		//**************************//
		//*      NLP@IDC PA2       *//
		//*   Statistical Parsing  *//
		//*     Point-of-Entry     *//
		//**************************//
		
		if (args.length < 4)
		{
			System.out.println("Usage: Parse <goldset> <trainset> <experiment-identifier-string>");
			return;
		}

		// 1. read input
		String goldFile = args[0];
		String trainFile = args[1];

		Treebank myGoldTreebank = TreebankReader.getInstance().read(true, goldFile);
		Treebank myTrainTreebank = TreebankReader.getInstance().read(true, trainFile);
		int remember = Integer.parseInt(args[3]);

		if(args.length >= 5 && args[4].equals("gabi"))
		{
			for( remember = -1; remember<=2 ;remember++)
			{
				// 2. transform trees
				// TODO

				// 3. train
				Grammar myGrammar = Train.getInstance().train(myTrainTreebank, remember);

				// 4. decode
				List<Tree> myParseTrees = new ArrayList<Tree>();

				for (int i = 0; i < myGoldTreebank.size(); i++)
				{
					System.out.println("decoding index " + i +" from -> " +  myGoldTreebank.size());
					List<String> mySentence = myGoldTreebank.getAnalyses().get(i).getYield();
					Tree myParseTree = Decode.getInstance(myGrammar).decode(mySentence);
					myParseTrees.add(myParseTree);
				}


				writeOutput(args[2] +"_" + remember+"_.txt", myGrammar, myParseTrees);
			}

		}
		else
		{
// 2. transform trees
			// TODO

			// 3. train
			Grammar myGrammar = Train.getInstance().train(myTrainTreebank, remember);

			// 4. decode
			List<Tree> myParseTrees = new ArrayList<Tree>();

			for (int i = 0; i < myGoldTreebank.size(); i++)
			{
				System.out.println("decoding index " + i +" from -> " +  myGoldTreebank.size());
				List<String> mySentence = myGoldTreebank.getAnalyses().get(i).getYield();
				Tree myParseTree = Decode.getInstance(myGrammar).decode(mySentence);
				myParseTrees.add(myParseTree);
			}


			writeOutput(args[2], myGrammar, myParseTrees);
		}



	}


	
	/**
	 * Writes output to files:
	 * = the trees are written into a .parsed file
	 * = the grammar rules are written into a .gram file
	 * = the lexicon entries are written into a .lex file
	 */
	private static void writeOutput(
			String sExperimentName, 
			Grammar myGrammar,
			List<Tree> myTrees) {
		
		writeParseTrees(sExperimentName, myTrees);
		writeGrammarRules(sExperimentName, myGrammar);
		writeLexicalEntries(sExperimentName, myGrammar);
	}

	/**
	 * Writes the parsed trees into a file.
	 */
	private static void writeParseTrees(String sExperimentName,
			List<Tree> myTrees) {
		LineWriter writer = new LineWriter(sExperimentName+".parsed");
		for (int i = 0; i < myTrees.size(); i++) {
			writer.writeLine(myTrees.get(i).toString());
		}
		writer.close();
	}
	
	/**
	 * Writes the grammar rules into a file.
	 */
	private static void writeGrammarRules(String sExperimentName,
			Grammar myGrammar) {
		LineWriter writer;
		writer = new LineWriter(sExperimentName+".gram");
		Set<Rule> myRules = myGrammar.getSyntacticRules();
		Iterator<Rule> myItrRules = myRules.iterator();
		while (myItrRules.hasNext()) {
			Rule r = (Rule) myItrRules.next();
			writer.writeLine(r.getMinusLogProb()+"\t"+r.getLHS()+"\t"+r.getRHS()); 
		}
		writer.close();
	}
	
	/**
	 * Writes the lexical entries into a file.
	 */
	private static void writeLexicalEntries(String sExperimentName, Grammar myGrammar) {
		LineWriter writer;
		Iterator<Rule> myItrRules;
		writer = new LineWriter(sExperimentName+".lex");
		Set<String> myEntries = myGrammar.getLexicalEntries().keySet();
		Iterator<String> myItrEntries = myEntries.iterator();
		while (myItrEntries.hasNext()) {
			String myLexEntry = myItrEntries.next();
			StringBuffer sb = new StringBuffer();
			sb.append(myLexEntry);
			sb.append("\t");
			Set<Rule> myLexRules =   myGrammar.getLexicalEntries().get(myLexEntry);
			myItrRules = myLexRules.iterator();
			while (myItrRules.hasNext()) {
				Rule r = (Rule) myItrRules.next();
				sb.append(r.getLHS().toString());
				sb.append(" ");
				sb.append(r.getMinusLogProb());
				sb.append(" ");
			}
			writer.writeLine(sb.toString());
		}
	}

	

	


}
