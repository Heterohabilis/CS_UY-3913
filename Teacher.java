package final_prj;

import org.mariadb.jdbc.Connection;
import org.mariadb.jdbc.Statement;

import java.io.*;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

/**
 This is the code for teacher's end. It has four guis: database setting gui, main gui, question setting gui, and a warning gui. After the software is started, the database setting gui will appear. The teacher can choose to fill the fields to set the database to where the result will be uploaded. If leaving it blank or failing to connect, then the teacher will still be able to use the software, but the result will not be uploaded. Then, in the main gui, the teacher will need to set the question first. In the question setting gui, the teacher should fill all fields: question, A, B, C, D, and the question ID. After that, the teacher may start the server. The server will be started at [IP]:1919. After students' end are connected, teacher's end will send the question via the output stream to student's end, and that end will handle the sent information and show it in correct format. A scanner will be ready to hear from student's end. The heard results will be recorded in a thread-safe list by each message listener thread. The teacher can check the pie chart by connecting the "view result" button. Teachers may not edit the question or download the result in txt file unless they stop the connection.
*/


// Structure of the Question
class Question{
    String question;
    String answerA;
    String answerB;
    String answerC;
    String answerD;
    String ID;          // The unique ID of the question

    /* Test: jdbc:mariadb://localhost:3306/polls, RECORDS */

    // check if the class has been initiated
    private boolean isInit(){
        return question!=null && answerA!=null && answerD!=null && answerC!=null && answerB!=null;
    }

    // check if the question is complete: A, B, C, D, ID, question must be filled
    boolean isComplete(){
        return isInit()&&(!question.isEmpty() && !answerA.isEmpty() && !answerB.isEmpty() && !answerC.isEmpty()
                && !answerD.isEmpty() && !ID.isEmpty());
    }
}



// class of teacher end gui
class GUI_Teacher{
    /* Main Window Components */
    private JFrame frame;           // main frame
    private JButton set_q;          // set question
    private JButton linkStart;      // start the connection
    private JButton linkStop;       // stop the connection
    private JButton set_d;          // set the database
    private JButton view;           // view the pie chart
    private JButton download;       // download the records as a txt file
    private WarningGUI warningGUI;  // warning gui, use repetitively
    private QuestionGUI qSetter;    // gui for setting questions

    /* Question and Records area */
    private Question now_q;         // the question now
    private int[] frac_ans;         // 4 parts, each for the number of answers of "A, B, C, D"
    private List<String> answers;   // format "ANS\tNAME"; thread safe.

    /* Connection area */
    private ServerSocket serverSocket;    // the socket
    private PrintStream out;        // the output stream for sending questions to clients
    private ArrayList<Socket>
            livingCLientSockets;    // socket list, contains all connected sockets; use to track the students connection

    /* Database Area */
    private Connection connection;  // Driver connector
    private Statement statement;    // sql statements
    private String dbAddr;          // the url of the database
    private String dbName;          // table name of the database
    private String dbUser;          // login username of the database
    private String dbPass;          // login password of the database
    private DBConnectGUI
            dbConnectGUI;           // database setting gui

    /* Flags Area */
    private boolean isQSet;         // is the question set?
    private boolean isConnected;    // is the server open?
    private boolean isDBSet;         // is the database set?


    // constructor for the class
    public GUI_Teacher() {
        //initialization
        warningGUI = new WarningGUI();
        elemInit();                                       // initialize the elements of the main gui
        dbConnectGUI = new DBConnectGUI();
        set_q.addActionListener(new SetQ());
        linkStart.addActionListener(new Link_start());
        linkStop.addActionListener(new Link_end());
        view.addActionListener(new Show_pie());
        download.addActionListener(new FileCreator());
        set_d.addActionListener(new DBConnector());

        isQSet = false;
        isConnected = false;
        answers = new ArrayList<>();
        answers = Collections.synchronizedList(answers);  // make the list thread-safe
        livingCLientSockets = new ArrayList<>();          // only one thread reach this, no need to make it thread safe
        frac_ans = new int[4];                            // four: abcd
        now_q = new Question();
        qSetter = new QuestionGUI();
    }

