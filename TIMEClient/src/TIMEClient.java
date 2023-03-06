import java.io.*;
import java.net.*;

public class TIMEClient {
    public static void main(String[] args) throws IOException {
        int portNumber = 9000;
        BufferedReader stdIn =
                new BufferedReader(
                        new InputStreamReader(System.in));
        String ip;
        String protocol;
        System.out.print("ip?: ");
        ip = stdIn.readLine();
        System.out.print("protocol tcp or udp?: ");
        protocol = stdIn.readLine();

        if (protocol.equals("tcp")) {
            Socket socket = new Socket(ip, portNumber);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            try {
                System.out.println("Time TCP: " + in.readLine());
            } catch(Exception e) {
                System.err.println("Error getting time, check TCP server ip and status");
            }
        } else if (protocol.equals("udp")) {
            DatagramSocket udpSocket = new DatagramSocket(portNumber + 1);
            DatagramPacket p = new DatagramPacket("".getBytes(), "".getBytes().length, InetAddress.getByName(ip), portNumber + 1);
            udpSocket.send(p);
            String msg;
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            udpSocket.receive(packet);
            msg = new String(packet.getData()).trim();
            System.out.println("Time UDP: " + msg);
        } else {
            System.out.println("Bad protocol write tcp or udp");
        }
    }
}