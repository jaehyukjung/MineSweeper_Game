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


public class Main extends JPanel implements ActionListener{
    // Game Admin Frame
    private static JFrame frame;

    // Board Size
    private static final int BOARD_WIDTH = 279;
    private static final int BOARD_HEIGHT = 279;

    // Mine Board Size
    private static final int MINE_BOARD_WIDTH = 9;
    private static final int MINE_BOARD_HEIGHT = 9;


    // mineBoard: 숫자 및 지뢰가 설치되는 보드 (default value : 0)
    // 0~8: 해당 칸 주변의 지뢰 개수, 9: 지뢰
    private static int[][] mineBoard = new int[9][9];

    // onOffBoard: 숫자를 심어놓은 보드를 열고 가리는 가림판
    // 0: , 1: 아직 열지 않은 지뢰판, 2: 깃발, TODO: 3: 물음표
    private static int[][] buttonBoard = new int[9][9];
    private static Timer timer;

    // mousePressed 이벤트 (pressed) 호출 시: true,
    private static boolean firstClick = true; //초기 클릭은 true 누르고 나면 false로 변경
    private static boolean gameOver = false;
    private static boolean winningFlag = false;

    private static int mine = 10; //지뢰 개수
    private static int timeCounter = 0;

    public Main() {
        for (int i = 0; i < MINE_BOARD_HEIGHT; i++) {
            Arrays.fill(buttonBoard[i], 1);
        }
        Timer init = new Timer(1000 / 30, this);
        init.start();
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                timeCounter++;
            }
        };
        timer = new Timer(1000, taskPerformer);
    }
    public static void main(String args[]) {
        frame = new JFrame("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창닫으면 코드 종료
        frame.setLayout(new GridLayout()); //버튼을 배치를 용이하게 하기 위해 GriedLayout 사용

        Main mineSweeper = new Main();
        frame.add(mineSweeper);

        frame.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // 마우스 클릭 시 좌표 검사
                if (!gameOver) {
                    // 지뢰판 내 클릭 검사
                    if (e.getX() >= 243 && e.getX() <= 243 + BOARD_WIDTH) {
                        if (e.getY() >= 131 && e.getY() <= 131 + BOARD_HEIGHT) {
                            // 어느 칸 클릭 했는지 확인 하는 수식
                            int x = (e.getX() - 246) / 31;
                            int y = (e.getY() - 136) / 31;
                            // 오른쪽 클릭일 때
                            if (e.getButton() == MouseEvent.BUTTON3) {
                                // 깃발이 아니고 1일 때 (가림판일 때), 지뢰 -1하고 깃발
                                if (buttonBoard[y][x] == 1) {
                                    buttonBoard[y][x] = 2;
                                    mine -= 1;
                                }
                                // 깃발이면 깃발 풀고 가림판으로, 지뢰도 +1
                                else if (buttonBoard[y][x] == 2) {
                                    buttonBoard[y][x] = 3;
                                    mine += 1;
                                } else if (buttonBoard[y][x] == 3) {
                                    buttonBoard[y][x] = 1;
                                }
                            }
                            // 왼쪽 클릭일 때
                            else {
                                if (buttonBoard[y][x] == 1) {
                                    // 첫 클릭
                                    if (firstClick) {
                                        timer.start(); // 타이머 시작
                                        mineBoard[y][x] = 0; // 첫 클릭에 게임오버 되지 않게 0으로 설정
                                        while (true) {
                                            makeBoard(y, x);
                                            if (mineBoard[y][x] == 0) {
                                                break;
                                            }
                                        }
                                        firstClick = false;
                                    }
                                    // 지뢰가 아니면
                                    if (mineBoard[y][x] != 9) {
                                        checkMap(y, x); // 클릭 주변으로 확장
                                    }
                                    // 지뢰인 경우
                                    else {
                                        gameOver(); // 게임오버 선언
                                    }
                                }
                            }
                        }
                    }
                }
                gameWin();
            }
        });

        frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // 초기화 버튼
                if (e.getX() >= 350 && e.getX() <= 400) {
                    if (e.getY() >= 70 && e.getY() <= 120) {
                        gameInit();
                    }
                }
            }
        });
        frame.setVisible(true);
        frame.setSize(750, 500);
    }

    public static void makeBoard(int y, int x) {  //첫 클릭시 맵 만들기 (맵 만드는 함수)
        for (int i = 0; i < MINE_BOARD_HEIGHT; i++) {
            Arrays.fill(mineBoard[i], 0);
        }
        int Boom = 10; // number of mines (지뢰 개수)
        Random rand = new Random();

        while (Boom > 0) { // 지뢰 생성
            int rand1 = rand.nextInt(9);
            int rand2 = rand.nextInt(9);

            if (mineBoard[rand1][rand2] == 9) { // 이미 해당 칸이 지뢰인 경우
                continue;
            }
            if (rand1 == y && rand2 == x) { // 클릭한 곳인 경우 (처음 선택한 곧은 지뢰 x)
                continue;
            }

            mineBoard[rand1][rand2] = 9; // 지뢰 있는 곳을 9로 설정
            Boom = Boom - 1;
        }
        //나머지 좌표를 설정
        for (int i = 0; i < mineBoard.length; i++) {
            for (int j = 0; j < mineBoard[0].length; j++) {
                if (mineBoard[i][j] != 9) { //지뢰가 없는 곳이라면
                    mineBoard[i][j] = count_boom(i, j); //해당 함수를 통해 값을 설정
                }
            }
        }
    }

    public static int count_boom(int row, int col) { //맵 생성시 주변을 확인해 맵 값 선택
        int sum = 0;
        int nx = 0;
        int ny = 0;
        for (int i = -1; i <= 1; i++) {  // 좌, 가운데, 우 판단
            for (int j = -1; j <= 1; j++){ // 아래, 가운데, 위 판단
                // 왼쪽 아래부터 순서대로 판단해서 지뢰가 있다면 더하기
                nx = (row + i);
                ny = (col + j);
                // 판단 범위가 게임 보드 밖이거나 row, col인경우 즉 주변이 아니라 자기 자신을 판단하는 경우 판단 x
                if (nx < 0 || nx >= mineBoard.length || ny < 0 || ny >= mineBoard[0].length || (nx == row && ny == col)){continue;}
                else if (mineBoard[nx][ny] == 9) { sum += 1;}
            }
        }
        return sum;
    }

    public static void gameInit() { //게임 초기화 하는 함수
        gameOver = false;
        firstClick = true;
        winningFlag = false;
        mine = 10;
        timeCounter = 0; // 게임 타이머 리셋 후
        timer.stop(); // 타이머 종료 -> 게임 시작하면 다시 타이머 재생
        for (int i = 0; i < MINE_BOARD_HEIGHT; i++) {
            Arrays.fill(buttonBoard[i], 1); // 모든 버튼을 초기 세팅으로 변경 (1이면 누를 수 있는 상태)
        }
    }
    public static void gameWin() { //게임 클리어시 보여주는 함수
        int openButtonCnt = 0;
        int findMineCnt = 0;
        for (int i = 0; i < MINE_BOARD_HEIGHT; i++) {
            for (int j = 0; j < MINE_BOARD_WIDTH; j++) {
                // 몇 칸 열렸는지 검사
                if (buttonBoard[i][j] == 0) {
                    openButtonCnt++;
                }
                // button ==2 는 지뢰 표시로 지뢰표시 이면서 그게 지뢰인 경우 find MineCnt 개수 늘리기
                if (buttonBoard[i][j] == 2 && mineBoard[i][j] == 9) {
                    findMineCnt++;
                }
            }
        }

        // 다 열렸으면 우승
        // 지뢰를 다 찾았거나 지뢰 빼고 누른 버튼이 전체 버튼 - 지뢰 개수 이면은 승리
        if (openButtonCnt == 71 || findMineCnt == 10) {
            mine = 0;
            winningFlag = true;
            frame.revalidate();
            gameOver();
        }
    }

    // 게임 오버 선언
    public static void gameOver() {
        gameOver = true;
        for (int i = 0; i < MINE_BOARD_HEIGHT; i++) {
            for (int j = 0; j < MINE_BOARD_WIDTH; j++) {
                if (mineBoard[i][j] == 9) {
                    buttonBoard[i][j] = 0; // 지뢰 공개
                }
            }
        }
        timer.stop();
    }

    // 재귀호출하며 확장
    public static void checkMap(int y, int x) {
        int[] template = {-1, 0, 1};
        try{
            // 보드판 숫자가 0이고 안 열려있으면 주변으로 확장
            if (mineBoard[y][x] == 0 && buttonBoard[y][x] == 1) {
                buttonBoard[y][x] = 0; // 열기
                for(int i = 0;i<template.length;i++){
                    for (int j =0;j<template.length;j++){
                        if (i == 1 && j == 1) continue;
                        else checkMap(y + template[i], x + template[j]);
                    }
                }
            }
            else{
                // 보드판 숫자가 0이 아니지만 안 열려있을 때
                if (buttonBoard[y][x] == 1) buttonBoard[y][x] = 0; // 열기만 하고 확장 X
            }
        }
        catch(java.lang.ArrayIndexOutOfBoundsException e){}
    }

    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(235, 100, BOARD_WIDTH, BOARD_HEIGHT);

        for (int i = 0; i < MINE_BOARD_HEIGHT; i++) {
            for (int j = 0; j < MINE_BOARD_WIDTH; j++) {
                // 열리지 않은 지뢰판
                if (buttonBoard[i][j] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(235 + j * 31, 100 + i * 31, 30, 30);
                }
                // 열린 지뢰판
                else if (buttonBoard[i][j] == 0) {
                    // 지뢰가 열렸을 경우
                    if (mineBoard[i][j] == 9) {
                        // 게임중 지뢰를 누른 경우
                        if (!winningFlag) {
                            g.setColor(Color.RED); // 빨간 배경으로 채우기
                            g.fillRect(235 + j * 31, 100 + i * 31, 30, 30);
                            g.setColor(Color.BLACK);
                            g.setFont(new Font("굴림", Font.BOLD, 18));
                            g.drawString(Integer.toString(mineBoard[i][j]), 245 + j * 31, 122 + i * 31);
                        }
                        // 게임에서 승리해서 지뢰를 공개 하는 경우
                        else {
                            g.setColor(Color.WHITE);
                            g.fillRect(235 + j * 31, 100 + i * 31, 30, 30);
                            g.setColor(Color.BLACK);
                            g.setFont(new Font("굴림", Font.BOLD, 18));
                            g.drawString(Integer.toString(mineBoard[i][j]), 245 + j * 31, 122 + i * 31);
                        }
                    }
                    // 지뢰가 아닌 지뢰판이 열렸을 경우
                    else {
                        g.setColor(Color.LIGHT_GRAY);
                        g.fillRect(235 + j * 31, 100 + i * 31, 30, 30);
                        g.setColor(Color.BLACK);
                        g.setFont(new Font("굴림", Font.BOLD, 18));
                        g.drawString(Integer.toString(mineBoard[i][j]), 245 + j * 31, 120 + i * 31);
                    }
                }
                // 깃발 그리기
                else if (buttonBoard[i][j] == 2) {
                    g.fillRect(235 + j * 31, 100 + i * 31, 30, 30);
                    ImageIcon flag = new ImageIcon("sprites/flag.png");
                    Image img = flag.getImage(); //아이콘을 이미지로 받아옴 -> 사이즈 조절을 위함
                    Image changeImg = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);// 아이콘 사이즈 조절
                    ImageIcon Flag = new ImageIcon(changeImg);// 다시 아이콘으로 변경!!
                    g.drawImage(changeImg, 235 + j * 31, 100 + i * 31,this);
                }
                else {
                    g.setColor(Color.BLACK);
                    g.fillRect(235 + j * 31, 100 + i * 31, 30, 30);
                    g.setFont(new Font("굴림", Font.BOLD, 18));
                    g.setColor(Color.WHITE);
                    g.drawString("?", 245 + j * 31, 120 + i * 31);
                }
                // 기타 GUI
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(350, 30, 50, 50);
                g.fillRect(235, 30, 70, 50);
                g.fillRect(445, 30, 70, 50);

                g.setColor(Color.BLACK);
                g.setFont(new Font("굴림", Font.BOLD, 24));
                g.drawString(Integer.toString(mine), 258, 60);
                g.drawString(":)", 365, 60);
                g.drawString(Integer.toString(timeCounter), 465, 60);
            }
        }
    }

    public void actionPerformed(ActionEvent e) { repaint();}
}