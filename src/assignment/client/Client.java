package assignment.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Vu Duy Anh Tuan This class represents a client connecting to server
 *
 */
public class Client {

	private static final int PORT = 9090; // my allocated port number
	private static final String HOST = "localhost"; // where the server resides
	private Socket socket;
	private BufferedWriter outSocket;
	private BufferedReader inSocket;
	private Scanner inputClient;
	private boolean isAlive;
	private ClientPingThread pingThread;
	private String guessString;

	public Client() {

		try {
			socket = new Socket(HOST, PORT); // HOST and PORT of Server
			isAlive = true;
			pingThread = new ClientPingThread(this);
			pingThread.start();

			// obtain stream from socket for communication
			outSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			outSocket.write("PLAYGAME");
			outSocket.write("\n");
			outSocket.flush();

			String welcomeMsg = inSocket.readLine();
			String command = inSocket.readLine();
			System.out.println("\t" + welcomeMsg + "\n");
			System.out.print(command + " ");
			inputClient = new Scanner(System.in);

			// Client will guess number in a loop and maximum of 4 guesses, this will be
			// kept track by server, when reaching max guess server will send code FAIL
			// with fail message
			do {
				guessString = null;

				KeepAliveThreadClient keepAliveThread = new KeepAliveThreadClient(this);
				keepAliveThread.start();
				guessString = inputClient.nextLine();
				keepAliveThread.interrupt();

				outSocket.write(guessString);
				outSocket.write("\n");
				outSocket.flush();
				String response = inSocket.readLine();
				if (response.equalsIgnoreCase(Status.SUCCESS.toString())) {
					String result = inSocket.readLine();
					System.out.println(result);
					break;
				} else {
					System.out.println("\n" + response + "\n");

					command = inSocket.readLine();
					if (command.equalsIgnoreCase(Status.FAIL.toString())) {
						String gameOverMsg = inSocket.readLine();
						System.out.println("\n" + gameOverMsg);
						break;
					} else {
						System.out.print(command + " ");
					}
				}
			} while (true);

		} catch (IOException e) {
			if (isAlive) {
				System.out.println(e.getMessage());
			} else {
				System.out.println("Server is down please reboot the server\n");
			}

		} finally {
			// avoid leaking of resource make sure to close the stream and connection
			closeResource();
		}
	}

	public void closeResource() {
		try {
			if (inSocket != null) {
				inSocket.close();
			}
			if (outSocket != null) {
				outSocket.close();
			}
			if (socket != null) {
				socket.close();
			}
			if (inputClient != null) {
				inputClient.close();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());

		}
	}

	public Socket getConnection() {
		return socket;
	}

	public void setServerDown() {
		isAlive = false;
	}

	public String getGuessString() {
		return guessString;
	}

}
