
public class BallInView extends BrainState{
    private static BallInView singleton;

    private BallInView(){
        readStateActions();
    }

    private BallInView(Brain brain){
        this();
        this.brain = brain;
    }

    public BrainState nextState(EnvironmentState e){
        if (e.getBallVisibility() == BallVisibility.NotVisible)
            return SearchingForBall.getInstance(brain);
        return singleton;
    }

    public static BallInView getInstance(Brain brain){
        if (singleton == null)
            singleton = new BallInView(brain);
        return singleton;
    }
}