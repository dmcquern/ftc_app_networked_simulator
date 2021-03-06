package com.ftdi.j2xx;

import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

abstract public class FT_Device
{
    private static final String TAG = "FTDI_Device::";
    D2xxManager.FtDeviceInfoListNode mDeviceInfoNode;
    String mFT_DeviceDescription;
    String mySerialNumber;
    int mMotor1Encoder;  // Simulate motor 1 encoder.  Increment each write.
    int mMotor2Encoder;  // Simulate motor 2 encoder.  Increment each write.
    double mMotor1TotalError;
    double mMotor2TotalError;
    long mTimeInMilliseconds=0;
    long mOldTimeInMilliseconds=0;
    long mDeltaWriteTime=0;

    NetworkManager mNetworkManager;
    LinkedBlockingQueue<byte[]> mWriteToPcQueue;
    LinkedBlockingQueue<byte[]> mReadFromPcQueue;

    protected final byte[] mCurrentStateBuffer = new byte[208];

    int mPacketCount=0;

    // Queue used to pass packets between writes and reads in the onboard simulator.
    // Read and Writes come from the ftc_app when it thinks it is talking to the
    // FTDI driver.
    protected final ConcurrentLinkedQueue<CacheWriteRecord> readQueue = new ConcurrentLinkedQueue<>();
    protected volatile boolean writeLocked = false;

    public FT_Device(String serialNumber, String description, String ipAddress, int port)
    {
        int i;
        mDeviceInfoNode = new D2xxManager.FtDeviceInfoListNode();

        mDeviceInfoNode.serialNumber = serialNumber;
        mDeviceInfoNode.description = description;
        mFT_DeviceDescription = description;  // for use in log

        mNetworkManager = new NetworkManager(ipAddress, port);
        mReadFromPcQueue = mNetworkManager.getReadFromPcQueue();
        mWriteToPcQueue = mNetworkManager.getWriteToPcQueue();
    }


    public synchronized void close()
    {

    }

    private int getPacketFromPC(byte[] data, int length, long wait_ms) {
        int rc = 0;
        byte[] packet;

        try {
            packet = mReadFromPcQueue.poll(wait_ms, TimeUnit.MILLISECONDS);

            // If timed out waiting for packet then return the last packet that was read
            if (packet==null) {
                System.arraycopy(mCurrentStateBuffer, 0, data, 0, length);
                rc = length;
            } else {
                System.arraycopy(packet, 0, data, 0, length);                   // return the packet
                System.arraycopy(packet, 0, mCurrentStateBuffer, 0, length);    // Save in case we lose a packet
                rc = length;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rc;
    }

    protected void sendPacketToPC(byte[] data, int start, int length) {
        byte[] tempBytes = new byte[length];
        System.arraycopy(data, start, tempBytes, 0, length);
        tempBytes[1] =(byte)mPacketCount;   // Add a counter so we can see lost packets
        mPacketCount++;
        mWriteToPcQueue.add(tempBytes);
    }

    public int read(byte[] data, int length, long wait_ms)
    {
        int rc = 0;
        Object localObject1;
        String logString[];

        if (length <= 0) {
            return -2;
        }


        // Check onboard read queue and see if we have a override
        // Use this packet instead of reading one from the network
        if (!this.readQueue.isEmpty()) {
            localObject1 = this.readQueue.poll();
            if (localObject1 == null)
                return rc;

            System.arraycopy(((CacheWriteRecord)localObject1).data, 0, data, 0, length);
            rc = length;
        } else {
            rc = getPacketFromPC(data, length, wait_ms);
        }

        //Log.v(mFT_DeviceDescription, "READ(): Buffer len=" + length + " (" + bufferToHexString(data,0,length) + ")");

        return rc;
    }


    public void queueUpForReadFromPhone(byte[] data) {
        //while (this.writeLocked) Thread.yield();
        this.readQueue.add(new CacheWriteRecord(data));
    }



    protected String bufferToHexString(byte[] data, int start, int length) {
        int i;
        int myStop;
        StringBuilder sb = new StringBuilder();
        //byte [] subArray = Arrays.copyOfRange(a, 4, 6);
        myStop = (length > data.length) ? data.length : length;
        for (i=start; i<start+myStop; i++) {
            sb.append(String.format("%02x ", data[i]));
        }
        return sb.toString();
    }



    /////////////////////   Stub routines from original FTDI Driver  ////////////////////////

    public int read(byte[] data, int length)
    {
        long l=0;
        return read(data, length, l);
    }

    public int read(byte[] data)
    {
        long l=0;
        return read(data, data.length,l);
    }

    abstract public int write(byte[] data, int length, boolean wait);

    public int write(byte[] data, int length)
    {
        return write(data, length, true);
    }

    public int write(byte[] data)
    {
        return write(data, data.length, true);
    }

    public boolean purge(byte flags)
    {
        return true;
    }

    public boolean setBaudRate(int baudRate)
    {
        return true;
    }

    public boolean setDataCharacteristics(byte dataBits, byte stopBits, byte parity)
    {
        return true;
    }

    public boolean setLatencyTimer(byte latency)
    {
        return true;
    }

    protected static class CacheWriteRecord {
        public byte[] data;

        public CacheWriteRecord(byte[] data) {
            this.data = data;
        }
    }
}

