import java.util.Objects;

public class EnvironmentState {
    private final Visibility ballVisibility;
    private final BallProximity ballProximity;
    private final Visibility goalVisibility;

    public EnvironmentState(Visibility ballVisibility, BallProximity ballProximity, Visibility goalVisibility) {
        this.ballVisibility = ballVisibility;
        this.ballProximity = ballProximity;
        this.goalVisibility = goalVisibility;
    }

    public Visibility getBallVisibility() {
        return ballVisibility;
    }

    public BallProximity getBallProximity() {
        return ballProximity;
    }

    public Visibility getGoalVisibility() {
        return goalVisibility;
    }

    @java.lang.Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        EnvironmentState that = (EnvironmentState) object;
        return ballVisibility.equals(that.ballVisibility) &&
                ballProximity.equals(that.ballProximity) &&
                goalVisibility.equals(that.goalVisibility);
    }

    @java.lang.Override
    // based on https://www.baeldung.com/java-hashcode
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (ballVisibility == null ? 0 : ballVisibility.toString().hashCode());
        hash = 31 * hash + (ballProximity == null ? 0 : ballProximity.toString().hashCode());
        hash = 31 * hash + (goalVisibility == null ? 0 : goalVisibility.toString().hashCode());
        return hash;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "[" +
                 ballVisibility +
                ", " + ballProximity +
                ", " + goalVisibility +
                ']';
    }
}