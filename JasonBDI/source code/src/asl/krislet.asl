
//Initial Facts
!start.

//Plans

// if the game hasn't started yet, then we can place the agent somewhere
+!start : connected & started_at_before_kick_off <-
	.random(RandX); .random(RandY);
	move(-1 * RandX * 52.5, 34 - RandY * 68.0);
	!play.
	
// if the game has already started once the agent is connected, we don't move it anywhere
+!start : connected & not started_at_before_kick_off <-
	!play.
	
// if the agent hasn't connected to the server, wait until this happens
+!start : not connected <-
	skip; // this line doesn't do anything besides wait for the next simulation cycle to see if the agent is now connected
	!start.

// if the ball is not visible, turn to try and find it
+!play : not ballVisible(BallDist, BallDir) & not ballClose <-
	if (ballRightOfPlayer){
		turn(30);
	} else {
		turn(-30);
	};!play.

// if the ball is visible, then we can determine if it is near or far,
// and we can determine if we are facing the ball (or should turn to the ball)
+!play : ballVisible(BallDist, BallDir) & not ballClose <-
	if (BallDir < 15 & BallDir > -15){
		if (BallDist < 1){
			+ballClose;
		} else {
			dash(100);
		}
	} elif (BallDir > 0) {
		+ballRightOfPlayer;
		turn(BallDir);
	} elif (BallDir < 0) {
		-ballRightOfPlayer;
		turn(BallDir);
	};!play.
	
// if the ball is close and opponent goal is visible, kick the ball at the goal
+!play : ballClose & lside & goalrVisible(GoalDist, GoalDir) <- 
	kick(100, GoalDir);
	-ballClose;
	!play.
+!play : ballClose & rside & goallVisible(GoalDist, GoalDir) <- 
	kick(100, GoalDir);
	-ballClose;
	!play.
	
// if the ball is close and opponent goal is not visible, turn to look for the goal
+!play : ballClose & lside & not goalrVisible(GoalDist, GoalDir)  <- 
	turn(30);
	!play.
+!play : ballClose & rside & not goallVisible(GoalDist, GoalDir) <- 
	turn(30);
	!play.