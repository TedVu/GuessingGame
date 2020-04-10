package assignment.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static final int PORT = 9090;
	private Socket connection;

	public Server() {
		ServerSocket server;
		try {
			server = new ServerSocket(PORT);
			System.out.println("Server is running on port " + PORT);

			while (true) {
				connection = server.accept();
				ClientHandler clientHandler = new ClientHandler(connection);
				clientHandler.run();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
