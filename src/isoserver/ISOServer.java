/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package isoserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import org.jpos.iso.ISOUtil;

/**
 *
 * @author mega-user
 */
public class ISOServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        // TODO code application logic here
        if (args.length < 1) {
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected (" + socket.getRemoteSocketAddress().toString().replace("/", "") + ")");

                ///InputStream input = socket.getInputStream();
                ///BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                DataInputStream in = new DataInputStream(socket.getInputStream());

                byte[] buffer = new byte[4096];
                byte[] ISObits;
                byte[] msgToSend;

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                String text = "";
                String responseText = "";
                String msg = "";

                DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                do {
                    in.read(buffer, 0, buffer.length);
                    String clientMsg = "";

                    for (byte b : buffer) {
                        clientMsg = clientMsg + b;
                    }

                    String clientMessage = ISOUtil.ebcdicToAscii(buffer);
                    System.out.println(clientMessage);
                    ///text = reader.readLine();
                    //System.out.println("Request from client: " + text);
                    ///String reverseText = new StringBuilder(text).reverse().toString();
                    //writer.println("Response From Server: " + reverseText);
                    
                    clientMessage = clientMessage.substring(2);
                    
                    ISObits = ISOUtil.asciiToEbcdic(clientMessage.replace("200", "210"));

                    for (byte b : ISObits) {
                        msg = msg + b;
                    }

                    ISOServer is = new ISOServer();
                    msgToSend = is.buildHeader(ISObits);
                    dOut.write(msgToSend);
                    //writer.println("Response From Server: " + clientMessage);

                } while (!text.equals("bye"));

                socket.close();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public byte[] buildHeader(byte[] msg) {
        //header
        //String value = eventContext.getMessage().getProperty("header",PropertyScope.SESSION);
        byte[] header = "00".getBytes();
        ByteBuffer byteBuff = ByteBuffer.allocate(msg.length + 2);
        header[0] = (byte) (msg.length / 256);
        header[1] = (byte) (msg.length % 256);
        byteBuff.put(header);
        byteBuff.put(msg);
        byteBuff.compact();
        return byteBuff.array();
    }

}
