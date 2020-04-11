package assignment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Queue;

import assignment.client.Status;

public class ClientGameHandler extends Thread {
	private Socket connection;
	private BufferedWriter out;
	private BufferedReader in;
	private int randomNum;

	public ClientGameHandler(Socket connection, int randomNum) {
		this.connection = connection;
		this.randomNum = randomNum;
	}

	/*
	 * (non-Javadoc) To be implement some logic for guessing game here
	 */
	@Override
	public void run() {
		int numGuess = 4;
		boolean guessSuccess = false;
		try {

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			System.out.println("Random Number in this round:" + randomNum);
			out.write("Welcome to guessing game\n");

			do {
				out.write("Please specify your guess number here:\n");
				out.flush();
				String inClient = in.readLine();
				try {
					int guessNum = Integer.parseInt(inClient);
					if (guessNum == randomNum) {
						guessSuccess = true;
						break;
					} else if (guessNum > randomNum) {
						out.write("Your guess is larger than the generated number\n");

						out.flush();
					} else {
						out.write("Your guess is smaller than the generated number\n");
						out.flush();
					}
					out.flush();
					--numGuess;

				} catch (NumberFormatException e) {
					--numGuess;
					out.write("Invalid input\n");
					out.flush();
				}
			} while (numGuess >= 1);

			if (guessSuccess) {
				out.write(Status.SUCCESS.toString());
				out.write("\n");
				out.write("Congratulations\n");
				out.flush();
			} else {
				out.write(Status.FAIL.toString());
				out.write("\n");
				out.write("Game Over - The correct number is: " + randomNum + "\n");
				out.flush();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
