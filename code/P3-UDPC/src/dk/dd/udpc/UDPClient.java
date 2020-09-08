package dk.dd.udpc;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Dora Di
 */
public class UDPClient 
{
    private static final int serverPort = 7777;
       
    // buffers for the messages
    public static String message;
    private static byte[] dataIn = new byte[256];
    private static byte[] dataOut = new byte[256];  
    
    // In UDP messages are encapsulated in packages and sent over sockets
    private static DatagramPacket requestPacket;    
    private static DatagramPacket responsePacket;  
    private static DatagramSocket clientSocket;
    
    public static void main(String[] args) throws IOException
    {
        // Enter server's IP address as a parameter from Run/Edit Configuration/Application/Program Arguments
        clientSocket = new DatagramSocket(); 
        InetAddress serverIP = InetAddress.getByName(args[0]);
        System.out.println(serverIP);

        Scanner scan = new Scanner(System.in);
        System.out.println("Type message or write -image to send an image: ");

        while((message = scan.nextLine()) != null)
        {
            if (message.length() >=6 && "-image".equals(message.substring(0,6)))
                sendImageRequest(serverIP);
            else
                sendRequest(serverIP);
            receiveResponse();
        }
        clientSocket.close(); 
    }
    public static void sendImageRequest(InetAddress serverIP) throws IOException
    {
        int bytesInPacket = (int)Math.pow(2,13); // 8192
        byte[] file = Files.readAllBytes(Path.of("src/64k-Board.jpg"));
        // Q&D UDP has max 65536 bytes minus header bytes. Let's split into byte[8192]'s
        // tell server we are sending numerous datagrams to combine.
        int noOfDatagrams = (int) Math.ceil(file.length / (float)bytesInPacket);
        System.out.println("Sending file in " + noOfDatagrams + " chunks...");

        // alert server how many packets to expect.
        String initialMessage = "-image," + noOfDatagrams;
        dataOut = initialMessage.getBytes();
        doSendRequest(dataOut, serverIP);

        // iterate through file, send 128 bytes at a time.
        int i = 0;
        byte[] chunk;
        while(i < file.length)
        {
            // set chunk size to 128 or remaining no of bytes.
            chunk = new byte[file.length-i >= bytesInPacket ? bytesInPacket : file.length-i];

            for(int x = 0; x < chunk.length; x++)
            {
                chunk[x] = file[i+x];
            }
            // send chunk
            doSendRequest(chunk, serverIP);
            i += bytesInPacket;
        }
    }

    public static void sendRequest(InetAddress serverIP) throws IOException
    {
        //clientSocket = new DatagramSocket();
        // create byte arr to fit image bytes.
        dataOut = new byte[message.getBytes().length];
        System.out.println("sending " + dataOut.length + " bytes");

        dataOut = message.getBytes();
        doSendRequest(dataOut, serverIP);
    }

    // private helper to follow DRY
    private static void doSendRequest(byte[] data, InetAddress serverIP) throws IOException
    {
        System.out.println("sending " + data.length + " bytes");
        requestPacket = new DatagramPacket(data, data.length, serverIP, serverPort);
        clientSocket.send(requestPacket);
    }
    
    public static void receiveResponse() throws IOException
    {
        //clientSocket = new DatagramSocket();
        responsePacket = new DatagramPacket(dataIn, dataIn.length);
        clientSocket.receive(responsePacket);
        String message = new String(responsePacket.getData(), 0, responsePacket.getLength());       
        System.out.println("Response from Server: " + message);      
    }    
}
