/**
 * @author XQY
 */
import java.io.*;
import java.net.*;

class UDPServerDownlink {
	static int port = 15004;
	static int measureTime = 0;

	static String sendStr2 = "";
	static byte[] sendBuf2;
	
    public static void main(String[] args)throws IOException{
    	if(args.length != 1) {
			System.out.println("Usage: UDPServerDownlink time(min)");
			System.exit(0);
		}
    	measureTime = Integer.valueOf(args[0]);
    	final DatagramSocket server = new DatagramSocket(port);
    	System.out.println("Server is listening to port " + port);
        byte[] recvBuf = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        server.receive(recvPacket);
        String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
        System.out.println("Got from client:" + recvStr);
              
        final int port = recvPacket.getPort();
        final InetAddress addr = recvPacket.getAddress();
//        int bufLen = 1 * 1024;				
//    	String sendStr = "";
//		for (int j = 0; j < bufLen; j++)
//			sendStr += '1';
//		byte[] sendBuf;
//        sendBuf = sendStr.getBytes();
        int bufLen = 1 * (1024-64);// MTU
    	sendStr2 = "";
		for (int j = 0; j < bufLen; j++)
			sendStr2 += ',';
		String tmp = String.format("%064d", 0);
		tmp = tmp + sendStr2;
		sendBuf2 = tmp.getBytes();
        final long start = System.currentTimeMillis();
        
        int i = 1;
		while (true) {								
			String t = String.format("%064d", i);
			i++;
			t = t + sendStr2;
			sendBuf2 = t.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendBuf2, sendBuf2.length, addr, port);
            try {
				server.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            long now = System.currentTimeMillis() - start;
            if (now > Long.valueOf(measureTime*60000)) {
				break;
			}
		}					

        server.close();
    }
}