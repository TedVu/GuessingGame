package assignment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
	private static final int PORT = 9090;
	private static final int MIN = 1;
	private static final int MAX = 12;
	private static final int MAX_PLAYER_EACH_ROUND = 3;

	private Socket connection;
	private Queue<ClientGameHandler> lobbyQueue;
	private int round;
	private boolean onRound;
	private boolean askStartGame;

	public Server() {
		ServerSocket server;
		round = 1;
		lobbyQueue = new LinkedList<ClientGameHandler>();
		try {
			server = new ServerSocket(PORT);
			System.out.println("Server is running on port " + PORT);

			while (true) {
				onRound = false;
				askStartGame = true;
				int randomNum = ThreadLocalRandom.current().nextInt(MIN, MAX);
				connection = server.accept();
				ClientGameHandler clientHandler = new ClientGameHandler(connection, randomNum);
				Thread regThread = new Thread(new ClientRegistrationHandler(lobbyQueue, clientHandler, connection));
				regThread.start();

				// another thread for starting game ?
				Thread startGameThread = new Thread() {
					public synchronized void run() {
						try {
							regThread.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (askStartGame && !onRound && lobbyQueue.size() > 0) {
							askStartGame = false;
							System.out.println("Please enter START to start round " + round);
							System.out.print("Command: ");
							Scanner in = new Scanner(System.in);
							String command = in.nextLine();
							if (command.equalsIgnoreCase("START") && !onRound) {
								onRound = true;
								Set<ClientGameHandler> threadInCurrentRound = new HashSet<ClientGameHandler>();
								Iterator<ClientGameHandler> it = lobbyQueue.iterator();
								int numPlayer = 0;
								while (it.hasNext() && numPlayer < MAX_PLAYER_EACH_ROUND) {
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
									askStartGame = true;
									round++;
									break;
								}
								// write the final result to each client here
								// remove client
								onRound = false;
							} else {
								System.out.println("Your command is not valid or there is currently ongoing round");
							}
						}
					}
				};
				startGameThread.start();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
