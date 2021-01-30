import java.util.Objects;

public final class EnvironmentState {
    private final BallVisibility ballVisibility;
    private final BallProximity ballProximity;
    private final GoalVisibility myGoalVisibility;
    private final GoalVisibility opponentGoalVisibility;

    public EnvironmentState(BallVisibility ballVisibility, BallProximity ballProximity,
                            GoalVisibility myGoalVisibility, GoalVisibility opponentGoalVisibility) {
        this.ballVisibility = ballVisibility;
        this.ballProximity = ballProximity;
        this.myGoalVisibility = myGoalVisibility;
        this.opponentGoalVisibility = opponentGoalVisibility;
    }

    public BallVisibility getBallVisibility() {
        return ballVisibility;
    }

    public BallProximity getBallProximity() {
        return ballProximity;
    }

    public GoalVisibility getMyGoalVisibility() {
        return myGoalVisibility;
    }

    public GoalVisibility getOpponentGoalVisibility() {
        return opponentGoalVisibility;
    }

    @java.lang.Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        EnvironmentState that = (EnvironmentState) object;
        return ballVisibility.equals(that.ballVisibility) &&
                ballProximity.equals(that.ballProximity) &&
                myGoalVisibility.equals(that.myGoalVisibility) &&
                opponentGoalVisibility.equals(that.opponentGoalVisibility);
    }

    @java.lang.Override
    // based on https://www.baeldung.com/java-hashcode
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (ballVisibility == null ? 0 : ballVisibility.toString().hashCode());
        hash = 31 * hash + (ballProximity == null ? 0 : ballProximity.toString().hashCode());
        hash = 31 * hash + (myGoalVisibility == null ? 0 : myGoalVisibility.toString().hashCode());
        hash = 31 * hash + (opponentGoalVisibility == null ? 0 : opponentGoalVisibility.toString().hashCode());
        return hash;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "(" +
                ballVisibility +
                ", " + ballProximity +
                ", " + myGoalVisibility +
                ", " + opponentGoalVisibility +
                ')';
    }
}