package it.jun.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.regex.Pattern;

import it.jun.exception.SystemException;

public class JunSystem {
	private static JunSystem system;
	private final String OS_NAME = System.getProperty("os.name").toLowerCase();

	/* costruttore */
	private JunSystem() {
	}

	/* singleton */
	protected static JunSystem getInstance() {
		return (system = (system == null) ? new JunSystem() : system);
	}

	/* metodo che controlla se il pid trovato nel file e' in esecuzione ed e' una jvm */
	protected boolean pidIsJavaRunning(String pid) throws SystemException {
		String cmnd[] = {"ps", "-p", pid, "-o", "comm="};
		String regex = "java";

		if (OS_NAME.contains("win")) {
			cmnd = new String[] {"tasklist", "/FI", "pid eq " + pid};
			regex = ".*java[w]{0,1}\\.exe.*";
		}

		BufferedReader br = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(cmnd);
			pb.redirectErrorStream(true);
			Process prcss = pb.start();
			
			InputStreamReader isr = new  InputStreamReader(prcss.getInputStream());
			br = new BufferedReader(isr);
			String stdo = null;
			while ((stdo = br.readLine()) != null) {
				if (Pattern.matches(regex, stdo.trim())) {
					br.close();
					return true;
				}
			}
			br.close();
		} catch (Exception e) {
			if (br != null)
				try {
					br.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			throw new SystemException("Errore durante la ricerca del PID.\n"
										+ e.getMessage());
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return false;
	}

	/* metodo che ritorna pid int */
	protected int getProcessId() {
		return Integer.valueOf(getProcessIdString());
	}

	/* metodo che ritorna pid string */
	protected String getProcessIdString() {
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}
}
