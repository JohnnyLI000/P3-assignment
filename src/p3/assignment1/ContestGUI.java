/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p3.assignment1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 *
 * @author Yuan Hao Li
 */
//THINGS TO DO: 
// 1. Save the player score ,output on the score panel
// 2. if the player got all correct , what will happen to the program.
// 3. Unit testing 
public class ContestGUI extends JFrame implements Runnable {

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = screenSize.width;
    int height = screenSize.height;
    public static Connection conn;
    public static String url = "jdbc:derby:Question&Answer;create=true";
    public static String username = "johnny";
    public static String password = "johnny";

    private String question = "", a = "", b = "", c = "", d = "", answer = "", hint = "", playerAnswer = "", audience = "Ask Someone Next To You";
    private JPanel informationPanel, questionPanel, userInputPanel, optionsPanel, prizePanel, displayPanel;
    private JButton aButton, bButton, cButton, dButton, hintButton, audienceButton, halfButton;
    private JLabel questionLabel, nextPrize, currentPrize, displayLabel, playerLabel;
    private ImageIcon logoIcon = new ImageIcon(new ImageIcon("Logo2.png").getImage().getScaledInstance(250, 250, Image.SCALE_DEFAULT));
    private JTextField inputName;
    private String playerName;
    public boolean check = true;
    PlayerInformationGUI playerGUI;
    Color gold = new Color(233, 139, 42);
    Color pink = new Color(254, 223, 225);
    Color gray = new Color(218, 201, 166);
    private int questionID = 0;
    private int prize = 0, prizeGoal = 100;
    private String[] playerArray; // top 3 players
    HashMap<String,Integer> records;
    public ContestGUI() {
        super();
        playerGUI = new PlayerInformationGUI(this);
    }

