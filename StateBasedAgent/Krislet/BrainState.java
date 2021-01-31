import java.util.HashMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Represents the internal state of the agent function.
 *
 * In :
 *      Agent: E x I -> Ac x I
 * this class is the I set
 */
public abstract class  BrainState{
    // the name of the file containing the agent specification
    private static final String agentSpecFilename = "AgentSpec.txt";

    // the mapping from enivonment states to actions (E -> Ac)
    protected HashMap<EnvironmentState, Action> agentMapping;
    // the mapping from enivonment states to next states (E -> I)
    protected HashMap<EnvironmentState, Class<?>> stateMapping;

    // a reference to the brain which contains locations of entitites
    protected static Brain brain;

    /** reads the agent specification from the specified file and parses into mappings
     * (creates E -> Ac and E -> I)
     * **/
    protected void readStateActions(){
        ArrayList<String> agentSpec = readAgentSpec();
        parseSpec(agentSpec);
    }

    /**
     * scans the agent specification file and reads in the behaviors for the current state
     * @return an list of agent behaviors from the specification file that belong to the current state
     */
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

    /**
     * converts a list of agent behavior strings into action and next-state mappings
     * @param agentSpec a list of agent behaviors from the specification file that belong to the current state
     */
    private void parseSpec(ArrayList<String> agentSpec){
        agentMapping = new HashMap<EnvironmentState, Action>();
        stateMapping = new HashMap<EnvironmentState, Class<?>>();

        for(String behavior: agentSpec){
            if(behavior.trim().isEmpty() || behavior.trim().startsWith("#"))
                continue;

            String[] split_behavior = behavior.split("->");
            String[] actionStateStrings = split_behavior[1].split("X");

            String[] environmentStates = split_behavior[0].replaceAll("\\(", "").replaceAll("\\)","").split(",");
            BallVisibility ballVisibility = BallVisibility.valueOf(environmentStates[0].trim());
            BallProximity ballProximity = BallProximity.valueOf(environmentStates[1].trim());
            GoalVisibility myGoalVisibility = GoalVisibility.valueOf(environmentStates[2].trim());
            GoalVisibility opponentGoalVisibility = GoalVisibility.valueOf(environmentStates[3].trim());

            EnvironmentState environmentState = new EnvironmentState(
                    ballVisibility, ballProximity, myGoalVisibility, opponentGoalVisibility);
            agentMapping.put(environmentState, Action.valueOf(actionStateStrings[0].trim()));
            try{
                stateMapping.put(environmentState, Class.forName(actionStateStrings[1].trim()));
            }catch(Exception e){
                System.out.println(actionStateStrings[1].trim() + " not a recognized class name");
                System.exit(0);
            }
        }
    }

    public BrainState doAction(EnvironmentState environmentState){
        // use agent mapping to determine action
        Action action = agentMapping.get(environmentState);
        System.out.println(this.getClass().getSimpleName() + ": " + environmentState + " -> " + action);

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
            return nextState(environmentState);
        }
        return this;
    }

    /**
     * determines the next state of the agent
     * @param environmentState the environment state E of the agent function definition
     * @return the next state I of the agent function definition
     */
    public BrainState nextState(EnvironmentState environmentState){
        try{
            Class<?> c = stateMapping.get(environmentState);
            System.out.println("Next state: " + c.getSimpleName());

            Method method = c.getDeclaredMethod("getInstance", Brain.class);
            return (BrainState) method.invoke(null, brain);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    };
}