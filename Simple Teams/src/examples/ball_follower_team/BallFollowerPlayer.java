package examples.ball_follower_team;

import java.awt.Rectangle;
import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EPlayerState;
import simple_soccer_lib.utils.Vector2D;

public class BallFollowerPlayer extends Thread {

	private int LOOP_INTERVAL = 100; // 0.1s

	protected PlayerCommander commander;

	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;

	Vector2D goalPos;// = new ​Vector2D(50*side.value(), 0);
	Vector2D ballPos;
	Vector2D initPos;

	int KICK_PASS = 80;
	int KICK_RUN = 25;
	int KICK_GOAL = 100;

	EFieldSide side;

	public BallFollowerPlayer(PlayerCommander player) {
		commander = player;

	}
	
	public void begin() {}
	

	@Override
	public void run() {

		System.out.println(">> 1. waiting initial perceptions...");

		if (this.selfPerc == null)
			this.selfPerc = commander.perceiveSelfBlocking();
		if (this.fieldPerc == null)
			this.fieldPerc = commander.perceiveFieldBlocking();
		if (this.matchPerc == null)
			this.matchPerc = commander.perceiveMatchBlocking();

		this.side = selfPerc.getSide();

		this.goalPos = new Vector2D(50 * side.value(), 0);
		this.goalPos = new Vector2D(50 * side.value(), 0);
		this.ballPos = fieldPerc.getBall().getPosition();

		while (commander.isActive()) {
			System.out.println(">> Runing...");
			long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
			updatePerceptions();

			switch (this.selfPerc.getUniformNumber()) {

			case 1:

				initializePosition(-48, 0);
				goolKeeperAction(nextIteration);
				break;
			case 2:

				initializePosition(-30, -10);// cima
				defenderAction(nextIteration, -1);
				break;
			case 3:

				initializePosition(-30, 10);// baixo
				defenderAction(nextIteration, 1);
				break;
			case 4:

				initializePosition(-18.75, -22.92);// cima
				fullBackAction(nextIteration, -1);
				break;
			case 5:

				initializePosition(-18.75, 22.92);
				fullBackAction(nextIteration, 1); // baixo
				break;
			case 6:

				initializePosition(-19.17, 0);
				midFieldAction(nextIteration);
			case 7:

				attackerAction(nextIteration);
				break;
			default:
				break;
			}

		}
	}

	/*
	 * @Override public void run() {
	 * System.out.println(">> 1. Waiting initial perceptions..."); selfPerc =
	 * commander.perceiveSelfBlocking(); fieldPerc =
	 * commander.perceiveFieldBlocking();
	 * 
	 * System.out.println(">> 2. Moving to initial position...");
	 * commander.doMoveBlocking(-25.0d, 0.0d);
	 * 
	 * selfPerc = commander.perceiveSelfBlocking(); fieldPerc =
	 * commander.perceiveFieldBlocking();
	 * 
	 * System.out.println(">> 3. Now starting..."); while (commander.isActive()) {
	 * 
	 * if (isAlignedToBall()) { if (closeToBall()) { commander.doKick(50.0d, 0.0d);
	 * } else { runToBall(); } } else { turnToBall(); }
	 * 
	 * updatePerceptions(); // non-blocking }
	 * 
	 * System.out.println(">> 4. Terminated!"); }
	 */
	private boolean closeToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();