    public void startGame() // generate the Game frame and connect to the database
    {
        establishMySQLConnection();
        this.setSize(width / 3 * 2, height / 3 * 2);
        this.setLocation((width - this.getWidth()) / 2, (height - this.getHeight()) / 2);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridLayout(3, 1));
        informationPanel();
        this.add(informationPanel);
        getQA();
        questionPanel();
        userInputPanel();
        this.add(questionPanel);
        this.add(userInputPanel);
        this.setVisible(true);
    }

    public void run() {
        synchronized (this) {
            if (!playerGUI.getIsClosed()) {
                try {
                    System.out.println("contest start to wait");
                    this.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("finished waitting");
            this.startGame();
        }
    }

    public void establishMySQLConnection() {
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println(url + "  connected...");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public ResultSet getQA() {
        ResultSet rs = null;
        try {
            Statement statement = conn.createStatement();
            questionID++;
            String sqlQuery = "select * from QA where QUESTIONID = " + questionID;
            rs = statement.executeQuery(sqlQuery);
            rs.next();
            question = rs.getString("QUESTION");
            System.out.println(question);
            a = rs.getString(3);
            b = rs.getString(4);
            c = rs.getString(5);
            d = rs.getString(6);
            answer = rs.getString(7);
            hint = rs.getString(8);
            prizeCount(questionID);
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        return (rs);
    }

    public void informationPanel() {
        informationPanel = new JPanel();
        informationPanel.setBackground(Color.blue);
        informationPanel.setLayout(new GridLayout(1, 3));
        displayPanel();
        prizePanel();
        informationPanel.add(scorePanel());
        informationPanel.add(displayPanel);
        informationPanel.add(prizePanel);
    }

    public void userInputPanel() {
        userInputPanel = new JPanel();
        userInputPanel.setBackground(Color.green);
        userInputPanel.setLayout(new BorderLayout());
        optionsPanel();
        userInputPanel.add(optionsPanel);
        userInputPanel.add(helpPanel(), BorderLayout.EAST);
    }

    public void displayPanel() {// display panel shows : who wants to be millionare and hints message 
        displayPanel = new JPanel();
        displayLabel = new JLabel();
        displayLabel.setIcon(logoIcon);
        displayPanel.setLayout(new BorderLayout());
        displayPanel.add(displayLabel, BorderLayout.CENTER);
        displayPanel.setBackground(gray);
    }

    public JPanel scorePanel() { //NEEDS TO BE DONE , this display the player previous score and the top 3 
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(4, 1));
        playerLabel = new JLabel();
        playerName = playerGUI.getPlayerName();
        playerLabel.setText("Current Player: " + playerName);
        playerLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 44));
        scorePanel.setBackground(gray);
        scorePanel.add(playerLabel);
//        findTheTop3();
        readPlayerInfo();

        JLabel top1Label = new JLabel("Top 1 ");
        JLabel top2Label = new JLabel("Top 2 ");
        JLabel top3Label = new JLabel("Top 3 ");
        top1Label.setFont(new Font("Comic Sans MS", Font.BOLD, 44));
        top2Label.setFont(new Font("Comic Sans MS", Font.BOLD, 44));
        top3Label.setFont(new Font("Comic Sans MS", Font.BOLD, 44));

        scorePanel.add(top1Label);
        scorePanel.add(top2Label);
        scorePanel.add(top3Label);

        return scorePanel;
    }

    public void prizePanel() {
        prizePanel = new JPanel();
        currentPrize = new JLabel("Current Prize: " + String.valueOf(prize));
        nextPrize = new JLabel("Prize Goal: " + String.valueOf(prizeGoal));
        currentPrize.setFont(new Font("Comic Sans MS", Font.BOLD, 44));
        nextPrize.setFont(new Font("Comic Sans MS", Font.BOLD, 44));

        prizePanel.setLayout(new GridLayout(2, 1));
        prizePanel.setBackground(gray);
        prizePanel.add(currentPrize);
        prizePanel.add(nextPrize);
    }

    public void questionPanel() {
        questionPanel = new JPanel();
        questionPanel.setBackground(gray);
        questionPanel.setLayout(new BorderLayout());

        questionLabel = new JLabel();

        questionLabel.setText(question);
        questionLabel.setPreferredSize(new Dimension(width, height / 3));
        questionLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 44));
        questionLabel.setForeground(Color.black);

        questionPanel.add(questionLabel);
    }

    public void optionsPanel() // when press a button , pass the buttons value , check the answer wether if its correct 
    {
        optionsPanel = new JPanel();
        optionsPanel.setBackground(gray);
        optionsPanel.setLayout(new GridLayout(2, 2, 20, 20));
        aButton = new JButton(a);
        bButton = new JButton(b);
        cButton = new JButton(c);
        dButton = new JButton(d);

        optionButtonsMouseListener optionButtonListener = new optionButtonsMouseListener();

        aButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        aButton.setBackground(Color.white);
        aButton.setForeground(Color.black);
        aButton.setBorder(new LineBorder(Color.BLACK, 4));
        aButton.addMouseListener(optionButtonListener);
        bButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        bButton.setBackground(Color.white);
        bButton.setForeground(Color.black);
        bButton.setBorder(new LineBorder(Color.BLACK, 4));
        bButton.addMouseListener(optionButtonListener);
        cButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        cButton.setBackground(Color.white);
        cButton.setForeground(Color.black);
        cButton.setBorder(new LineBorder(Color.BLACK, 4));
        cButton.addMouseListener(optionButtonListener);
        dButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        dButton.setBackground(Color.white);
        dButton.setForeground(Color.black);
        dButton.setBorder(new LineBorder(Color.BLACK, 4));
        dButton.addMouseListener(optionButtonListener);

        optionsPanel.add(aButton);
        optionsPanel.add(bButton);
        optionsPanel.add(cButton);
        optionsPanel.add(dButton);
    }

    private class optionButtonsMouseListener implements MouseListener {

        JButton jb;

        @Override
        public void mouseClicked(MouseEvent e) {
//            Map<String, Integer> records = readPlayerInfo();
            jb = (JButton) e.getSource();
            playerAnswer = jb.getText();
            check = checkAnswer(playerAnswer.charAt(0));
            System.out.println(checkAnswer(playerAnswer.charAt(0)));
            if (check) {
                getQA();
                questionLabel.setText(question);
                aButton.setText(a);
                bButton.setText(b);
                cButton.setText(c);
                dButton.setText(d);
                questionPanel.revalidate();
                // change the map value 
                records.put(playerGUI.getPlayerName(), prize);
                writeToPlayerInfo(records);
                currentPrize.setText("Current Prize: " + String.valueOf(prize));
                nextPrize.setText("Prize Goal: " + String.valueOf(prizeGoal));
//                saveFile()
                prizePanel.revalidate();

                displayLabel.setText("");
                displayLabel.setIcon(logoIcon);
                displayPanel.revalidate();

                aButton.setVisible(true);
                bButton.setVisible(true);
                cButton.setVisible(true);
                dButton.setVisible(true);

            } else {
                exitGame();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
            jb = (JButton) e.getSource();
            jb.setBackground(Color.black);
            jb.setForeground(gold);
            jb.setBorder(new LineBorder(gold, 4));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            jb = (JButton) e.getSource();
            jb.setBackground(Color.white);
            jb.setForeground(Color.black);
            jb.setBorder(new LineBorder(Color.black, 4));
        }
    }

    private class helpButtonsMouseListener implements MouseListener // have not done the Audience and half half yet 
    {

        JButton jb;

        @Override
        public void mouseClicked(MouseEvent e) {
            jb = (JButton) e.getSource();
            if (jb.equals(hintButton)) {
                System.out.println("Hint Pressed");
                hintButton.setVisible(false);
                displayPanel.removeAll();
                displayLabel = new JLabel(hint);
                displayLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
                displayPanel.add(displayLabel);
                displayPanel.revalidate();
            } else if (jb.equals(audienceButton)) // display audience panel
            {
                audienceButton.setVisible(false);
                System.out.println("Auidence Pressed");

                displayPanel.removeAll();
                displayLabel = new JLabel(audience);
                displayLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
                displayPanel.add(displayLabel);
                displayPanel.revalidate();
            } else if (jb.equals(halfButton)) // display audience panel
            {
                halfButton.setVisible(false);
                System.out.println("HalfButton Pressed");
                Random random = new Random();
                int answerInt = 0;
                if (a.charAt(0) == answer.charAt(0)) {
                    answerInt = 0;
                } else if (b.charAt(0) == answer.charAt(0)) {
                    answerInt = 1;
                } else if (c.charAt(0) == answer.charAt(0)) {
                    answerInt = 2;
                } else if (d.charAt(0) == answer.charAt(0)) {
                    answerInt = 3;
                }

                boolean success;
                int firstDisabledButton;
                int secondDisabledButton;
                //disable first button
                do {
                    success = true;
                    firstDisabledButton = random.nextInt(4);
                    switch (firstDisabledButton) {
                        case 0:
                            if (answerInt == 0) {
                                success = false;
                            } else {
                                aButton.setVisible(false);
                            }
                            break;
                        case 1:
                            if (answerInt == 1) {
                                success = false;
                            } else {
                                bButton.setVisible(false);
                            }
                            break;
                        case 2:
                            if (answerInt == 2) {
                                success = false;
                            } else {
                                cButton.setVisible(false);
                            }
                            break;
                        case 3:
                            if (answerInt == 3) {
                                success = false;
                            } else {
                                dButton.setVisible(false);
                            }
                            break;
                    }
                } while (success == false);

                //disable second one
                do {
                    success = true;
                    secondDisabledButton = random.nextInt(4);
                    switch (secondDisabledButton) {
                        case 0:
                            if (answerInt == 0 || secondDisabledButton == firstDisabledButton) {
                                success = false;
                            } else {
                                aButton.setVisible(false);

                            }
                            break;
                        case 1:
                            if (answerInt == 1 || secondDisabledButton == firstDisabledButton) {
                                success = false;
                            } else {
                                bButton.setVisible(false);

                            }
                            break;
                        case 2:
                            if (answerInt == 2 || secondDisabledButton == firstDisabledButton) {
                                success = false;
                            } else {
                                cButton.setVisible(false);

                            }
                            break;
                        case 3:
                            if (answerInt == 3 || secondDisabledButton == firstDisabledButton) {
                                success = false;
                            } else {
                                dButton.setVisible(false);

                            }
                            break;

                    }
                } while (success == false);
            }

        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            jb = (JButton) e.getSource();
            jb.setBackground(Color.black);
            jb.setForeground(gold);
            jb.setBorder(new LineBorder(gold, 4));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            jb = (JButton) e.getSource();
            jb.setBackground(Color.white);
            jb.setForeground(Color.black);
            jb.setBorder(new LineBorder(Color.black, 4));
        }
    }

    public JPanel helpPanel() {
        JPanel helpPanel = new JPanel();
        helpPanel.setBackground(gray);
        hintButton = new JButton("     Hint     ");
        audienceButton = new JButton("     Audience     ");
        halfButton = new JButton("     Half Half     ");

        helpButtonsMouseListener helpButtonsMouseListener = new helpButtonsMouseListener();

        hintButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        hintButton.setBackground(Color.white);
        hintButton.setForeground(Color.black);
        hintButton.setBorder(new LineBorder(Color.BLACK, 4));
        hintButton.addMouseListener(helpButtonsMouseListener);
        audienceButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        audienceButton.setBackground(Color.white);
        audienceButton.setForeground(Color.black);
        audienceButton.setBorder(new LineBorder(Color.BLACK, 4));
        audienceButton.addMouseListener(helpButtonsMouseListener);
        halfButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        halfButton.setBackground(Color.white);
        halfButton.setForeground(Color.black);
        halfButton.setBorder(new LineBorder(Color.BLACK, 4));
        halfButton.addMouseListener(helpButtonsMouseListener);

        helpPanel.setLayout(new GridLayout(3, 1, 0, 20));
        helpPanel.add(hintButton);
        helpPanel.add(audienceButton);
        helpPanel.add(halfButton);
        return helpPanel;
    }

    public boolean checkAnswer(char playerAnswer) {
        return playerAnswer == answer.charAt(0);
    }

    public void exitGame() {
//        Map<String, Integer> records = readPlayerInfo();
//        saveFile(records);
//        System.out.println(records);
        System.out.println("You got : " + prize + "    Your goal :" + prizeGoal);
        System.exit(0);
        this.dispose();
        this.setVisible(false);
    }

    public void prizeCount(int questionNo) // start from 100 , next question is a double the prize of the privous one 
    {
        prize = 0;
        prizeGoal = 100;
        for (int i = 1; i < questionNo; i++) {
            prize += prizeGoal;
            prizeGoal *= 2;
        }
    }


//
//    //System read the player info as it runs 
//    // Need to save the file NOT after exit because it crashes
//    // Need to save the file as the score is updated
//    // Therefore append everytime user select the answer <<currentScore>>
//    // Use string.value of prize to get the integer value of prize
//    // put it into the hashmap as (name, score/prize)
//    // get the highest and print it
    
       public  void readPlayerInfo() {
        HashMap<String, Integer> playerFile = new HashMap<>();
        BufferedReader br = null;
        try {
            //read scores txt file
            br = new BufferedReader(new FileReader("PlayerInformation.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitString = line.split(" ");
                playerFile.put(splitString[0], Integer.valueOf(splitString[1]));
            }
        } catch (IOException ex) {
            Logger.getLogger(ContestGUI.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(ContestGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        this.records = playerFile;
    }
       
    public void writeToPlayerInfo(HashMap<String, Integer> records) {
        PrintWriter pw = null;
        try {
            //create scores txt file
            pw = new PrintWriter("PlayerInformation.txt");
            for (Map.Entry<String, Integer> recordEntry : records.entrySet()) {
                //if this contains the current player, append to it
//        //else just record the player key and value
                if (recordEntry.getKey().equals(playerName)) {
                    recordEntry.setValue(prize);
                }

                pw.println(recordEntry.getKey() + " " + recordEntry.getValue());

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ContestGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
    
    public static void main(String[] args) {
     
//
//        //Getting highest Value
//         int maxValueInMap=(Collections.max(recordsOfPlayers.values())); // Returns max value
//         for (Entry<String, Integer> entry : recordsOfPlayers.entrySet()) {  // Itrate through hashmap
//            if (entry.getValue()== maxValueInMap) {
//                System.out.println(entry.getKey());     // Print the key with max value
//            }
//        }
        
        ContestGUI contestFrame = new ContestGUI();
        Thread contestThread = new Thread(contestFrame);
        Thread playerThread = new Thread(contestFrame.playerGUI);
        contestThread.start();
        playerThread.start();
    }
}
