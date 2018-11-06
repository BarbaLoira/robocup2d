package examples.ball_follower_team;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.utils.Vector2D;

public class Defender extends BallFollowerPlayer {

	public Defender(PlayerCommander player) {
		super(player);
	}

	@Override
	public void run() {
		super.run();
	 
//estados
		while (commander.isActive()) {
			System.out.println(">> Runing...");
			long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
			updatePerceptions();

			switch (this.selfPerc.getUniformNumber()) {

			case 1:

				initializePosition(-48, 0);
				goolKeeperAction(nextIteration);
				break;
			case 2:

				initializePosition(-30, -10);// cima
				defenderAction(nextIteration, -1);
				break;
			case 3:

				initializePosition(-30, 10);// baixo
				defenderAction(nextIteration, 1);
				break;
			case 4:

				initializePosition(-18.75, -22.92);// cima
				fullBackAction(nextIteration, -1);
				break;
			case 5:

				initializePosition(-18.75, 22.92);
				fullBackAction(nextIteration, 1); // baixo
				break;
			case 6:

				initializePosition(-19.17, 0);
				midFieldAction(nextIteration);
			case 7:

				attackerAction(nextIteration);
				break;
			default:
				break;
			}

		}
	}

}
