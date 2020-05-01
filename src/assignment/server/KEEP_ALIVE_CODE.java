package assignment.server;

public enum KEEP_ALIVE_CODE {

	PLAYGAME {

		@Override
		public String toString() {
			return "PLAYGAME";
		}

	},

	PING {

		@Override
		public String toString() {
			return "PING";
		}

	};

	public abstract String toString();
}
