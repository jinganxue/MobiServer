/**
 * @author XQY
 */
import java.io.*;
import java.net.*;

class UDPServerUplink {
	static int port = 15003;
    public static void main(String[] args)throws IOException{
    	if(args.length != 1){
			System.out.println("Usage: UDPServerUplink time(min)");
			System.exit(0);
		}
    	int measureTime = Integer.valueOf(args[0]);
    	long i = 0;
        DatagramSocket server = new DatagramSocket(port);
        System.out.println("Server is listening to port " + port);
        byte[] recvBuf = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        server.receive(recvPacket);
    	long start = System.currentTimeMillis();
        String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
        System.out.println(i++ + " " + recvStr.length());
        while (true) {
        	server.receive(recvPacket);
            recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
            System.out.println(i++ + " " + recvStr.length());
            long now = System.currentTimeMillis() - start;
            if (now > Long.valueOf(measureTime)*60000) {
            	System.out.println(now/1000);
				break;
			}
		}      
        server.close();
    }
}