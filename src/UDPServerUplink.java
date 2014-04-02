/**
 * @author XQY
 */
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class UDPServerUplink {
	static int port = 503;
	static PrintStream writer;
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS");
	
    public static void main(String[] args)throws IOException{
    	if(args.length != 1){
			System.out.println("Usage: UDPServerUplink time(min)");
			System.exit(0);
		}
    	int measureTime = Integer.valueOf(args[0]);
    	long i = 0;
        DatagramSocket server = new DatagramSocket(port);
        System.out.println("Server is listening to port " + port);
        
        writer = new PrintStream(new FileOutputStream(df.format(new Date()) + " UDPuplink.txt"));
    	writer.println("command: java UDPServerUplink " + args[0]);
		writer.println("Server is listening to port " + port);
		writer.println("waiting for client......\n");
        
        byte[] recvBuf = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        server.receive(recvPacket);
    	long start = System.currentTimeMillis();
        String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
        System.out.println(i++ + " " + recvStr.length());
        while (true) {
        	server.receive(recvPacket);
            recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
            System.out.println("Server has received " + i + " packets: " + recvStr.length());
            if (i%5 == 0) {
            	writer.println(df.format(new Date()) + " Receive: " + i);
			}
            i++;
            long now = System.currentTimeMillis() - start;
            if (now > Long.valueOf(measureTime)*60000) {
            	System.out.println(now/1000);
				break;
			}
		}      
        server.close();
    }
}