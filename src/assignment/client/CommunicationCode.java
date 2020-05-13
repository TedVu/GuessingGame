package assignment.client;

/**
 * @author Ted Vu - S3678491 CommunicationCode so clients and server can
 *         communicate with each other
 *
 */
public enum CommunicationCode {

	SUCCESS {

		@Override
		public String toString() {
			return "SUCCESS";
		}

	},
	FAIL {

		@Override
		public String toString() {
			return "FAIL";
		}

	},
	CONTINUE {

		@Override
		public String toString() {
			return "CONTINUE";
		}

	},

	TIMEOUT {

		@Override
		public String toString() {
			return "TIMEOUT";
		}

	},

	EXIT {

		@Override
		public String toString() {
			return "EXIT";
		}

	},

	FULL {

		@Override
		public String toString() {
			return "FULL";
		}

	},

	NOTFULL {

		@Override
		public String toString() {
			return "NOTFULL";
		}

	},

	QUIT {

		@Override
		public String toString() {
			return "QUIT";
		}

	},

	PLAYAGAIN {

		@Override
		public String toString() {
			return "PLAYAGAIN";
		}

	},

	ERROR {

		@Override
		public String toString() {
			return "ERROR";
		}
	},

	SERVERUP {

		@Override
		public String toString() {
			return "SERVERUP";
		}

	},

	REQUESTCONNECT {

		@Override
		public String toString() {
			return "REQUESTCONNECT";
		}

	}

	;

	public abstract String toString();

}
