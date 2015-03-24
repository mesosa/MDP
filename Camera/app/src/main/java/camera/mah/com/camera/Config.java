package camera.mah.com.camera;

/**
 * Created by Muhamet Ademi on 2015-03-24.
 */
public class Config {

    private static String IP = "http://192.168.1.191/index.php";

    public static String getIP() {
        return IP;
    }

    public static void setIP(String IP) {
        Config.IP = IP;
    }
}
