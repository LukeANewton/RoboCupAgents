
public class SearchingForBall extends BrainState{
    private static SearchingForBall singleton;

    private SearchingForBall(){
        readStateActions();
    }

    private SearchingForBall(Brain brain){
        this();
        this.brain = brain;
    }

    public BrainState nextState(EnvironmentState e){
        if (e.getBallVisibility() == BallVisibility.Visible)
            return BallInView.getInstance(brain);
        return singleton;

    }

    public static SearchingForBall getInstance(Brain brain){
        if (singleton == null)
            singleton = new SearchingForBall(brain);
        return singleton;
    }
}