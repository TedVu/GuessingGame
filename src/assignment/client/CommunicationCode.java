package assignment.client;

public enum CommunicationCode {

	SUCCESS {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "SUCCESS";
		}

	},
	FAIL {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "FAIL";
		}

	},
	CONTINUE {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "CONTINUE";
		}

	},

	TIMEOUT {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "TIMEOUT";
		}

	},

	EXIT {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "EXIT";
		}

	},

	FULL {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "FULL";
		}

	},

	NOTFULL {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "NOTFULL";
		}

	},

	QUIT {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "QUIT";
		}

	},

	PLAYAGAIN {

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "PLAYAGAIN";
		}

	}

	;

	public abstract String toString();

}
