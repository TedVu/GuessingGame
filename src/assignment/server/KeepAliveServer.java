package assignment.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class KeepAliveServer extends Thread {

	private Socket connection;
	private static final int KEEP_ALIVE_INTERVAL = 5000;

	public KeepAliveServer(Socket connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			while (true) {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				Thread.sleep(KEEP_ALIVE_INTERVAL);
				out.write("Please specify your guess number here:\n");
				out.flush();
			}
		} catch (IOException ex) {

		} catch (InterruptedException e) {
			
		}
	}
}
