/**
 * @author XQY
 */
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class UDPServerDownlink {
	static int port = 504;
	static int measureTime = 0;

	static String sendStr2 = "";
	static byte[] sendBuf2;
	static PrintStream writer;
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS");
	
    public static void main(String[] args)throws IOException{
    	if(args.length != 1) {
			System.out.println("Usage: UDPServerDownlink time(min)");
			System.exit(0);
		}
    	measureTime = Integer.valueOf(args[0]);
    	final DatagramSocket server = new DatagramSocket(port);
    	System.out.println("Server is listening to port " + port);
    	   	
    	writer = new PrintStream(new FileOutputStream(df.format(new Date()) + " UDPdownlink.txt"));
    	writer.println("command: java UDPServerDownlink " + args[0]);
		writer.println("Server is listening to port " + port);
		writer.println("waiting for client......\n");
    	
        byte[] recvBuf = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        server.receive(recvPacket);
        String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
        System.out.println("Got from client:" + recvStr);
              
        final int port = recvPacket.getPort();
        final InetAddress addr = recvPacket.getAddress();
        int bufLen = 1 * (1024-32);// MTU
    	sendStr2 = "";
		for (int j = 0; j < bufLen; j++)
			sendStr2 += ',';
		String tmp = String.format("%032d", 0);
		tmp = tmp + sendStr2;
		sendBuf2 = tmp.getBytes();
        final long start = System.currentTimeMillis();
        
        int i = 1;
		while (true) {								
			String t = String.format("%032d", i);			
			t = t + sendStr2;
			sendBuf2 = t.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendBuf2, sendBuf2.length, addr, port);
            try {
				server.send(sendPacket);
				Thread.sleep(25);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (i%5 == 0) {
				System.out.println("Server has sent " + i + " packets.");
				writer.println(df.format(new Date()) + " Send: " + i);
			}
            i++;
            long now = System.currentTimeMillis() - start;
            if (now > Long.valueOf(measureTime*60000)) {
				break;
			}
		}					

        server.close();
    }
}