import java.util.Vector;

class VisualInfoUtil {
    static String LEFT_GOAL = "goal l";
    static String RIGHT_GOAL = "goal r";

    static GoalInfo getGoalInfo(VisualInfo visualInfo, String side) {
        Vector<GoalInfo> goalInfoVector = visualInfo.getGoalList();
        for (GoalInfo goalInfo : goalInfoVector) {
            if (side.equals(goalInfo.getType())) {
                return goalInfo;
            }
        }
        return null;
    }

    static BallInfo getBallInfo(VisualInfo visualInfo) {
        Vector<BallInfo> ballList = visualInfo.getBallList();
        if (ballList != null && ballList.size() > 0) {
            return ballList.iterator().next();
        }
        return null;
    }
}
