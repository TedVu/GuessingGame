package assignment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {

	private static final Logger logger = Logger.getLogger(StartGameThread.class.getName());
	private FileHandler fileHandler;

	public static final int PORT = 9090;
	public static final int MIN_GUESS = 0;
	public static final int MAX_GUESS = 12;
	public static final int MAX_PLAYER_EACH_ROUND = 3;
	public static final int TIME_PER_ROUND = 20; // in seconds
	public static final int MAX_PLAYER_QUEUE = 6;
	public static final int MAX_NUM_GUESS = 4;

	private Socket connection;
	private Queue<ClientGameHandler> lobbyQueue;
	private int round;
	private Boolean onRound;
	private Boolean endTimer = false;

	private Boolean initialPrompt = true;

	public Server() {
		try {
			fileHandler = new FileHandler("ProgramLogs.log");
			logger.addHandler(fileHandler);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ServerSocket server;
		round = 1;
		lobbyQueue = new LinkedList<ClientGameHandler>();

		try {
			server = new ServerSocket(PORT);
			System.out.println("Server is running on port " + PORT);

			while (true) {
				onRound = false;

				connection = server.accept();
				logger.log(Level.INFO,
						"COMMUNICATION LOG: " + connection.getRemoteSocketAddress() + " CONNECT TO SERVER");
				ClientGameHandler clientHandler = new ClientGameHandler(connection);
				Thread regThread = new Thread(new ClientRegistrationHandler(lobbyQueue, clientHandler, connection));
				regThread.start();

				if (!onRound) {
					Thread startGameThread = new StartGameThread(this);

					startGameThread.start();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public Queue<ClientGameHandler> getQueue() {
		return lobbyQueue;
	}

	public int getRoundNum() {
		return round;
	}

	public Boolean getEndTimer() {
		return endTimer;
	}

	public Boolean getOnRound() {
		return onRound;
	}

	public Boolean getInitialPrompt() {
		return initialPrompt;
	}

	public FileHandler getFileHandler() {
		return fileHandler;
	}

	public void setRoundNum(int round) {
		this.round = round;
	}

	public void setInitialPrompt(boolean initialPrompt) {
		this.initialPrompt = initialPrompt;
	}

	public void setOnRound(boolean onRound) {
		this.onRound = onRound;
	}

	public void setEndTimer(boolean endTimer) {
		this.endTimer = endTimer;
	}
}
