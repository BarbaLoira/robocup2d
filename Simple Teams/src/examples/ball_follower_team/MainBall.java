package examples.ball_follower_team;

import java.net.UnknownHostException;


public class MainBall {

	public static void main(String[] args) throws UnknownHostException {
		
		BallFollowerTeam team1 = new BallFollowerTeam("Brasil");
		
		BallFollowerTeam team2 = new BallFollowerTeam("Alemanha");
	 
		team1.launchTeamAndServer();
		team2.launchNoSwarm();
	 
	}
	
}

