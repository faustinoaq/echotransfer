import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TIMEServer {
    public static void main(String[] args) throws IOException {

        String MyIP;
        // Getting My Local IP
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("1.1.1.1"), 10002);
            MyIP = socket.getLocalAddress().getHostAddress();
        }

        // Local Port
        int portNumber = 9000;

        System.out.println("TCP and UDP TIME Server on " + MyIP + ":" + portNumber);

        // TCP TIME Server
        Thread thread = new Thread("TCP TIME Server") {
            public void run() {
                try {
                    ServerSocket tcpSocket = new ServerSocket(portNumber, 1, InetAddress.getByName(MyIP));
                    while (tcpSocket.isBound()) {
                        Socket clientSocket = tcpSocket.accept();
                        PrintWriter out =
                                new PrintWriter(clientSocket.getOutputStream(), true);
                        String dateTimeTCP;
                        dateTimeTCP = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new Date());
                        out.println(dateTimeTCP);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        thread.start();

        // UDP TIME Server
        DatagramSocket udpSocket = new DatagramSocket(portNumber + 1);

        while (udpSocket.isBound()) {
            try {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                udpSocket.receive(packet);
                String ip = packet.getAddress().getHostAddress();
                String dateTimeUDP;
                dateTimeUDP = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new Date());
                DatagramPacket timePacket = new DatagramPacket(
                        dateTimeUDP.getBytes(), dateTimeUDP.getBytes().length, InetAddress.getByName(ip), portNumber + 1);
                udpSocket.send(timePacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
