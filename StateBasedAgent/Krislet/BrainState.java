import java.util.HashMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Iterator;
import java.util.Map;

public abstract class  BrainState{
    private static final String agentSpecFilename = "AgentSpec.txt";
    protected HashMap<EnvironmentState, Action> agentMapping;

    protected static Brain brain;

    protected void readStateActions(){
        ArrayList<String> agentSpec = readAgentSpec();
        agentMapping = parseSpec(agentSpec);
    }

    private ArrayList<String> readAgentSpec(){
        try {
            File file = new File(agentSpecFilename);
            BufferedReader br = new BufferedReader(new FileReader(file));

            ArrayList<String> lines = new ArrayList<>();
            String st;
            boolean foundStateName = false;
            while ((st = br.readLine()) != null){
                if(st.startsWith("State:")){
                    String stateName = st.split(":")[1].trim();
                    if (stateName.equals(this.getClass().getSimpleName())){
                        foundStateName = true;
                        continue;
                    } else
                        foundStateName = false;
                }
                if (foundStateName)
                    lines.add(st);
            }

            return lines;
        }catch(Exception e){
            return null;
        }
    }

    private HashMap<EnvironmentState, Action> parseSpec(ArrayList<String> agentSpec){
        HashMap<EnvironmentState, Action> mapping = new HashMap<>();

        for(String behavior: agentSpec){
            if(behavior.trim().isEmpty() || behavior.trim().startsWith("#"))
                continue;

            String[] split_behavior = behavior.split("->");
            String actionString = split_behavior[1];

            String[] environmentStates = split_behavior[0].replaceAll("\\(", "").replaceAll("\\)","").split(",");
            BallVisibility ballVisibility = BallVisibility.valueOf(environmentStates[0].trim());
            BallProximity ballProximity = BallProximity.valueOf(environmentStates[1].trim());
            GoalVisibility myGoalVisibility = GoalVisibility.valueOf(environmentStates[2].trim());
            GoalVisibility opponentGoalVisibility = GoalVisibility.valueOf(environmentStates[3].trim());

            mapping.put(new EnvironmentState(ballVisibility, ballProximity, myGoalVisibility, opponentGoalVisibility),
                    Action.valueOf(actionString.trim()));
        }
        return mapping;
    }

    public BrainState doAction(EnvironmentState e){
        // STEP 2: use agent mapping to determine action
        Action action = agentMapping.get(e);
        System.out.println(this.getClass().getSimpleName() + ": " + e + " -> " + action);

        if (action != null){
            switch(action){
                case Turn:
                    brain.getM_krislet().turn(30);
                    break;
                case TurnToBall:
                    if(brain.getBall() != null)
                        brain.getM_krislet().turn(brain.getBall().m_direction);
                    break;
                case TurnToOpponentGoal:
                    if(brain.getOpponentGoal() != null)
                        brain.getM_krislet().turn(brain.getOpponentGoal().m_direction);
                    break;
                case TurnAround:
                    brain.getM_krislet().turn(180);
                    break;
                case KickForward:
                    brain.getM_krislet().kick(100, 0);
                    break;
                case KickBehind:
                    brain.getM_krislet().kick(100, 180);
                    break;
                case KickAtOpponentGoal:
                    if(brain.getOpponentGoal() != null)
                        brain.getM_krislet().kick(100, brain.getOpponentGoal().m_direction);
                    break;
                case Dash:
                    brain.getM_krislet().dash(100);
                    break;
            }
        }
        return nextState(e);
    }

    public abstract BrainState nextState(EnvironmentState e);
}