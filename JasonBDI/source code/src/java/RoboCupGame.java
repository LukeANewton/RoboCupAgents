// Environment code for project roboCupTeam

import jason.asSyntax.*;
import jason.environment.*;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.*;

public class RoboCupGame extends Environment {
	private static final int PORT_NUMBER = 6000;
	
    private Logger logger = Logger.getLogger("roboCupTeam."+RoboCupGame.class.getName());

    static final Map<String, Player> PLAYERS = new HashMap<String, Player>();

    static final Map<PlayerRole, Perceptor> PERCEPTORS = new HashMap<>();

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        
        //add perceptors
        PERCEPTORS.put(PlayerRole.goalie, new Perceptor.Goalie());

        //add players
        addPlayer("player1", PlayerRole.forward, "test");
        addPlayer("player2", PlayerRole.forward, "test");
        addPlayer("player3", PlayerRole.defender, "test");
        addPlayer("player4", PlayerRole.defender, "test");
        addPlayer("player5", PlayerRole.goalie, "test");

        addPlayer("player6", PlayerRole.forward, "carleton");
        addPlayer("player7", PlayerRole.forward, "carleton");
        addPlayer("player8", PlayerRole.defender, "carleton");
        addPlayer("player9", PlayerRole.defender, "carleton");
        addPlayer("player10", PlayerRole.goalie, "carleton");
    }

    private void addPlayer(String playerName, PlayerRole role, String team) {
        try {
            Player player = new Player(InetAddress.getByName("localhost"), PORT_NUMBER, team, playerName, role);
            PLAYERS.put(playerName, player);
            player.start();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //updating player's percepts
    void addPlayerPercept(String player, Literal literal) {
        addPercept(player, literal);
        //logger.info("update " + player + " percepts: " + literal);
    }

    /** update player percepts with messageInfo contents*/
    private void updatePlayerPerceptsFromHearing(String player, MessageInfo messageInfo) {
        //pattern: message(sender, uttered, time)
        Literal msgLiteral = ASSyntax.createLiteral("message",
                ASSyntax.createString(messageInfo.getSender()),
                ASSyntax.createString(messageInfo.getUttered()),
                ASSyntax.createNumber(messageInfo.getTime()));
        addPlayerPercept(player, msgLiteral);
    	logger.info("*****player " + player + " , hears: " + messageInfo);
        // After we add the visual percepts, we delete the player's messageInfo.
        // This way, we only get a percept when we receive a message, instead of 
        // every time a we get percepts after receiving the message.
        PLAYERS.get(player).messageInfo = null;
    }

    /** update player percepts with visualInfo contents*/
    private void updatePlayerPerceptsFromVisual(String player, VisualInfo visualInfo) {   	
    	addPerceptsForObjectInfos(player, visualInfo.getBallList(), "");
    	addPerceptsForObjectInfos(player, visualInfo.getGoalList(), "");
    	addPerceptsForObjectInfos(player, visualInfo.getFlagList(), "");
    	
    	Vector<?> visiblePlayers =  visualInfo.getPlayerList();
    	addPerceptsForObjectInfos(player, visiblePlayers, "");
    	
    	Vector<PlayerInfo> visibleTeammates = new Vector<>();
    	Vector<PlayerInfo> visibleOpponents = new Vector<>();
    	for(Object p : visiblePlayers) {
    		PlayerInfo playerInfo = (PlayerInfo) p;
    		if (!playerInfo.getTeamName().isEmpty()) { // if we can't tell what team the player is, we don't assume either side
    			if(playerInfo.getTeamName().equals(PLAYERS.get(player).getM_team()))
        			visibleTeammates.add(playerInfo);
        		else
        			visibleOpponents.add(playerInfo);
    		}
    	}
    	addPerceptsForObjectInfos(player, visibleTeammates, "team");
    	addPerceptsForObjectInfos(player, visibleOpponents, "opponent");
    }
    
    /** add the direction and distances for a list of visible objects to a player's percepts*/
    private void addPerceptsForObjectInfos(String player, Vector<?> objects, String prefix) {
    	for(Object o: objects) {
    		Literal literal;
    		if (o instanceof PlayerInfo) { // if the object is a player, we want the team name, uniform number, distance, and direction
    			literal = ASSyntax.createLiteral(
    					prefix+((PlayerInfo)o).getType().replaceAll("\\s","")+"Visible", 
        				ASSyntax.createString(((PlayerInfo)o).getTeamName()),
        				ASSyntax.createNumber(((PlayerInfo)o).getTeamNumber()),
                		ASSyntax.createNumber(((PlayerInfo)o).getDistance()), 
                		ASSyntax.createNumber(((PlayerInfo)o).getDirection()));
    		} else { // if the object is not a player, we just care about its distance and direction
    			literal = ASSyntax.createLiteral(
        				((ObjectInfo)o).getType().replaceAll("\\s","")+"Visible", 
                		ASSyntax.createNumber(((ObjectInfo)o).getDistance()), 
                		ASSyntax.createNumber(((ObjectInfo)o).getDirection()));
    		}
    		addPlayerPercept(player, literal);
    	}
    }
    
    /** set the percepts for a given player based on what they can see, hear, and known properties about themselves*/
    public void setPlayerPercepts(String playerName, VisualInfo visualInfo, MessageInfo messageInfo) {
    	Player player = PLAYERS.get(playerName);
    	clearPercepts(playerName);

    	// add percept for team name in the form: team(<name>)
    	addPlayerPercept(playerName, ASSyntax.createLiteral("team", ASSyntax.createString(player.getM_team())));
    	// add percept for side of field in the form: lside | risde
    	addPlayerPercept(playerName, ASSyntax.createAtom(player.getM_side()+"side"));
    	// add percept for mode of play the agent is created during in the form: stated_at_<playmode>
    	addPlayerPercept(playerName, ASSyntax.createAtom("started_at_"+player.getM_playMode()));
    	
    	if(player.isConnected_to_server()) // add percept for being connected to the server
    		addPlayerPercept(playerName, ASSyntax.createAtom("connected"));    	
        if (messageInfo != null)  // add percepts for what the agent can hear
        	updatePlayerPerceptsFromHearing(playerName, messageInfo);
        if (visualInfo != null) // add percepts for what the agent can see
        	updatePlayerPerceptsFromVisual(playerName, visualInfo);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        Player player = PLAYERS.get(agName);
        player.doAction(action.toString());

        //wait 2 steps for action affect
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //run default setPlayerPercepts which adds all visualInfo and messageInfo percepts
        setPlayerPercepts(agName,  player.visualInfo,  player.messageInfo);

        PlayerRole role = player.playerRole;
        //Run custom perceptor for the role
        if (PERCEPTORS.containsKey(role)) {
            Perceptor perceptor = PERCEPTORS.get(role);
            perceptor.accept(this, player);
        }
        return true; // the action was executed with success
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
        for(Player player : PLAYERS.values()) {
            player.finalize();
        }
    }
}