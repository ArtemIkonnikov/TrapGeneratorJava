import java.util.ArrayList;
import java.util.List;

public class TrapProperty {

    String ip;
    String version;
    String trapOid;
    String type;
    List<String> oids = new ArrayList<>();
    List<String> finalOids = new ArrayList<>();
    List<String> values = new ArrayList<>();

    TrapProperty(String ip, String version, String trapOid, List<String> oids, List<String> finalOids, List<String> values, String type) {
        this.ip = ip;
        this.version = version;
        this.trapOid = trapOid;
        this.oids = oids;
        this.finalOids = finalOids;
        this.values = values;
        this.type = type;
    }

    String getIp() {
        return ip;
    }

    String getVersion() {
        return version;
    }

    List<String> getOids() {
        return oids;
    }

    List<String> getValues() {
        return values;
    }

    String getTrapOid() {
        return trapOid;
    }

    String getType() {
        return type;
    }

    List<String> getFinalOids() {
        return finalOids;
    }


    @Override
    public String toString() {
        return "TrapProperty{" +
                "ip='" + ip + '\'' +
                ", version='" + version + '\'' +
                ", trapOid='" + trapOid + '\'' +
                ", type='" + type + '\'' +
                ", oids=" + oids +
                ", finalOids=" + finalOids +
                ", values=" + values +
                '}';
    }
}
