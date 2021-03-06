//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

//    Modified by:		Luke Newton
//	  Date:				January 23, 2021

import java.lang.Math;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

class Brain extends Thread implements SensorInput
{
	// the name of the file containing the agent behaviors
	private static final String agentSpecFilename = "AgentSpec.txt";

	// a set of possible action outputs for the agent specification function (the Ac in E -> Ac)
	private static enum Action {Turn, Dash, Kick};

	// a mapping of environemtn states to actions (this is the agent function E -> Ac)
	private HashMap<EnvironmentState, String> agentMapping;

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

		// read in specifcation file and translate it into the agent function
		ArrayList<String> agentSpec = readAgentSpec();
		agentMapping = parseSpec(agentSpec);

		start();
    }

	/**
	 * Read in the agent specification file to determine agent behavior
	 *
	 * returns the contents of the specification file as an ArrayList of strings
	 */
	private ArrayList<String> readAgentSpec(){
		try {
			File file = new File(agentSpecFilename);
			BufferedReader br = new BufferedReader(new FileReader(file));

			ArrayList<String> lines = new ArrayList<>();
			String st;
			while ((st = br.readLine()) != null)
				lines.add(st);
			return lines;
		}catch(Exception e){
			return null;
		}
	}

	/**
	 * translates agent specification file contents into an agent function mapping
	 *
	 * @param agentSpec a list of strings representing the contents of the specification file
	 * @return a map of environment states to actions (E -> Ac)
	 */
	private HashMap<EnvironmentState, String> parseSpec(ArrayList<String> agentSpec){
		HashMap<EnvironmentState, String> mapping = new HashMap<>();

		for(String behavior: agentSpec){
			if(behavior.trim().isEmpty() || behavior.startsWith("#"))
				continue;

			String[] split_behavior = behavior.split("->");
			String actionString = split_behavior[1];

			String[] environmentStates = split_behavior[0].replaceAll("\\(", "").replaceAll("\\)","").split(",");
			Visibility ballVisibility = Visibility.valueOf(environmentStates[0].trim());
			BallProximity ballProximity = BallProximity.valueOf(environmentStates[1].trim());
			Visibility goalVisibility = Visibility.valueOf(environmentStates[2].trim());

			mapping.put(new EnvironmentState(ballVisibility, ballProximity, goalVisibility), actionString);
		}

		return mapping;
	}

    public void run() {
		ObjectInfo ball;
		ObjectInfo goal;

		// first put it somewhere on my side
		if(Pattern.matches("^before_kick_off.*",m_playMode))
			m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );

		while( !m_timeOver ){
			//STEP 1: determine current environment state
			Visibility ballVisibility;
			BallProximity ballProximity;
			Visibility goalVisibility;
			ball = m_memory.getObject("ball");
			if( ball == null ){
			   ballVisibility = Visibility.NotVisible;
			   ballProximity = BallProximity.Unknown;
			} else {
				ballProximity = ball.m_distance > 1.0 ? BallProximity.Far : BallProximity.Close;
				ballVisibility = ball.m_direction != 0 ? Visibility.Visible : Visibility.DirectlyInFront;
			}
			goal = m_side == 'l' ? m_memory.getObject("goal r") : m_memory.getObject("goal l");
			if( goal == null ){
				goalVisibility = Visibility.NotVisible;
			} else if( goal.m_direction != 0 ) {
				goalVisibility = Visibility.Visible;
			} else {
				goalVisibility = Visibility.DirectlyInFront;
			}
			EnvironmentState environmentState = new EnvironmentState(ballVisibility, ballProximity, goalVisibility);



			// STEP 2: use agent mapping to determine action
			String actionString = agentMapping.get(environmentState);
			System.out.println(environmentState + " -> " + actionString);
			String[] actionComponents = actionString.split(":");
			Action action = Action.valueOf(actionComponents[0].trim());
			switch(action){
				case Dash:
					m_krislet.dash(Integer.parseInt(actionComponents[1]));
					break;
				case Turn:
					if(isInteger(actionComponents[1])){
						m_krislet.turn(Integer.parseInt(actionComponents[1]));
					} else if(actionComponents[1].toLowerCase().equals("ball") && ball != null){
						m_krislet.turn(ball.m_direction);
					} else if(actionComponents[1].toLowerCase().equals("goal") && goal != null){
						m_krislet.turn(goal.m_direction);
					}
					m_memory.waitForNewInfo();
					break;
				case Kick:
					if(isInteger(actionComponents[2])){
						m_krislet.kick(Integer.parseInt(actionComponents[1]), Integer.parseInt(actionComponents[2]));
					} else if(actionComponents[2].toLowerCase().equals("ball") && ball != null){
						m_krislet.kick(Integer.parseInt(actionComponents[1]), ball.m_direction);
					} else if(actionComponents[2].toLowerCase().equals("goal") && goal != null){
						m_krislet.kick(Integer.parseInt(actionComponents[1]), goal.m_direction);
					}
					break;
			}


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
	 * Determines if a string contains a number
	 * @param str the string to check
	 * @return true if the input str can be parsed into a number
	 */
	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException e){
			return false;
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
