package examples.ball_follower_team;

import java.awt.Rectangle;
import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;

public class BallFollowerPlayer extends Thread {

	private int LOOP_INTERVAL = 100; // 0.1s

	private PlayerCommander commander;

	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;

	public BallFollowerPlayer(PlayerCommander player) {
		commander = player;
	}

	@Override
	public void run() {

		System.out.println(">> 1. waiting initial perceptions...");

		if (this.selfPerc == null)
			this.selfPerc = commander.perceiveSelfBlocking();
		if (this.fieldPerc == null)
			this.fieldPerc = commander.perceiveFieldBlocking();
		if (this.matchPerc == null)
			this.matchPerc = commander.perceiveMatchBlocking();

		while (commander.isActive()) {
			System.out.println(">> Runing...");
			long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
			updatePerceptions();
			switch (this.selfPerc.getUniformNumber()) {
			case 1:
				goolKeeperAction(nextIteration);
				break;
			case 2:
				shipownerAction(nextIteration, -1); // cima
				break;
			case 3:
				shipownerAction(nextIteration, 1); // baixo
				break;
			case 4:
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
	 * System.out.println(">> 3. Now starting..."); while (commander.isActive())
	 * {
	 * 
	 * if (isAlignedToBall()) { if (closeToBall()) { commander.doKick(50.0d,
	 * 0.0d); } else { runToBall(); } } else { turnToBall(); }
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
		// System.out.println("Vetores: " + ballPos.sub(myPos) + "  " +
		// selfPerc.getDirection());
		// System.out.println(" => Angulo agente-bola: " + angle);

		return angle < 15.0d && angle > -15.0d;
	}

	double angleToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();

		return selfPerc.getDirection().angleFrom(ballPos.sub(myPos));
	}

	private void updatePerceptions() {
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

	}

	private void turnToBall() {
		System.out.println("TURN");
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		System.out.println(" => Angulo agente-bola: " + angleToBall()
				+ " (desalinhado)");
		System.out.println(" => Posicoes: ball = " + ballPos + ", player = "
				+ myPos);

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
		double angle = point.sub(selfPerc.getPosition()).angleFrom(
				selfPerc.getDirection());
		return angle < margin && angle > margin * (-1);
	}

	private boolean isPointsAreClose(Vector2D reference, Vector2D point,
			double margin) {
		return reference.distanceTo(point) <= margin;
	}

	// Retorna a percepção do jogador de um time (side) mais próximo do
	// ponto.
	private PlayerPerception getClosesPlayerPoint(Vector2D point,
			EFieldSide side, double margin) {
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
		double xInit = -48, yInit = 0, // init position of the goolkeeper
		ballX = 0, ballY = 0;

		EFieldSide side = selfPerc.getSide();

		Vector2D initPos = new Vector2D(xInit * side.value(), yInit
				* side.value());

		Vector2D ballPos;
		Rectangle area = side == EFieldSide.LEFT ? new Rectangle(-52, -20, 16,
				40) : new Rectangle(36, -20, 16, 40);

		while (true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {

			case BEFORE_KICK_OFF:
				// posiciona
				commander.doMoveBlocking(xInit, yInit);
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

	private void shipownerAction(long nextInteration, int post) {
		double xInit = -30, yInit = 10 * post;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit * side.value(), yInit
				* side.value());
		Vector2D goalPos = new Vector2D(50 * side.value(), 0);
		Vector2D ballPos, vTemp = null;
		PlayerPerception pTemp;
		while (true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);
				break;
			case PLAY_ON:

				if (isPointsAreClose(selfPerc.getPosition(), ballPos, 1)) {
					// pass
					if (selfPerc.getUniformNumber() == 2) {
						if (isPointsAreClose(selfPerc.getPosition(), fieldPerc
								.getTeamPlayer(side, 3).getPosition(), 2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 3)
									.getPosition();
						} else if (isPointsAreClose(selfPerc.getPosition(),
								fieldPerc.getTeamPlayer(side, 4).getPosition(),
								2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 4)
									.getPosition();
						}
					} else if (selfPerc.getUniformNumber() == 3) {
						if (isPointsAreClose(selfPerc.getPosition(), fieldPerc
								.getTeamPlayer(side, 2).getPosition(), 2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 2)
									.getPosition();
						} else if (isPointsAreClose(selfPerc.getPosition(),
								fieldPerc.getTeamPlayer(side, 4).getPosition(),
								2)) {
							vTemp = fieldPerc.getTeamPlayer(side, 4)
									.getPosition();
						}
					}
					Vector2D vTempF = vTemp.sub(selfPerc.getPosition());
					double intensity = (vTempF.magnitude() * 100) / 40;
					kickToPoint(vTemp, intensity);
				} else {

					PlayerPerception pTempDistanceClosed = getClosesPlayerPoint(
							ballPos, side, 1);
					PlayerPerception pTempDistanceMedium = getClosesPlayerPoint(
							ballPos, side, 1.5);
					PlayerPerception pTempDistanceLarge = getClosesPlayerPoint(
							ballPos, side, 2);

					if (pTempDistanceClosed != null
							&& pTempDistanceClosed.getUniformNumber() == selfPerc
									.getUniformNumber()) {
						// get the ball
						dash(ballPos);
					} else if (pTempDistanceMedium.getUniformNumber() == selfPerc
							.getUniformNumber()) {
						if (isPointsAreClose(
								pTempDistanceClosed.getPosition(),
								fieldPerc.getTeamPlayer(side,
										pTempDistanceClosed.getUniformNumber())
										.getPosition(), 2)) {
							dash(goalPos);

						} else {
							dash(pTempDistanceClosed.getPosition());
						}
					} else if (pTempDistanceLarge != null
							&& pTempDistanceLarge.getUniformNumber() == selfPerc
									.getUniformNumber()) {

						dash(initPos);
					}
				}

				/*
				 * else { pTemp = getClosesPlayerPoint(ballPos, side, 1);
				 * 
				 * if (pTemp != null && pTemp.getUniformNumber() == selfPerc
				 * .getUniformNumber()) { // pega a bola dash(ballPos); } else
				 * if (!isPointsAreClose(selfPerc.getPosition(), initPos, 4)) {
				 * // recua dash(initPos); } else { // olha para a bola
				 * turnToPoint(ballPos); } }
				 */
				break;
			/* Todos os estados da partida */
			default:
				break;
			}
		}
	}

	private void attackerAction(long nextIteration) {
		double xInit = -15, yInit = 0;
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
				} else if (isPointsAreClose(selfPerc.getPosition(), fieldPerc
						.getTeamPlayer(side, 2).getPosition(), 2)
						|| isPointsAreClose(selfPerc.getPosition(), fieldPerc
								.getTeamPlayer(side, 3).getPosition(), 2)) {

					if (isPointsAreClose(selfPerc.getPosition(), fieldPerc
							.getTeamPlayer(side, 2).getPosition(), 2)) {
						vTemp = fieldPerc.getTeamPlayer(side, 2).getPosition();
					} else if (isPointsAreClose(selfPerc.getPosition(),
							fieldPerc.getTeamPlayer(side, 3).getPosition(), 2)) {
						vTemp = fieldPerc.getTeamPlayer(side, 3).getPosition();
					}

					Vector2D vTempF = vTemp.sub(selfPerc.getPosition());
					double intensity = (vTempF.magnitude() * 100) / 40;
					kickToPoint(vTemp, intensity);
				} else {
					pTemp = getClosesPlayerPoint(ballPos, side, 3);

					if (pTemp != null
							&& pTemp.getUniformNumber() == selfPerc
									.getUniformNumber()) {
						// get the ball
						dash(ballPos);
					} else if (pTemp != null
							&& pTemp.getUniformNumber() != selfPerc
									.getUniformNumber()) {
						if (isPointsAreClose(pTemp.getPosition(), fieldPerc
								.getTeamPlayer(side, 1).getPosition(), 2)) {
							dash(pTemp.getPosition());
						} else {
							dash(goalPos);
						}
					} else if (!isPointsAreClose(selfPerc.getPosition(),
							initPos, 3)) {
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

}
