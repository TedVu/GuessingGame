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

						// produce a random number
						int randomNum = ThreadLocalRandom.current().nextInt(Server.MIN_GUESS, Server.MAX_GUESS);

						System.out.println("\nRandom Number in this round:" + randomNum);

						// 3 are playing, 3 are waiting => there cannot be concurrent round play
						// wait for next available round
						// locking to alter global variable
						synchronized (onRound) {
							mainServer.setOnRound(true);
						}

						Set<ClientGameHandler> playersInCurrentRound = new HashSet<ClientGameHandler>();
						int numPlayer = addPlayerToCurrentRound(randomNum, playersInCurrentRound);

						StringBuilder participantsName = getAllParticipantsName(playersInCurrentRound, numPlayer);

						startPlayingGameThread(playersInCurrentRound, participantsName);

						Thread trackTime = startTrackingTimerThread();

						boolean exitNotDueToTimer = trackingGamePlayWithTimer(playersInCurrentRound, trackTime);

						handleTimeoutResult(playersInCurrentRound, exitNotDueToTimer);

						// write the final result to each client here
						Set<Socket> playerSockets = new HashSet<Socket>();
						StringBuilder finalResult = new StringBuilder("");
						List<ClientGameHandler> winners = new ArrayList<>();
						List<ClientGameHandler> losers = new ArrayList<>();
						List<ClientGameHandler> notFinish = new ArrayList<>();
						categorizePlayers(playersInCurrentRound, playerSockets, winners, losers, notFinish);

						finalResult.append("Winners: ");
						addWinnerPlayerWithRanking(finalResult, winners);
						finalResult.append("\t|\tLosers: ");
						addLoserPlayer(finalResult, losers);
						finalResult.append("\t|\tNot Finish Game: ");
						addNotFinishGamePlayer(finalResult, notFinish);

						writeFinalResult(playerSockets, finalResult);

						Set<RepromptForRegistrationHandler> repromptThreads = new HashSet<>();
						startRepromptThread(playersInCurrentRound, repromptThreads);
						trackingRepromptThread(repromptThreads);

						synchronized (onRound) {
							mainServer.setRoundNum(++round);
							mainServer.setOnRound(false);
						}
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

	private void trackingRepromptThread(Set<RepromptForRegistrationHandler> repromptThreads) {
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
	}

	private void startRepromptThread(Set<ClientGameHandler> playersInCurrentRound,
			Set<RepromptForRegistrationHandler> repromptThreads) {
		for (ClientGameHandler player : playersInCurrentRound) {
			RepromptForRegistrationHandler reprompt = new RepromptForRegistrationHandler(lobbyQueue, player);
			repromptThreads.add(reprompt);
			reprompt.start();
		}
	}

	private void writeFinalResult(Set<Socket> playerSockets, StringBuilder finalResult) {
		for (Socket playerSocket : playerSockets) {
			try {
				writer = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
				writer.write("Final result of round " + round + "\n");
				writer.flush();
				writer.write(finalResult.toString());
				writer.write("\n");
				writer.flush();
			} catch (IOException e) {
				System.err.println("Error in client side an interrupted exception occured\n");
			}
		}
	}

	private void addNotFinishGamePlayer(StringBuilder finalResult, List<ClientGameHandler> notFinish) {
		for (ClientGameHandler player : notFinish) {
			finalResult.append(player.getClientName()).append(" ");
		}
	}

	private void addLoserPlayer(StringBuilder finalResult, List<ClientGameHandler> losers) {
		for (ClientGameHandler player : losers) {
			finalResult.append(player.getClientName()).append(" ").append(player.getNumGuessClient()).append(" ");
		}
	}

	private void addWinnerPlayerWithRanking(StringBuilder finalResult, List<ClientGameHandler> winners) {
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
		addLoserPlayer(finalResult, winners);
	}

	private void categorizePlayers(Set<ClientGameHandler> playersInCurrentRound, Set<Socket> playerSockets,
			List<ClientGameHandler> winners, List<ClientGameHandler> losers, List<ClientGameHandler> notFinish) {
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
	}

	private void handleTimeoutResult(Set<ClientGameHandler> playersInCurrentRound, boolean exitNotDueToTimer) {
		if (!exitNotDueToTimer) {
			endTimer = false;

			for (ClientGameHandler thread : playersInCurrentRound) {
				if (thread.isAlive()) {
					thread.interrupt();
					try {
						writer = new BufferedWriter(new OutputStreamWriter(thread.getConnection().getOutputStream()));
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
	}

	private boolean trackingGamePlayWithTimer(Set<ClientGameHandler> playersInCurrentRound, Thread trackTime) {
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
		return exitNotDueToTimer;
	}

	private Thread startTrackingTimerThread() {
		Thread trackTime = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(Server.TIME_PER_ROUND * 1000);
					endTimer = true;

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("All players in this round has finished gameplay before the timer goes off");
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
		return trackTime;
	}

	private void startPlayingGameThread(Set<ClientGameHandler> playersInCurrentRound, StringBuilder participantsName) {
		for (ClientGameHandler player : playersInCurrentRound) {
			player.setNameParticipants(participantsName.toString());

			player.start();
		}
	}

	private int addPlayerToCurrentRound(int randomNum, Set<ClientGameHandler> playersInCurrentRound) {
		Iterator<ClientGameHandler> it = lobbyQueue.iterator();
		int numPlayer = 0;

		while (it.hasNext() && numPlayer < Server.MAX_PLAYER_EACH_ROUND) {
			ClientGameHandler player = it.next();
			player.setRandomNum(randomNum);
			playersInCurrentRound.add(player);
			numPlayer++;
		}
		return numPlayer;
	}

	private StringBuilder getAllParticipantsName(Set<ClientGameHandler> playersInCurrentRound, int numPlayer) {
		StringBuilder participantsName = new StringBuilder("");
		for (ClientGameHandler player : playersInCurrentRound) {
			participantsName.append(player.getClientName()).append(" ");
			player.setNumPlayerRound(numPlayer);
			player.setRoundNum(round);
		}
		return participantsName;
	}
}