    // initialize the components of the main window
    private void elemInit(){
        linkStart = new JButton();
        set_q = new JButton();
        download = new JButton();
        linkStop = new JButton();
        view = new JButton();
        set_d = new JButton();
        frame = new JFrame("Teacher's End");

        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setSize(600, 850);

        linkStart.setText("Start Connection");

        set_q.setText("Set Question");

        download.setText("Download");

        linkStop.setText("End Connection");

        view.setText("View Result");

        set_d.setText("Set Database");

        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(164, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(set_d, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(download, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(set_q, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(linkStart, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(linkStop, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(view, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
                                .addGap(165, 165, 165))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(70, 70, 70)
                                .addComponent(set_q, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(linkStart, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(linkStop, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(view, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(download, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(set_d, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(114, Short.MAX_VALUE))
        );
        frame.setResizable(false);
        //frame.setVisible(true);
    }

    /** The following parts are definition of the methods */

    // file writer, called by action listener of the "download" button
    private void file_writer(){
        // do some checks
        if(!isQSet){
            warningGUI.init("Set a question first!");
            return;
        }
        if(isConnected){
            warningGUI.init("Please stop the connection first!");
            return;
        }

        // filename format: mm-dd-yyyy h-min-sec_qID
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date date = new Date();
        String dir = System.getProperty("user.dir");  // get the current dir.
        String time = formatter.format(date);
        String fileName = time+"_"+now_q.ID + ".txt";
        File file = new File(dir, fileName);
        String path=file.getPath();
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(time+"\n");
            writer.write("Q: "+now_q.question+"\n");
            writer.write("A: "+now_q.answerA+"\n");
            writer.write("B: "+now_q.answerB+"\n");
            writer.write("C: "+now_q.answerC+"\n");
            writer.write("D: "+now_q.answerD+"\n");
            writer.write("There are "+answers.size()+" student(s) answered the question.\n");
            writer.write(frac_ans[0]+" student(s) chose A\n");
            writer.write(frac_ans[1]+" student(s) chose B\n");
            writer.write(frac_ans[2]+" student(s) chose C\n");
            writer.write(frac_ans[3]+" student(s) chose D\n");
            writer.write("-------------------------------------------------------------\n\n");
            writer.write("Option | name\n");
            for (String str : answers) {
                writer.write(str+"\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    /* because of the format of the answers are always the same, \t will be the token of
        splitting the answer and the name */
    private int splitterFinder(String answer){
        int i;
        for(i = 0; i < answer.length(); i++){
            if(answer.charAt(i) == '\t'){
                return i;
            }
        }
        return -1;
    }

    // add '' to string
    private String augString(String str){
        return "'"+str+"'";
    }


    // thread-safe: only one thread can write to the database at the same time
    private synchronized void safeUploader(String name, String id, String ans, String db){
        String toExe = "INSERT INTO "+db+"(student, question_id, answer) VALUES ("+ augString(name) + "," +
                augString(id) + "," + augString(ans) + ");";

        try {
            statement.execute(toExe);
        } catch (SQLException e) {
            System.out.println("error");
        }

    }

    /** The following parts are definition of inner classes */

    // A thread to send the question and answer to the clients
    private class ServerStarter extends Thread{
        public void run(){
            try {
                serverSocket = new ServerSocket(1919);      // set the port
            } catch (IOException e) {
                warningGUI.init("Unable to start server...try again later") ;
                return;
            }
            while (isConnected){
                // main loop
                Socket client;
                try {
                    client = serverSocket.accept();
                    System.out.println("got connection from "+client.getInetAddress().toString());
                    out = new PrintStream(client.getOutputStream());
                } catch (IOException e) {
                    break;
                }

                // send the question to the client; using strange string as the token of split
                out.println(";:Q:"+now_q.question+";:A."+now_q.answerA+";:B."
                        +now_q.answerB+";:C."+now_q.answerC+";:D."+now_q.answerD);
                MsgReceiver receiver = new MsgReceiver(client);     // message receiver; hear from client
                receiver.start();
                livingCLientSockets.add(client);                    // track the user sockets

            }
        }
    }


    // the message receiver from the student end (client); after receiving, close the socket
    private class MsgReceiver extends Thread{
        Socket s;       // the socket obtained by calling accept()
        MsgReceiver(Socket s){
            this.s=s;
        }
        @Override
        public void run() {
            Scanner scanner;
            try {
                scanner = new Scanner(s.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (isConnected)
                if(scanner.hasNext()){
                    String localAns = scanner.nextLine();
                    answers.add(localAns);  // add to records

                    // find the "\t" in the feedback to separate the strings
                    int indexOfSplitter = splitterFinder(localAns);
                    String ans = localAns.substring(0,indexOfSplitter);
                    String name = localAns.substring(indexOfSplitter+1);

                    // if the database is available, upload the result
                    if(isDBSet)
                        safeUploader(name, now_q.ID, ans, dbName);

                    // close it immediately
                    try {
                        s.close();
                    } catch (IOException e) {
                        warningGUI.init("Failed to close the socket!");
                    }
                    break;
                }
        }
    }


    // Action listener of set question button
    private class SetQ implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // not allowed to set when it is connected
            if(isConnected){
                warningGUI.init("Please stop the connection first!");
                return;
            }
            // let the question setter show
            qSetter.init();
        }
    }


    // this is the action listener for starting the connection
    private class Link_start implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // only start the server when the question is set and the server is not connected
            if(isQSet && !isConnected){

                //change the flag
                isConnected=true;

                // change state of the frame
                frame.setTitle("Teacher End--Running");

                // start the server
                new ServerStarter().start();
            }else if(!isQSet){
                warningGUI.init("Please set a question first!");
            }else{
                warningGUI.init("It's running now! ");
            }
        }
    }


    // Action Listener for end the connection
    private class Link_end implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // some checks
            if(!isQSet){
                warningGUI.init("Set a question first!");
                return;
            }
            if(!isConnected){
                warningGUI.init("Not connected!");

            }

            // stop everything about connection
            else {
                isConnected = false;
                try {
                    serverSocket.close();

                    // close all student sockets: no lateness allowed
                    for(Socket socket : livingCLientSockets){
                        if(!socket.isClosed())
                            socket.close();
                    }

                    // save memory: clean all closed student sockets
                    livingCLientSockets.clear();

                    // change the tittle back
                    frame.setTitle("Teacher End");
                } catch (IOException e) {
                    warningGUI.init("Failed to close the socket!");
                }
            }
        }
    }

    // action listener to show the pie chart
    private class Show_pie implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // do some check
            if(!isQSet) {
                warningGUI.init("Set a question first!");
                return;
            }
            frac_ans = new int[4];                                      // each time create a new array
            for (var elem : answers) {                                  // count the number of each choice
                    if (elem.charAt(0) == 'A') ++frac_ans[0];
                    else if (elem.charAt(0) == 'B') ++frac_ans[1];
                    else if (elem.charAt(0) == 'C') ++frac_ans[2];
                    else if (elem.charAt(0) == 'D') ++frac_ans[3];
                }
            JFrame frame = new JFrame("Result of "+now_q.ID);      // start a new frame. initialize
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.setSize(900,600);
            frame.setResizable(true);
            JPanel bgrd = new JPanel(new BorderLayout());
            JPanel labelP = new JPanel(new GridLayout(4,1));
            bgrd.add(labelP, BorderLayout.WEST);
            labelP.add(new JLabel(frac_ans[0]+" chose A (red)", SwingConstants.LEFT));
            labelP.add(new JLabel(frac_ans[1]+" chose B (blue)",SwingConstants.LEFT));
            labelP.add(new JLabel(frac_ans[2]+" chose C (green)",SwingConstants.LEFT));
            labelP.add(new JLabel(frac_ans[3]+" chose D (yellow)",SwingConstants.LEFT));
            // This is the pie panel
            PieChartPanel panel = new PieChartPanel(frac_ans);
            bgrd.add(panel);
            frame.add(bgrd);
            frame.setVisible(true);
        }
    }


    // action listener for creating a new txt file
    private class FileCreator implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            file_writer();
        }
    }


    // action listener to start the db setter
    private class DBConnector implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(!isConnected) dbConnectGUI.DBFrame.setVisible(true);
            else{
                warningGUI.init("Stop the connection first!");
            }
        }
    }


    // this is the gui for setting a question
    private class QuestionGUI{
        private final JFrame frame;                 // the main frame
        private final JPanel background;            // the background panel
        private final JTextArea question;           // the input area for the question description
        private JScrollPane qPane;                  // allow multiple lines
        private final JTextArea choiceA;            // the input area for the choice A
        private JScrollPane aPane;                  // allow multiple lines
        private final JTextArea choiceB;            // the input area for the choice B
        private JScrollPane bPane;                  // allow multiple lines
        private final JTextArea choiceC;            // same for C
        private JScrollPane cPane;
        private final JTextArea choiceD;            // same for D
        private JScrollPane dPane;
        private final JTextField qID;               // the input field for qid
        private final JButton done;                 // the confirmation button


        public QuestionGUI(){
            frame = new JFrame("set question");
            background = new JPanel(new GridLayout(7, 2,20,20));
            frame.add(background);
            frame.setSize(700,800);
            frame.setResizable(false);
            question = new JTextArea();
            qPane = new JScrollPane(question);
            question.setLineWrap(true);
            choiceA = new JTextArea();
            aPane = new JScrollPane(choiceA);
            question.setLineWrap(true);
            choiceB = new JTextArea();
            bPane = new JScrollPane(choiceB);
            question.setLineWrap(true);
            choiceC = new JTextArea();
            cPane = new JScrollPane(choiceC);
            question.setLineWrap(true);
            choiceD = new JTextArea();
            dPane = new JScrollPane(choiceD);
            choiceD.setLineWrap(true);
            qID = new JTextField();
            background.add(new JLabel("Write your question here: ", SwingConstants.CENTER));
            background.add(qPane);
            background.add(new JLabel("Write the Option A here: ", SwingConstants.CENTER));
            background.add(aPane);
            background.add(new JLabel("Write the Option B here: ",SwingConstants.CENTER));
            background.add(bPane);
            background.add(new JLabel("Write the Option C here: ",SwingConstants.CENTER));
            background.add(cPane);
            background.add(new JLabel("Write the Option D here: ",SwingConstants.CENTER));
            background.add(dPane);
            background.add(new JLabel("Short name for this question: ",SwingConstants.CENTER));
            background.add(qID);
            background.add(new JLabel("(Make sure that you have filled all fields!)", SwingConstants.CENTER));
            done = new JButton("DONE");
            background.add(done);
            ActionListener ac = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    now_q.question = question.getText();
                    now_q.answerA = choiceA.getText();
                    now_q.answerB = choiceB.getText();
                    now_q.answerC = choiceC.getText();
                    now_q.answerD = choiceD.getText();
                    now_q.ID = qID.getText();
                    if(!now_q.isComplete()){    // all blanks must be filled
                        warningGUI.init("Question Not Complete!");
                    }
                    else{
                    isQSet = true;              // change the flag
                    frame.setVisible(false);
                    }
                }
            };
            done.addActionListener(ac);

        }
        public void init(){
            if(isQSet) {
                qID.setEditable(false);         // qID cannot be edited
                // retrieve the last edit
                question.setText(now_q.question);
                choiceA.setText(now_q.answerA);
                choiceB.setText(now_q.answerB);
                choiceC.setText(now_q.answerC);
                choiceD.setText(now_q.answerD);
            }
            frame.setVisible(true);
        }
    }

    // The warning gui, set visible when something wrong happens
    private class WarningGUI{
        private JFrame frame;
        private JLabel warning;
        WarningGUI(){
            frame = new JFrame("Error");
            frame.setSize(500,300);
            warning = new JLabel("", SwingConstants.CENTER);
            frame.add(warning);
            frame.setResizable(false);
        }
        public void init(String warning){
            this.warning.setText(warning);
            frame.setVisible(true);
        }

        // not used here, but may be used in the future: fatal problem, shutdown the program after close the window
        public void setBad(){
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }


    // This is the gui for set/change db
    private class DBConnectGUI{
        private JFrame DBFrame;             // main frame
        private JLabel addr;                // label for the address field
        private JTextArea addrField;        // the input field for address of the db
        private JButton connectB;           // the button of "connect"
        private JTextField dField;          // the input field for the table name
        private JLabel dbNameLabel;         // the label for the table name
        private JLabel pass;                // the label for the password
        private JPasswordField passField;   // the input field for the password
        private JLabel uname;               // the label for the username
        private JTextField usrField;        // the input field of the username

        // initialize the gui
        public DBConnectGUI() {
            DBFrame = new JFrame("Database Connection");
            dbNameLabel = new JLabel();
            addr = new JLabel();
            uname = new JLabel();
            pass = new JLabel();
            addrField = new JTextArea();
            usrField = new JTextField();
            dField = new JTextField();
            passField = new JPasswordField();
            connectB = new JButton();

            DBFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            DBFrame.setSize(463,300);
            DBFrame.setResizable(false);

            dbNameLabel.setText("Table name:");

            addr.setText("Address:");

            uname.setText("Username:");

            pass.setText("Password:");

            DBFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            connectB.setText("Connect");

            GroupLayout layout = new GroupLayout(DBFrame.getContentPane());
            DBFrame.getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addGroup(layout.createSequentialGroup()
                                                                    .addGap(41, 41, 41)
                                                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                            .addComponent(dbNameLabel)
                                                                            .addComponent(addr)
                                                                            .addComponent(uname)))
                                                            .addGroup(layout.createSequentialGroup()
                                                                    .addGap(39, 39, 39)
                                                                    .addComponent(pass)))
                                                    .addGap(18, 18, 18)
                                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addComponent(passField, GroupLayout.PREFERRED_SIZE, 299, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(addrField, GroupLayout.PREFERRED_SIZE, 299, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(dField, GroupLayout.PREFERRED_SIZE, 299, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(usrField, GroupLayout.PREFERRED_SIZE, 299, GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGap(178, 178, 178)
                                                    .addComponent(connectB)))
                                    .addContainerGap(33, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGap(27, 27, 27)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(addr)
                                            .addComponent(addrField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(dbNameLabel)
                                            .addComponent(dField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(uname)
                                            .addComponent(usrField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(pass)
                                            .addComponent(passField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addComponent(connectB)
                                    .addContainerGap(27, Short.MAX_VALUE))
            );
            ActionListener ac = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    dbAddr = addrField.getText();
                    dbName = dField.getText();
                    dbUser = usrField.getText();
                    dbPass = String.valueOf(passField.getPassword());
                    try {

                        // must make sure that the fields are all filled
                        if(!dbName.isEmpty() && !dbUser.isEmpty() && !dbPass.isEmpty()&&!dbAddr.isEmpty() ) {
                            connection = (Connection) DriverManager.getConnection(dbAddr, dbUser, dbPass);
                            statement = connection.createStatement();
                            isDBSet = true;
                        }
                    } catch (SQLException e) {
                        isDBSet = false;     // if cannot connect, unset the flag
                    } finally {
                        /* Since this window will be wake up when the program start, this choice was set to
                        be "exit"; but after the connection is first-time tried, it will be set to "hide" */
                        DBFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                        frame.setVisible(true);
                        DBFrame.setVisible(false);

                        // connect failed notice
                        if(!isDBSet) warningGUI.init("<html>Unable to connect to the database<br/>" +
                                "The data will not be uploaded; but you can set it later.</html>");
                    }
                }
            };
            connectB.addActionListener(ac);
            DBFrame.setVisible(true);

        }

        }
    }



public class Teacher{
    public static void main(String[] args) {
        GUI_Teacher gt = new GUI_Teacher();
    }
}
