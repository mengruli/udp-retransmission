import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;

import model.GbnPacket;

import static java.lang.System.exit;

public class GbnServer {

    public static void main(String[] args) {
	    if (args.length < 4 || !args[0].equals("--lost") || !args[2].equals("--output")) {
            System.err.println("Illegal arguments! Usage: java GbnServer --lost <lost rate> --output <output filename>");
            exit(1);
        }

        int port = 55055;//Integer.parseInt(args[1]);
	    String filename = args[3] != null ?
                args[3] : "/home/mengruli/Documents/teo-repo/udp-retransmission/results/COSC635_P2_DataReceived.txt";

	    int lost_rate = Integer.parseInt(args[1]);

        try {

            RequestHandler handler = new RequestHandler(port, filename, lost_rate);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("Shutdown signaled...");
                    handler.close();
                }
            });

            handler.start();

        } catch (IOException ex) {
            System.err.println(String.format("Error in creating RequestHandler: %d", ex.getMessage()));
        }

    }

    public static class RequestHandler extends Thread {
        private DatagramSocket socket;
        private FileOutputStream os;
        private int lostRate;
        private short expectedSeq;

        public RequestHandler(int port, String filename, int lostRate) throws IOException{
            socket = new DatagramSocket(port);
            File file = new File(filename);
            file.createNewFile();
            this.os = new FileOutputStream(file, false);
            this.lostRate = lostRate;
        }

        public void run() {
            System.out.println("RequestHanler is running at localhost:55055 with lost rate " + lostRate + " %");

            while(true) {

                try {
                    ByteBuffer payload = ByteBuffer.allocate(GbnPacket.BUFSIZE);
                    ByteBuffer header = ByteBuffer.allocate(8);
                    ByteBuffer packet = ByteBuffer.allocate(GbnPacket.BUFSIZE+8);

                    // start receiving packets
                    DatagramPacket receivedPacket = new DatagramPacket(packet.array(), packet.array().length);
                    socket.receive(receivedPacket);

                    // retrieve packet data
                    GbnPacket.printByteBuffer(packet);
                    short seq = packet.getShort(0);
//                    isLast = packet.getShort(2) == 0 ? false : true;
                    char type = (char)packet.getShort(4);
                    short len = packet.getShort(6);

                    if (len < 0) {
                        // if you want the server continue serving other clients, change it to continue;
                        System.out.println("Done Writing to file!");
                        break;
                    }

                    // retrieve sender information
                    InetAddress address = receivedPacket.getAddress();
                    int port = receivedPacket.getPort();

                    // check whether to simulate packet loss
                    Random rand = new Random(System.currentTimeMillis());
                    int rn = rand.nextInt(100); // [0, 100)

                    if (rn < lostRate) { // lost
                        System.out.println(String.format("Lost packet for seq # = %d", seq));

                        // Acknowledge loss
                        char message_type = 'L';
                        header.putShort(0, expectedSeq);
                        header.putShort(2, GbnPacket.WINDOW_SIZE);
                        header.putShort(4, (short)message_type);
                        DatagramPacket packetToSend = new DatagramPacket(header.array(), header.array().length, address, port);
                        socket.send(packetToSend);
                    }
                    else if (seq != expectedSeq) { // not an expected packet, simply drop it
                        System.out.println(String.format("Expected seq# <> received seq#! Simply drop it...", seq));
                    }
                    else if (seq == expectedSeq){
                        // save to file
                        payload.put(packet.array(), 8, len);
                        writeToFile(payload.array(), len);

                        // set expected
                        expectedSeq = (short) (expectedSeq + 1);

                        // ack received
                        char message_type = 'A';
                        header.putShort(0, expectedSeq);
                        header.putShort(2, GbnPacket.WINDOW_SIZE);
                        header.putShort(4, (short)message_type);
                        DatagramPacket packetToSend = new DatagramPacket(header.array(), header.array().length, address, port);
                        socket.send(packetToSend);
                    }


//                    System.out.println(String.format("Sent to Client at %s: %d", address.getHostAddress(), port));
                } catch (IOException ex) {
                    System.out.println(String.format("Error in RequestHandler.run()", ex.getMessage()));
                }

//                try {
//
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    exit(1);
//                }
            }
        }

        public void close() {
            this.socket.close();
            try {
                this.os.close();
            } catch (IOException ex) {
                System.err.println(String.format("Error when closing handler: %d", ex.getMessage()));
            }
        }

        private void writeToFile(byte[] buf, int len) throws IOException{
            this.os.write(buf, 0, len);
        }

    }
}
