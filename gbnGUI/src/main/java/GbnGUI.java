import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GbnGUI extends Frame implements ActionListener {

    private Label lostRateLabel;
    private TextField lostRateInput;
    private Label outFileLabel;
    private TextField outFileInput;
    private Button startServer;
    private Button sendFile;
    private Label clientOutputLabel;
    private TextArea clientOutputArea;
    private Label serverOutputLabel;
    private TextArea serverOutputArea;

    public GbnGUI() {
        setLayout(new FlowLayout());

        lostRateLabel = new Label("Lost Rate [0-99]");
        add(lostRateLabel);

        outFileLabel = new Label("Output Path (example: /tmp/out.txt or d://temp/out.txt)");
        add(outFileLabel);

        clientOutputLabel = new Label("Client Output");
        add(clientOutputLabel);
        serverOutputLabel = new Label("Server Output");
        add(serverOutputLabel);

        clientOutputArea = new TextArea(100, 50);
        add(clientOutputArea);
        serverOutputArea = new TextArea(100, 50);
        add(clientOutputArea);

        lostRateInput = new TextField(10);
        add(lostRateInput);

        outFileInput = new TextField(100);
        add(outFileInput);

        startServer = new Button("Start Server");
        sendFile = new Button("Send File");

        startServer.addActionListener(new BtnStartServerListener());
        sendFile.addActionListener(new BtnSendFileListener());
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

    }

    private class BtnStartServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {

        }
    }

    private class BtnSendFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {

        }
    }

    public static void main(String[] args) {
        new GbnGUI();
    }
}
