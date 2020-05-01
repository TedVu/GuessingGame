package assignment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String clientType = in.readLine();

				if (clientType.equalsIgnoreCase(KEEP_ALIVE_CODE.PLAYGAME.toString())) {
					ClientHandler clientHandler = new ClientHandler(connection, randomNum);
					clientHandler.start();
				} else if (clientType.equalsIgnoreCase(KEEP_ALIVE_CODE.PING.toString())) {
					connection.close();
				}

			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
