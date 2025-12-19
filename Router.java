import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

// Router class to manage wifi connections
public class Router {
    private Semaphore semaphore;
    private boolean[] connections;
    private List<String> waitingDevices;
    private Object lock = new Object();
    
    public Router(int maxConnections) {
        semaphore = new Semaphore(maxConnections);
        connections = new boolean[maxConnections];
        waitingDevices = new ArrayList<>();
    }
    
    // method for device to connect
    public int connect(Device device) throws InterruptedException {
        String deviceName = device.getDeviceName();
        String deviceType = device.getDeviceType();
        PrintWriter writer = device.getOutputWriter();
        
        // check if need to wait
        synchronized (lock) {
            if (semaphore.availablePermits() == 0) {
                String waitingMsg = deviceName + "(" + deviceType + ") arrived and waiting";
                System.out.println(waitingMsg);
                writer.println(waitingMsg);
                waitingDevices.add(deviceName);
            }
        }
        
        // wait for available connection
        semaphore.acquire();
        
        // find and assign connection number
        int connectionNumber = -1;
        synchronized (this) {
            for (int i = 0; i < connections.length; i++) {
                if (!connections[i]) {
                    connections[i] = true;
                    connectionNumber = i;
                    break;
                }
            }
            
            waitingDevices.remove(deviceName);
            
            String occupyMsg = "Connection " + (connectionNumber + 1) + ": " + 
                              deviceName + " Occupied";
            System.out.println(occupyMsg);
            writer.println(occupyMsg);
        }
        
        return connectionNumber;
    }
    
    // serve the device
    public synchronized void serve(String deviceName, int connectionNumber, PrintWriter writer) {
        String serveMsg = "Connection " + (connectionNumber + 1) + ": " + 
                        deviceName + " Being Served";
        System.out.println(serveMsg);
        writer.println(serveMsg);
    }
    
    // disconnect device
    public synchronized void disconnect(String deviceName, int connectionNumber, PrintWriter writer) {
        connections[connectionNumber] = false;
        
        String logoutMsg = "Connection " + (connectionNumber + 1) + ": " + 
                          deviceName + " Logged out";
        System.out.println(logoutMsg);
        writer.println(logoutMsg);
        
        semaphore.release();
    }
    
    // main method
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // get user input
        System.out.print("What is number of WI-FI Connections? ");
        int maxConnections = scanner.nextInt();
        
        System.out.print("What is number of devices Clients want to connect? ");
        int totalDevices = scanner.nextInt();
        
        // create output file
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("output.txt");
        } catch (FileNotFoundException e) {
            System.err.println("Error creating output file");
            System.exit(1);
        }
        
        Router router = new Router(maxConnections);
        
        // create devices
        Device[] devices = new Device[totalDevices];
        String[] types = {"mobile", "pc", "tablet"};
        
        for (int i = 0; i < totalDevices; i++) {
            String deviceName = "C" + (i + 1);
            String deviceType = types[i % types.length];
            devices[i] = new Device(deviceName, deviceType, router, writer);
        }
        
        // start all devices
        for (Device device : devices) {
            device.start();
            try {
                Thread.sleep((long) (Math.random() * 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // wait for all to finish
        for (Device device : devices) {
            try {
                device.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("\nAll devices done!");
        writer.close();
        scanner.close();
    }
}