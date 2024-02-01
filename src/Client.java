import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private boolean close;

    @Override
    public void run() {

        try {
            client = new Socket("192.168.0.101",6969);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String inWriter;
            while((inWriter = in.readLine()) != null) System.out.println(inWriter);

        } catch (IOException e) {
            shutDown();
        }
    }

    private void shutDown() {
        close = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) client.close();
        } catch (IOException e) {
            // nothing here
        }
    }


    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

                while (!close) {
                    String message = inReader.readLine();
                    if (message.startsWith("/quit")) {
                        out.println(message);
                        inReader.close();
                        shutDown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutDown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
