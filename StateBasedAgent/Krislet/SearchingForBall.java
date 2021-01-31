/**
 * This is one possible state for the Brain, representing a state in which the agent is looking for the ball.
 */
public class SearchingForBall extends BrainState{
    /* This is implemented as a singleton */
    private static SearchingForBall singleton;
    public static SearchingForBall getInstance(Brain brain){
        if (singleton == null)
            singleton = new SearchingForBall(brain);
        return singleton;
    }
    private SearchingForBall(){
        readStateActions();
    }
    private SearchingForBall(Brain brain){
        this();
        this.brain = brain;
    }

    /* Old code for state transitions before it was moved into the txt file for greater flexibility
    public BrainState nextState(EnvironmentState e){
        if (e.getBallVisibility() == BallVisibility.Visible)
            return BallInView.getInstance(brain);
        return singleton;

    }*/


}