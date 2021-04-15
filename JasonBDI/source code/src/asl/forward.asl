
// intentions
!start.


// plans

// if the game hasn't started yet, then we can place the agent somewhere
+!start : connected & started_at_before_kick_off <-
	.random(RandX); .random(RandY);
	move(-1 * RandX * 15, 34 - RandY * 68.0);
	!reachBall.
	
// if the game has already started once the agent is connected, we don't move it anywhere
+!start : connected & not started_at_before_kick_off <-
	!reachBall.
	
// if the agent hasn't connected to the server, wait until this happens
+!start : not connected <-
	skip; // this line doesn't do anything besides wait for the next simulation cycle to see if the agent is now connected
	!start.
	
// if the ball is not visible, turn to try and find it
+!reachBall : not ballVisible(BallDist, BallDir) <-
	if (ballRightOfPlayer){ // if we remember seeing the ball on our right, turn right
		turn(30);
	} else {
		turn(-30);
	};!reachBall.	
	
// if the ball is visible turn towards it and run
+!reachBall : ballVisible(BallDist, BallDir) & not message(MsgSrc, Msg, Time) & not haveBall <-
	if (BallDir < 15 & BallDir > -15){
		if (BallDist < 1){ // we have reached the ball
			say(gotBall);
			+haveBall;
			!passOrKeepBall;
		} else { // we can see the ball, run towards it	
			dash(100);
			-haveBall;
			!reachBall;
		}
	} elif (BallDir > 0) { // turn to face the visible ball
		+ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	} elif (BallDir < 0) { // turn to face the visible ball
		-ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	}.
	
+!reachBall : ballVisible(BallDist, BallDir) & not message(MsgSrc, Msg, Time) & haveBall <-
	if (BallDir < 15 & BallDir > -15){
		if (BallDist < 1){ // we have reached the ball
			say(gotBall);
			+haveBall;
			!passOrKeepBall;
		} else { // we can see the ball, run towards it	
			dash(100);
			-haveBall;
			!reachBall;
		}
	} elif (BallDir > 0) { // turn to face the visible ball
		+ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	} elif (BallDir < 0) { // turn to face the visible ball
		-ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	}.

+!reachBall : ballVisible(BallDist, BallDir) & message(MsgSrc, Msg, Time) & not haveBall <-
	if (BallDir < 15 & BallDir > -15){
		if (BallDist < 1){ // we have reached the ball
			say(gotBall);
			+haveBall;
			!passOrKeepBall;
		} elif(.substring("gotBall", Msg) & .substring("our",Msg)) { // we can see the ball, run towards it	
			-haveBall;
			!gotoNet;
		}else{
			dash(100);
			!reachBall;
		}
	} elif (BallDir > 0) { // turn to face the visible ball
		+ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	} elif (BallDir < 0) { // turn to face the visible ball
		-ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	}.	
	
+!reachBall : ballVisible(BallDist, BallDir) & haveBall <-
	if (BallDir < 15 & BallDir > -15){
		if (BallDist < 1){ // we have reached the ball
			say(gotBall);
			!passOrKeepBall;
		} else { // This should not happen	
			-haveBall;
			dash(100);
			!reachBall;
		}
	} elif (BallDir > 0) { // turn to face the visible ball
		+ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	} elif (BallDir < 0) { // turn to face the visible ball
		-ballRightOfPlayer;
		turn(BallDir);
		!reachBall;
	}.	
	
+!gotoNet : lside & not goalrVisible(GoalDist, GoalDir) & not haveBall <-
     turn(40);
     !gotoNet.
+!gotoNet : rside & not goallVisible(GoalDist, GoalDir) & not haveBall <-
     turn(40);
     !gotoNet.     
     
+!gotoNet : lside & goalrVisible(GoalDist, GoalDir) & not haveBall<-
	if(GoalDist<25){
		!waitforBall;
	} elif(GoalDir < 10 & GoalDir > -10){
		dash(100);
		!gotoNet;
	}else{
		turn(GoalDir);
    	!gotoNet;
    }.
       	
