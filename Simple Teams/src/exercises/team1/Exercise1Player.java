package exercises.team1;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;


public class Exercise1Player extends Thread {
	private static final double ERROR_RADIUS = 2.0d;

	private PlayerCommander commander;
	
	private PlayerPerception selfInfo;
	private FieldPerception  fieldInfo;
	private MatchPerception  matchInfo;
	
	private Vector2D homebase;
	
	public Exercise1Player(PlayerCommander player, double x, double y) {
		commander = player;
		homebase = new Vector2D(x, y);
	}

	@Override
	public void run() {
		System.out.println(">> 1. Waiting initial perceptions...");
		selfInfo  = commander.perceiveSelfBlocking();
		fieldInfo = commander.perceiveFieldBlocking();
		matchInfo = commander.perceiveMatchBlocking();
		
		System.out.println(">> 2. Movendo para uma posição inicial aleatoria...");
		commander.doMoveBlocking(Math.random() * (selfInfo.getSide() == EFieldSide.LEFT ? -52.0 : 52.0), (Math.random() * 68.0) - 34.0);
 
		if (selfInfo.getSide() == EFieldSide.RIGHT) { //ajusta a posição base de acordo com o lado do jogador (basta mudar o sinal do x)
			homebase.setX(- homebase.getX());
		}

		try {
			Thread.sleep(3000); // espera, para dar tempo de ver as mensagens iniciais
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		selfInfo  = commander.perceiveSelfBlocking();  //para ler a nova posicao (após o move)
		fieldInfo = commander.perceiveFieldBlocking();
		
		System.out.println(">> 3. Turning to the target...");
		turnToHomebase();
		
		try {
			Thread.sleep(3000); //para dar tempo ver esta ação
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(">> 4. Going to the target...");
		while (commander.isActive() && !arrivedAtHomeBase()) {
			commander.doDashBlocking(100.0d);

			if (!isAlignedToHomeBase()) {
				System.out.println(">>    - turning to the target...");
				turnToHomebase();
			}
			
			updatePerceptions(); //non-blocking
		}
			
	}

	private void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();
		MatchPerception newMatch = commander.perceiveMatch();
		
		// só atualiza os atributos se tiver nova percepção (senão, mantém as percepções antigas)
		if (newSelf != null) {
			this.selfInfo = newSelf;
		}
		if (newField != null) {
			this.fieldInfo = newField;
		}
		if (newMatch != null) {
			this.matchInfo = newMatch;
		}
	}

	private boolean arrivedAtHomeBase() {
		Vector2D myPos = selfInfo.getPosition();
		return myPos.distanceTo(homebase) <= ERROR_RADIUS;
	}

	private void turnToHomebase() {
		/*Vector2D myPos = selfInfo.getPosition();
		System.out.println(" => Target = " + homebase + " -- Player = " + myPos);
		Vector2D newDirection = homebase.sub(myPos);
		commander.doTurnToDirectionBlocking(newDirection);*/
		
		commander.doTurnToPointBlocking(homebase);
	}
	
	private boolean isAlignedToHomeBase() {
		Vector2D myPos = selfInfo.getPosition();
		double angle = selfInfo.getDirection().angleFrom(homebase.sub(myPos));
		return angle < 15.0d && angle > -15.0d;
	}
	
}

