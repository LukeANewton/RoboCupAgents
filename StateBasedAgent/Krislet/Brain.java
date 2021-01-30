//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

//    Modified by:		Luke Newton
//	  Date:				January 27, 2021

import java.lang.Math;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class Brain extends Thread implements SensorInput
{
	private ObjectInfo ball;
	private ObjectInfo lGoal;
	private ObjectInfo rGoal;
	private BrainState state;

    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    // - starts thread for this object
    public Brain(SendCommand krislet, 
		 String team, 
		 char side, 
		 int number, 
		 String playMode)
    {
	m_timeOver = false;
	m_krislet = krislet;
	m_memory = new Memory();
	//m_team = team;
	m_side = side;
	// m_number = number;
	m_playMode = playMode;

	//select first state
	state = SearchingForBall.getInstance(this);

	start();
    }

    public void run() {
		// first put it somewhere on my side
		if(Pattern.matches("^before_kick_off.*",m_playMode))
			m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );

		while( !m_timeOver ){
			//STEP 1: determine current environment state
			BallVisibility ballVisibility;
			BallProximity ballProximity;
			GoalVisibility myGoalVisibility;
			GoalVisibility opponentGoalVisibility;
			ball = m_memory.getObject("ball");
			if( ball == null ){
			   ballVisibility = BallVisibility.NotVisible;
			   ballProximity = BallProximity.Unknown;
			} else {
			   if( ball.m_distance > 1.0 )
				   ballProximity = BallProximity.Far;
			   else
				   ballProximity = BallProximity.Close;
			   if( ball.m_direction != 0 )
				   ballVisibility = BallVisibility.Visible;
			   else
				   ballVisibility = BallVisibility.InFront;
			}
			lGoal = m_memory.getObject("goal l");
			rGoal = m_memory.getObject("goal r");
			if( m_side == 'l' ){
				if (rGoal == null)
					opponentGoalVisibility = GoalVisibility.NotVisible;
				else
					opponentGoalVisibility = GoalVisibility.Visible;
				if (lGoal == null)
					myGoalVisibility = GoalVisibility.NotVisible;
				else
					myGoalVisibility = GoalVisibility.Visible;
			} else {
				if (rGoal == null)
					myGoalVisibility = GoalVisibility.NotVisible;
				else
					myGoalVisibility = GoalVisibility.Visible;
				if (lGoal == null)
					opponentGoalVisibility = GoalVisibility.NotVisible;
				else
					opponentGoalVisibility = GoalVisibility.Visible;
			}
			EnvironmentState environmentState = new EnvironmentState(
					ballVisibility, ballProximity, myGoalVisibility, opponentGoalVisibility);

			// STEP 2: do an action based on the current state and obtain the new state
			state = state.doAction(environmentState);

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

	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException e){
			return false;
		}
	}

	public ObjectInfo getBall() {
		return ball;
	}

	public ObjectInfo getMyGoal() {
		if( m_side == 'l' ){
			return lGoal;
		} else {
			return rGoal;
		}
	}

	public ObjectInfo getOpponentGoal() {
		if( m_side == 'l' ){
			return rGoal;
		} else {
			return lGoal;
		}
	}

	public SendCommand getM_krislet() {
		return m_krislet;
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
