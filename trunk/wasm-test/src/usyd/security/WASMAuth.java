package usyd.security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


public class WASMAuth {

	//Command line test mode
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: java WASMAuth <key> <mode>");
			System.out.println("       mode := I | S");
			System.exit(1);
		}

		int mode = -1;
		String iKey = null, sKey = null;

		if (args[1].equalsIgnoreCase("I")) {
			mode = WASMAuth.IKEY_MODE;
			iKey = args[0];
		} else if (args[1].equalsIgnoreCase("S")) {
			mode = WASMAuth.SKEY_MODE;
			sKey = args[0];
		} else {
			throw new IllegalArgumentException("Mode must be one of I (interim) or S (session)");
		}

		WASMAuth auth = WASMAuth.getAuth(iKey, sKey, mode);
		HashMap attrs = auth.getAttributes();
		for (Iterator it = attrs.keySet().iterator(); it.hasNext();) {
			String key = (String)it.next();
			String value = (String)attrs.get(key);
			System.out.println(key + " : " + value);
		}
	}
	
	/**
	 * @param lines A LinkedList of String objects containing the attributes returned from the session manager.
	 *		These should be of the format: name:value
	 **/
	protected WASMAuth(LinkedList lines) {
		attrs = new HashMap();
		parse(lines);
	}
	
	public HashMap getAttributes() {
		return attrs;
	}

	public String get(String attributeName) {
		return (String)attrs.get(attributeName);
	}
	
	protected void parse(LinkedList lines) {
		String line = null;

		for (Iterator it = lines.iterator(); it.hasNext();) {
			line = (String)it.next();
			if (line == null) continue;
			int sepIdx = line.indexOf(':');
			if (sepIdx == -1)
				throw new RuntimeException("Parse error: " + line);
			attrs.put(line.substring(0, sepIdx).trim(), line.substring(sepIdx + 1).trim());
		}
	}
	

	public static WASMAuth getAuth(String iKey, String sKey, int mode) throws IOException {

		if (mode != IKEY_MODE && mode != SKEY_MODE) { 
			throw new IllegalArgumentException("Invalid mode");
		} else if (mode == IKEY_MODE && (iKey == null || iKey.length() == 0)) {
			throw new IllegalArgumentException("iKey cannot be null or empty in IKEY_MODE");
		} else if (mode == SKEY_MODE && (sKey == null || sKey.length() == 0)) {
			throw new IllegalArgumentException("sKey cannot be null or empty in SKEY_MODE");
		}

		char[] modeCharL = { 'i', 's' };
		char[] modeCharU = { 'I', 'S' };

		Socket socket = new Socket(InetAddress.getByName(smHost), smPort);
		socket.setSoTimeout(10000);

		BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		int msgID = new Random().nextInt();

		socketOut.write("msgID:" + msgID + "\n");
		socketOut.write("appID:" + appID + "\n");
		socketOut.write("appRealm:" + appRealm + "\n");
		socketOut.write("appPassword:" + appPassword + "\n");
		if (mode == IKEY_MODE) {
			socketOut.write("action:whoisIkey\n");
			socketOut.write("iKey:" + iKey + "\n");
			if (sKey != null && sKey.length() > 0) {
				socketOut.write("sKey:" + sKey + "\n");
			}
		} else {
			socketOut.write("action:whoisSkey\n");
			socketOut.write("sKey:" + sKey + "\n");
		}
		socketOut.write("\n");
		socketOut.flush();

		LinkedList lines = new LinkedList();
		String line = socketIn.readLine();
		while (line != null && !line.trim().equals("") && !socket.isClosed()) {
			lines.add(line);
			line = socketIn.readLine();
		}

		try { socket.close(); } catch (IOException ioe) { }

		WASMAuth authen = new WASMAuth(lines);

		if (authen.get("msgID") == null)
			throw new RuntimeException("No msgID returned by wasm");
		if (authen.get("status") == null)
			throw new RuntimeException("No status returned by wasm");

		if (msgID != Integer.parseInt(authen.get("msgID")))
			throw new RuntimeException("Message IDs differ: " + msgID + " != " + authen.get("msgID"));

		if (!(authen.get("status").equalsIgnoreCase("OK") || authen.get("status").equalsIgnoreCase("noSuch" + modeCharU[mode] + "Key")))
			throw new RuntimeException("WASM error: " + authen.get("status") + (authen.get("statusDesc") == null ? "" : ": " + authen.get("statusDesc")));

		return authen;
	}

	protected HashMap attrs = null;

	protected final static String smHost = "wasm-sm-test.ucc.usyd.edu.au";
	protected final static int smPort = 1317;
	protected final static String appID = "mda-intersect";
	protected final static String appRealm = "usyd";
	protected final static String appPassword = "ukmnhka5";

	public final static int IKEY_MODE = 0;
	public final static int SKEY_MODE = 1;
}
