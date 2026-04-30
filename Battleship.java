import java.io.*;
import java.util.*;

public class Battleship {
    static class Fleet {
        int rows, cols;
        char[][] orig;

        Fleet(int r, int c, List<String> lines) {
            rows = r;
            cols = c;
            orig = new char[r][c];
            for (int i = 0; i < r; i++) {
                orig[i] = lines.get(i).toCharArray();
            }
        }
    }

    static class Player {
         String name;
         Fleet fleet;
         char[][] grid;
         int score = 0;
         boolean eliminated = false;

         Player(String name, Fleet f) {
             this.name = name;
             this.fleet = f;
             grid = new char[f.rows][f.cols];
             for (int i = 0; i < f.rows; i++)
                 System.arraycopy(f.orig[i], 0, grid[i], 0, f.cols);
         }

         boolean allSunk() {
             for (char[] row : grid)
                 for (char c : row)
                     if (c >= 'A' && c <= 'Z') return false;
             return true;
         }

        private List<int[]> getShipCells(int r, int c, char shipLetter) {
            List<int[]> cells = new ArrayList<>();
            boolean[][] visited = new boolean[fleet.rows][fleet.cols];
            Queue<int[]> q = new LinkedList<>();
            q.add(new int[]{r, c});
            visited[r][c] = true;

            int[] dr = {-1, 1, 0, 0};
            int[] dc = {0, 0, -1, 1};

            while (!q.isEmpty()) {
                int[] pos = q.poll();
                int cr = pos[0], cc = pos[1];
                cells.add(new int[]{cr, cc});

                for (int i = 0; i < 4; i++) {
                    int nr = cr + dr[i];
                    int nc = cc + dc[i];
                    if (nr >= 0 && nr < fleet.rows && nc >= 0 && nc < fleet.cols &&
                            !visited[nr][nc] && fleet.orig[nr][nc] == shipLetter) {
                        visited[nr][nc] = true;
                        q.add(new int[]{nr, nc});
                    }
                }
            }
            return cells;
        }

        int shoot(int r, int c) {
            char cell = grid[r][c];
            if (cell == '.') return 0;
            if (cell == '*') return 2;

            char shipLetter = fleet.orig[r][c];
            List<int[]> shipCells = getShipCells(r, c, shipLetter);

            for (int[] pos : shipCells) {
                grid[pos[0]][pos[1]] = '*';
            }

            if (allSunk()) eliminated = true;
            return 1;
        }

         int getShipSize(int r, int c) {
             char type = fleet.orig[r][c];
             if (type == '.') return 0;

             return getShipCells(r, c, type).size();
         }

         void printGrid() {
             for (char[] row : grid) {
                 System.out.println(new String(row));
             }
         }

         static List<Fleet> readFleets(String file) throws Exception {
             List<Fleet> list = new ArrayList<>();
             BufferedReader br = new BufferedReader(new FileReader(file));

             String line;
             while ((line = br.readLine()) != null) {
                 if (line.trim().isEmpty()) continue;

                 String[] p = line.trim().split("\\s+");
                 int r = Integer.parseInt(p[0]);
                 int c = Integer.parseInt(p[1]);

                 List<String> rows = new ArrayList<>();
                 for (int i = 0; i < r; i++) {
                     String row = br.readLine();
                     if (row.length() > c) row = row.substring(0, c);
                     rows.add(row);
                 }

                 list.add(new Fleet(r, c, rows));
             }

             br.close();
             return list;
         }

         static Player find(List<Player> players, String name) {
             for (Player p : players)
                 if (p.name.equals(name)) return p;
             return null;
         }

         static int alive(List<Player> players) {
             int count = 0;
             for (Player p : players)
                 if (!p.eliminated) count++;
             return count;
         }

         static int nextIdx(List<Player> players, int i) {
             int n = players.size();
             for (int k = 1; k <= n; k++) {
                 int idx = (i + k) % n;
                 if (!players.get(idx).eliminated) return idx;
             }
             return i;
         }

         static String winner(List<Player> players) {
             int max = Integer.MIN_VALUE;

             for (Player p : players)
                 if (p.score > max) max = p.score;

             List<Player> top = new ArrayList<>();
             for (Player p : players)
                 if (p.score == max) top.add(p);

             if (top.size() == 1) return top.get(0).name;

             for (Player p : top)
                 if (!p.eliminated) return p.name;

             return top.get(0).name;
         }

