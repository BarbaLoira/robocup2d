package exercises.team1;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;


public class Exercise1Team extends AbstractTeam {

	public Exercise1Team(String suffix) {
		super("Ex1" + suffix, 2, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		double targetX, targetY;
		
		if (ag == 0) {
			targetY = 34.0d / 2;   //posi��o que aparece mais baixa no monitor
		} else {
			targetY = -34.0d / 2;  //posi��o mais alta
		}
		
		targetX = 52.5d / 2;
		
		Exercise1Player pl = new Exercise1Player(commander, targetX, targetY);
		pl.start();
	}

}
