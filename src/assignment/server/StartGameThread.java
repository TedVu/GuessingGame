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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import assignment.client.CommunicationCode;

/**
 * @author Ted Vu - S3678491
 * 
 *         This class is responsible for starting a game, it will handle<br>
 *         gameplay in each round
 *
 */
public class StartGameThread extends Thread {

	private static final Logger logger = Logger.getLogger(StartGameThread.class.getName());
	private FileHandler fileHandler;

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

	@Override
	public synchronized void run() {
		if (!mainServer.getStartGameThreadExist()) {
			try {
				fileHandler = new FileHandler("GammingLogs.log");
				logger.addHandler(fileHandler);
				SimpleFormatter formatter = new SimpleFormatter();
				fileHandler.setFormatter(formatter);
				logger.setUseParentHandlers(false);
			} catch (SecurityException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			mainServer.setStartGameThreadExist();
		} else {
			logger.setUseParentHandlers(false);
		}

		lobbyQueue = mainServer.getQueue();
		round = mainServer.getRoundNum();
		onRound = mainServer.getOnRound();
		endTimer = mainServer.getEndTimer();
		initialPrompt = mainServer.getInitialPrompt();

		while (true) {
			// synching effect when prompting
			// if not initial prompt then just terminate gracefully this will avoid
			// creating too many start game threads
			if (initialPrompt) {
				synchronized (initialPrompt) {
					mainServer.setInitialPrompt(false);
				}

				System.out.print("\nEnter START to start game:");
				Scanner in = new Scanner(System.in);
				String cmd = in.nextLine();
				if (cmd.equalsIgnoreCase("START")) {

					if (!onRound && lobbyQueue.size() > 0) {
						logger.log(Level.INFO, "ROUND " + round + " START");

						// produce a random number
						int randomNum = ThreadLocalRandom.current().nextInt(Server.MIN_GUESS, Server.MAX_GUESS);
						logger.log(Level.INFO, "RANDOM NUMBER: " + randomNum);
						// 3 are playing, 3 are waiting => there cannot be concurrent round play
						// wait for next available round
						// locking to alter global variable
						synchronized (onRound) {
							mainServer.setOnRound(true);
						}

						Set<ClientGameHandler> playersInCurrentRound = new HashSet<ClientGameHandler>();
						int numPlayer = addPlayerToCurrentRound(randomNum, playersInCurrentRound);

						StringBuilder participantsName = getAllParticipantsName(playersInCurrentRound, numPlayer);
						logger.log(Level.INFO, numPlayer + " PARTICIPANTS IN THIS ROUND:" + participantsName);
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

						// categorize player type
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

						logger.log(Level.INFO, "ROUND " + round + " FINISHED");

						synchronized (onRound) {
							mainServer.setRoundNum(++round);
							mainServer.setOnRound(false);
						}
						mainServer.setInitialPrompt(true);

					} else {
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
				System.err.println(e.getMessage());
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
						logger.log(Level.INFO, "NOT FINISHED GAME BEFORE TIMER");
					} catch (IOException e) {
						System.out.println(e.getMessage());
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
					logger.log(Level.INFO, "ALL FINISHED GAME BEFORE TIMER");
					try {
						this.finalize();
					} catch (Throwable e1) {
						System.out.println(e1.getMessage());
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
