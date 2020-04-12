package assignment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Queue;

public class ClientRegistrationHandler implements Runnable {

	private Queue<ClientGameHandler> queue;
	private ClientGameHandler clientHandler;
	private BufferedWriter out;
	private BufferedReader in;
	private Socket connection;

	public ClientRegistrationHandler(Queue<ClientGameHandler> queue, ClientGameHandler clientHandler,
			Socket connection) {
		// TODO Auto-generated constructor stub
		this.queue = queue;
		this.clientHandler = clientHandler;
		this.connection = connection;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			out.write("Guessing game registration\n");
			out.write("Enter your name:\n");
			out.flush();
			String nameClient = in.readLine();
			// doing some looping validation here
			clientHandler.setClientName(nameClient);
			synchronized (queue) {
				queue.add(clientHandler);
			}
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



}
