import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;

public class Player extends Thread {
	private Logger logger = Logger.getLogger("Player."+RoboCupGame.class.getName());

	// ===========================================================================
	// Private members
	// class members
	private DatagramSocket m_socket; // Socket to communicate with server
	private InetAddress m_host; // Server address
	private int m_port; // server port
	private String m_team; // team name
	protected char m_side;
	private int m_number;
	private String m_playMode;
	private boolean connected_to_server;
	protected boolean m_playing; // controls the MainLoop
	private Pattern message_pattern = Pattern.compile("^\\((\\w+?)\\s.*");
	private Pattern hear_pattern = Pattern.compile("^\\(hear\\s(\\w+?)\\s(\\w+?)\\s(.*)\\).*");

	// private Pattern coach_pattern = Pattern.compile("coach");
	// constants
	private static final int MSG_SIZE = 4096; // Size of socket buffer
	protected String playerName;

	//updated on each player loop
	protected VisualInfo visualInfo;
	protected MessageInfo messageInfo;
	protected final PlayerRole playerRole;

	// ---------------------------------------------------------------------------
	// This constructor opens socket for connection with server
	public Player(InetAddress host, int port, String team, String name, PlayerRole playerRole) throws SocketException {
		m_socket = new DatagramSocket();
		m_host = host;
		m_port = port;
		m_team = team;
		m_playing = true;
		this.playerName = name;
		this.playerRole = playerRole;
	}

	// ---------------------------------------------------------------------------
	// This destructor closes socket to server
	public void finalize() {
		m_socket.close();
	}

	// ===========================================================================
	// Protected member functions

	// ---------------------------------------------------------------------------
	// This is main loop for player
	@Override
	public void run() {
		try {
			byte[] buffer = new byte[MSG_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);

			// first we need to initialize connection with server
			init(PlayerRole.goalie.equals(playerRole));

			m_socket.receive(packet);

			parseInitCommand(new String(buffer));
			m_port = packet.getPort();

			// Now we should be connected to the server
			// and we know side, player number and play mode
			while (m_playing) {
				try {
					parseSensorInformation(receive());
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			finalize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doAction(String action) {
		logger.info(playerName + " is doing action " + action);
		Action.execute(this, action);
	}

	public char getM_side() {
		return m_side;
	}

	public String getM_playMode() {
		return m_playMode;
	}
	
	public boolean isConnected_to_server() {
		return connected_to_server;
	}
	
	public String getM_team() {
		return m_team;
	}
	
	// ===========================================================================
	// Implementation of SendCommand Interface

	// ---------------------------------------------------------------------------
	// This function sends move command to the server
	public void move(double x, double y) {
		send("(move " + Double.toString(x) + " " + Double.toString(y) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends turn command to the server
	public void turn(double moment) {
		send("(turn " + Double.toString(moment) + ")");
	}

	public void turn_neck(double moment) {
		send("(turn_neck " + Double.toString(moment) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends dash command to the server
	public void dash(double power) {
		send("(dash " + Double.toString(power) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends kick command to the server
	public void kick(double power, double direction) {
		send("(kick " + Double.toString(power) + " " + Double.toString(direction) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends say command to the server
	public void say(String message) {
		send("(say " + message + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends chage_view command to the server
	public void changeView(String angle, String quality) {
		send("(change_view " + angle + " " + quality + ")");
	}
	
	// ---------------------------------------------------------------------------
	// This function sends catch command to the server
	public void catch_ball(double direction) {
		send("(catch " + Double.toString(direction) + ")");
	}
	
	// ---------------------------------------------------------------------------
	// This function sends score command to the server
	public void score() {
		send("(score)");
	}
	
	// ---------------------------------------------------------------------------
	// This function sends sense_body command to the server
	public void sense_body() {
		send("(sense-body)");
	}

	// ---------------------------------------------------------------------------
	// This function sends bye command to the server
	public void bye() {
		m_playing = false;
		send("(bye)");
	}

	// ===========================================================================
	// Here comes collection of communication function
	// ---------------------------------------------------------------------------
	// This function sends initialization command to the server
	private void init(boolean isGoalie) {
		String msg = "(init " + m_team + " (version 9)";
		if (isGoalie)
			msg += " (goalie)";
		msg += ")";
		send(msg);
	}

	// ---------------------------------------------------------------------------
	// This function parses initial message from the server
	protected void parseInitCommand(String message) throws IOException {
		Matcher m = Pattern.compile("^\\(init\\s(\\w)\\s(\\d{1,2})\\s(\\w+?)\\).*$").matcher(message);
		if (!m.matches()) {
			throw new IOException(message);
		}
		m_side = m.group(1).charAt(0);
		m_number = Integer.parseInt(m.group(2));
		m_playMode = m.group(3);
		connected_to_server = true;
	}
	
	// ---------------------------------------------------------------------------
	// This function parses sensor information
	private void parseSensorInformation(String message) throws IOException {
		// First check kind of information
		Matcher m = message_pattern.matcher(message);
		if (!m.matches()) {
			throw new IOException(message);
		}
		if (m.group(1).compareTo("see") == 0) {
			VisualInfo info = new VisualInfo(message);
			info.parse();
			this.visualInfo = info;
		} else if (m.group(1).compareTo("hear") == 0)
			parseHear(message);
	}

	// ---------------------------------------------------------------------------
	// This function parses hear information
	private void parseHear(String message) throws IOException {
		logger.info(m_number + " hearing " + message);
		// get hear information
		Matcher m = hear_pattern.matcher(message);
		int time;
		String sender;
		String uttered;
		if (!m.matches()) {
			throw new IOException(message);
		}
		time = Integer.parseInt(m.group(1));
		sender = m.group(2);
		uttered = m.group(3);
		
		this.messageInfo = new MessageInfo(time, sender, uttered);
	}

	// ---------------------------------------------------------------------------
	// This function sends via socket message to the server
	private void send(String message) {
		byte[] buffer = Arrays.copyOf(message.getBytes(), MSG_SIZE);
		try {
			DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE, m_host, m_port);
			m_socket.send(packet);
		} catch (IOException e) {
			System.err.println("socket sending error " + e);
		}

	}

	// ---------------------------------------------------------------------------

	// This function waits for new message from server
	private String receive() {
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);
		try {
			m_socket.receive(packet);
		} catch (SocketException e) {
			System.out.println("shutting down...");
		} catch (IOException e) {
			System.err.println("socket receiving error " + e);
		}
		return new String(buffer);
	}

}
