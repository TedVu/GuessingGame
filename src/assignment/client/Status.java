package assignment.client;

public enum Status {

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

	};

	public abstract String toString();

}
