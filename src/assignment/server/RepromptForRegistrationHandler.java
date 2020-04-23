package assignment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import assignment.client.CommunicationCode;

public class RepromptForRegistrationHandler extends Thread {

	private static final Logger logger = Logger.getLogger(Server.class.getName());

	private Queue<ClientGameHandler> queue;
	private Socket connection;
	private ClientGameHandler client;

	private BufferedWriter out;
	private BufferedReader in;

	public RepromptForRegistrationHandler(Queue<ClientGameHandler> queue, ClientGameHandler client) {
		this.queue = queue;
		this.client = client;
	}

	@Override
	public void run() {

		connection = client.getConnection();
		try {
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while (true) {
				out.write("Do you want to play again (p-play again | q-quit)?\n");
				out.flush();

				// some form of timer implement here
				// if not receive any response for an amount of time kill this thread
				String answer = null;
				try {
					answer = in.readLine();
				} catch (SocketException e) {
					System.out.println("DEBUG");
				}
				if (answer.equalsIgnoreCase("p") && queue.size() <= Server.MAX_PLAYER_QUEUE) {
					synchronized (queue) {
						queue.remove();
						queue.add(new ClientGameHandler(connection, client.getClientName()));
					}
					out.write(CommunicationCode.NOTFULL.toString());
					out.write("\n");
					out.flush();
					out.write("Waiting for server to announce next available round...\n");
					out.flush();

					break;

				} else if (answer.equalsIgnoreCase("q")) {
					logger.log(Level.INFO, connection.getRemoteSocketAddress() + " DISCONNECT TO SERVER");

					out.write(CommunicationCode.QUIT.toString());
					out.write("\n");
					out.flush();
					synchronized (queue) {
						queue.remove();
					}
					out.write("Goodbye\n");
					out.flush();
					break;
				} else if (queue.size() > Server.MAX_PLAYER_QUEUE) {
					out.write(CommunicationCode.FULL.toString());
					out.write("\n");
					out.flush();
					out.write("The queue is full at the moment please register later...");
					out.write("\n");
					out.flush();
					continue;
				} else {
					// handling error here
					out.write(CommunicationCode.ERROR.toString());
					out.write("\n");
					out.flush();
					out.write("Invalid input please reenter\n");
					out.flush();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

	}

}
