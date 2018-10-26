package exercises.team1;

import java.net.UnknownHostException;


public class MainEx1 {

	public static void main(String[] args) throws UnknownHostException {
		Exercise1Team team1 = new Exercise1Team("palm");
		Exercise1Team team2 = new Exercise1Team("corin");
		
		team1.launchTeamAndServer();
		team2.launchTeam();
	}
	
}
