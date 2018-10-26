package exercises.team2;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EMatchState;
import simple_soccer_lib.utils.Vector2D;


public class Exercise2Player extends Thread {
	private static final double ERROR_RADIUS = 2.0d;
	
	private enum State { ATTACKING, RETURN_TO_HOME };

	private PlayerCommander commander;
	private State state;
	
	private PlayerPerception selfInfo;
	private FieldPerception  fieldInfo;
	private MatchPerception  matchInfo;
	
	private Vector2D homebase; //posição base do jogador
	
	public Exercise2Player(PlayerCommander player, double x, double y) {
		commander = player;
		homebase = new Vector2D(x, y);
	}
	
	@Override
	public void run() {
		_printf("Waiting initial perceptions...");
		selfInfo  = commander.perceiveSelfBlocking();
		fieldInfo = commander.perceiveFieldBlocking();
		matchInfo = commander.perceiveMatchBlocking();
		
		state = State.RETURN_TO_HOME; //todos começam neste estado
		
		_printf("Starting in a random position...");
		commander.doMoveBlocking(Math.random() * (selfInfo.getSide() == EFieldSide.LEFT ? -52.0 : 52.0), (Math.random() * 68.0) - 34.0);
 
		if (selfInfo.getSide() == EFieldSide.RIGHT) { //ajusta a posição base de acordo com o lado do jogador (basta mudar o sinal do x)
			homebase.setX(- homebase.getX());
		}

		try {
			Thread.sleep(3000); // espera, para dar tempo de ver as mensagens iniciais
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while (commander.isActive()) {
			updatePerceptions();  //deixar aqui, no começo do loop, para ler o resultado do 'doMove'

			if (matchInfo.getState() == EMatchState.PLAY_ON) {
			
				switch (state) {
				case RETURN_TO_HOME:
					stateReturnToHomeBase();
					break;
				case ATTACKING:
					stateAttacking();
					break;
				default:
					_printf("Invalid state: %s", state);
					break;	
				}
				
			}
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

	////// Estado RETURN_TO_HOME_BASE ///////
	
	private void stateReturnToHomeBase() {
		if (isCloserToTheBall()) {
			state = State.ATTACKING;
			return;
		}
		
		if (! arrivedAt(homebase)) {
			if (isAlignedTo(homebase)) {
				_printf_once("RTHB: Running to the base...");
				commander.doDashBlocking(100.0d);			
			} else {
				_printf("RTHB: Turning...");
				commander.doTurnToPointBlocking(homebase);
			}
		}		
	}

	private boolean isCloserToTheBall() {
		//TODO: testar se este é o jogador do time que está mais perto da bola, usando os dados em fieldInfo
		//      (implementação provisória para testar -- somente o jogador 1 corre atrás da bola)
		return selfInfo.getUniformNumber() == 1;  
	}
	
	private boolean arrivedAt(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		return Vector2D.distance(myPos, targetPosition) <= ERROR_RADIUS;
	}

	private boolean isAlignedTo(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		double angle = selfInfo.getDirection().angleFrom(targetPosition.sub(myPos));
		return angle < 15.0d && angle > -15.0d;
	}
	
	/////// Estado ATTACKING ///////	
	
	private void stateAttacking() {
		if (! isCloserToTheBall()) {
			state = State.RETURN_TO_HOME;
			return;
		}

		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		
		if (arrivedAt(ballPosition)) {
			commander.doKick(100.0d, 0d);  //TODO: chutar em direção ao gol adversário (usar kick passando direction ou kickTo passando o ponto)
			
		} else {
			if (isAlignedTo(ballPosition)) {
				_printf_once("ATK: Running to the ball...");
				commander.doDashBlocking(100.0d);
			} else {
				_printf("ATK: Turning...");
				commander.doTurnToPointBlocking(ballPosition);
			}
		}		
	}

	//for debugging
	public void _printf_once(String format, Object...objects) {
		if (! format.equals(lastformat)) {  //dependendo, pode usar ==
			_printf(format, objects);
		}
	}
	private String lastformat = ""; 
	public void _printf(String format, Object...objects) {
		String playerInfo = "";
		if (selfInfo != null) {
			playerInfo += "[" + selfInfo.getTeam() + "/" + selfInfo.getUniformNumber() + "] ";
		}
		System.out.printf(playerInfo + format + "%n", objects);
		lastformat = format;
	}

}

