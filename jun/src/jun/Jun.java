package jun;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.util.regex.Pattern;

import exception.ApplicationException;
import exception.FileLockException;
import exception.SystemException;

/**
 * Class to block th second instance of an application
 * @author Andrea Serra
 *
 */
public class Jun {
	private final String FILE_NAME = "single.instance.app";
	private static Jun jun;

	/* CONSTRUCTOR */
	private Jun() {
	}

	/* SINGLETON */
	public static Jun getInstance() {
		return jun = jun == null ? new Jun() : jun;
	}

	/* metodo per provare bloccare l'istanza */
	/**
	 * method to try to lock a new instance
	 * @param path of lock file
	 * @throws IOException
	 * @throws ApplicationException
	 * @throws SystemException
	 */
	public void tryLock(String path) throws IOException, ApplicationException, SystemException {
		File file = new File(path);
		if (!file.exists()) file.createNewFile();

		RandomAccessFile raf = null;
		if (file.length() > 0) {
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			String pidRead = raf.readLine();
			if (JunSystem.getInstance().pidIsJavaRunning(pidRead)) {
				raf.close();
				throw new ApplicationException("The application is already running");
			}
			file.delete();
			file.createNewFile();
		}

		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(0);
			raf.writeBytes(JunSystem.getInstance().getProcessIdString());
			file.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) raf.close();
		}
	}

	/* metodo per sbloccare l'istanza */
	/**
	 * method to unlock the instance
	 * @param path of lock file
	 */
	public void unlock(String path) {
		File file = new File(path);
		file.delete();
	}

	/* metodo per forzare l'istanza */
	/**
	 * method to force the lock of instance
	 * @param path of lock file
	 * @throws IOException
	 * @throws FileLockException
	 */
	public void forceLock(String path) throws IOException, FileLockException {
		unlock(path);
		File file = new File(path);
		if (file.exists()) {
			String msg = "Unable to delete the lock file: '" + file.getAbsolutePath() + "'";
			throw new FileLockException(msg);
		}

		file.createNewFile();

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(0);
			raf.writeBytes(JunSystem.getInstance().getProcessIdString());
			file.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) raf.close();
		}
	}

	/* metodo per provare bloccare l'istanza */
	/**
	 * method to try to lock a new instance
	 * @throws IOException
	 * @throws ApplicationException
	 * @throws SystemException
	 */
	public void tryLock() throws IOException, ApplicationException, SystemException {
		tryLock(FILE_NAME);
	}

	/**
	 * method to unlock the instance
	 */
	public void unlock() {
		unlock(FILE_NAME);
	}

	/* metodo per forzare l'istanza */
	/**
	 * method to force the lock of instance
	 * @throws IOException
	 * @throws FileLockException
	 */
	public void forceLock() throws IOException, FileLockException {
		forceLock(FILE_NAME);
	}
}

/**
 * Class for system interaction
 * @author Andrea Serra
 *
 */
class JunSystem {
	private static JunSystem system;
	private final String OS_NAME = System.getProperty("os.name").toLowerCase();

	/* CONSTRUCTOR */
	private JunSystem() {
	}

	/* SINGLETON */
	protected static JunSystem getInstance() {
		return (system = (system == null) ? new JunSystem() : system);
	}

	/* metodo che controlla se il pid trovato nel file e' in esecuzione ed e' una jvm */
	/**
	 * method that check if pid proccess is a Java Application and it's running
	 * @param pid of proccess
	 * @return true if running and is Java Application, else false
	 * @throws SystemException
	 */
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
			throw new SystemException("Error while searching for PID.\n"
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
	/**
	 * method that get a pid of application 
	 * @return Integer pid of app
	 */
	protected int getProcessId() {
		return Integer.valueOf(getProcessIdString());
	}

	/* metodo che ritorna pid string */
	/**
	 * method that get a pid of application
	 * @return String pid of app
	 */
	protected String getProcessIdString() {
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}
}
