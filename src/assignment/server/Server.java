package assignment.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Ted Vu - S3678491
 * 
 *         This class represents a listening endpoint of a connection request It<br>
 *         is also responsible for managing the thread pool
 *
 */
public class Server {

	private static final Logger logger = Logger.getLogger(Server.class.getName());
	private FileHandler communicationFileHandler;

	public static final int PORT = 9090; // your allocated port on school server
	public static final int MIN_GUESS = 0;
	public static final int MAX_GUESS = 12;
	public static final int MAX_PLAYER_EACH_ROUND = 3; // 3 player in each round only
	public static final int TIME_PER_ROUND = 20; // in seconds
	public static final int MAX_PLAYER_QUEUE = 6; // 6 player waiting in the queue
	public static final int MAX_NUM_GUESS = 4;

	private ServerSocket TCPServer;
	private Socket TCPSocket;
	private DatagramSocket UDPSocket;
	private Queue<ClientGameHandler> lobbyQueue;
	private List<UDPClientHandler> clientPings;
	private int round;
	private Boolean onRound;
	private Boolean endTimer = false;

	private Boolean initialPrompt = true;
	private boolean startGameThreadExist = false;

	public Server() {
		// register logger here
		try {
			communicationFileHandler = new FileHandler("CommunicationLogs.log");
			logger.addHandler(communicationFileHandler);
			SimpleFormatter formatter = new SimpleFormatter();
			communicationFileHandler.setFormatter(formatter);
			logger.setUseParentHandlers(false); // avoid logging to console

		} catch (SecurityException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		round = 1;
		lobbyQueue = new LinkedList<ClientGameHandler>();
		clientPings = new ArrayList<UDPClientHandler>();

		try {
			// we have two servers one for handling ping
			// one for handling gameplay
			UDPSocket = new DatagramSocket(PORT);
			TCPServer = new ServerSocket(PORT);
			System.out.println("TCP Server is running on port " + PORT + " to handle gameplay");
			System.out.println("UDP Server is running on port " + PORT + " to handle ping");

			while (true) {
				onRound = false;

				TCPSocket = TCPServer.accept();

				byte[] buf = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(buf, 1024);
				UDPSocket.receive(receivePacket);

				logger.log(Level.INFO, TCPSocket.getRemoteSocketAddress() + " CONNECT TO SERVER");

				ClientGameHandler clientHandler = new ClientGameHandler(TCPSocket);

				Thread regThread = new Thread(new ClientRegistrationHandler(lobbyQueue, clientHandler, TCPSocket));
				regThread.start();

				// call join() here will have the most effect on the very first player
				// blocking START game before actually there is a player
				regThread.join();

				// start a ping thread concurrent with gameplay, the effect is pinging
				// between server and client during gameplay
				UDPClientHandler clientPingHandler = new UDPClientHandler(receivePacket.getPort(),
						clientHandler.getClientName());
				clientPings.add(clientPingHandler);
				clientPingHandler.start();

				if (!onRound) {
					Thread startGameThread = new StartGameThread(this);

					startGameThread.start();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
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

	public boolean getStartGameThreadExist() {
		return startGameThreadExist;
	}

	public void setStartGameThreadExist() {
		startGameThreadExist = true;
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
