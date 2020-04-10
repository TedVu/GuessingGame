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

	};

	public abstract String toString();

}
