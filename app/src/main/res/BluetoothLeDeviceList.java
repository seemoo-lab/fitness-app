import android.bluetooth.BluetoothDevice;

/**
 * Created by Sven on 01.05.2017.
 */

public class BluetoothLeDeviceList {

    private ArrayList<BluetoothDevice> listOfDevices;

    public BluetoothLeDeviceList(){
        listOfDevices = new ArrayList<BluetoothDevice>();
    }

    public void addDevice(BluetoothDevice device){
        if(!listOfDevices.contains(device)){
            listOfDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position){
        return listOfDevices.get(postion);
    }

    public int size(){
        return listOfDevices.size();
    }

    public void clear(){
        listOfDevices.clear();
    }
}
