package examples.ball_follower_team;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;


public class BallFollowerTeam extends AbstractTeam {

	public BallFollowerTeam(String suffix) {
	 super( suffix, 4, true);// 7 jogadores , true ( com goleiro )
	 
	} 
	
	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {// ag = 1 goleiro ag > 1 outros tipos de jogadores
	System.out.println("launch player");
		BallFollowerPlayer pl = new BallFollowerPlayer(commander);
		pl.start(); 	
  } 
}
