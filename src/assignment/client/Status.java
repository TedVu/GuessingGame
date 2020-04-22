package assignment.client;

/**
 * @author Vu Duy Anh Tuan This class represents Communication Code that
 *         server</br>
 *         and client use to identify which actions to perform
 *
 */
public enum Status {

	/**
	 * @author Vu Duy Anh Tuan Will be sent by server to client when client guesses
	 *         correctly
	 *
	 */
	SUCCESS {

		@Override
		public String toString() {
			return "SUCCESS";
		}

	},

	/**
	 * @author Vu Duy Anh Tuan Will be sent by server to client when client guesses
	 *         fail 4 times
	 */
	FAIL {

		@Override
		public String toString() {
			return "FAIL";
		}

	};
	public abstract String toString();

}
