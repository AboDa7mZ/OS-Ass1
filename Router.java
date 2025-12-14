import java.util.concurrent.Semaphore;

/**
 * Router Class - Manages Wi-Fi connections using Semaphore
 * 
 * SEMAPHORE EXPLANATION:
 * =====================
 * A Semaphore is a synchronization tool that controls access to a shared resource
 * through the use of permits. In this Router implementation:
 * 
 * 1. The semaphore is initialized with N permits (N = number of allowed connections)
 * 2. When a client wants to connect:
 *    - It calls acquire() to get a permit
 *    - If permits are available, the client gets one and proceeds
 *    - If no permits are available (all N connections occupied), the client BLOCKS and waits
 * 3. When a client logs out:
 *    - It calls release() to return the permit
 *    - This automatically wakes up one waiting client (if any) to acquire the permit
 * 
 * HOW SEMAPHORES LIMIT CONCURRENT CONNECTIONS:
 * ============================================
 * - The semaphore acts as a gatekeeper with exactly N keys (permits)
 * - Only clients holding a key can access the router connection
 * - When all N keys are given out, new clients must wait in line
 * - When a client returns a key (logs out), the next waiting client automatically gets it
 * - This ensures NEVER more than N clients are connected simultaneously
 * 
 * BENEFITS:
 * =========
 * - Thread-safe: No race conditions or inconsistent states
 * - Fair queuing: Waiting clients are served in order (with fair semaphore)
 * - Automatic blocking: No busy-waiting or manual synchronization needed
 * - Simple API: Just acquire() and release()
 */
public class Router {
    // Semaphore to limit concurrent connections
    private final Semaphore semaphore;
    
    // Array to track which connection numbers are occupied
    private final boolean[] connections;
    
    /**
     * Constructor - Initialize the Router with N connections
     * @param maxConnections Number of simultaneous connections allowed (N)
     */
    public Router(int maxConnections) {
        semaphore = new Semaphore(maxConnections);
        connections = new boolean[maxConnections];
    }
    
    /**
     * Client attempts to connect to the router
     * 
     * Key Design: semaphore.acquire() is called OUTSIDE synchronized block
     * This allows multiple threads to wait in the semaphore's queue simultaneously
     * 
     * @param deviceName Name of the device/client (e.g., C1)
     * @return Connection number assigned to the client (0-based index)
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public int connect(String deviceName) throws InterruptedException {
        // Wait if no connections available (multiple threads can wait here)
        semaphore.acquire();

        // Only synchronize the connection assignment part
        int connectionNumber;
        synchronized(this) {
            connectionNumber = -1;
            for (int i = 0; i < connections.length; i++) {
                if (!connections[i]) {
                    connections[i] = true;
                    connectionNumber = i;
                    break;
                }
            }
            System.out.println(
                "Connection " + (connectionNumber + 1) + ": " + deviceName + " Occupied"
            );
        }
        return connectionNumber;
    }

    /**
     * Client is being served
     * 
     * @param deviceName Name of the device/client
     * @param connectionNumber The connection number (0-based index)
     */
    public synchronized void serve(String deviceName, int connectionNumber) {
        System.out.println(
            "Connection " + (connectionNumber + 1) + ": " + deviceName + " Being Served"
        );
    }

    /**
     * Client logs out and disconnects from the router
     * This releases the connection and allows another waiting client to connect
     * 
     * @param deviceName Name of the device/client
     * @param connectionNumber The connection number to release (0-based index)
     */
    public synchronized void disconnect(String deviceName, int connectionNumber) {
        connections[connectionNumber] = false;

        System.out.println(
            "Connection " + (connectionNumber + 1) + ": " + deviceName + " Logged out"
        );

        // Release the permit - allows another waiting client to proceed
        semaphore.release();
    }
}
