package seemoo.fitbit.miscellaneous;

public class FirmwareFileDescriptor {
    private String deviceName = null;
    private String fwshortname = null;
    private String description = null;
    private String version = null;
    private String location = null;

    FirmwareFileDescriptor(String deviceName, String fwshortname, String description, String version, String location) {
        this.deviceName = deviceName;
        this.fwshortname = fwshortname;
        this.description = description;
        this.version = version;
        this.location = location;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getFwshortname() {
        return fwshortname;
    }

    public void setFwshortname(String fwshortname) {
        this.fwshortname = fwshortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
