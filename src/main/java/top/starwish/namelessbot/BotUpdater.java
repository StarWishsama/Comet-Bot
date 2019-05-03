package top.starwish.namelessbot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BotUpdater {
    public static String getLatestVer() {
        String version = "";
        try {
            URL url = null;
            if (VerClass.VERSION.toLowerCase().contains("release")){
               url = new URL("https://raw.githubusercontent.com/StarWishsama/Nameless-Bot/master/Version.txt");
            } else
                url = new URL("https://raw.githubusercontent.com/StarWishsama/Nameless-Bot/dev/Version.txt");
            InputStream a = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(a, StandardCharsets.UTF_8));
            version = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static boolean isLatest(){
        boolean isLatest = false;
        String latestVer = getLatestVer();
        String current = VerClass.VERSION;
        if (latestVer.equalsIgnoreCase(current)){
            isLatest = true;
        }
        return isLatest;
    }
}
