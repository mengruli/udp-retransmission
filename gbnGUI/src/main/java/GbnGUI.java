
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class GbnGUI extends JFrame {

    private InputPanel inputPanel;

    public GbnGUI() {
        Container cp = getContentPane();

        cp.setLayout(new GridLayout(0, 1));
        setSize(1200, 600);

        inputPanel = new InputPanel();
        add(inputPanel);

        setTitle("Go-Back-N Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public class InputPanel extends JPanel {

        private static final long TIMEOUT_LIMIT = 10000;
        private JLabel lostRateLabel;
        private JTextField lostRateInput;
        private JLabel outFileLabel;
        private JTextField outFileInput;

        private JLabel serverJarLabel;
        private JLabel clientJarLabel;
        private JTextField serverJarInput;
        private JTextField clientJarInput;

        private JButton startServer;
        private JButton sendFile;

        private JLabel clientOutputLabel;
        private JTextArea clientOutputArea;
        private JLabel serverOutputLabel;
        private JTextArea serverOutputArea;

        public Component getComponentByName(String name) {
            for (Component c : getComponents()) {

                if (c != null && c.getName() != null && c.getName().equals(name)) {
                    return c;
                }
            }
            return null;
        }

        public InputPanel() {
            setName("INPUT_PANEL");
            lostRateLabel = new JLabel("Lost Rate [0-99]");
            lostRateLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            lostRateInput = new JTextField(50);
            lostRateInput.setName("LOST_RATE_INPUT");
            lostRateInput.setSize(new Dimension(50, 1));
            outFileLabel = new JLabel("Output Path");
            outFileLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            outFileInput = new JTextField(50);
            outFileInput.setName("OUT_FILE_INPUT");
            outFileInput.setSize(new Dimension(50, 1));
            serverJarLabel = new JLabel("Server jar");
            serverJarLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            clientJarLabel = new JLabel("Client jar");
            clientJarLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            serverJarInput= new JTextField(50);
            serverJarInput.setName("SERVER_JAR_INPUT");
            serverJarInput.setSize(new Dimension(50, 1));
            serverJarInput.setText("absolute path to server jar");
            clientJarInput= new JTextField(50);
            clientJarInput.setName("CLIENT_JAR_INPUT");
            clientJarInput.setSize(new Dimension(50, 1));
            clientJarInput.setText("absolute path to client jar");

            clientOutputLabel = new JLabel("Client Output");
            clientOutputLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            clientOutputArea = new JTextArea(10, 50);
            clientOutputArea.setEnabled(false);
            clientOutputArea.setName("CLIENT_OUTPUT");
            serverOutputLabel = new JLabel("Server Output");
            serverOutputLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            serverOutputArea = new JTextArea(10, 50);
            serverOutputArea.setName("SERVER_OUTPUT");
            serverOutputArea.setEnabled(false);


            startServer = new JButton("Start Server");
            startServer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Component comp = getComponentByName("SERVER_JAR_INPUT");
                    Component so_area = getComponentByName("SERVER_OUTPUT");
                    Component lost_rate_input = getComponentByName("LOST_RATE_INPUT");
                    Component outfile_input = getComponentByName("OUT_FILE_INPUT");

                    JTextArea server_out = null;

                    try {

                        if (comp instanceof  JTextField && so_area instanceof JTextArea
                                && lost_rate_input instanceof JTextField
                                && outfile_input instanceof JTextField) {
                            server_out = (JTextArea)so_area;

                            String lostRate = ((JTextField)lost_rate_input).getText();
                            String output = ((JTextField)outfile_input).getText();
                            String serverJar;

                            JTextField tf = (JTextField)comp;
                            serverJar = tf.getText();
                            System.out.println(serverJar);
                            ProcessBuilder pb1 = new ProcessBuilder("java", "-jar", serverJar, "--lost"
                                    , lostRate, "--output", output);
                            // start server
                            Process p1 = pb1.start();
                            server_out.append("Server started...waiting for response...");
                            server_out.append("Click 'Send FIle' !");

                            boolean exited = false;
                            long time_elapsed = 0;
                            long start_time = System.currentTimeMillis();
                            while (!exited && time_elapsed < TIMEOUT_LIMIT) {
                                try {
                                    int status = p1.exitValue();

                                    exited = true;

                                    if (status != 0) {
                                        ((JTextArea)so_area).append("Server exited with error " + status);
                                    } else if (status == 0){
                                        ((JTextArea)so_area).append("Server exited with NO error ");
                                    }
                                } catch (Exception ex) {
                                    time_elapsed = System.currentTimeMillis() - start_time;
                                }

                                if (time_elapsed > TIMEOUT_LIMIT) {
                                    ((JTextArea)so_area).append("Server timeout. ");
                                }
                            }

                        } else {
                            throw new Exception("Cannot found server jar input or output area");
                        }

                    } catch (Exception ex) {
                        if (server_out != null) {
                            server_out.setText(ex.getMessage());
                        }
                        System.err.println(ex.getMessage());
                    }

                }
            });
            sendFile = new JButton("Send File");
            sendFile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String clientJar = null;
                    Component comp = getComponentByName("CLIENT_JAR_INPUT");
                    Component co_area = getComponentByName("CLIENT_OUTPUT");
                    clientJar = comp != null && ((JTextField)comp).getText() != null ? ((JTextField)comp).getText() : null;

                    if (clientJar != null) {
                        // start client
                        try {
                            ProcessBuilder pb2 = new ProcessBuilder("java", "-jar", clientJar);
                            Process p2 = pb2.start();
                            ((JTextArea)co_area).append("Client started... wait to finish...");

                            boolean exited = false;
                            long time_elapsed = 0;
                            long start_time = System.currentTimeMillis();
                            while (!exited && time_elapsed < TIMEOUT_LIMIT) {
                                try {
                                    int status = p2.exitValue();

                                    exited = true;

                                    if (status != 0) {
                                        ((JTextArea)co_area).append("Client exited with error " + status);
                                    } else if (status == 0){
                                        ((JTextArea)co_area).append("Client exited with NO error ");
                                    }
                                } catch (Exception ex) {
                                    time_elapsed = System.currentTimeMillis() - start_time;
                                }

                                if (time_elapsed > TIMEOUT_LIMIT) {
                                    ((JTextArea)co_area).append("Client timeout. ");
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println(ex.getMessage());
                        }
                    }

                }
            });

            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.anchor = GridBagConstraints.WEST;

            // row 1
            gbc.weightx = 0.2;
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(lostRateLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            add(lostRateInput, gbc);

            // row 2
            gbc.weightx = 0.2;
            gbc.gridy = 1;
            gbc.gridx = 0;
            add(outFileLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            add(outFileInput, gbc);

            // row 3
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0.2;
            add(clientJarLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            add(clientJarInput, gbc);

            // row 4
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.weightx = 0.2;
            add(serverJarLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            add(serverJarInput, gbc);

            // row 5
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.weightx = 0.2;
            add(startServer, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            add(sendFile, gbc);

            // row 6
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.weightx = 1;
            add(serverOutputLabel, gbc);

            // row 7
            gbc.gridx = 0;
            gbc.weightx = 1;
            gbc.gridy = 6;
            gbc.gridwidth = 2;
            add(serverOutputArea, gbc);

            // row 8
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.weightx = 1;
            add(clientOutputLabel, gbc);

            // row 9
            gbc.gridx = 0;
            gbc.weightx = 1;
            gbc.gridy = 8;
            gbc.gridwidth = 2;
            add(clientOutputArea, gbc);
        }
    }

    public class MyOutputStream extends OutputStream {
        private JTextArea out;
        private Reader reader;

        public MyOutputStream(JTextArea textArea, InputStream in) throws IOException {
            this.out = textArea;
            reader = new InputStreamReader(in, "UTF-8");
        }

        @Override
        public void write(int i) throws IOException {
            write (new byte [] {(byte)i}, 0, 1);
        }

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException {
            final String text = new String (buffer, offset, length);
            SwingUtilities.invokeLater(new Runnable ()
            {
                @Override
                public void run()
                {
                    out.append (text);
                }
            });
        }

        public void flush() throws IOException {
            if (reader.ready()) {
                char[] chars = new char[1024];
                int n = reader.read(chars);

                String txt = new String(chars, 0, n);

                System.err.print(txt);
            }
        }
    }

        public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GbnGUI();
            }
        });
    }
}
