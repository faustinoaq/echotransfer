import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class FILEClient {

    private Socket socket;
    private MyFileReader fileReader;

    private void sendFile(String fileName, int tbytes, String continuar) throws IOException {
        fileReader = new MyFileReader();
        byte[] data = fileReader.readFile(fileName);
        int contador = 0;
        for (byte i : data) {
            contador++;
            if (continuar.equals("s")) {
                if (contador > tbytes) {
                    this.socket.getOutputStream().write(i);
                }
            } else {
                this.socket.getOutputStream().write(i);
            }
        }

        System.out.println("Enviado " + (contador - tbytes) + " bytes al server");
    }

    private FILEClient() throws Exception {
        int portNumber = 9000;

        BufferedReader stdIn =
                new BufferedReader(
                        new InputStreamReader(System.in));
        String ip;
        String file;
        System.out.println("FILE Client el localizacion " + System.getProperty("user.dir"));
        System.out.print("IP: ");
        ip = stdIn.readLine();
        System.out.print("Archivo: ");
        file = stdIn.readLine();

        try {
            this.socket = new Socket(ip, portNumber);
        } catch (Exception exception) {
            System.out.println("Error al conectarse con server");
        }

        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
        fileReader = new MyFileReader();
        byte[] data = fileReader.readFile(file);
        String dateTime;
        dateTime = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.getDefault()).format(new Date());
        out.println(file + " " + data.length + " " + dateTime);
        out.flush();

        BufferedReader in =
                new BufferedReader(
                        new InputStreamReader(this.socket.getInputStream()));
        String response = in.readLine();
        Scanner scan = new Scanner(response);
        int tbytes = scan.nextInt();
        System.out.println("Transferencia aceptada!\nTotal de bytes temporales: " + tbytes);
        String continuar = "n";
        if (tbytes > 0) {
            System.out.println("Faltan " + (data.length - tbytes) + " bytes");
            System.out.print("Continuar transferencia anterior (s/n): ");
            continuar = stdIn.readLine();
            out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(continuar);
            out.flush();
            if (continuar.equals("s")) {
                System.out.println("Comenzando desde byte " + tbytes);
            }
        }

        System.out.println("Enviando archivo " + file + "...");
        sendFile(file, tbytes, continuar);
        System.out.println("Termino de enviarse archivo correctamente!");
        this.socket.close();
    }

    public static void main(String[] args) throws Exception {
        new FILEClient();
    }
}
