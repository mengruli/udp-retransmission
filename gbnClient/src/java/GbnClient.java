import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.GbnPacket;

public class GbnClient {

    public static void main(String[] args) throws IOException, URISyntaxException{

        InputStream in = GbnClient.class.getResourceAsStream("/sample_data.txt");

        if (args.length > 0) {
            String inputFIle = args[0];
            File file = new File(inputFIle);

            // override the default file if there's input file specified
            in = new FileInputStream(file);
        }
        Sender sender = new Sender();

        short read = 0;
        int total_file_read = 0;

        int numPacketSent = 0;
        int numPacketLost = 0;
        long start_time = 0;
        long end_time = 0;

        short seq = 0;
        short window_size = GbnPacket.WINDOW_SIZE;
        char message_type = 'S';
        short last_ack = 0;
        short waiting_for_ack = 0; // smallest not acked #
        Map<Short, ByteBuffer> cachedRequest = new HashMap<>();

        start_time = System.currentTimeMillis();
        while(read >= 0) {

            short outstanding_packet = (short)(seq - waiting_for_ack);

            if (outstanding_packet < GbnPacket.WINDOW_SIZE) {
                ByteBuffer payload = ByteBuffer.allocate(GbnPacket.BUFSIZE);
                ByteBuffer header = ByteBuffer.allocate(8);
                ByteBuffer packet = ByteBuffer.allocate(GbnPacket.BUFSIZE+8);

                // header
                header.putShort(0, seq);
                header.putShort(2, window_size);
                header.putShort(4, (short)message_type);

                // payload
                if (cachedRequest.get(seq) != null) {
                    packet = cachedRequest.get(seq);
//                    System.out.println("Send cached seq # " + seq);
                    sender.send(packet);
                }
                else {
                    read = (short)in.read(payload.array());

                    header.putShort(6, read);

                    packet.put(header.array());
                    System.arraycopy(payload.array(), 0, packet.array(), header.array().length, payload.array().length);
                    cachedRequest.put(seq, packet);
                    total_file_read = total_file_read + read;

//                    System.out.println("Send new seq # " + seq);
                    sender.send(packet);

                    if (read < 0) { // last packet, don't expect to receive anything
                        System.out.println(String.format("Done reading all the file.\n Total bytes read: %d", total_file_read));
                        end_time = System.currentTimeMillis();
                        long time_elapsed = end_time - start_time;

                        System.out.println("Generating Client Stats...");
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("| # Packets Sent | # Packets Lost + | Time Elapsed (ms) |");
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("|        " + numPacketSent +"      |        " + numPacketLost+ "       |     "+ time_elapsed + "        |");
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        break;
                    }
                }

                numPacketSent++;

            } else { // window is full, just wait another around
                continue;
            }

            // receive
            ByteBuffer packet_received = sender.receive();

            ByteBuffer payload_received = ByteBuffer.allocate(GbnPacket.BUFSIZE);
            ByteBuffer header_received = ByteBuffer.allocate(8);
            short seq_acked = packet_received.getShort(0);
            char type = (char)packet_received.getShort(4);

            if (type == 'L') {
                seq = seq_acked;
                waiting_for_ack = (short)(seq_acked + 1);
                numPacketLost++;

            } else if (type == 'A') {
                waiting_for_ack = (short)(seq_acked + 1);
                seq = (short)(seq + 1);
            }

        }
    }

    public static class Sender {
        private DatagramSocket socket;
        private InetAddress address;
        private List<ByteBuffer> sentPackets;

        public Sender() throws IOException{
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
            sentPackets = new ArrayList<ByteBuffer>();
            System.out.println("Starting the UDP client...");
        }

        public void send(ByteBuffer buffer) throws IOException {
            byte[] msg = buffer.array();
            DatagramPacket packet
                    = new DatagramPacket(msg, msg.length, address, 55055);
            socket.send(packet);
        }

        public ByteBuffer receive() throws IOException {

            ByteBuffer packet = ByteBuffer.allocate(GbnPacket.BUFSIZE+8);
            DatagramPacket receivedPacket = new DatagramPacket(packet.array(), packet.array().length);
            socket.receive(receivedPacket);

            return packet;
        }

        public void close() {
            socket.close();
        }

    }

}
