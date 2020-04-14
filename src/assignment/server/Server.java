package assignment.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

	private BufferedWriter writer;

	private Socket connection;
	private Queue<ClientGameHandler> lobbyQueue;
	private int round;
	private Boolean onRound;
	private Boolean endTimer = false;

	private boolean initialPrompt = true;

	public Server() {
		ServerSocket server;
		round = 1;
		lobbyQueue = new LinkedList<ClientGameHandler>();

		try {
			server = new ServerSocket(PORT);
			System.out.println("Server is running on port " + PORT);

			while (true) {
				onRound = false;

				connection = server.accept();
				ClientGameHandler clientHandler = new ClientGameHandler(connection);
				Thread regThread = new Thread(new ClientRegistrationHandler(lobbyQueue, clientHandler, connection));
				regThread.start();

				Thread startGameThread = new Thread() {
					public synchronized void run() {
						while (true) {
							// synching effect when prompting
							if (initialPrompt) {
								initialPrompt = false;

								System.out.print("\nEnter START to start game:");
								Scanner in = new Scanner(System.in);
								String cmd = in.nextLine();
								if (cmd.equalsIgnoreCase("START")) {
									if (!onRound && lobbyQueue.size() > 0) {
										int randomNum = ThreadLocalRandom.current().nextInt(MIN, MAX);
										System.out.println("\nRandom Number in this round:" + randomNum);

										// 3 are playing, 3 are waiting => there cannot be concurrent round play
										// wait for next available round
										synchronized (onRound) {
											onRound = true;
										}

										Set<ClientGameHandler> playersInCurrentRound = new HashSet<ClientGameHandler>();
										Iterator<ClientGameHandler> it = lobbyQueue.iterator();
										int numPlayer = 0;

										while (it.hasNext() && numPlayer < MAX_PLAYER_EACH_ROUND) {
											ClientGameHandler player = it.next();
											player.setRandomNum(randomNum);
											player.start();
											playersInCurrentRound.add(player);
											numPlayer++;
										}
										Thread trackTime = new Thread() {
											@Override
											public void run() {
												try {
													Thread.sleep(5000);
													synchronized (endTimer) {
														endTimer = true;
													}

												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										};
										trackTime.start();

										while (true && !endTimer) {
											boolean guessContinue = false;

											for (ClientGameHandler thread : playersInCurrentRound) {
												if (thread.isAlive()) {
													guessContinue = true;
												}
											}
											if (!guessContinue) {
												break;
											}
										}

										if (endTimer) {
											endTimer = false;
											for (ClientGameHandler thread : playersInCurrentRound) {
												if (thread.isAlive()) {
													try {
														writer = new BufferedWriter(new OutputStreamWriter(
																thread.getConnection().getOutputStream()));
														writer.write("TIMEOUTCODE");
														writer.write("\n");
														writer.flush();
														writer.write("Game Ended due to timeout");
														writer.write("\n");
														writer.flush();

													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													thread.interrupt();
												}
											}
										}

										// write the final result to each client here
										Set<Socket> playerSockets = new HashSet<Socket>();
										StringBuilder finalResult = new StringBuilder();
										for (ClientGameHandler player : playersInCurrentRound) {
											playerSockets.add(player.getConnection());
											if (!player.getExitGuess()) {
												finalResult.append(player.getClientName()).append(" ")
														.append(player.getNumGuessClient()).append(" ");
											}
										}

										for (Socket playerSocket : playerSockets) {
											try {
												writer = new BufferedWriter(
														new OutputStreamWriter(playerSocket.getOutputStream()));
												writer.write("Final result of round " + round + "\n");
												writer.flush();
												writer.write(finalResult.toString());
												writer.write("\n");
												writer.flush();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
										Set<RepromptForRegistrationHandler> repromptThreads = new HashSet<>();
										for (ClientGameHandler player : playersInCurrentRound) {
											RepromptForRegistrationHandler reprompt = new RepromptForRegistrationHandler(
													lobbyQueue, player);
											repromptThreads.add(reprompt);
											reprompt.start();
										}
										while (true) {
											boolean threadFinish = false;
											for (RepromptForRegistrationHandler repromptThread : repromptThreads) {
												if (repromptThread.isAlive()) {
													threadFinish = true;
												}
											}
											if (!threadFinish) {
												break;
											}
										}
										synchronized (onRound) {
											round++;
											onRound = false;
										}
										initialPrompt = true;
									} else {
										initialPrompt = true;
										System.out.println(
												"\nThere is currently ongoing round or no people joining this round");
									}
								} else {
									System.out.println("Invalid command\n");
								}
							} else {
								break;
							}
						}
					}
				};
				startGameThread.start();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
}
