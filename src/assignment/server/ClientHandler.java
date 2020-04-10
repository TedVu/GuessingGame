package assignment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
	private Socket connection;
	private BufferedWriter out;
	private BufferedReader in;

	public ClientHandler(Socket connection) {
		this.connection = connection;
	}

	/* (non-Javadoc)
	 *  To be implement some logic for guessing game here
	 */
	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			out.write("Welcome to guessing game\n");
			out.write("Please specify your guess number here:\n");
			out.flush();

			// byte[] buffer = new byte[1024];
			// int bytesRead;
			// do {
			// bytesRead = in.read(buffer, 0, buffer.length);
			// if (bytesRead <= 0) {
			// break;
			// }
			//
			// } while (true);
			// for (int i = 0; i < buffer.length; ++i) {
			// System.out.println(buffer[i]);
			// }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
