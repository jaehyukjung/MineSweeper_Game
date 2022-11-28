import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;
import javax.swing.*;


public class Main extends JPanel implements ActionListener{
    private static JFrame frame;
    private static Main main;

    // 지뢰판
    // private static int width = 30;
    // private static int height = 16;
    private static int width = 9;
    private static int height = 9;

    // 게임창
    private static int windowWidth = 750;
    private static int windowHeight = 400;
    private static int frameWidth = 200;
    private static int frameHeight = 200;

    // board: 숫자 및 지뢰가 설치되는 보드 (default value : 0)
    // 0~8: 해당 칸 주변의 지뢰 개수, 9: 지뢰
    private static int[][] board = new int[height][width];

    // onOffBoard: 숫자를 심어놓은 보드를 열고 가리는 가림판
    // 0: , 1: 아직 열지 않은 지뢰판, 2: 깃발, //TODO: 3: 물음표
    private static int[][] onOffBoard = new int[height][width];
    private static int[] template = {-1, 0, 1};
    private Timer timer;
    private static Timer secondsTimer;
    private static int startX = 5;
    private static int startY = 100;

    // mousePressed 이벤트 (pressed) 호출 시: true,
    private static boolean firstClick = true;
    private static boolean gameOver = false;
    private static Color buttonColorChange = Color.LIGHT_GRAY;
    private static String buttonFace = ":)";

    // private static int numBombsLeft = 99;
    private static int numBombsLeft = 10;
    private static int secondsCounter = 0;

    static JButton[][] buttonsBoard = new JButton[height][width];

