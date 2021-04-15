import jason.asSyntax.ASSyntax;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public abstract class Perceptor implements BiConsumer<RoboCupGame, Player> {
    @Override
    public abstract void accept(RoboCupGame roboCupGame, Player player);

    static class Goalie extends Perceptor {
        Logger logger = Logger.getLogger("Perceptor."+Perceptor.class.getName());

        @Override
        public void accept(RoboCupGame game, Player player) {
            char side = player.m_side;
            String playerName = player.playerName;
            VisualInfo visualInfo = player.visualInfo;
            if (visualInfo != null) {
                //update own goal position
                GoalInfo ownGoal = null;
                GoalInfo oppGoal = null;
                if (side == 'l') {
                    ownGoal = VisualInfoUtil.getGoalInfo(visualInfo, VisualInfoUtil.LEFT_GOAL);
                    oppGoal = VisualInfoUtil.getGoalInfo(visualInfo, VisualInfoUtil.RIGHT_GOAL);
                }
                else {
                    ownGoal = VisualInfoUtil.getGoalInfo(visualInfo, VisualInfoUtil.RIGHT_GOAL);
                    oppGoal = VisualInfoUtil.getGoalInfo(visualInfo, VisualInfoUtil.LEFT_GOAL);
                }
                if (ownGoal != null) {
                    game.addPercept(playerName, ASSyntax.createLiteral(
                            "ownGoal",
                            ASSyntax.createNumber(ownGoal.getDistance()),
                            ASSyntax.createNumber(ownGoal.getDirection())));
                    logger.info("update " + player + " percepts: " + ASSyntax.createLiteral(
                            "ownGoal",
                            ASSyntax.createNumber(ownGoal.getDistance()),
                            ASSyntax.createNumber(ownGoal.getDirection())));
                }
                if (oppGoal != null) {
                    game.addPercept(playerName, ASSyntax.createLiteral(
                            "oppGoal",
                            ASSyntax.createNumber(oppGoal.getDistance()),
                            ASSyntax.createNumber(oppGoal.getDirection())));
                    logger.info("update " + player + " percepts: " +  ASSyntax.createLiteral(
                            "oppGoal",
                            ASSyntax.createNumber(oppGoal.getDistance()),
                            ASSyntax.createNumber(oppGoal.getDirection())));
                }

                //update ball position
                BallInfo ballInfo = VisualInfoUtil.getBallInfo(visualInfo);
                if (ballInfo != null) {
                    game.addPercept(playerName, ASSyntax.createLiteral(
                            "ball",
                            ASSyntax.createNumber(ballInfo.getDistance()),
                            ASSyntax.createNumber(ballInfo.getDirection())
                    ));
                }
            }
        }
    }
}
