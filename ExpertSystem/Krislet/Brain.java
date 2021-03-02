//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

//    Modified by:		Luke Newton
//	  Date:				January 28, 2021

import java.lang.Math;
import java.util.regex.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

class Brain extends Thread implements SensorInput
{
	// the name of the file containing the agent behaviors
	private static final String agentSpecFilename = "AgentSpec.txt";
	// the list of facts the agent has in its knowledge base
	private ArrayList<SimplePredicate> facts;
	// the list of facts that an agent should not remember
	private ArrayList<SimplePredicate> transientFacts;
	// a list of fact lists, where at most only one fact from each list should be in the knowledge base at any time
	private ArrayList<ArrayList<SimplePredicate>> conflictingFacts;
	// the list of rules the agent has in its knowledge base
	private ArrayList<Rule> rules;
	// the last message received from the server about the state of the game
	private String lastMessage;
	// an enumeration of the possible agent actions
	private enum Action {turn, dash, kick;}

    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    // - starts thread for this object
    public Brain(SendCommand krislet, String team, char side, int number, String playMode) {
		m_timeOver = false;
		m_krislet = krislet;
		m_memory = new Memory();
		//m_team = team;
		m_side = side;
		// m_number = number;
		m_playMode = playMode;

		lastMessage = "";

		//read in agent specification file for behaviors
		ArrayList<String> fileContents = readAgentSpec();
		facts = readAgentSpecFacts(fileContents);
		rules = readAgentSpecRules(fileContents);
		transientFacts = readAgentSpecTransientFacts(fileContents);
		conflictingFacts  = readAgentSpecConflictingFacts(fileContents);

		start();
    }

    /** the agent's behavior */
    public void run() {
		// first put it somewhere on my side
		if(Pattern.matches("^before_kick_off.*",m_playMode))
			m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );

