package assignment.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.sun.org.apache.bcel.internal.classfile.Code;

public class Client {

	private static final int PORT = 9090;
	private static final String HOST = "localhost";
	private Socket socket;
	private BufferedWriter outSocket;
	private BufferedReader inSocket;
	private Scanner inputClient;

	public Client() {

		try {
			socket = new Socket(HOST, PORT);
			inputClient = new Scanner(System.in);

			outSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String registrationMsg = inSocket.readLine();
			String nameMsg = inSocket.readLine();

			System.out.println(registrationMsg + "\n");
			System.out.print(nameMsg);
			String nameResponse = inputClient.nextLine();
			outSocket.write(nameResponse);
			outSocket.write("\n");
			outSocket.flush();

			
			System.exit(0);
			String welcomeMsg = inSocket.readLine();
			String command = inSocket.readLine();
			System.out.println("\t" + welcomeMsg + "\n");
			System.out.print(command + " ");
			do {
				String guessString = inputClient.nextLine();
				outSocket.write(guessString);
				outSocket.write("\n");
				outSocket.flush();
				String response = inSocket.readLine();
				if (response.equalsIgnoreCase(Status.SUCCESS.toString())) {
					String result = inSocket.readLine();
					System.out.println("\n" + result);
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

		} catch (Exception e) {
			System.exit(0);// TODO Auto-generated catch block
		}
	}
}
