package assignment.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private static final int PORT = 9090;
	private static final String HOST = "localhost";
	private Socket socket;
	private BufferedWriter outSocket;
	private BufferedReader inSocket;
	private Scanner inputClient;

	public Client() {

		try {
			byte[] buffer = new byte[1024];
			socket = new Socket(HOST, PORT);
			outSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String welcomeMsg = inSocket.readLine();
			String command = inSocket.readLine();
			System.out.println(welcomeMsg);
			System.out.println(command);
			inputClient = new Scanner(System.in);
			do {
				try {
					String guessString = inputClient.nextLine();

					int guessNum = Integer.parseInt(guessString);// validation code here
					outSocket.write(guessString);
					outSocket.write("\n");
					outSocket.flush();
					// implements both Client-Server validation ?
					break;
				} catch (NumberFormatException e) {
					System.out.println("Error: There is error in input format");
				}
			} while (true);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
