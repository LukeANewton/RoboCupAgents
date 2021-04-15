
//intentions
!start.

// if the game hasn't started yet, then we can place the agent somewhere
+!start : connected & started_at_before_kick_off <-
	.random(RandX); .random(RandY);
	move(-1 * RandX * 15 - 20, 34 - RandY * 68.0);
	!waitDefendBall.
	
// if the game has already started once the agent is connected, we don't move it anywhere
+!start : connected & not started_at_before_kick_off <-
	!waitDefendBall.
	
// if the agent hasn't connected to the server, wait until this happens
+!start : not connected <-
	skip; // this line doesn't do anything besides wait for the next simulation cycle to see if the agent is now connected
	!start.


//plans

//Waiting at own position 
+!waitDefendBall : not ballVisible(BallDist, BallDir) <-
    turn(40);
    !waitDefendBall.
+!waitDefendBall : ballVisible(BallDist, BallDir) <-
    if (BallDist > 20 | not BallDir == 0) {
        turn(BallDir);
		!waitDefendBall;
    }
    else {
		//reach ball
        dash(100);
		!reachBall;
    }.


//Reach ball
+!reachBall :  ballVisible(BallDist, BallDir) <-
	if (not BallDir == 0) {
		turn(BallDir);
		!reachBall;
	}
	elif (BallDist > 1 & BallDist < 20 ) {
		dash(100);
		!reachBall;
	}
	elif (BallDist > 20) {
		!runBackAtOwnPosition; // It's useful when defender is going to reach the ball but suddenly the ball is gotten away by another player
	}
	elif (BallDist <= 1) {
		!findOpponentGoal;
	}.
+!reachBall : not ballVisible(BallDist, BallDir) <-
	turn(40);
	!reachBall.
			
	
//looking for Opponent Goal 
+!findOpponentGoal : lside & not goalrVisible(GoalDist, GoalDir) <-
     turn(40);
     !findOpponentGoal.
+!findOpponentGoal : rside & not goallVisible(GoalDist, GoalDir) <-
     turn(40);
     !findOpponentGoal.     

     
+!findOpponentGoal : lside & goalrVisible(GoalDist, GoalDir) <-
     !findCenterPlayer. 
+!findOpponentGoal : rside & goallVisible(GoalDist, GoalDir) <-
     !findCenterPlayer.     
	
// try to find a center attacker in order to pass the ball
+!findCenterPlayer : not playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	 turn(40);
	 !findRightPlayer.
+!findCenterPlayer :  playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	if ( PlayerTeam=="test"){    // checking if the player is team mate or not
		if (not PlayerDir == 0) {
			turn(PlayerDir);
			!passBall;
		}
		else  {
	   		!passBall;
		}
	}
	else{
		 turn(40);
	 	!findRightPlayer;
	}.
// try to find an attacker on right-hand side in order to pass the ball
+!findRightPlayer : not playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	 turn(-80);
	 !findLeftPlayer.
+!findRightPlayer :  playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	if ( PlayerTeam=="test"){
		if (not PlayerDir == 0) {
			turn(PlayerDir);
			!passBall;
		}	
		else  {
   			!passBall;
   		}
   	
   	}
   	else{
   		turn(-80);
	 	!findLeftPlayer;
	}.
// try to find an attacker on left-hand side in order to pass the ball
+!findLeftPlayer : not playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	!findOpponentGoaltoKick.
+!findLeftPlayer :  playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	if ( PlayerTeam=="test"){
		if (not PlayerDir == 0) {
			turn(PlayerDir);
			!passBall;
		}
		else  {
	   		!passBall; 		
	   		}
	 }
	 else{
		!findOpponentGoaltoKick;
	}.
//Kick the ball to the goal
//this state occurs if Defender couldn't find any player to pass the ball
+!findOpponentGoaltoKick : lside & not goalrVisible(GoalDist, GoalDir) <-
     turn(40);
     !findOpponentGoaltoKick.
+!findOpponentGoaltoKick : rside & not goallVisible(GoalDist, GoalDir) <-
     turn(40);
     !findOpponentGoaltoKick.     
     
+!findOpponentGoaltoKick : lside & goalrVisible(GoalDist, GoalDir) <-
	turn(GoalDir);
    !kickBall.
+!findOpponentGoaltoKick : rside & goallVisible(GoalDist, GoalDir) <-
    turn(GoalDir);
    !kickBall.     
     
//pass the ball to attacker
+!passBall : playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	kick(3*PlayerDist, 0);  //kick the ball according to the distance of other player
	!runBackAtOwnPosition.
	
 // clear ball when no team mate is found to pass 
+!kickBall <- 
	kick(100, 0);
	!runBackAtOwnPosition.

//run back to defend position ( a distance of 20 from own goal)
+!runBackAtOwnPosition : lside & not goallVisible(GoalDist, GoalDir) <-
	turn(40);
	!runBackAtOwnPosition.
+!runBackAtOwnPosition : rside & not goalrVisible(GoalDist, GoalDir) <-
	turn(40);
	!runBackAtOwnPosition.	
	
	
+!runBackAtOwnPosition : lside & goallVisible(GoalDist, GoalDir) <-
	if (not GoalDir == 0) {
		turn(GoalDir);
		!runBackAtOwnPosition;
	}
	elif (GoalDist > 30) {
		dash(100);
		!runBackAtOwnPosition;
	}
	else {
		!waitDefendBall;
	}.
+!runBackAtOwnPosition : rside & goalrVisible(GoalDist, GoalDir) <-
	if (not GoalDir == 0) {
		turn(GoalDir);
		!runBackAtOwnPosition;
	}
	elif (GoalDist > 30) {
		dash(100);
		!runBackAtOwnPosition;
	}
	else {
		!waitDefendBall; // waiting again for defending 
	}.
