/**
 * This is one possible state for the Brain, representing a state in which the ball is visible to the agent.
 */
public class BallInView extends BrainState{

    /* This is implemented as a singleton */
    private static BallInView singleton;
    public static BallInView getInstance(Brain brain){
        if (singleton == null)
            singleton = new BallInView(brain);
        return singleton;
    }
    private BallInView(){
        readStateActions();
    }
    private BallInView(Brain brain){
        this();
        this.brain = brain;
    }

    /* Old code for state transitions before it was moved into the txt file for greater flexibility
    public BrainState nextState(EnvironmentState e){
        if (e.getBallVisibility() == BallVisibility.NotVisible)
            return SearchingForBall.getInstance(brain);
        return singleton;
    }
    */


}