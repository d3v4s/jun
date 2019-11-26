package jun;

import java.io.IOException;

import exception.ApplicationException;
import exception.SystemException;

/**
 * This is an example main class for the implementation of the Jun library
 * For more info read the wiki https://github.com/d3v4s/jun/wiki
 * @author Andrea Serra
 *
 */
public class MyApplication {

	public MyApplication() {
	}

	public static void main(String[] args) {
		// manage the lock with try catch
		try {
			// this try to lock the application
			Jun.getInstance().tryLock();
		} catch (ApplicationException e) {
			// exception raised if an instance
			// of the application is already running
			System.out.println("Application already running!!!");
			System.exit(-1);
		} catch (IOException | SystemException e) {
			// other error
			e.printStackTrace();
			System.exit(-1);
		}

		/* SUCCESSFULLY LOCK */

		// start your app
		MyApplication app = new MyApplication();
		app.startApp();

		// unlock the application and exit
		Jun.getInstance().unlock();
		System.exit(0);
	}

	private void startApp() {
		System.out.println("Application starting");
	}
}
