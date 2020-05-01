package assignment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import assignment.client.Status;

/**
 * @author Vu Duy Anh Tuan
 * 
 *         Representing a Thread to handle client request
 *
 */
public class ClientHandler extends Thread {

	private Socket connection;
	private BufferedWriter out;
	private BufferedReader in;
	private int randomNum;

	public ClientHandler(Socket connection, int randomNum) {
		this.connection = connection;
		this.randomNum = randomNum;
	}

	/*
	 * Guessing game logic implemented inside run()
	 */
	@Override
	public void run() {
		int numGuess = Server.MAX_NUM_GUESS;
		boolean guessSuccess = false;
		try {
			// use reader and writer to handle string instead of byte
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

			System.out.println("Random Number in this round:" + randomNum);
			out.write("Welcome to guessing game\n");

			// playing game in a loop with numGuess to keep track of number of guess
			do {
				out.write("Please specify your guess number here:\n");
				out.flush();
				KeepAliveServer keepAliveThread = new KeepAliveServer(connection);
				keepAliveThread.start();
				String inClient = in.readLine();
				keepAliveThread.interrupt();
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
					// help handle invalid input
					--numGuess;
					out.write("Invalid input\n");
					out.flush();
				}
			} while (numGuess >= 1);

			// sending Communication Code to Client
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
			System.out.println(e.getMessage());
		} finally {
			closeResource();
		}
	}

	public void closeResource() {
		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
