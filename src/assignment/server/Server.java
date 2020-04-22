package assignment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
	public static final int MAX_NUM_GUESS = 4;
	private static final int PORT = 9090;
	private static final int MIN = 0;
	private static final int MAX = 12;
	private Socket connection;

	public Server() {
		ServerSocket server;

		try {
			server = new ServerSocket(PORT);
			System.out.println("Server is running on port " + PORT);

			while (true) {
				int randomNum = ThreadLocalRandom.current().nextInt(MIN, MAX);
				connection = server.accept();
				ClientHandler clientHandler = new ClientHandler(connection, randomNum);
				clientHandler.run();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
