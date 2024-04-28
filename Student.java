import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;


// the class of the client
class Client{
    private Socket socket;              // the socket
    private PrintStream cout;           // the output stream
    private Scanner cin;                // the input stream
    private boolean isConnected;        // flag: is connected?
    private NotifyGUI notifyGUI;        // similar function as the warningGUI in the teacher end
    private ConnectGui connectGui;      // connect with teacher's end; start first

    // initialize
    public Client(){
        isConnected = false;
        connectGui = new ConnectGui();
        notifyGUI = new NotifyGUI();
    }

    // the class of the connect gui
    private class ConnectGui{
        private JFrame connectFrame;
        private JTextField ipField;
        private JTextField portField;
        private JButton connectButton;
        public ConnectGui(){
            init();                             // initialize the gui
            ActionListener ac = new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // fetch the information passed by the student
                    String ip = ipField.getText();
                    int port;
                    try {
                        port = Integer.parseInt(portField.getText());
                    }catch(NumberFormatException e){
                        notifyGUI.init("Bad input!", "Error");
                        return;
                    }

                    // call the connect function, which will change the flag
                    connect(ip, port);
                    if(isConnected){

                        // if success, close the connect frame and start the main frame: QAGui
                        connectFrame.setVisible(false);
                        QAGui qagui = new QAGui();
                    }
                }
            };
            connectButton.addActionListener(ac);
            connectFrame.setVisible(true);
        }

        private void init(){
            connectButton = new JButton();
            JLabel portL = new JLabel();
            JLabel ipL = new JLabel();
            ipField = new JTextField();
            portField = new JTextField();
            connectFrame = new JFrame("Login");
            connectFrame.setResizable(false);

            connectFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            connectFrame.setSize(459, 300);

            connectButton.setText("Login");

            portL.setText("Port:");

            ipL.setText("IP:");
            

            GroupLayout layout = new GroupLayout(connectFrame.getContentPane());
            connectFrame.getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGap(189, 189, 189)
                                                    .addComponent(connectButton, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGap(64, 64, 64)
                                                    .addComponent(portL, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(31, 31, 31)
                                                    .addComponent(portField, GroupLayout.PREFERRED_SIZE, 273, GroupLayout.PREFERRED_SIZE)))
                                    .addContainerGap(45, Short.MAX_VALUE))
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(ipField, GroupLayout.PREFERRED_SIZE, 273, GroupLayout.PREFERRED_SIZE)
                                    .addGap(45, 45, 45))
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(66, 66, 66)
                                            .addComponent(ipL, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
                                            .addContainerGap(347, Short.MAX_VALUE)))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(72, 72, 72)
                                    .addComponent(ipField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(portL)
                                            .addComponent(portField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(44, 44, 44)
                                    .addComponent(connectButton, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                    .addGap(36, 36, 36))
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(76, 76, 76)
                                            .addComponent(ipL)
                                            .addContainerGap(169, Short.MAX_VALUE)))
            );
        }


        // try to use passed-in's to connect to teacher's end
        private void connect(String ip, int port){
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                cout = new PrintStream(socket.getOutputStream());
                cin = new Scanner(socket.getInputStream());
                isConnected = true;         // if success, change the flag
            } catch (IOException e) {
                notifyGUI.init("Wrong IP or port!", "Error");
                // throw new RuntimeException(e);
            }
        }
    }

    // similar to the warningGUI of teacher end
    private class NotifyGUI{
        private JFrame frame;
        private JLabel warning;
        NotifyGUI(){
            frame = new JFrame("error");
            frame.setSize(500,300);
            warning = new JLabel("");
            frame.add(warning);
        }
        public void init(String content, String tittle){
            this.warning.setText(content);
            frame.setTitle(tittle);
            frame.setVisible(true);
        }
        public void setBad(){
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }


    // Q and A gui
    private class QAGui{
        Question q;                 // the question now (Question struct)
        JFrame qaframe;             // main frame
        JTextArea questionArea;     // text area for putting the question
        JTextField nameField;       // text field for writing name
        JButton submit;             // submit button
        JRadioButton buttonA;       // choices
        JRadioButton buttonB;
        JRadioButton buttonC;
        JRadioButton buttonD;
        ButtonGroup options;        // make it single choice
        public QAGui(){
            q = new Question();
            init();
        }

        // initialize the components
        private void init() {
            qaframe = new JFrame("Student End");
            JScrollPane jScrollPane1 = new JScrollPane();
            questionArea = new JTextArea();
            buttonD = new JRadioButton();
            buttonA = new JRadioButton();
            buttonB = new JRadioButton();
            buttonC = new JRadioButton();
            JLabel nameL = new JLabel();
            nameField = new JTextField();
            submit = new JButton();

            qaframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            qaframe.setSize(485, 600);
            qaframe.setResizable(false);

            questionArea.setColumns(20);
            questionArea.setRows(5);
            jScrollPane1.setViewportView(questionArea);
            questionArea.setLineWrap(true);
            questionArea.setEditable(false);

            options = new ButtonGroup();

            options.add(buttonA);
            options.add(buttonB);
            options.add(buttonC);
            options.add(buttonD);

            nameL.setText("Your Name:");

            submit.setText("submit");

            GroupLayout layout = new GroupLayout(qaframe.getContentPane());
            qaframe.getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                    .addGap(37, 37, 37)
                                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addGroup(layout.createSequentialGroup()
                                                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                            .addComponent(buttonA)
                                                                            .addComponent(buttonB)
                                                                            .addComponent(buttonC)
                                                                            .addComponent(buttonD))
                                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                            .addGroup(layout.createSequentialGroup()
                                                                    .addComponent(nameL, GroupLayout.PREFERRED_SIZE, 121, GroupLayout.PREFERRED_SIZE)
                                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addComponent(nameField))))
                                            .addGroup(layout.createSequentialGroup()
                                                    .addContainerGap(45, Short.MAX_VALUE)
                                                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 398, GroupLayout.PREFERRED_SIZE)))
                                    .addGap(45, 45, 45))
                            .addGroup(layout.createSequentialGroup()
                                    .addGap(203, 203, 203)
                                    .addComponent(submit)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
                                    .addGap(54, 54, 54)
                                    .addComponent(buttonA)
                                    .addGap(32, 32, 32)
                                    .addComponent(buttonB)
                                    .addGap(32, 32, 32)
                                    .addComponent(buttonC)
                                    .addGap(32, 32, 32)
                                    .addComponent(buttonD)
                                    .addGap(31, 31, 31)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(nameL, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                                    .addComponent(submit)
                                    .addGap(23, 23, 23))
            );

            ActionListener ac = new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String msg2Send="";

                    // check choice
                    if(buttonA.isSelected()){
                        msg2Send = "A";
                    }
                    else if(buttonB.isSelected()){
                        msg2Send = "B";
                    }
                    else if(buttonC.isSelected()){
                        msg2Send = "C";
                    }
                    else if(buttonD.isSelected()){
                        msg2Send = "D";
                    }

                    // student must fill their name
                    if(nameField.getText().isEmpty()){
                        notifyGUI.init("Fill your name and retry!", "Error");
                        return;
                    }
                    msg2Send = msg2Send+"\t"+ nameField.getText();
                    cout.println(msg2Send);
                    close();

                    // students only have one chance to answer the question; after "successfully" submit, they will quit
                    notifyGUI.setBad();
                    notifyGUI.init("Result sent! Close this window to quit.", "Result");
                    /* Even the client notice in this way, when students click submit, the teacher might have already
                       closed the server because no late submission allowed. Their message might not be received by the
                       teacher. */

                }
            };
            submit.addActionListener(ac);
            fillQuestion();                 // fill the question area and the choices
            buttonA.setText(q.answerA);
            buttonB.setText(q.answerB);
            buttonC.setText(q.answerC);
            buttonD.setText(q.answerD);
            questionArea.setText(q.question);
            qaframe.setVisible(true);
        }

        // ths method will listen to the teacher's end and pass the question and choices to the question area and choices
        private void fillQuestion(){
            String QAndA="";
            while(cin.hasNextLine()){
                QAndA = cin.nextLine();
                break;
            }

            // find the marker of different part of the question, find substring, and set value
            int QStart=QAndA.indexOf(";:Q:");
            int AStart=QAndA.indexOf(";:A.");
            int BStart=QAndA.indexOf(";:B.");
            int CStart=QAndA.indexOf(";:C.");
            int DStart=QAndA.indexOf(";:D.");
            q.question=QAndA.substring(QStart+2,AStart);
            q.answerA=QAndA.substring(AStart+2,BStart);
            q.answerB=QAndA.substring(BStart+2,CStart);
            q.answerC=QAndA.substring(CStart+2,DStart);
            q.answerD=QAndA.substring(DStart+2);

        }

        // close everything
        private void close(){
            cin.close();
            cout.close();
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}


    public class Student {
    public static void main(String[] args) {
        Client s = new Client();

    }
}