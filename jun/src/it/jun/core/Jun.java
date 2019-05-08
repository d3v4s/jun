package it.jun.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import it.jun.exception.ApplicationException;
import it.jun.exception.FileLockException;
import it.jun.exception.SystemException;

public class Jun {
	private static Jun jun;
	private final String FILE_NAME = "single.instance.app";

	/* costruttore */
	private Jun() {
	}

	/* singleton */
	public static Jun getInstance() {
		return jun = jun == null ? new Jun() : jun;
	}

	/* metodo per provare bloccare l'istanza */
	public void tryLock(String path) throws IOException, ApplicationException, SystemException {
		File file = new File(path);
		if (!file.exists())
			file.createNewFile();

		RandomAccessFile raf = null;
		if (file.length() > 0) {
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			String pidRead = raf.readLine();
			if (JunSystem.getInstance().pidIsJavaRunning(pidRead)) {
				raf.close();
				throw new ApplicationException("L'applicazione e' gia' in esecuzione");
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
			if (raf != null)
				raf.close();
		}
	}

	/* metodo per sbloccare l'istanza */
	public void unlock(String path) {
		File file = new File(path);
		file.delete();
	}

	/* metodo per forzare l'istanza */
	public void forceLock(String path) throws IOException, FileLockException {
		unlock(path);
		File file = new File(path);
		if (file.exists()) {
			String msg = "Impossibile eliminare il file di lock: '" + file.getAbsolutePath() + "'";
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
			if (raf != null)
				raf.close();
		}
	}

	/* metodo per provare bloccare l'istanza */
	public void tryLock() throws IOException, ApplicationException, SystemException {
		tryLock(FILE_NAME);
	}

	/* metodo per sbloccare l'istanza */
	public void unlock() {
		unlock(FILE_NAME);
	}

	/* metodo per forzare l'istanza */
	public void forceLock() throws IOException, FileLockException {
		forceLock(FILE_NAME);
	}
}
