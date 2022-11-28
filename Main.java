import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TimerTask;

public class Main extends JPanel implements ActionListener {
    private static JFrame frame;
    private static Main main;
    private static int width = 30;
    private static int height = 16;
    private static int windowWidth = 750;
    private static int windowHeight = 600;
    private static int frameWidth = 690;
    private static int frameHeight = 368;
    private static int[][] board = new int[height][width]; // 게임 보드
    private static int[][] onOffBoard = new int[height][width]; // 게임 버튼이 눌렸는지 아닌지 판단하기 위한 맵
    private static int[] template = { -1, 0, 1 };
    private Timer timer;
    private static Timer secondsTimer;
    private static int startX = 20;
    private static int startY = 110;
    private static boolean firstClick = true;
    private static boolean gameOver = false;
    private static Color buttonColorChange = Color.LIGHT_GRAY;
    private static String buttonFace = ":)";
    private static int numBombsLeft = 99;
    private static int secondsCounter = 0;

    public Main() {
        for (int i = 0; i < height; i++) {
            Arrays.fill(onOffBoard[i], 1); // onoffBoard가 1이면 클릭이 가능한 상태
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

    public static void makeBoard(int y, int x) { // 첫 클릭시 맵 만들기 (맵 만드는 함수)
        for (int i = 0; i < height; i++) {
            Arrays.fill(board[i], 0);
        }
        int Boom = 99; // number of mines (지뢰 개수)
        Random rand = new Random();

        while (Boom-- > 0) { // 지뢰 생성
            if (Boom == 0) {
                break;
            }
            int rand1 = rand.nextInt(16);
            int rand2 = rand.nextInt(30);

            if (board[rand1][rand2] == 9) { // 이미 해당 칸이 지뢰인 경우
                Boom++;
                continue;
            }
            if (rand1 == y && rand2 == x) { // 클릭한 곳인 경우 (처음 선택한 곧은 지뢰 x)
                Boom++;
                continue;
            }

            board[rand1][rand2] = 9; // 지뢰 있는 곳을 9로 설정
        }
        // 나머지 좌표를 설정
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] != 9) { // 지뢰가 없는 곳이라면
                    board[i][j] = count_boom(i, j); // 해당 함수를 통해 값을 설정
                }
            }
        }
    }

    public static int count_boom(int row, int col) { // 맵 생성시 주변을 확인해 맵 값 선택
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
                    } else if (board[nx][ny] == 9)
                        sum += 1;
                }
            }
        }
        return sum;
    }

    public static void main(String args[]) {
        frame = new JFrame("5Team JJang");
        frame.setLayout(new BorderLayout());

        Main game = new Main();
        frame.add(game, BorderLayout.CENTER); // center에 온오프 보드 배열 배치?
        frame.addMouseListener(new MouseAdapter() { // 클릭이벤트가 발생했을 때
            public void mouseClicked(MouseEvent e) {
                if (!gameOver) { // 게임이 진행되고 있다면
                    if (e.getX() >= startX + 10 && e.getX() <= (startX + 10) + frameWidth) { // 버튼이 프레임의 센터 위치를 고려(온오프보드
                                                                                             // 쯤)
                        if (e.getY() >= startY + 35 && e.getY() <= startY + 35 + frameHeight) {
                            int x = (e.getX() - startX - 11) / 23; // 23으로 나누는 이유는,,??
                            int y = (e.getY() - startY - 36) / 23;
                            if (e.getButton() == MouseEvent.BUTTON3) {
                                if (onOffBoard[y][x] == 2) { // 녹색(지뢰로 표시돼 있는걸) 우클릭한다면
                                    onOffBoard[y][x] = 1; // 클릭가능한 일반으로 바꾸고
                                    numBombsLeft += 1; // 남아있는 지뢰수를 하나 늘리기
                                } else {
                                    if (onOffBoard[y][x] != 0) { // 1이라면
                                        onOffBoard[y][x] = 2; // 녹색으로(지뢰로)바꾸고
                                        numBombsLeft -= 1; // 찾아야하는 지뢰수 감소
                                    }
                                }
                            } else { // 우클릭이 아니고 좌클릭 이라면?
                                if (onOffBoard[y][x] != 2) { // 1이라면
                                    if (firstClick) { // 첫 클릭이라면
                                        secondsTimer.start(); // 타이머 시작하고
                                        board[y][x] = 0; // 첫클릭으로 누른건 0으로 설정하고
                                        while (true) { //
                                            makeBoard(y, x);
                                            if (board[y][x] == 0) {
                                                break;
                                            }
                                        }
                                        firstClick = false; // 첫 클릭 하고 나면 False로 변경
                                    }
                                    if (board[y][x] != 9) { // 클릭을 누르면 expand로 확장
                                        expand(y, x);
                                    } else {
                                        secondsTimer.stop();
                                        buttonFace = "X(";
                                        gameOver();
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
                if (e.getX() >= 350 && e.getX() <= 400) { // 종료버튼
                    if (e.getY() >= 70 && e.getY() <= 120) {
                        buttonColorChange = Color.GRAY;
                        gameOver = false;
                        firstClick = true;
                        secondsCounter = 0;
                        secondsTimer.start();
                        numBombsLeft = 99;
                        for (int i = 0; i < height; i++) {
                            Arrays.fill(onOffBoard[i], 1);
                        }
                    }
                }
                if (!gameOver) { // 게임 화면
                    if (e.getX() >= startX + 10 && e.getX() <= (startX + 10) + frameWidth) {
                        if (e.getY() >= startY + 35 && e.getY() <= startY + 35 + frameHeight) {
                            buttonFace = ":o";
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (!gameOver) {
                    buttonColorChange = Color.LIGHT_GRAY;
                    buttonFace = ":)";
                }

            }

        });
        frame.setVisible(true);
        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void gameOver() {
        gameOver = true;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j] == 9) {
                    onOffBoard[i][j] = 0;
                }
            }
        }
        numBombsLeft = 99;
    }

    public static void hasWon() { // 게임 성공 시
        int blankCounter = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (onOffBoard[i][j] == 0) {
                    blankCounter++;
                }
            }
        }
        if (blankCounter == 382) { //
            buttonFace = "B)";
            gameOver();
        }
    }

    public static void expand(int cellY, int cellX) { // 재귀 함수를 통해 구현
        try {
            if (board[cellY][cellX] == 0 && onOffBoard[cellY][cellX] == 1) {
                onOffBoard[cellY][cellX] = 0;
                for (int i = 0; i < template.length; i++) {
                    for (int j = 0; j < template.length; j++) {
                        if (i == 1 && j == 1) {
                        } else {
                            expand(cellY + template[i], cellX + template[j]);
                        }

                    }
                }
            } else {
                if (onOffBoard[cellY][cellX] == 1) { // expand가 멈추면 선택을 못하도록 선택
                    onOffBoard[cellY][cellX] = 0;
                }
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {

        }

    }

    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(startX, startY, frameWidth, frameHeight);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (onOffBoard[i][j] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(startX + j * 23, startY + i * 23, 22, 22);
                } else if (onOffBoard[i][j] == 0) {
                    if (board[i][j] == 9) {
                        if (buttonFace != "B)") {
                            g.setColor(Color.RED);
                        }
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    g.fillRect(startX + j * 23, startY + i * 23, 22, 22);
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Courier", Font.PLAIN, 14));
                    if (board[i][j] != 9)
                        g.drawString(Integer.toString(board[i][j]), startX + 8 + j * 23, startY + 15 + i * 23);
                    else
                        g.drawString("B", startX + 8 + j * 23, startY + 15 + i * 23);
                } else {
                    // g.setColor(Color.GREEN);
                    g.fillRect(startX + j * 23, startY + i * 23, 22, 22);
                    ImageIcon flag = new ImageIcon("images/flag.png");
                    Image img = flag.getImage();// 아이콘을 이미지로 받아옴 -> 사이즈 조절을 위함
                    Image changeImg = img.getScaledInstance(22, 22, Image.SCALE_SMOOTH);// 아이콘 사이즈 조절
                    ImageIcon Flag = new ImageIcon(changeImg);// 다시 아이콘으로 변경!!
                    g.drawImage(changeImg, startX + 8 + j * 23 - 8, startY + 15 + i * 23 - 14, this);

                }
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

    public void printBoard() { // for test
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println("\n");
        }
    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}