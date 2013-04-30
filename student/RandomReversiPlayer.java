package student;

import client.PlayerProtocol;
import java.util.List;
import java.util.Random;
import reversi.*;

/**
 * Klasa studenta koja sadrzi algoritme za igranje igre Reversi.
 * Nasledjuje od nadklase polje board koje predstavlja tablu igre.
 * Student treba da implementira metode init(), 
 * getMove() i proizvoljno metod opponentsMove(Field field) 
 * kojom igra prosledjuje igracu 
 * polje na kome je protivnik odigrao svoj prethodni potez.
 * Ime klase treba da bude broj indeksa studenta u formatu "ipggbbbbf" 
 * gde su "ip" inicijali studenta, "gg" godina upisa, "bbbb" broj indeksa
 * i "f" da naznaci da je implementacija igre Freedom.
 * 
 * @author Student
 */
public class RandomReversiPlayer extends ReversiPlayer {
	private final Random _random = new Random();
	private Player _player;
	private Board _board;

  /**
     * Postavlja igru u pocetno stanje. Poziva se od strane servera svaki put 
     * kada se zapocinje nova partija.
     * Unutar ove metode bi trebalo da se svi korisceni objekti postave u 
     * stanje u kome bi trebali da se nalaze pre pocetka partije.
     */
	@Override
	public void init(Player player) {
		_player = player;
		_board = new Board();
	}

  /**
     * Bira koordinate polja za naredni potez (klasa Position(int x, int y)). 
     * Indeksi polja su u opsegu od 0 do 7.
     * Igranje na nepraznom polju ili polju sa koordinatama van opsega table
     * racuna se kao mana u algoritmu i tada igrac gubi partiju.
     * @return Position sa koordinatama.
     */
	@Override
	public Position getMove() {
		List<Position> moves = _board.legalMoves(_player);
		Position move = moves.get(_random.nextInt(moves.size()));
		_board.makeMove(_player, move);

		return move;
	}

  /**
     * Metoda kojom server prosledjuje igracu 
     * polje na kome je protivnik odigrao svoj prethodi potez.
     * @param position Position na kome je protivnik odigrao svoj prethodi potez.
     */
	@Override
	public void opponentsMove(Position position) {
		_board.makeMove(_player.opponent(), position);
	}

	public static void main(String[] args) {
		PlayerProtocol player = new ReversiPlayerProtocol(new RandomReversiPlayer());
		player.gameStart();
	}
}
