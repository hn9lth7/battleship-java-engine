import java.util.*;

public class Battleship {
    static class Player {
        String name;
        char[] original;
        char[] current;
        int score = 0;
        boolean allSunk = false;

        Player(String name, String fleet) {
            this.name = name;
            this.original = fleet.toCharArray();
            this.current = fleet.toCharArray();
        }

        boolean checkAllSunk() {
            for (char c : current) {
                if (c != '.' && c != '*') return false;
            }
            return true;
        }

        int getShipSize(char type) {
            if (type == '.') return 0;
            int size = 0;
            for (char c : original) {
                if (c == type) size++;
            }
            return size;
        }

        int receiveShot(int idx) {
            char cell = current[idx];
            if (cell == '.') return 0;
            if (cell == '*') return 2;

            char type = cell;
            for (int i = 0; i < current.length; i++) {
                if (original[i] == type) current[i] = '*';
            }
            if (checkAllSunk()) allSunk = true;
            return 1;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Player p1 = new Player(sc.nextLine(), sc.nextLine());
        Player p2 = new Player(sc.nextLine(), sc.nextLine());
        int gridSize = p1.original.length;

        Player currentPlayer = p1;
        Player opponent = p2;
        boolean gameOver = false;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            if (line.equals("quit")) {
                if (!gameOver) {
                    System.out.println("The game was not over yet");
                } else {
                    Player winner;
                    if (p1.score > p2.score) {
                        winner = p1;
                    } else if (p2.score > p1.score) {
                        winner = p2;
                    } else {
                        winner = currentPlayer;
                    }
                    System.out.println(winner.name + " won the game!");
                }
                break;
            }

            if (!line.equals("player") && !line.startsWith("score ") &&
            !line.startsWith("fleet ") && !line.startsWith("shoot ")) {
                System.out.println("Invalid command");
                continue;
            }

            if (line.equals("player")) {
                if (gameOver) {
                    System.out.println("The game is over");
                } else {
                    System.out.println("Next player: " + currentPlayer.name);
                }
                continue;
            }

            if (line.startsWith("score ")) {
                String name = line.substring(6);
                if (name.equals(p1.name)) {
                    System.out.println(p1.name + " has " + p1.score + " points");
                } else if (name.equals(p2.name)) {
                    System.out.println(p2.name + " has " + p2.score + " points");
                } else {
                    System.out.println("Nonexistent player");
                }
                continue;
            }

            if (line.startsWith("fleet ")) {
                String name = line.substring(6);
                if (name.equals(p1.name)) {
                    System.out.println(new String(p1.current));
                } else if (name.equals(p2.name)) {
                    System.out.println(new String(p2.current));
                } else {
                    System.out.println("Nonexistent player");
                }
                continue;
            }

            if (line.startsWith("shoot ")) {
                if (gameOver) {
                    System.out.println("The game is over");
                    continue;
                }

                int pos;
                try {
                    pos = Integer.parseInt(line.substring(6));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid shot");
                    continue;
                }

                if (pos < 1 || pos > gridSize) {
                    System.out.println("Invalid shot");
                    continue;
                }

                int idx = pos - 1;
                char shipType = opponent.original[idx];
                int size = opponent.getShipSize(shipType);
                int result = opponent.receiveShot(idx);

                if (result == 1) {
                    currentPlayer.score += size * 100;
                } else if (result == 2) {
                    currentPlayer.score -= size * 30;
                }

                if (opponent.allSunk) {
                    gameOver = true;
                } else {
                    Player temp = currentPlayer;
                    currentPlayer = opponent;
                    opponent = temp;
                }
                continue;
            }
        }
        sc.close();
    }
}