         public static void main(String[] args) throws Exception {
             // 1. Спочатку введіть кількість гравців, потім для кожного:
             //    - ім'я (рядок, може містити пробіли)
             //    - номер флоту (ціле число, відповідає fleets.txt)
             // 2. Далі введіть команди:
             //    - player              ->  показати, чий хід
             //    - players             ->  список активних гравців
             //    - scores              ->  рейтинг за балами
             //    - score Ім'я          ->  бали гравця
             //    - fleet Ім'я          ->  показати сітку (* замість потоплених)
             //    - shoot r c Ім'я      ->  постріл (r, c - числа від 1)
             //    - quit                ->  завершити гру

             List<Fleet> fleets = readFleets("fleets.txt");
             Scanner sc = new Scanner(System.in);

             int n = Integer.parseInt(sc.nextLine().trim());
             List<Player> players = new ArrayList<>();

             for (int i = 0; i < n; i++) {
                 String name = sc.nextLine();
                 int id = Integer.parseInt(sc.nextLine().trim());
                 players.add(new Player(name, fleets.get(id - 1)));
             }

             int cur = 0;
             boolean over = false;

             while (sc.hasNextLine()) {
                 String cmd = sc.nextLine().trim();

                 if (cmd.equals("quit")) {
                     if (!over) System.out.println("The game was not over yet");
                     else System.out.println(winner(players) + " won the game!");
                     break;
                 }

                 if (!(cmd.equals("player") || cmd.equals("players") || cmd.equals("scores")
                                            || cmd.startsWith("score ") || cmd.startsWith("fleet ")
                                            || cmd.startsWith("shoot "))) {
                     System.out.println("Invalid command");
                     continue;
                 }

                 Player current = players.get(cur);

                 if (cmd.equals("player")) {
                     if (over) System.out.println("The game is over");
                     else System.out.println("Next player: " + current.name);
                 }

                 else if (cmd.startsWith("score ")) {
                     Player p = find(players, cmd.substring(6));
                     System.out.println(p == null ? "Nonexistent player"
                             : p.name + " has " + p.score + " points");
                 }

                 else if (cmd.startsWith("fleet ")) {
                     Player p = find(players, cmd.substring(6));
                     if (p == null) System.out.println("Nonexistent player");
                     else p.printGrid();
                 }

                 else if (cmd.equals("scores")) {
                     players.stream()
                             .sorted((a, b) -> a.score == b.score ?
                                     a.name.compareTo(b.name) : b.score - a.score)
                             .forEach(p -> System.out.println(p.name + " has " + p.score + " points"));
                 }

                 else if (cmd.equals("players")) {
                     for (Player p : players)
                         if (!p.eliminated) System.out.println(p.name);
                 }

                 else if (cmd.startsWith("shoot ")) {
                     if (over) {
                         System.out.println("The game is over");
                         continue;
                     }

                     String[] p = cmd.split("\\s+", 4);

                     if (p.length < 4) {
                         System.out.println("Invalid command");
                         continue;
                     }

                     int r = Integer.parseInt(p[1]);
                     int c = Integer.parseInt(p[2]);
                     String name = p[3];

                     if (name.equals(current.name)) {
                         System.out.println("Self-inflicted shoot");
                         continue;
                     }

                     Player target = find(players, name);
                     if (target == null) {
                         System.out.println("Nonexistent player");
                         continue;
                     }
                     if (target.eliminated) {
                         System.out.println("Eliminated player");
                         continue;
                     }

                     if (r < 1 || r > target.fleet.rows || c < 1 || c > target.fleet.cols) {
                         System.out.println("Invalid shoot");
                         continue;
                     }

                     int size = target.getShipSize(r - 1, c - 1);
                     int res = target.shoot(r - 1, c - 1);

                     if (res == 1) current.score += size * 100;
                     if (res == 2) current.score -= size * 30;

                     if (alive(players) == 1) over = true;
                     else cur = nextIdx(players, cur);
                 }
             }
             sc.close();
         }
    }
}