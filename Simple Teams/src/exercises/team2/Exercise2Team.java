package exercises.team2;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;


public class Exercise2Team extends AbstractTeam {

	public Exercise2Team(String suffix) {
		super("Ex2" + suffix, 2, false);
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
		
		Exercise2Player pl = new Exercise2Player(commander, targetX, targetY);
		pl.start();
	}

}
