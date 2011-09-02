package combattalk.speech;

import combattalk.mobile.data.People;

public interface CommandHandler {
	public static final int WHERE_IS_COMMAND = 1;
	public static final int SAY_AGAIN_COMMAND = 2;
	public static final int WHO_IS_NEAR_COMMAND = 3;

	public void whereIsCommand(People person);
	public void sayAgainCommand();
	public void whoIsNearCommand(People person);
}
