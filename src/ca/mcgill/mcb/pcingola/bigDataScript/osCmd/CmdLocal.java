package ca.mcgill.mcb.pcingola.bigDataScript.osCmd;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import ca.mcgill.mcb.pcingola.bigDataScript.executioner.ExecutionerLocal;
import ca.mcgill.mcb.pcingola.bigDataScript.util.Gpr;

/**
 * Execute a command in a local computer.
 * I.e.: Launches an 'OS command' (e.g. "ls", "dir")
 * 
 * Note: Launching a system command in Java is not trivial, we need to start 2 threads that read STDOUT and STDERR of
 * the process, otherwise it will block (actually it may even cause a deadlock)
 * 
 * References: 
 * 		http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=1
 * 		http://kylecartmell.com/?p=9
 * 		http://www.kylecartmell.com/public_files/ProcessTimeoutExample.java
 * 
 * WARNING: In this case, we assume the child process takes care of redirections and we DO NOT take care of STDIN, STDOUT or timeout
 * 
 * @author pcingola
 */
public class CmdLocal extends Cmd {

	public static final int MAX_PID_LINE_LENGTH = 1024; // A 'PID line' should not be longer than this...

	protected Process process; // Java process (the one that actually executes our command)
	protected boolean readPid;
	protected String pid; // Only if child process reports PID and readPid is true
	protected String feedStdin; // Feed this string to stdin when the process starts 

	public CmdLocal(String id, String args[]) {
		super(id, args);
	}

	@Override
	protected void execCmd() throws Exception {
		// Wait for the process to finish and store exit value
		exitValue = process.waitFor();
	}

	@Override
	protected void execPrepare() throws Exception {
		// Build process and start it
		ProcessBuilder pb = new ProcessBuilder(commandArgs);
		if (debug) {
			StringBuilder cmdsb = new StringBuilder();
			for (String arg : commandArgs)
				cmdsb.append(" " + arg);
			Gpr.debug("Executing: " + cmdsb);
		}
		process = pb.start();

		feedStdin(); // Feed something to STDIN?
		readPid(); // Child process prints PID to STDOUT? Read it
	}

	/**
	 * Feed a string to process' STDIN
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected void feedStdin() throws InterruptedException, IOException {
		if ((feedStdin == null) || feedStdin.isEmpty()) return; // Nothing to do

		// Wait for STDOUT to become available
		while (getStdin() == null)
			sleep(1);

		// Write and close STDIN
		BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(getStdin()));
		bos.write(feedStdin);
		bos.flush();
		bos.close();
	}

	public String getPid() {
		return pid;
	}

	public InputStream getStderr() {
		if (process == null) return null;
		return process.getErrorStream();
	}

	public OutputStream getStdin() {
		if (process == null) return null;
		return process.getOutputStream();
	}

	public InputStream getStdout() {
		if (process == null) return null;
		return process.getInputStream();
	}

	/**
	 * Send a kill signal using 'bds kill'
	 * @param pid
	 */
	protected void killBds(int pid) {
		// Create arguments
		ArrayList<String> args = new ArrayList<String>();

		// Add command and arguments
		if ((executioner != null) && (task != null)) {
			for (String arg : executioner.osKillCommand(task))
				args.add(arg);
		} else {
			for (String arg : ExecutionerLocal.LOCAL_KILL_COMMAND)
				args.add(arg);
			args.add("" + pid);
		}

		// Add tasks's pis
		if (task != null) args.add(task.getPid());
		else args.add("" + pid);

		// Execute kill command
		try {
			// Execute 'bds kill pid'
			Process proc = Runtime.getRuntime().exec(args.toArray(ARGS_ARRAY_TYPE));
			if (debug) Gpr.debug("Executing kill process for pid " + pid);
			int exitVal = proc.waitFor();
			if (exitVal != 0) System.err.println("Error killing process " + pid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void killCmd() {
		if (process != null) {
			// Do we have a PID number? Kill using that number
			int pidNum = Gpr.parseIntSafe(pid);
			if (pidNum > 0) killBds(pidNum);

			error += "Killed!\n";
			if (debug) Gpr.debug("Killing process " + id);
			process.destroy();
		}
	}

	/**
	 * Read child process pid (or cluster job id)
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected void readPid() throws InterruptedException, IOException {
		// Nothing to do?
		pid = "";
		if (!readPid) return;

		// Wait for STDOUT to become available
		while (getStdout() == null)
			sleep(1);

		// Read one line.
		// Note: We want to limit the damage if something goes wrong, so we use a small buffer...
		StringBuilder sb = new StringBuilder();
		while (pid.isEmpty()) {
			for (int i = 0; true; i++) {
				char ch = (char) getStdout().read();
				if (ch == '\n') break;
				sb.append(ch);
				if (i >= MAX_PID_LINE_LENGTH) //
					throw new RuntimeException("PID line too long!\n" + sb.toString());
			}

			// Parse line. Format "PID \t pidNum \t childPidNum"
			if (debug) Gpr.debug("Got line: '" + sb + "'");

			pid = sb.toString().trim(); // Ignore empty lines
		}

		if (task != null) task.setPid(pid); // Update task's pid
	}

	public void setReadPid(boolean readPid) {
		this.readPid = readPid;
	}

	public void setStdin(String stdin) {
		feedStdin = stdin;
	}
}