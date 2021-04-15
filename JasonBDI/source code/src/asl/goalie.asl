//beliefs
atGoal.

//intentions
!start.

//plans

// if the game hasn't started yet, then we can place the agent somewhere
+!start : connected & started_at_before_kick_off <-
	move(-50.0, 0.0);
	!waitSaveBall.

// if the game has already started once the agent is connected, we don't move it anywhere
+!start : connected & not started_at_before_kick_off <-
	!waitSaveBall.

// if the agent hasn't connected to the server, wait until this happens
+!start : not connected <-
	skip; // this line doesn't do anything besides wait for the next simulation cycle to see if the agent is now connected
	!start.

//Waiting at goal
+!waitSaveBall : atGoal & not ball(BallDist, BallDir) <-
    turn(40);
    !waitSaveBall.
+!waitSaveBall : atGoal & ball(BallDist, BallDir) <-
    if (BallDist > 22 | not BallDir == 0) {
        turn(BallDir);
		!waitSaveBall;
    }
    elif (BallDist > 2 & BallDist <= 22) {
		//reach ball
        dash(100);
        -atGoal;
		!reachBall;
    }
	else {
		!saveBall;
	}.

//Reach ball
+!reachBall : flagcVisible(Dist, Dir) <-
	if (Dist > 36) {
		!chaseBall;
	}
	else {
		!runBackAtOwnGoal;
	}.

+!reachBall : flagctVisible(Dist, Dir) <-
	if (Dist > 40) {
		!chaseBall;
	}
	else {
		!runBackAtOwnGoal;
	}.

+!reachBall : flagcbVisible(Dist, Dir) <-
	if (Dist > 40) {
		!chaseBall;
	}
	else {
		!runBackAtOwnGoal;
	}.
	
+!reachBall : not oppGoal(Dist, Dir) & not flagctVisible(Dt,Dirt) & not flagcbVisible(Db,Dirb) <-
	!runBackAtOwnGoal.

+!chaseBall : ball(BallDist, BallDir) <-
	if (not BallDir == 0) {
		turn(BallDir);
		!reachBall;
	}
	elif (BallDist >= 1) {
		dash(100);
		!reachBall;
	}
	elif (BallDist < 1) {
		!saveBall;
	}.

+!chaseBall : not ball(BallDist, BallDir) <-
	turn(40);
	!reachBall.

//save ball
+!saveBall : ball(BallDist, BallDir) <-
	catch(BallDir);
	+savedBall;
	!freeKickBall.
	
	
//clear ball
//not catch
+!freeKickBall : savedBall & not message(MsgSrc, Msg, Time) <-
	-savedBall;
	!runBackAtOwnGoal.

//catched ball, move back to goal position
+!freeKickBall : savedBall & message(MsgSrc, Msg, Time) <-
	if (MsgSrc == "referee" & (Msg == "free_kick_l" | Msg == "free_kick_r")) {
		say("hello");
        move(-50, 0);
		-savedBall;
		!passBall;
	}.

//pass ball to team mate or long kick to opp goal
+!passBall : not playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) & not oppGoal(Dist, Dir) <-
	turn(40);
	!passBall.

+!passBall : playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) <-
	if ( PlayerTeam=="test"){    // checking if the player is team mate or not
		kick(3*PlayerDist, PlayerDir);  //kick the ball according to the distance of other player
		!runBackAtOwnGoal;
	}
	else{
		 turn(40);
		 !passBall;
	}.

+!passBall : not playerVisible(PlayerTeam, PlayerNum, PlayerDist, PlayerDir) & oppGoal(Dist, Dir) <-
	kick(Dist, Dir);
	!runBackAtOwnGoal.

	
//run back
+!runBackAtOwnGoal : not ownGoal(GoalDist, GoalDir) <-
	turn(40);
	!runBackAtOwnGoal.
	
+!runBackAtOwnGoal : ownGoal(GoalDist, GoalDir) <-
	if (not GoalDir == 0) {
		turn(GoalDir);
		!runBackAtOwnGoal;
	}
	elif (GoalDist > 5) {
		dash(100);
		!runBackAtOwnGoal;
	}
	else {
		turn(180); //turn back
		+atGoal;
		!waitSaveBall;
	}.