		return ballPos.distanceTo(myPos) < 2.0;
	}

	private boolean isAlignedToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();

		if (ballPos == null || myPos == null) {
			return false;
		}

		double angle = selfPerc.getDirection().angleFrom(ballPos.sub(myPos));
		// System.out.println("Vetores: " + ballPos.sub(myPos) + " " +
		// selfPerc.getDirection());
		// System.out.println(" => Angulo agente-bola: " + angle);

		return angle < 15.0d && angle > -15.0d;
	}

	double angleToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();

		return selfPerc.getDirection().angleFrom(ballPos.sub(myPos));
	}

	protected void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();
		MatchPerception newMatch = commander.perceiveMatchBlocking();

		if (newSelf != null) {
			this.selfPerc = newSelf;
		}
		if (newField != null) {
			this.fieldPerc = newField;
		}

		if (newMatch != null) {
			this.matchPerc = newMatch;
		}

		ballPos = fieldPerc.getBall().getPosition();
	}

	private void turnToBall() {
		System.out.println("TURN");
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		System.out.println(" => Angulo agente-bola: " + angleToBall() + " (desalinhado)");
		System.out.println(" => Posicoes: ball = " + ballPos + ", player = " + myPos);

		Vector2D newDirection = ballPos.sub(myPos);
		System.out.println(" => Nova direcao: " + newDirection);

		commander.doTurnToPoint(ballPos);
		// DirectionBlocking(newDirection);
	}

	private void runToBall() {
		System.out.println("RUN");
		commander.doDashBlocking(100.0d);
	}

	private void kickToPoint(Vector2D point, double intensity) {

		Vector2D newDirection = point.sub(selfPerc.getPosition());
		double angle = newDirection.angleFrom(selfPerc.getDirection());
		if (angle > 90 || angle < -90) {
			commander.doTurnToDirectionBlocking(newDirection);
			angle = 0;
		}
		commander.doKickBlocking(intensity, angle);
	}

	private boolean isAlignToPoint(Vector2D point, double margin) {
		double angle = point.sub(selfPerc.getPosition()).angleFrom(selfPerc.getDirection());
		return angle < margin && angle > margin * (-1);
	}

	private boolean isPointsAreClose(Vector2D reference, Vector2D point, double margin) {
		return reference.distanceTo(point) <= margin;
	}

	// Retorna a percepção do jogador de um time (side) mais próximo do
	// ponto.
	private PlayerPerception getClosesPlayerPoint(Vector2D point, EFieldSide side, double margin) {
		ArrayList<PlayerPerception> lp = fieldPerc.getTeamPlayers(side);
		PlayerPerception np = null;
		if (lp != null && !lp.isEmpty()) {
			double dist, temp;
			dist = lp.get(0).getPosition().distanceTo(point);
			np = lp.get(0);

			if (isPointsAreClose(np.getPosition(), point, margin)) {
				return np;
			}

			for (PlayerPerception p : lp) {
				if (p.getPosition() == null)
					break;
				if (isPointsAreClose(p.getPosition(), point, margin))
					return p;
				temp = p.getPosition().distanceTo(point);
				if (temp < dist) {
					dist = temp;
					np = p;
				}
			}
		}
		return np;
	}

	private void goolKeeperAction(long nextIteration) {
		double ballX = 0, ballY = 0; // init position of the goolkeeper

		EFieldSide side = selfPerc.getSide();

		Vector2D ballPos;
		Rectangle area = side == EFieldSide.LEFT ? new Rectangle(-52, -20, 16, 40) : new Rectangle(36, -20, 16, 40);

		while (true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {

			case BEFORE_KICK_OFF:
				// posiciona
				commander.doMoveBlocking(this.initPos.getX(), this.initPos.getY());
				break;
			case PLAY_ON:
				ballX = fieldPerc.getBall().getPosition().getX();
				ballY = fieldPerc.getBall().getPosition().getY();
				if (isPointsAreClose(selfPerc.getPosition(), ballPos, 2)) {
					// kick
					kickToPoint(new Vector2D(0, 0), 100); // TODO CHANGE TO KICK
															// TO ANOTHER PLAYER
															// THE SAME TEAM
				} else if (area.contains(ballX, ballY)) {
					// defend
					dash(ballPos);

				} else if (!isPointsAreClose(selfPerc.getPosition(), initPos, 3)) {
					// retreat
					dash(initPos);
				} else {
					// look to the ball
					turnToPoint(ballPos);
				}
				break;

			default:
				break;
			}
		}
	}

	private void defenderAction(long nextInteration, int post) {

		Vector2D vTemp = null;
		PlayerPerception pTemp;

		while (true) {
			updatePerceptions();

			System.out.println("ESTADO DA PARTIDA - " + matchPerc.getState());

			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(this.initPos.getX(), this.initPos.getY());
				break;
			case PLAY_ON:

				break;
			case FREE_KICK_LEFT:

				break;
			case FREE_KICK_RIGHT:

				break;
			case GOAL_KICK_LEFT:

				break;
			case GOAL_KICK_RIGHT:

				break;
			case AFTER_GOAL_LEFT:

				break;
			case AFTER_GOAL_RIGHT:

				break;
			case DROP_BALL:

				break;
			case OFFSIDE_LEFT:

				break;
			case OFFSIDE_RIGHT:

				break;
			case MAX:

				break;
			case FREE_KICK_FAULT_LEFT:

				break;
			case FREE_KICK_FAULT_RIGHT:

				break;
			case KICK_IN_LEFT:

				break;
			case KICK_IN_RIGHT:

				break;
			case KICK_OFF_LEFT:

				break;
			case KICK_OFF_RIGHT:

				break;
			case INDIRECT_FREE_KICK_LEFT:

				break;
			case INDIRECT_FREE_KICK_RIGHT:

				break;
			case CORNER_KICK_LEFT:

				break;
			case CORNER_KICK_RIGHT:

				break;

			/* Todos os estados da partida */
			default:
				break;
			}
		}
	}

	private void defenderState() {

	}

	private void fullBackAction(long nextInteration, int post) {

		Vector2D vTemp = null;
		PlayerPerception pTemp;

		while (true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(this.initPos.getX(), this.initPos.getY());
				break;
			case PLAY_ON:

				if (isPointsAreClose(selfPerc.getPosition(), ballPos, 1)) {
					// pass
					if (selfPerc.getUniformNumber() == 2) {
						if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 3).getPosition(),
								2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 3).getPosition();
						} else if (isPointsAreClose(selfPerc.getPosition(),
								fieldPerc.getTeamPlayer(side, 4).getPosition(), 2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 4).getPosition();
						}
					} else if (selfPerc.getUniformNumber() == 3) {
						if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 2).getPosition(),
								2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 2).getPosition();
						} else if (isPointsAreClose(selfPerc.getPosition(),
								fieldPerc.getTeamPlayer(side, 4).getPosition(), 2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 4).getPosition();
						}
					}
					Vector2D vTempF = vTemp.sub(selfPerc.getPosition());
					double intensity = (vTempF.magnitude() * 100) / 40;
					kickToPoint(vTemp, intensity);
				} else {

					PlayerPerception pTempDistanceClosed = getClosesPlayerPoint(ballPos, side, 1);
					PlayerPerception pTempDistanceMedium = getClosesPlayerPoint(ballPos, side, 1.5);
					PlayerPerception pTempDistanceLarge = getClosesPlayerPoint(ballPos, side, 2);

					if (pTempDistanceClosed != null
							&& pTempDistanceClosed.getUniformNumber() == selfPerc.getUniformNumber()) {
						// get the ball
						dash(ballPos);
					} else if (pTempDistanceMedium.getUniformNumber() == selfPerc.getUniformNumber()) {
						if (isPointsAreClose(pTempDistanceClosed.getPosition(),
								fieldPerc.getTeamPlayer(side, pTempDistanceClosed.getUniformNumber()).getPosition(),
								2)) {
							dash(goalPos);

						} else {
							dash(pTempDistanceClosed.getPosition());
						}
					} else if (pTempDistanceLarge != null
							&& pTempDistanceLarge.getUniformNumber() == selfPerc.getUniformNumber()) {

						dash(initPos);
					}
				}

				/*
				 * else { pTemp = getClosesPlayerPoint(ballPos, side, 1);
				 * 
				 * if (pTemp != null && pTemp.getUniformNumber() == selfPerc
				 * .getUniformNumber()) { // pega a bola dash(ballPos); } else if
				 * (!isPointsAreClose(selfPerc.getPosition(), initPos, 4)) { // recua
				 * dash(initPos); } else { // olha para a bola turnToPoint(ballPos); } }
				 */
				break;
			/* Todos os estados da partida */
			default:
				break;
			}
		}
	}

	private void midFieldAction(long nextIteration) {

		EFieldSide side = selfPerc.getSide();

		Vector2D goalPos = new Vector2D(50 * side.value(), 0);
		Vector2D ballPos;
		PlayerPerception pTemp;
		Vector2D vTemp = null;
		;
		while (true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(this.initPos.getX(), this.initPos.getY());
				break;
			case PLAY_ON:

				if (isPointsAreClose(selfPerc.getPosition(), ballPos, 1)) {
					if (isPointsAreClose(ballPos, goalPos, 30)) {
						// chuta para o gol
						kickToPoint(goalPos, 100); // goalPosition + itensity of
													// kick
					} else {
						// conduz para o gol
						kickToPoint(goalPos, 25);
					}
				} else if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 2).getPosition(), 2)
						|| isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 3).getPosition(),
								2)) {

					if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 2).getPosition(), 2)) {
						vTemp = fieldPerc.getTeamPlayer(side, 2).getPosition();
					} else if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 3).getPosition(),
							2)) {
						vTemp = fieldPerc.getTeamPlayer(side, 3).getPosition();
					}

					Vector2D vTempF = vTemp.sub(selfPerc.getPosition());
					double intensity = (vTempF.magnitude() * 100) / 40;
					kickToPoint(vTemp, intensity);
				} else {
					pTemp = getClosesPlayerPoint(ballPos, side, 3);

					if (pTemp != null && pTemp.getUniformNumber() == selfPerc.getUniformNumber()) {
						// get the ball
						dash(ballPos);
					} else if (pTemp != null && pTemp.getUniformNumber() != selfPerc.getUniformNumber()) {
						if (isPointsAreClose(pTemp.getPosition(), fieldPerc.getTeamPlayer(side, 1).getPosition(), 2)) {
							dash(pTemp.getPosition());
						} else {
							dash(goalPos);
						}
					} else if (!isPointsAreClose(selfPerc.getPosition(), initPos, 3)) {
						// retreat
						dash(initPos);
					} else {
						// olha para a bola
						turnToPoint(ballPos);
					}
				}
				break;
			/* Todos os estados da partida */
			default:
				break;
			}
		}
	}

	private void attackerAction(long nextIteration) {
		double xInit = -10.62, yInit = 0;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit * side.value(), yInit);
		Vector2D goalPos = new Vector2D(50 * side.value(), 0);
		Vector2D ballPos;
		PlayerPerception pTemp;
		Vector2D vTemp = null;
		;
		while (true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);
				break;
			case PLAY_ON:

				if (isPointsAreClose(selfPerc.getPosition(), ballPos, 1)) {
					if (isPointsAreClose(ballPos, goalPos, 30)) {
						// chuta para o gol
						kickToPoint(goalPos, 100); // goalPosition + itensity of
													// kick
					} else {
						// conduz para o gol
						kickToPoint(goalPos, 25);
					}
				} else if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 2).getPosition(), 2)
						|| isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 3).getPosition(),
								2)) {

					if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 2).getPosition(), 2)) {
						vTemp = fieldPerc.getTeamPlayer(side, 2).getPosition();
					} else if (isPointsAreClose(selfPerc.getPosition(), fieldPerc.getTeamPlayer(side, 3).getPosition(),
							2)) {
						vTemp = fieldPerc.getTeamPlayer(side, 3).getPosition();
					}

					Vector2D vTempF = vTemp.sub(selfPerc.getPosition());
					double intensity = (vTempF.magnitude() * 100) / 40;
					kickToPoint(vTemp, intensity);
				} else {
					pTemp = getClosesPlayerPoint(ballPos, side, 3);

					if (pTemp != null && pTemp.getUniformNumber() == selfPerc.getUniformNumber()) {
						// get the ball
						dash(ballPos);
					} else if (pTemp != null && pTemp.getUniformNumber() != selfPerc.getUniformNumber()) {
						if (isPointsAreClose(pTemp.getPosition(), fieldPerc.getTeamPlayer(side, 1).getPosition(), 2)) {
							dash(pTemp.getPosition());
						} else {
							dash(goalPos);
						}
					} else if (!isPointsAreClose(selfPerc.getPosition(), initPos, 3)) {
						// retreat
						dash(initPos);
					} else {
						// olha para a bola
						turnToPoint(ballPos);
					}
				}
				break;
			/* Todos os estados da partida */
			default:
				break;
			}
		}
	}

	private void dash(Vector2D point) {
		if (selfPerc.getPosition().distanceTo(point) <= 1)
			return;
		if (!isAlignToPoint(point, 15)) {
			turnToPoint(point);
		}
		commander.doDashBlocking(70);
	}

	private void turnToPoint(Vector2D point) {
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		commander.doTurnToDirectionBlocking(newDirection);
	}

	private void initializePosition(double xInit, double yInit) {
		this.initPos = new Vector2D(xInit * side.value(), yInit * side.value());
	}

	private void goToInitPosition() {
		turnToPoint(this.initPos);
		dash(this.initPos);
	}

	/*
	 * corre para roubar a bola
	 */
	private void thief() {
		turnToPoint(this.ballPos);
		dash(this.ballPos);
	}

	/**
	 * jogador vai para perto do gol esperar a bola para atacar de acordo com o
	 * espaco dado para o quao perto ele vai ficar perto do gol
	 **/
	private void waitBallAttack(int closeGoal) {

		if (!isPointsAreClose(selfPerc.getPosition(), this.goalPos, closeGoal)) {
			turnToPoint(this.goalPos);
			dash(this.goalPos);
		} else {
			turnToPoint(this.ballPos);
		}
	}

	private void attack() {
		if (!isPointsAreClose(selfPerc.getPosition(), this.goalPos, 1)) {
			kickToPoint(this.goalPos, KICK_RUN);
		} else {
			kickToPoint(this.goalPos, KICK_GOAL);
		}
	}

	/*
	 * se esta atacando passa pra quem esta mais perto do gol ou pra alguem perto
	 */
	private void passTheBall(boolean attack) {
		PlayerPerception target = null;

		if (attack) { // se o time esta atacando

			target = this.getClosesPlayerPoint(this.goalPos, this.side, 1);
		} else {

			target = this.getClosesPlayerPoint(this.selfPerc.getPosition(), this.side, 1);
		}
		if (target != null)
			if (target.getUniformNumber() != this.selfPerc.getUniformNumber())// se for diferente de si toque
				kickToPoint(target.getPosition(), KICK_PASS);

	}

	/*
	 * deixar o zagueiro perto do meio de campo pra dar suport ao time
	 */
	private void supportTeam() {
		Vector2D middle = new Vector2D(0, 0);
		if (this.selfPerc.getPosition().distanceTo(middle) >= 1) {
			turnToPoint(this.ballPos);
			dash(middle);
		} else {
			turnToPoint(this.ballPos);
		}

	}

	/*
	 * o jogador vai pra area do gol defender ou se tiver perto da bola ele vai
	 * tentar roubar a bola
	 */
	private void defend() {
		Vector2D myGoal = new Vector2D(this.goalPos.getX() * -1, this.goalPos.getY());
		PlayerPerception target = this.getClosesPlayerPoint(this.ballPos, this.side, 1);

		if (target.getUniformNumber() != this.selfPerc.getUniformNumber()) {
			if (this.selfPerc.getPosition().distanceTo(myGoal) >= 1) {
				dash(myGoal);
			}
		} else {
			this.thief();
		}
	}

	/***
	 * faz um cruzamento para fazer o ataque
	 */
	private void cross() {
		Vector2D crossPlace = new Vector2D(44.83 * side.value(), -28.54);

		if (!(this.selfPerc.getPosition().distanceTo(crossPlace) <= 1)) {
			turnToPoint(crossPlace);
			kickToPoint(crossPlace, 25);
		} else {
			PlayerPerception target = this.getClosesPlayerPoint(this.goalPos, this.side, 1);
			if (target != null) {
				if (target.getUniformNumber() == this.selfPerc.getUniformNumber()) {
					target = this.getClosesPlayerPoint(this.selfPerc.getPosition(), this.side, 1);
				}
				kickToPoint(target.getPosition(), KICK_PASS);
			}

		}

	}

}
