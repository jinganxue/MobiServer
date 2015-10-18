package thu.mobinet.mobiserver.deploy;
import java.io.*;
import java.net.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class TU {
	static int port = 1601;

	public static void main(String argv[]) throws Exception {
		if (argv.length != 1) {
			System.out.println("Usage: TCPServerUplink interval(s)");
			System.exit(0);
		}

		ServerSocket welcomeSocket = new ServerSocket(port);

		df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS");
		fileName = df.format(new Date());
		System.out.println(fileName + " Server is listening to port " + port);

		writer = new PrintStream(new FileOutputStream(fileName + " uplink.txt"));

		System.out.println("waiting for client......\n");
		writer.print("command: java TCPServerUplink " + argv[0] + "\n");
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
					System.out.println(date + " Uplink has established");
					writer.print(date + " Uplink has established" + "\n");

					String local = "Local "
							+ serverSocket.getLocalAddress().getHostAddress()
							+ " port " + serverSocket.getLocalPort();
					String peer = serverSocket.getRemoteSocketAddress()
							.toString();
					System.out.println(local + " connected to " + peer);
					System.out
							.println("--------------------------split line-----------------------------");
					writer.print(local + " connected to " + peer + "\n");
					writer.print("--------------------------split line-----------------------------\n");

					mInterval = Integer.parseInt(argv[0]) * 1000;

					numF = NumberFormat.getInstance();
					numF.setMaximumFractionDigits(0);

					mTotalLen = 0;
					mLastTotalLen = 0;

					System.out.println("Uplink testing......Server is receiving data from client.");
					writer.print("Uplink testing......Server is receiving data from client.\n");

					int bufLen = 1 * 1024;
					char buf[] = new char[bufLen];

					int currLen = 0;

					BufferedReader inFromClient = new BufferedReader(
							new InputStreamReader(serverSocket.getInputStream()));

					mStartTime = System.currentTimeMillis();
					mLastTime = mStartTime;
					mNextTime = mStartTime + mInterval;

					do {
						currLen = inFromClient.read(buf);
						// currLen = -1 means reaching the end of the stream
						if (currLen == -1)
							break;

						packetTime = System.currentTimeMillis();

						ReportPeriodicBW();

						mTotalLen += currLen;
					} while (true);

					System.out.println("TotalTime	Received	Throughput");
					writer.print("TotalTime	Received	Throughput\n");
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
					str += " Uplink has closed";
					System.out.println(str);
					writer.println(str);

					System.out
							.println("--------------------------split line-----------------------------");
					System.out.println("waiting for client......\n");
					writer.print("--------------------------split line-----------------------------\n");
					writer.print("waiting for client......\n\n");
				} catch (SocketException ex) {
					String str = df.format(new Date());
					System.out.println(str + " Network has disconnected. (Exception)");
					writer.print(str + " Network has disconnected. (Exception)\n");
					ex.printStackTrace();
					System.out
							.println("--------------------------split line-----------------------------");
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
	protected static long mInterval;

	protected static long packetTime;
	protected static long mLastTime;
	protected static long mNextTime;
	protected static long mTotalTime;

	protected static long mTotalLen;
	protected static long mLastTotalLen;

	protected static NumberFormat numF;
	protected static SimpleDateFormat df;
	protected static PrintStream writer;
	protected static String fileName;
}
