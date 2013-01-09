package edu.iris.wss;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class StreamEater implements Runnable  {

	InputStream inputStream;

	boolean     done    = false;

	String      output = null;
	IOException ioException = null;

	public static final Logger logger = Logger.getLogger(StreamEater.class);

	public StreamEater(Process process, InputStream is ) throws Exception {

		if (is == null) {
			throw new Exception("Null InputStream");
		}
		inputStream = is;
		
		// Automatically start
		this.start();
	}

	private void start() {
        // Create a new thread and start it
        Thread thread = new Thread(this, "StreamEater");
        thread.start();
	}
	
	public synchronized String getOutputString() throws IOException {

		// wait until done, then return the string
		if (!done) {
			try {
				wait();
			} catch (InterruptedException e) {
				logger.info("interrupted");
			}
		}
			
		if( ioException != null ) throw ioException;
		return output;
	}

	public void run() {

		// accumulate lines in this buffer
		StringBuffer sb = new StringBuffer();		
		
		byte [] buffer = new byte[1024];
		
		try {
			int nRead;
			while ((nRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
				sb.append(new String(buffer, 0, nRead));
			}
			output = sb.toString();

		} catch(IOException e ) {
			ioException = e;
		} finally {
			synchronized (this) {
				// Set done and notify any waiting threads.  Typically, somebody calling getOutputString()
				done = true;
				notify();
			}
			try{ inputStream.close(); } catch( Exception e) {;}
		}		
	}
}
