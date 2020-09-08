package dk.dd.udps;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * @author Dora
 */
public class UDPServer
{
    private static final int serverPort = 7777;
    
    // buffers for the messages - set length according to sockets receive buffer size. 64K max.
    private static byte[] dataIn;
    private static byte[] dataOut;
    private static final int byteArraySize = 128;

    
    // In UDP messages are encapsulated in packages and sent over sockets
    private static DatagramPacket requestPacket;    
    private static DatagramPacket responsePacket;     
    private static DatagramSocket serverSocket;  
    

    public static void main(String[] args) throws Exception
    {   
        String messageIn, messageOut;
        try
        {
            String serverIP = InetAddress.getLocalHost().getHostAddress();
            // Opens socket for accepting requests
            serverSocket = new DatagramSocket(serverPort);

            while(true)
            {
               System.out.println("Server " + serverIP + " running ...");  
               messageIn = receiveRequest();
               if (messageIn.equals("stop")) break;
               else if (messageIn.contains("-image"))
               {
                   messageOut = receiveImage(Integer.valueOf(messageIn.substring(messageIn.indexOf(',')+1)));
               }
               else // default
                messageOut = processRequest(messageIn);
               sendResponse(messageOut);
            } 
        }
        catch(Exception e)
        {
            System.out.println(" Connection fails: " + e); 
        }
        finally
        {       
            serverSocket.close();
            System.out.println("Server port closed");
        }
    }

    private static String receiveImage(Integer numPackets)  throws IOException
    {
        ArrayList<byte[]> file = new ArrayList<>(numPackets);
        dataIn = new byte[byteArraySize];
        for(int i = 0; i < numPackets; i++) {
            requestPacket = new DatagramPacket(dataIn, dataIn.length);

            serverSocket.receive(requestPacket); // blocks until received.
            file.add(requestPacket.getData());

        }
        return file.size() + " packets received";
    }

    public static String receiveRequest() throws IOException
    {
          dataIn = new byte[serverSocket.getReceiveBufferSize()];
          requestPacket = new DatagramPacket(dataIn, dataIn.length);

          serverSocket.receive(requestPacket);
          String message = new String(requestPacket.getData(), 0, requestPacket.getLength());
          System.out.println("Request: " + message);   
          return message;
    }
    
    public static String processRequest(String message)
    {
        return message.toUpperCase();
    }
    
    public static void sendResponse(String message) throws IOException
    {
        InetAddress clientIP;
        int clientPort;
        dataOut = new byte[serverSocket.getReceiveBufferSize()];
        clientIP = requestPacket.getAddress();
        clientPort = requestPacket.getPort();
        System.out.println("Client port: " + clientPort);
        System.out.println("Response: " + message); 
        dataOut = message.getBytes();
        responsePacket = new DatagramPacket(dataOut, dataOut.length, clientIP, clientPort);
        serverSocket.send(responsePacket);
        System.out.println("Message sent back " + message);
    }    
}
