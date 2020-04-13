package assignment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Queue;

public class RepromptForRegistrationHandler extends Thread {

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
				out.write("Do you want to play again ?\n");
				out.flush();
				String answer = in.readLine();
				if (answer.equalsIgnoreCase("p") && queue.size() < 6) {
					synchronized (queue) {
						queue.remove();
						queue.add(client);
						break;
					}
				} else if (answer.equalsIgnoreCase("q")) {
					
					synchronized (queue) {
						queue.remove();
					}
					break;
				} else if (queue.size() == 6) {
					continue;
				} else {
					// handling error here
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}

	}
}