		while( !m_timeOver ){
			if(lastMessage.equals(m_memory.getLastMessage()))
				lastMessage = " ";

			boolean actionPerformed = false;
			boolean factAdded = true;
			while (factAdded && !actionPerformed){
				/* repeatedly check rules until a rule holds that results in an action, or
				no new knowledge is gained over an iteration (no action will ever be
				perfromed)*/
				factAdded = false;
				for (Rule rule: rules){
					if (rule.holds(facts, rules, m_memory)){
						/* when we find a rule that holds, we can add its consequent to the
						list of facts by implication elimination once any conlficting facts
						have been removed */
						SimplePredicate consequent = rule.getConsequent();

						// check if the knowledge base has any conflicting facts
						// and remove them
						boolean foundConflict = false;
						for(ArrayList<SimplePredicate> conflictingPredicates: conflictingFacts){
							for(SimplePredicate p: conflictingPredicates){
								if(consequent.equals(p)) {
									facts.removeAll(conflictingPredicates);
									foundConflict = true;
									break;
								}
							}
							if (foundConflict)
								break;
						}

						// check if the consequence directs the agent to do an action
						String[] consequentParts = consequent.getPredicate().split(" ");
						if (consequentParts.length > 1){
							switch(Action.valueOf(consequentParts[1])){
								case turn:
									m_krislet.turn(Integer.parseInt(consequentParts[2]));
									break;
								case dash:
									m_krislet.dash(Integer.parseInt(consequentParts[2]));
									break;
								case kick:
									m_krislet.kick(100, Integer.parseInt(consequentParts[2]));
									break;
							}
							actionPerformed = true;
							break;
						}
						//add the fact to the knowledge base
						if(!facts.contains(consequent)) {
							facts.add(consequent);
							factAdded = true;
						}
					}
				}
			}
			//remove all transient facts from our knowledge base
			facts.removeAll(transientFacts);

			//store the last message received so we can see if we received a message while sleeping
			lastMessage = m_memory.getLastMessage();

			// sleep one step to ensure that we will not send
			// two commands in one cycle.
			try{
				Thread.sleep(2*SoccerParams.simulator_step);
			}catch(Exception e){}
		}
		m_krislet.bye();
    }

    //===========================================================================
    // Here are suporting functions for implement logic

	/**
	 * parse the facts listed in the agent specification file
	 *
	 * @param fileContents an arraylist containing the contents of the agent specification file
	 * @return an arraylist of facts for the knowledge base
	 */
	private ArrayList<SimplePredicate> readAgentSpecFacts(ArrayList<String> fileContents){
		return readAgentSpecField(fileContents, "FACTS");
	}

	/**
	 * parse the transient facts listed in the agent specification file
	 *
	 * @param fileContents an arraylist containing the contents of the agent specification file
	 * @return an arraylist of transient facts to forget after each action is taken
	 */
	private ArrayList<SimplePredicate> readAgentSpecTransientFacts(ArrayList<String> fileContents){
		return readAgentSpecField(fileContents, "TRANSIENT_FACTS");
	}

	/**
	 * a helper function to parse a text containing a list of facts
	 *
	 * @param fileContents an arraylist containing the contents of the agent specification file
	 * @return an arraylist of facts
	 */
	private ArrayList<SimplePredicate> readAgentSpecField(ArrayList<String> fileContents, String tag){
		ArrayList<SimplePredicate> facts = new ArrayList<>();
		boolean foundStart = false;
		for(String line: fileContents){
			if (line.equals(tag)){
				foundStart = true;
				continue;
			} else if (line.equals("END_"+tag))
				return facts;
			else if(foundStart)
				facts.add(new SimplePredicate(line));
		}
		return facts;
	}

	/**
	 * parse the conflicting fact lists in the agent specification
	 *
	 * @param fileContents an arraylist containing the contents of the agent specification file
	 * @return a list of lists where each list is a set of conflicting facts
	 */
	private ArrayList<ArrayList<SimplePredicate>> readAgentSpecConflictingFacts(ArrayList<String> fileContents){
		ArrayList<ArrayList<SimplePredicate>> facts = new ArrayList<>();
		boolean foundStart = false;
		for(String line: fileContents){
			if (line.equals("CONFLICTING_FACTS")){
				foundStart = true;
				continue;
			} else if (line.equals("END_CONFLICTING_FACTS")){
				return facts;
			} else if(foundStart){
				ArrayList<SimplePredicate> conflicts = new ArrayList<>();
				for(String c: line.trim().split(","))
					conflicts.add(new SimplePredicate(c.trim()));
				facts.add(conflicts);
			}
		}
		return facts;
	}

	/**
	 * parse the rules contained in the agent specification
	 *
	 * @param fileContents an arraylist containing the contents of the agent specification file
	 * @return an arraylist of rules
	 */
	private ArrayList<Rule> readAgentSpecRules(ArrayList<String> fileContents){
		ArrayList<Rule> rules = new ArrayList<>();
		boolean foundStart = false;

		for(String line: fileContents){
			if (line.equals("RULES")){
				foundStart = true;
				continue;
			} else if (line.equals("END_RULES")){
				return rules;
			} else if(foundStart){
				String[] splitLine = line.split("->");
				String antecedentString = splitLine[0].trim();
				String consequentString = splitLine[1].trim();

				String[] predicates = antecedentString.split("AND");
				ArrayList<Predicate> antecedent = new ArrayList<>();
				for (String s : predicates){
					String[] predicateParts = s.trim().split(" ");
					if (predicateParts.length == 1){
						antecedent.add(new SimplePredicate(s.trim()));
					} else {
						String object = predicateParts[0];
						ObjectAttribute attribute = ObjectAttribute.valueOf(predicateParts[1]);
						if(attribute.equals(ObjectAttribute.VISIBLE) || attribute.equals(ObjectAttribute.NOTVISIBLE) || attribute.equals(ObjectAttribute.HEARD)){
							antecedent.add(new ComparisonPredicate(object, attribute));
							continue;
						}
						ComparisonOperator comparisonOperator = ComparisonOperator.valueOf(predicateParts[2]);
						float value = Float.parseFloat(predicateParts[3]);
						antecedent.add(new ComparisonPredicate(object, attribute, comparisonOperator, value));
					}
				}
				rules.add(new Rule(antecedent, new SimplePredicate(consequentString)));
			}
		}
		return rules;
	}


	/**
	 * Read in the agent specification file to determine agent behavior
	 *
	 * returns the contents of the specification file as an ArrayList of strings
	 */
	private ArrayList<String> readAgentSpec(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(agentSpecFilename)));
			ArrayList<String> lines = new ArrayList<>();
			String st;
			while ((st = br.readLine()) != null){
				if(st.trim().isEmpty() || st.trim().startsWith("#"))
					continue;
				lines.add(st.trim());
			}
			return lines;
		}catch(Exception e){
			return null;
		}
	}

    //===========================================================================
    // Implementation of SensorInput Interface

    //---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info)
    {
	m_memory.store(info);
    }


    //---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message)
    {
    }

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message)
    {
		m_memory.setLastMessage(message);

		if(message.compareTo("time_over") == 0)
	    m_timeOver = true;

    }


    //===========================================================================
    // Private members
    private SendCommand	                m_krislet;			// robot which is controled by this brain
    private Memory			m_memory;				// place where all information is stored
    private char			m_side;
    volatile private boolean		m_timeOver;
    private String                      m_playMode;
    
}
