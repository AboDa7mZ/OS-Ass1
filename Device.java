import java.io.PrintWriter;

// Device class represents a client trying to connect
public class Device extends Thread {
    private String deviceName;
    private String deviceType;
    private Router router;
    private PrintWriter outputWriter;
    
    public Device(String deviceName, String deviceType, Router router, PrintWriter outputWriter) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.router = router;
        this.outputWriter = outputWriter;
    }
    
    @Override
    public void run() {
        try {
            // device arrives
            String arrivalMsg = "(" + deviceName + ")(" + deviceType + ") arrived";
            System.out.println(arrivalMsg);
            outputWriter.println(arrivalMsg);
            
            Thread.sleep((long) (Math.random() * 1000));
            
            // try to connect
            int connectionNumber = router.connect(this);
            
            Thread.sleep((long) (Math.random() * 1000));
            
            // get served
            router.serve(deviceName, connectionNumber, outputWriter);
            
            Thread.sleep((long) (Math.random() * 2000));
            
            // log out
            router.disconnect(deviceName, connectionNumber, outputWriter);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public PrintWriter getOutputWriter() {
        return outputWriter;
    }
}
