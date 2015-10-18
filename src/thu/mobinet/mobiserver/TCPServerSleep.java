package thu.mobinet.mobiserver;
import java.io.*;
import java.net.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class TCPServerSleep {
	static int port = 505;
	static int size = 1024;

	public static void main(String argv[]) throws Exception {
		if (argv.length != 3) {
			System.out.println("Usage: TCPServerFlowLong time(min) interval(s) size(KB)");
			System.exit(0);
		}
		
		size = Integer.valueOf(argv[2]);
		ServerSocket welcomeSocket = new ServerSocket(port);

		df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS");
		fileName = df.format(new Date());
		System.out.println(fileName + " Server is listening to port " + port);

		writer = new PrintStream(new FileOutputStream(fileName + " flowlong.txt"));

		System.out.println("waiting for client......\n");
		writer.print("command: java TCPServerFlow " + argv[0] + " " + argv[1] + " " + argv[2] + "\n");
		writer.println("Server is listening to port " + port);
		writer.print("waiting for client......\n\n");

		while (true) {
			Socket serverSocket = welcomeSocket.accept();
			invoke(serverSocket, argv);
		}
	}

	private static void invoke(final Socket serverSocket, final String argv[])
			throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println();
					writer.println();

					String date = df.format(new Date());
					System.out.println(date + " Downlink has established");
					writer.print(date + " Downlink has established" + "\n");

					String local = "Local "
							+ serverSocket.getLocalAddress().getHostAddress()
							+ " port " + serverSocket.getLocalPort();
					String peer = serverSocket.getRemoteSocketAddress()
							.toString();
					System.out.println(local + " connected to " + peer);
					System.out.println("--------------------------split line-----------------------------");
					writer.print(local + " connected to " + peer + "\n");
					writer.print("--------------------------split line-----------------------------\n");

					measureTime = Integer.parseInt(argv[0]) * 60 * 1000;
					mInterval = Integer.parseInt(argv[1]) * 1000;
					numF = NumberFormat.getInstance();
					numF.setMaximumFractionDigits(0);

					mTotalLen = 0;
					mLastTotalLen = 0;

					System.out.println("Flow testing......Server is sending data to client.");
					writer.print("Flow testing......Server is sending data to client.\n");

					int bufLen = 1 * 1024;
					int currLen = bufLen * 2;
					String buf = "";
					for (int i = 0; i < bufLen; i++)
						buf += '1';

					DataOutputStream outToClient = new DataOutputStream(
							serverSocket.getOutputStream());

					mStartTime = System.currentTimeMillis();
					mEndTime = mStartTime + measureTime;
					mLastTime = mStartTime;
					mNextTime = mStartTime + mInterval;

					do {
						outToClient.writeChars(buf);
						packetTime = System.currentTimeMillis();

						ReportPeriodicBW();

						mTotalLen += currLen;
						
						if (mTotalLen > size * 1024) {
							Thread.sleep(8000);
                            mTotalLen=0;
						}
					} while (packetTime < mEndTime);

					System.out.println("TotalTime	Transfer	Throughput");
					writer.print("TotalTime	Transfer	Throughput\n");
					mTotalTime = packetTime - mStartTime;
					double throughput = (double) mTotalLen * 8
							/ (mTotalTime / 1000) / 1000;
					String rate = numF.format(throughput);
					System.out.println("0-" + mTotalTime / 1000 + " sec "
							+ mTotalLen / 1024 + " KB " + rate + " kbps");
					writer.print("0-" + mTotalTime / 1000 + " sec " + mTotalLen
							/ 1024 + " KB " + rate + " kbps\n");

					serverSocket.close();
					String str = df.format(new Date());
					str += " Downlink has closed";
					System.out.println(str);
					writer.println(str);

					System.out.println("--------------------------split line-----------------------------");
					System.out.println("waiting for client......\n");
					writer.print("--------------------------split line-----------------------------\n");
					writer.print("waiting for client......\n\n");
				} catch (SocketException ex) {
					String str = df.format(new Date());
					System.out.println(str + " Network has disconnected. (Exception)");
					writer.print(str + " Network has disconnected. (Exception)\n");
					ex.printStackTrace();

					System.out.println("--------------------------split line-----------------------------");
					System.out.println("waiting for client......\n");
					writer.print("--------------------------split line-----------------------------\n");
					writer.print("waiting for client......\n\n");
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}).start();

	}

	static void ReportPeriodicBW() throws IOException {
		if (packetTime >= mNextTime) {
			long inBytes = mTotalLen - mLastTotalLen;
			long inStart = mLastTime - mStartTime;
			long inStop = mNextTime - mStartTime;

			// 1KB = 1024B; 1kbps = 1000bps
			double throughput = (double) inBytes * 8 / (mInterval / 1000)
					/ 1000;
			String rate = numF.format(throughput);
			System.out.println(inStart / 1000 + "-" + inStop / 1000 + " sec "
					+ inBytes / 1024 + " KB " + rate + " kbps");
			writer.print(inStart / 1000 + "-" + inStop / 1000 + " sec "
					+ inBytes / 1024 + " KB " + rate + " kbps\n");

			mLastTime = mNextTime;
			mNextTime += mInterval;
			mLastTotalLen = mTotalLen;

			if (packetTime > mNextTime) {
				ReportPeriodicBW();
			}
		}
	}

	protected static long mStartTime;
	protected static long mEndTime;
	protected static long measureTime;
	protected static long mInterval;

	protected static long packetTime;
	protected static long mLastTime;
	protected static long mNextTime;
	protected static long mTotalTime;

	protected static long mTotalLen; 
	protected static long mLastTotalLen;

	protected static NumberFormat numF;
	public static SimpleDateFormat df;
	public static PrintStream writer;
	public static String fileName;
}
