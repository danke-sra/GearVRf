package org.gearvrf.debug;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gearvrf.GVRContext;
import org.gearvrf.debug.cli.Shell;

/**
 * Debug server provides a command line interface (CLI) for GVRf
 * framework. <p>
 *
 * The commands are defined in {@link ShellCommandHandler}. For example,
 * you can use the command 'lua' to enter lua mode, and the command 'js'
 * to enter Javascript mode. While in the script mode, you can access the
 * GVRContext object using the variable 'gvrf'. Type 'exit' to exit from
 * the script shell, or the top-level shell. <p>
 *
 * To connect to the debug server, you can use telnet from Linux, or
 * putty from Windows. If Windows environment, you would need to configure
 * the terminal to add a CR (\r) for each LF (\n) for proper display.
 */
public class DebugServer implements Runnable {
    public static final boolean RUN_DEBUG_SERVER = true;
    private static final int DEFAULT_DEBUG_PORT = 1645;
    private static final int NUM_CLIENTS = 2;

    private static final String PROMPT = "gvrf";
    private static final String APP_NAME = "GearVR Framework";

    // Need a way to stop the program...
    private boolean shuttingDown;
    private ServerSocket serverSocket;

    private GVRContext gvrContext;
    int port;
    int maxClients;

    class DebugConnection implements Callable<Object> {
        private Socket socket;

        public DebugConnection(Socket socket) {
            this.socket = socket;
        }

        @Override
        public Object call() throws Exception {
            PrintStream out = new PrintStream(socket.getOutputStream());
            // Hand over to shell
            Shell shell = ConsoleFactory.createConsoleShell(PROMPT, APP_NAME,
                    new ShellCommandHandler(gvrContext),
                    new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    out, out);
            shell.commandLoop();
            return null;
        }
    }

    /**
     * Constructor.
     *
     * To start a debug server, add the following line to your {@link org.gearvrf.GVRScript#onInit(GVRContext)} method:
     *
     * <pre>
     *     Threads.spawn(new DebugServer(gvrContext));
     * </pre>
     *
     * The default port is 1645, and the default maximum number of connections is 2.
     *
     * @param gvrContext
     *     The {@link GVRContext} object.
     *
     */
    public DebugServer(GVRContext gvrContext) {
        this(gvrContext, DEFAULT_DEBUG_PORT, NUM_CLIENTS);
    }

    /**
     * Constructor.
     *
     * @param gvrContext
     *     The {@link GVRContext} object.
     * @param port
     *     The port to override the default port 1645.
     * @param maxClients
     *     Maximum number of clients.
     */
    public DebugServer(GVRContext gvrContext, int port, int maxClients) {
        this.gvrContext = gvrContext;
        this.port = port;
        this.maxClients = maxClients;
    }

    /**
     * Shuts down the server.
     */
    public void shutdown() {
        shuttingDown = true;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the server.
     */
    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(maxClients);
        try {
            serverSocket = new ServerSocket(port, maxClients);
            while (!shuttingDown) {
                try {
                    Socket socket = serverSocket.accept();
                    executorService.submit(new DebugConnection(socket));
                } catch (SocketException e) {
                    // closed
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (Exception e) {
            }
            executorService.shutdownNow();
        }
    }
}