+!gotoNet : rside & goallVisible(GoalDist, GoalDir) & not haveBall<-
	if(GoalDist<25){
		!waitforBall;
	} elif(GoalDir < 10 & GoalDir > -10){
		dash(100);
		!gotoNet;
	}else{
		turn(GoalDir);
    	!gotoNet;
    }.
    
+!waitforBall : not ballVisible(BallDist, BallDir) <-
    turn(40);
    !waitforBall.

+!waitforBall : ballVisible(BallDist, BallDir) <-
    if (BallDist > 20 | not BallDir == 0) {
        turn(BallDir);
		!waitforBall;
    }
    else {
		//reach ball
        dash(20);
		!reachBall;
    }.   
    
//decide whether keep the ball or pass it. We try to pass the ball if there is an opponent near
+!passOrKeepBall : not opponentplayerVisible(_, _, _, _) <-
	!keepBall. 
+!passOrKeepBall : opponentplayerVisible(_, _, PlayerDist, _) <-
	if(PlayerDist < 20){
		!findCenterPlayer;
	}else{
		!keepBall;
	}.
+!passOrKeepBall : not opponentplayerVisible(_, _, _, _) &  teamplayerVisible(_, _, PlayerDist, _) <-
	if(PlayerDist > 10){
		!findCenterPlayer;
	}else{
		!keepBall;
	}.

// find the goal once we have decided to keep the ball
+!keepBall : lside & not goalrVisible(_, _) <-
	turn(40);
	!keepBall.
+!keepBall : rside & not goallVisible(_, _) <-
	turn(40);
	!keepBall. 

// if the ball is close and opponent goal is visible, kick the ball at the goal
// if the goal is close enough. Otherwise dribble the ball
+!keepBall :  lside & goalrVisible(GoalDist, GoalDir)  <- 
	if (GoalDist > 25){
		kick(40, 0);
	} else {
		kick(100, GoalDir);
	};
	!reachBall.
+!keepBall :  rside & goallVisible(GoalDist, GoalDir)  <- 
	if (GoalDist > 25){
		kick(40, 0);
	} else {
		kick(100, GoalDir);
	};
	!reachBall.	    
	
// try to find a center attacker in order to pass the ball
+!findCenterPlayer : not teamplayerVisible(_, _, _, _) <-
	 turn(40);
	 !findRightPlayer.
+!findCenterPlayer : teamplayerVisible(_, _, _, PlayerDir) <-
	kick(3*PlayerDist, PlayerDir); 
	!reachBall.

// try to find an attacker on right-hand side in order to pass the ball
+!findRightPlayer : not teamplayerVisible(_, _, _, _) <-
	 turn(40);
	 !findRightRightPlayer.
+!findRightPlayer : teamplayerVisible(_, _, _, PlayerDir) <-
	kick(3*PlayerDist, PlayerDir); 
	!reachBall.

// try to find an attacker on right-hand side in order to pass the ball
+!findRightRightPlayer : not teamplayerVisible(_, _, _, _) <-
	 turn(-120);
	 !findLeftPlayer.
+!findRightRightPlayer : teamplayerVisible(_, _, _, PlayerDir) <-
	kick(3*PlayerDist, PlayerDir); 
	!reachBall.
	
// try to find an attacker on left-hand side in order to pass the ball
+!findLeftPlayer : not teamplayerVisible(_, _, _, _) <-
	turn(-40);
	!findLeftLeftPlayer. // at this point we havent found a player left or right, so we wont pass the ball
+!findLeftPlayer : teamplayerVisible(_, _, PlayerDist, PlayerDir) <-
	kick(3*PlayerDist, PlayerDir); 
	!reachBall.

// try to find an attacker on left-hand side in order to pass the ball
+!findLeftLeftPlayer : not teamplayerVisible(_, _, _, _) <-
	!keepBall. // at this point we havent found a player left or right, so we wont pass the ball
+!findLeftLeftPlayer : teamplayerVisible(_, _, PlayerDist, PlayerDir) <-
	kick(3*PlayerDist, PlayerDir); 
	!reachBall.