package assignment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
	public static final int MAX_NUM_GUESS = 4;
	private static final int PORT = 9090; // the port where the server opens a socket
	private static final int MIN = 0;
	private static final int MAX = 12;
	private Socket connection;
	private BufferedReader in;

	public Server() {
		ServerSocket server = null;

		try {
			server = new ServerSocket(PORT);
			System.out.println("Server is running on port " + PORT);

			while (true) {
				int randomNum = ThreadLocalRandom.current().nextInt(MIN, MAX);
				connection = server.accept();

				ClientHandler clientHandler = new ClientHandler(connection, randomNum);
				clientHandler.start();

			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
