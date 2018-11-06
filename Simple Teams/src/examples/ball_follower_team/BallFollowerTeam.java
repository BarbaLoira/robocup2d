package examples.ball_follower_team;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class BallFollowerTeam extends AbstractTeam {

	public BallFollowerTeam(String suffix) {
		super(suffix, 7, true);// 7 jogadores , true ( com goleiro )

	}

	// ag = 1 goleiro ag > 1 outros tipos de jogadores
	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		System.out.println("launch player");

		if (ag == 1) {
			Defender player = new Defender(commander);
			player.start();

		} else if (ag > 1 && ag <= 3) {

			Defender player = new Defender(commander);
			player.start();

		} else if (ag > 3 && ag <= 5) {

			Defender player = new Defender(commander);
			player.start();

		} else if (ag > 5 && ag <= 7) {

			Defender player = new Defender(commander);
			player.start();

		}

	}
}
