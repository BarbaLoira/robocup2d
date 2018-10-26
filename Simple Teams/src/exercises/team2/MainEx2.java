package exercises.team2;

import java.net.UnknownHostException;


public class MainEx2 {

	public static void main(String[] args) throws UnknownHostException {
		Exercise2Team team1 = new Exercise2Team("Palmeiras");
		//Exercise2Team team2 = new Exercise2Team("Corinthians");
		
		team1.launchTeamAndServer();
		//team2.launchTeam();
	}
	
}
