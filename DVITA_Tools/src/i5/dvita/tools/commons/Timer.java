package i5.dvita.tools.commons;


/* Stoppuhr-Klasse zum Messen von Zeiten in Millisekunden.
*/
public class Timer {

	/**
	 * Zeit der letzten Zeitabfrage.
	 */
	private long mLastTime;

	/**
	 * Zähler für die Millisekunden.
	 */
	private long mCounter;

	/**
	 * Flag, ob gezählt wird oder ob "Pause" aktiviert wurde
	 */
	private boolean mCounting;

	/**
	 * Erzeugt einen neuen Timer.
	 */
	public Timer() {
		mCounting = false;
	}

	/**
	 * Startet die Stopuhr.
	 */
	public void start() {
		mLastTime = System.currentTimeMillis();
		mCounting = true;
	}

	/**
	 * Aktualisiert den Counter und gibt die Zeit zurück.
	 * 
	 * @return vergangene Zeit in Millisekunden
	 */
	public long getTime() {
		if (mCounting) {
			long actualTime = System.currentTimeMillis();
			mCounter += actualTime - mLastTime;
			mLastTime = actualTime;
		}

		return mCounter;
	}

	public String toString() {
		long time = stop();
		/*long std = time / 3600000;
		time = time % 3600000;
		long mins = time / 60000;
		time = time % 60000;
		long secs = time / 1000;
		time = time % 1000;*/
		long msecs = time;
		return "time: "/* + std + ":" + mins + ":" + secs + "." */+ msecs;
	}

	/**
	 * Stoppt die Uhr und setzt alle Variablen zurück.
	 * 
	 * @return Die verstrichene Zeit
	 */
	public long stop() {
		long counter = getTime();
		mCounting = false;
		mCounter = 0;
		return counter;
	}

	/**
	 * Pausiert die Uhr. <br>
	 * Zum Beenden der Pause muss die Methode {link start()} aufgerufen werden.
	 */
	public void pause() {
		long actualTime = System.currentTimeMillis();
		mCounter += actualTime - mLastTime;
		mLastTime = actualTime;
		mCounting = false;
	}
}