    public Main() {

        //
        for (int i = 0; i < height; i++) {
            Arrays.fill(onOffBoard[i], 1);
        }
        timer = new Timer(1000 / 30, this);
        timer.start();
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                secondsCounter++;
            }
        };
        secondsTimer = new Timer(1000, taskPerformer);
    }

    public static void makeBoard(int y, int x) {  //첫 클릭시 맵 만들기 (맵 만드는 함수)
        for (int i = 0; i < height; i++) {
            Arrays.fill(board[i], 0);
        }
        int Boom = 10; // number of mines (지뢰 개수)
        Random rand = new Random();

        while (Boom > 0) { // 지뢰 생성
            int rand1 = rand.nextInt(9);
            int rand2 = rand.nextInt(9);

            if (board[rand1][rand2] == 9) { // 이미 해당 칸이 지뢰인 경우
                continue;
            }
            if (rand1 == y && rand2 == x) { // 클릭한 곳인 경우 (처음 선택한 곧은 지뢰 x)
                continue;
            }

            board[rand1][rand2] = 9; // 지뢰 있는 곳을 9로 설정
            Boom = Boom - 1;
        }
        //나머지 좌표를 설정
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] != 9) { //지뢰가 없는 곳이라면
                    board[i][j] = count_boom(i, j); //해당 함수를 통해 값을 설정
                }
            }
        }
    }

    public static int count_boom(int row, int col) { //맵 생성시 주변을 확인해 맵 값 선택
        int sum = 0;
        int nx = 0;
        int ny = 0;
        for (int i = 0; i < template.length; i++) {
            nx = (row + template[i]);
            if (nx < 0 || nx >= board.length) {
            } else {
                for (int j = 0; j < template.length; j++) {
                    ny = col + template[j];
                    if (ny < 0 || ny >= board[0].length || (nx == row && ny == col)) {
                    } else if (board[nx][ny] == 9) {
                        sum += 1;
                    }
                }
            }
        }
        return sum;
    }
    public static void main(String args[]) {
        frame = new JFrame("Minesweeper");
        frame.setLayout(new BorderLayout());

        Main game = new Main();
        frame.add(game, BorderLayout.CENTER);

        frame.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // 마우스 클릭 시 좌표 검사
                if (!gameOver) {
                    // 지뢰판 내 클릭 검사
                    if (e.getX() >= startX + 10 && e.getX() <= (startX + 10) + frameWidth) {
                        if (e.getY() >= startY + 35 && e.getY() <= startY + 35 + frameHeight) {
                            // 어느 칸 클릭 했는지 확인 하는 수식
                            int x = (e.getX() - startX - 11) / 23;
                            int y = (e.getY() - startY - 36) / 23;
                            // 오른쪽 클릭일 때
                            if (e.getButton() == MouseEvent.BUTTON3) {
                                // 깃발이면 깃발 풀고 가림판으로, 지뢰도 +1
                                if (onOffBoard[y][x] == 2) {
                                    onOffBoard[y][x] = 1;
                                    numBombsLeft += 1;
                                }
                                // 깃발이 아니면
                                else {
                                    // 깃발이 아니고 1일 때 (가림판일 때), 지뢰 -1하고 깃발
                                    if (onOffBoard[y][x] != 0) {
                                        onOffBoard[y][x] = 2;
                                        numBombsLeft -= 1;
                                    }
                                }
                            }
                            // 왼쪽 클릭일 때
                            else {
                                if (onOffBoard[y][x] != 2) {
                                    // 첫 클릭
                                    if (firstClick) {
                                        secondsTimer.start(); // 타이머 시작
                                        board[y][x] = 0; // 첫 클릭에 게임오버 되지 않게 0으로 설정
                                        while (true) {
                                            makeBoard(y, x);
                                            if (board[y][x] == 0) {
                                                break;
                                            }
                                        }
                                        firstClick = false;
                                    }
                                    // 지뢰가 아니면
                                    if (board[y][x] != 9) {
                                        expand(y, x); // 클릭 주변으로 확장
                                    }
                                    // 지뢰인 경우
                                    else {
                                        secondsTimer.stop(); // 타이머 중지
                                        buttonFace = "X(";
                                        gameOver(); // 게임오버 선언
                                    }
                                }
                            }
                        }
                    }
                }
                hasWon();
            }
        });

        frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // 초기화 버튼
                if (e.getX() >= 350 && e.getX() <= 400) {
                    if (e.getY() >= 70 && e.getY() <= 120) {
                        buttonColorChange = Color.GRAY;
                        gameOver = false;
                        firstClick = true;
                        numBombsLeft = 10;
                        secondsCounter = 0;
                        secondsTimer.stop();
                        for (int i = 0; i < height; i++) {
                            Arrays.fill(onOffBoard[i], 1);
                        }
                    }
                }
                // 지뢰판 클릭하면
                if (!gameOver) {
                    if (e.getX() >= startX + 10 && e.getX() <= (startX + 10) + frameWidth) {
                        if (e.getY() >= startY + 35 && e.getY() <= startY + 35 + frameHeight) {
                            buttonFace = ":o";
                        }
                    }
                }
            }
            // 클릭 후
            public void mouseReleased(MouseEvent e) {
                if (!gameOver) {
                    buttonColorChange = Color.LIGHT_GRAY;
                    buttonFace = ":)";
                }
            }
        });
        frame.setVisible(true);
        frame.setSize(windowWidth, windowHeight);
        // 프로세스까지 종료
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // 게임 오버 선언
    public static void gameOver() {
        gameOver = true;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j] == 9) {
                    onOffBoard[i][j] = 0; // 지뢰 공개
                }
            }
        }
        secondsTimer.stop();
    }

    public static void hasWon() {
        int blankCounter = 0;
        int userFindMineCnt = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 몇 칸 열렸는지 검사
                if (onOffBoard[i][j] == 0) {
                    blankCounter++;
                }
                if (onOffBoard[i][j] == 2 && board[i][j] == 9) {
                    userFindMineCnt++;

                }
            }
        }

        // 다 열렸으면 우승
        if (blankCounter == 71 || userFindMineCnt == 10) {
            numBombsLeft = 0;
            frame.revalidate();
            buttonFace = "B)";
            gameOver();
        }
    }

    // 재귀호출하며 확장
    public static void expand(int cellY, int cellX) {
        try {
            // 보드판 숫자가 0이고 안 열려있으면 주변으로 확장
            if (board[cellY][cellX] == 0 && onOffBoard[cellY][cellX] == 1) {
                onOffBoard[cellY][cellX] = 0; // 열기
                expand(cellY + 1, cellX);
                expand(cellY - 1, cellX);
                expand(cellY, cellX + 1);
                expand(cellY, cellX - 1);
                expand(cellY + 1, cellX + 1);
                expand(cellY - 1, cellX - 1);
                expand(cellY - 1, cellX + 1);
                expand(cellY + 1, cellX - 1);
            }
            else {
                // 보드판 숫자가 0이 아니지만 안 열려있을 때
                if (onOffBoard[cellY][cellX] == 1) {
                    onOffBoard[cellY][cellX] = 0; // 열기만 하고 확장 X
                }
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {

        }
    }


    // 계속 호출됨 - actionPerformed 메소드에 의해 repaint()가 계속해서 호출되며 역시 paint 또한 호출되는 듯함
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(startX, startY, frameWidth, frameHeight);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 열리지 않은 지뢰판
                if (onOffBoard[i][j] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(startX + j * 23, startY + i * 23, 22, 22);
                }
                // 열린 지뢰판
                else if (onOffBoard[i][j] == 0) {
                    // 지뢰가 열렸을 경우
                    if (board[i][j] == 9) {
                        if (buttonFace != "B)") {
                            g.setColor(Color.RED);
                        }
                    }
                    // 지뢰가 아닌 지뢰판이 열렸을 경우
                    else {
                        g.setColor(Color.LIGHT_GRAY);
                    }

                    g.fillRect(startX + j * 23, startY + i * 23, 22, 22);
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Courier", Font.PLAIN, 14));
                    // 지뢰가 아니면 해당 숫자 적어두기
                    if (board[i][j] != 9) {
                        g.drawString(Integer.toString(board[i][j]), startX + 8 + j * 23,startY + 15 + i * 23);
                    }
                    // 지뢰면 B로 표시
                    else {
                        g.drawString("B", startX + 8 + j * 23, startY + 15 + i * 23);
                    }
                }
                // 깃발 그리기
                else {
                    g.setColor(Color.GREEN);
                    g.fillRect(startX + j * 23, startY + i * 23, 22, 22);
                }
                // 기타 GUI
                g.setColor(buttonColorChange);
                g.fillRect(350, 30, 50, 50);
                g.setColor(Color.BLACK);
                g.drawString(buttonFace, 365, 55);
                g.fillRect(150, 30, 70, 50);
                g.fillRect(530, 30, 70, 50);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Courier", Font.PLAIN, 24));
                g.drawString(Integer.toString(numBombsLeft), 175, 55);
                g.drawString(Integer.toString(secondsCounter), 555, 55);
            }
        }
    }

    // 계속 호출됨 - Component.java.repaint()
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}