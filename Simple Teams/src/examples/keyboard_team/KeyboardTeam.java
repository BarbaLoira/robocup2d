package examples.keyboard_team;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class KeyboardTeam extends AbstractTeam {

	public KeyboardTeam(boolean pair) {
		super("KEYB", pair? 2 : 1, true);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		if (ag == 0) {
			System.out.println("Player convencional lancado");
			KeyboardPlayer p = new KeyboardPlayer(commander, true);
			p.start();
		} else {
			KeyboardPlayer p = new KeyboardPlayer(commander, false);
			p.start();
		}
	}

	
}
