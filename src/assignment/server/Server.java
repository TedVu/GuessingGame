package assignment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
	private static final int PORT = 9090;
	private static final int MIN = 1;
	private static final int MAX = 12;
	private static final int MAX_PLAYER = 3;

	private Socket connection;
	private Queue<ClientGameHandler> lobbyQueue;
	private int round;
	private boolean onRound;

	public Server() {
		ServerSocket server;
		round = 0;
		lobbyQueue = new PriorityQueue<ClientGameHandler>();
		try {
			server = new ServerSocket(PORT);
			System.out.println("Server is running on port " + PORT);

			while (true) {
				onRound = false;
				int randomNum = ThreadLocalRandom.current().nextInt(MIN, MAX);
				connection = server.accept();
				ClientGameHandler clientHandler = new ClientGameHandler(connection, randomNum);
				Thread regThread = new Thread(new ClientRegistrationHandler(lobbyQueue, clientHandler));
				regThread.start();

				while (lobbyQueue.size() >= 0) {
					System.out.println("Please enter START to start round " + round);
					Scanner in = new Scanner(System.in);
					String command = in.nextLine();
					if (command.equalsIgnoreCase("START") && !onRound) {
						onRound = true;
						Set<ClientGameHandler> threadInCurrentRound = new HashSet<ClientGameHandler>();
						Iterator<ClientGameHandler> it = lobbyQueue.iterator();
						int numPlayer = 0;
						while (it.hasNext() && numPlayer < MAX_PLAYER) {
							ClientGameHandler clientGameHandler = it.next();
							threadInCurrentRound.add(clientGameHandler);
							clientGameHandler.start();
							numPlayer++;
						}
						while (true) {
							for (ClientGameHandler thread : threadInCurrentRound) {
								if (thread.isAlive()) {
									continue;
								}
							}
							break;
						}
						// write the final result to each client here
						// remove client
						onRound = true;
					} else {
						System.out.println("Your command is not valid or there is currently ongoing round");
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
