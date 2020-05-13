package assignment.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Ted Vu - S3678491
 * 
 *         This class represents a client/player
 *
 */
public class Client {

	private static final int PORT = 9090; // server port number
	private static final String HOST = "localhost"; // where server locates (i.e:netprog2.csit.rmit.edu.au)

	private static final int WAIT_TIME = 22;

	private Socket socket;
	private UDPClient UDPSocket;
	private BufferedWriter outSocket;
	private BufferedReader inSocket;
	private Scanner inputClient;
	private boolean quitGame = false;
	private String guessString;

	public Client() {
		try {
			// start a thread to handle ping from client
			UDPSocket = new UDPClient();
			UDPSocket.start();
			socket = new Socket(HOST, PORT);

			inputClient = new Scanner(System.in);

			outSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String registrationMsg = inSocket.readLine();
			System.out.println(registrationMsg + "\n");
			registerClient();

			while (true) {
				String pendingMsg = inSocket.readLine();
				System.out.println("\n" + pendingMsg + "\n");
				String welcomeMsg = inSocket.readLine();
				String participantsMsg = inSocket.readLine();

				String command = inSocket.readLine();
				System.out.println("\t" + welcomeMsg);
				System.out.println(participantsMsg + "\n");
				System.out.print(command + " ");
				int waitTime = WAIT_TIME;
				do {

					long trackTime = 0;
					BufferedReader inputRandomNum = new BufferedReader(new InputStreamReader(System.in));
					long startTime = System.currentTimeMillis();
					while ((trackTime = System.currentTimeMillis() - startTime) <= waitTime * 1000
							&& !inputRandomNum.ready()) {

					}

					if (inputRandomNum.ready()) {
						guessString = inputRandomNum.readLine();
						waitTime -= trackTime / 1000;
					} else {
						guessString = "";
						System.out
								.print("\n\nRound timer goes off, no guess will be recorded\nHit enter to continue: ");
						inputClient.nextLine();
						System.out.println("");
					}

					outSocket.write(guessString);
					outSocket.write("\n");
					outSocket.flush();
					String response = inSocket.readLine();

					if (response.equalsIgnoreCase(CommunicationCode.TIMEOUT.toString())
							|| response.equalsIgnoreCase(CommunicationCode.EXIT.toString())) {
						String timeOutMsg = inSocket.readLine();
						System.out.println(timeOutMsg + "\n");
						break;
					}
					if (response.equalsIgnoreCase(CommunicationCode.SUCCESS.toString())) {
						String result = inSocket.readLine();
						System.out.println("\n" + result);
						break;
					} else {
						System.out.println("\n" + response + "\n");

						command = inSocket.readLine();
						if (command.equalsIgnoreCase(CommunicationCode.FAIL.toString())) {
							String gameOverMsg = inSocket.readLine();
							System.out.println("\n" + gameOverMsg);
							break;
						} else {
							System.out.print(command + " ");
						}
					}
				} while (true);
				String finalMsg = inSocket.readLine();

				System.out.println("\n\t" + finalMsg + "\t");
				String finalResult = inSocket.readLine();
				System.out.println("\n" + finalResult);

				waitTime = WAIT_TIME;
				repromptPlayAgain(waitTime);
				// releasing all the resource when shutting down
				if (quitGame) {
					socket.close();
					inSocket.close();
					outSocket.close();
					break;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		UDPSocket.setEndGame();
	}

	private void repromptPlayAgain(int waitTime) throws IOException {
		do {
			String repromptMsg = inSocket.readLine();
			System.out.print("\n" + repromptMsg);

			BufferedReader inputReprompt = new BufferedReader(new InputStreamReader(System.in));
			long startTime = System.currentTimeMillis();
			while ((System.currentTimeMillis() - startTime) <= waitTime * 1000 && !inputReprompt.ready()) {

			}
			if (inputReprompt.ready()) {
				String answer = inputClient.nextLine();

				outSocket.write(answer);
				outSocket.write("\n");
				outSocket.flush();
				String serverReplayCode = inSocket.readLine();
				if (serverReplayCode.equalsIgnoreCase(CommunicationCode.QUIT.toString())) {
					String goodbyeMsg = inSocket.readLine();
					System.out.println(goodbyeMsg);
					quitGame = true;
					break;
				} else if (serverReplayCode.equalsIgnoreCase(CommunicationCode.FULL.toString())) {
					String fullNoti = inSocket.readLine();
					System.out.println(fullNoti);

				} else if (serverReplayCode.equalsIgnoreCase(CommunicationCode.ERROR.toString())) {
					String errorNoti = inSocket.readLine();
					System.out.println(errorNoti + "\n");
				} else {
					break;
				}
			} else {
				outSocket.write("q");
				outSocket.write("\n");
				outSocket.flush();
				inSocket.readLine();
				String goodbyeMsg = inSocket.readLine();
				System.out.println("\n" + goodbyeMsg);
				quitGame = true;
				break;
			}
		} while (true);
	}

	private void registerClient() throws IOException {
		while (true) {

			String nameMsg = inSocket.readLine();

			System.out.print(nameMsg);
			String nameResponse = inputClient.nextLine();

			outSocket.write(nameResponse);
			outSocket.write("\n");
			outSocket.flush();

			String registerCode = inSocket.readLine();
			if (registerCode.equalsIgnoreCase(CommunicationCode.NOTFULL.toString())) {
				break;
			} else {
				String registerAgainMsg = inSocket.readLine();
				System.out.println(registerAgainMsg + "\n");
			}
		}
	}
}
