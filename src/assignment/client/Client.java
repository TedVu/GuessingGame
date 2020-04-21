package assignment.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	private static final int PORT = 9090;
	private static final String HOST = "localhost";

	private int waitTime = 22;

	private Socket socket;
	private BufferedWriter outSocket;
	private BufferedReader inSocket;
	private Scanner inputClient;
	private boolean quitGame = false;
	private String guessString;

	public Client() {
		try {
			socket = new Socket(HOST, PORT);
			inputClient = new Scanner(System.in);

			outSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String registrationMsg = inSocket.readLine();
			System.out.println(registrationMsg + "\n");

			while (true) {

				String nameMsg = inSocket.readLine();

				System.out.print(nameMsg);
				String nameResponse = inputClient.nextLine();

				outSocket.write(nameResponse);
				outSocket.write("\n");
				outSocket.flush();

				String registerCode = inSocket.readLine();
				if (registerCode.equalsIgnoreCase("NOTFULL")) {
					break;
				} else {
					String registerAgainMsg = inSocket.readLine();
					System.out.println(registerAgainMsg + "\n");
				}
			}
			// sending server side
			while (true) {
				String pendingMsg = inSocket.readLine();
				System.out.println("\n" + pendingMsg + "\n");
				String welcomeMsg = inSocket.readLine();
				String participantsMsg = inSocket.readLine();

				String command = inSocket.readLine();
				System.out.println("\t" + welcomeMsg);
				System.out.println(participantsMsg + "\n");
				System.out.print(command + " ");
				waitTime = 22;
				do {

					// guessString = inputClient.nextLine();
					long trackTime = 0;
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					long startTime = System.currentTimeMillis();
					while ((trackTime = System.currentTimeMillis() - startTime) <= waitTime * 1000 && !in.ready()) {

					}

					if (in.ready()) {
						guessString = in.readLine();
						waitTime -= trackTime / 1000;
					} else {
						guessString = "";
						System.out
								.print("\n\nRound timer goes off, no guess will be recorded\nHit enter to continue: ");
						inputClient.nextLine();
						System.out.println("");// print newline here to format result msg
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
				do {
					String repromptMsg = inSocket.readLine();
					System.out.print("\n" + repromptMsg);

					String answer = inputClient.nextLine();
					// conduct validation server side here
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
				} while (true);
				if (quitGame) {
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
