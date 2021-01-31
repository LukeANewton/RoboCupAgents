/**
 * An action represents the output of the agent function.
 *
 * In :
 *      Agent: E x I -> Ac x I
 * this class is the Ac set
 *
 */
public enum Action{
    Turn, TurnToBall, TurnToMyGoal, TurnToOpponentGoal, TurnAround, KickForward, KickBehind, KickAtOpponentGoal, Dash;
}