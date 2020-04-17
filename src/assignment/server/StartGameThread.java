package assignment.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import assignment.client.CommunicationCode;

public class StartGameThread extends Thread {

	private BufferedWriter writer;
	private Queue<ClientGameHandler> lobbyQueue;
	private int round;
	private Boolean onRound;
	private Boolean endTimer = false;

	private Boolean initialPrompt;

	private Server mainServer;

	public StartGameThread(Server mainServer) {
		this.mainServer = mainServer;
	}

	public synchronized void run() {

		lobbyQueue = mainServer.getQueue();
		round = mainServer.getRoundNum();
		onRound = mainServer.getOnRound();
		endTimer = mainServer.getEndTimer();
		initialPrompt = mainServer.getInitialPrompt();

		while (true) {
			// synching effect when prompting
			if (initialPrompt) {
				synchronized (initialPrompt) {
					// initialPrompt = false;
					mainServer.setInitialPrompt(false);
				}

				System.out.print("\nEnter START to start game:");
				Scanner in = new Scanner(System.in);
				String cmd = in.nextLine();
				if (cmd.equalsIgnoreCase("START")) {

					if (!onRound && lobbyQueue.size() > 0) {
						int randomNum = ThreadLocalRandom.current().nextInt(Server.MIN_GUESS, Server.MAX_GUESS);
						System.out.println("\nRandom Number in this round:" + randomNum);

						// 3 are playing, 3 are waiting => there cannot be concurrent round play
						// wait for next available round
						synchronized (onRound) {
							// onRound = true;
							mainServer.setOnRound(true);
						}

						Set<ClientGameHandler> playersInCurrentRound = new HashSet<ClientGameHandler>();
						Iterator<ClientGameHandler> it = lobbyQueue.iterator();
						int numPlayer = 0;

						while (it.hasNext() && numPlayer < Server.MAX_PLAYER_EACH_ROUND) {
							ClientGameHandler player = it.next();
							player.setRandomNum(randomNum);
							playersInCurrentRound.add(player);
							numPlayer++;
						}

						StringBuilder participantsName = new StringBuilder("");
						for (ClientGameHandler player : playersInCurrentRound) {
							participantsName.append(player.getClientName()).append(" ");
							player.setNumPlayerRound(numPlayer);
							player.setRoundNum(round);
						}

						for (ClientGameHandler player : playersInCurrentRound) {
							player.setNameParticipants(participantsName.toString());
							player.start();
						}

						Thread trackTime = new Thread() {
							@Override
							public void run() {
								try {
									Thread.sleep(Server.TIME_PER_ROUND * 1000);
									synchronized (endTimer) {
										// endTimer = true;
										mainServer.setEndTimer(true);
									}

								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									System.out.println(
											"All players in this round has finished gameplay before the timer goes off");
									try {
										this.finalize();
									} catch (Throwable e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}

								}
							}
						};
						trackTime.start();
						boolean exitNotDueToTimer = false;
						while (true && !endTimer) {
							boolean guessContinue = false;

							for (ClientGameHandler thread : playersInCurrentRound) {
								if (thread.isAlive()) {
									guessContinue = true;
								}
							}
							if (!guessContinue) {
								exitNotDueToTimer = true;
								trackTime.interrupt();
								break;
							}
						}

						if (!exitNotDueToTimer) {
							endTimer = false;
							for (ClientGameHandler thread : playersInCurrentRound) {
								if (thread.isAlive()) {
									thread.interrupt();
									try {
										writer = new BufferedWriter(
												new OutputStreamWriter(thread.getConnection().getOutputStream()));
										writer.write(CommunicationCode.TIMEOUT.toString());
										writer.write("\n");
										writer.flush();
										writer.write("Game Ended due to timeout");
										writer.write("\n");
										writer.flush();

									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}

						// write the final result to each client here
						Set<Socket> playerSockets = new HashSet<Socket>();
						StringBuilder finalResult = new StringBuilder("");
						List<ClientGameHandler> winners = new ArrayList<>();
						List<ClientGameHandler> losers = new ArrayList<>();
						List<ClientGameHandler> notFinish = new ArrayList<>();
						for (ClientGameHandler player : playersInCurrentRound) {
							playerSockets.add(player.getConnection());
							if (!player.getExitGuess() && !player.isInterrupted()) {
								if (player.getGuessSuccess()) {
									winners.add(player);
								} else {
									losers.add(player);
								}
							} else {
								notFinish.add(player);
							}
						}
						finalResult.append("Winners: ");
						winners.sort(new Comparator<ClientGameHandler>() {

							@Override
							public int compare(ClientGameHandler p1, ClientGameHandler p2) {
								// TODO Auto-generated method stub
								if (p1.getNumGuessClient() < p2.getNumGuessClient()) {
									return -1;
								} else {
									return 1;
								}
							}

						});
						for (ClientGameHandler player : winners) {
							finalResult.append(player.getClientName()).append(" ").append(player.getNumGuessClient())
									.append(" ");
						}
						finalResult.append("\t|\tLosers: ");
						for (ClientGameHandler player : losers) {
							finalResult.append(player.getClientName()).append(" ").append(player.getNumGuessClient())
									.append(" ");
						}
						finalResult.append("\t|\tNot Finish Game: ");
						for (ClientGameHandler player : notFinish) {
							finalResult.append(player.getClientName()).append(" ");
						}

						for (Socket playerSocket : playerSockets) {
							try {
								writer = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
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
							RepromptForRegistrationHandler reprompt = new RepromptForRegistrationHandler(lobbyQueue,
									player);
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
							// round++;
							mainServer.setRoundNum(++round);
							mainServer.setOnRound(false);
							// onRound = false;
						}
						// initialPrompt = true;
						mainServer.setInitialPrompt(true);
					} else {
						// initialPrompt = true;
						mainServer.setInitialPrompt(true);
						System.out.println("\nThere is currently ongoing round or no people joining this round");
					}
				} else {
					System.out.println("Invalid command\n");
				}
			} else {
				break;
			}
		}
	}
}
