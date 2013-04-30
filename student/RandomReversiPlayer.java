package student;

import client.PlayerProtocol;
import java.util.*;
import reversi.*;

public class RandomReversiPlayer extends ReversiPlayer {
	private final Random _random = new Random();
	private Player _player;
	private Board _board;

	@Override
	public void init(Player player) {
		_player = player;
		_board = new Board();
	}

	@Override
	public Position getMove() {
		List<Position> moves = _board.legalMoves(_player);
		Position move = moves.get(_random.nextInt(moves.size()));
		_board.makeMove(_player, move);

		return move;
	}

	@Override
	public void opponentsMove(Position position) {
		_board.makeMove(_player.opponent(), position);
	}

	public static void main(String[] args) {
		PlayerProtocol player = new ReversiPlayerProtocol(new RandomReversiPlayer());
		player.gameStart();
	}
}
