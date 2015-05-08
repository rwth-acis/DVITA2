package i5.dvita.tools.DynamicLDA;
/*
 * Log.java
 *
 * Created on 24. Mai 2005, 11:46
 */



import java.io.File;
import java.io.RandomAccessFile;

/**
 * Speichert Textausgaben innerhalb einer Logdatei. <br>
 * <br>
 */
final public class Log {

	/**
	 * Name der Logdatei.
	 */
	public String m_LogFilename;

	/**
	 * Flag, ob eine Ausgabe auf dem Bildschirm erfolgen soll.
	 */
	public boolean m_LogConsole = false;

	/**
	 * Flag, ob in der Logdatei geschrieben werden soll.
	 */
	public boolean m_LogFile = true;

	/**
	 * Datei, in der die Logeintr�ge gespeichert werden.
	 */
	protected RandomAccessFile m_File;

	/**
	 * Erstellt einen neuen Log-Eintrag.
	 * 
	 * @param logString
	 *            String f�r den Log-Eintrag
	 */
	public final void log(String logString) {
		if (m_LogConsole)
			System.out.print(logString);
		if (m_LogFile) {
			try {
				m_File.writeBytes(logString);
			} catch (Exception e) {
//				System.out.println("Error: " + e.toString());
			}
		}
	}
	
	public Log(String filename, boolean file, boolean console, boolean deleteExisting) {
		m_LogConsole = console;
		m_LogFile = file;
		m_LogFilename = filename;
		if (file)
			try {
				if(deleteExisting) { 
					File tmp = new File(filename);
					tmp.delete();
					tmp = null;
				}
				m_File = new RandomAccessFile(filename, "rw");
			} catch (Exception e) {
//				System.out
//						.println("Error: Cannot create RandomAccessFile from '"
//								+ filename + "'.");
//				System.out.println(e.toString());
				m_LogFile = false;
			}
		else
			assert filename == null;
	}

	/**
	 * Erzeugt eine neue Instanz von Log.
	 * 
	 * @param filename
	 *            Name der Logdatei
	 */
	public Log(String filename, boolean file, boolean console) {
		m_LogConsole = console;
		m_LogFile = file;
		m_LogFilename = filename;
		if (file)
			try {
				m_File = new RandomAccessFile(filename, "rw");
			} catch (Exception e) {
//				System.out
//						.println("Error: Cannot create RandomAccessFile from '"
//								+ filename + "'.");
//				System.out.println(e.toString());
				m_LogFile = false;
			}
		else
			assert filename == null;
	}

	/**
	 * Schliesst die Logdatei.
	 * 
	 * @throws Exception
	 *             IOException beim Schliessen der Datei
	 */
	public void close() throws Exception {
		m_File.close();
	}
}
