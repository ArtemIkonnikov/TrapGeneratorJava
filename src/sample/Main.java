package sample;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {

    static File batFile;
    static String batFilePath = " ";
    static String rawTrapsReceivedPath = null;
    static String jsonRulesFilePath = null;
    static List<String> trapsReceivedPaths = new ArrayList<>();
    static List<String> versionList = new ArrayList<>();
    static Boolean errorVisibility = false;
    static Boolean isCommandLine = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("TrapGenerator");
        primaryStage.setScene(new Scene(root, 480, 220));
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            launch(args);
        } else {
            isCommandLine = true;
            versionList = Arrays.asList("v1", "v2");
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("-d")) {
                    rawTrapsReceivedPath = args[i + 1];
                } else if (args[i].equals("-r")) {
                    jsonRulesFilePath = args[i + 1];
                } else if (args[i].equals("-o")) {
                    batFilePath = args[i + 1];
                }
            }
            inputDataParser();
            Platform.exit();
        }
    }

    public static void inputDataParser() {

        Map<String, Object> rawMapVarbinds = new HashMap<>();
        Map<String, String> mapVarbinds = new HashMap<>();

        String root = System.getProperty("user.dir");
        if (batFilePath == null || batFilePath.equals(" ")) {
            batFilePath = root;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            rawMapVarbinds = mapper.readValue(
                    new File(jsonRulesFilePath),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (JsonParseException e) {
            errorVisibility = true;
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            errorVisibility = true;
            e.printStackTrace();
        } catch (JsonMappingException e) {
            errorVisibility = true;
            e.printStackTrace();
        } catch (IOException e) {
            errorVisibility = true;
            e.printStackTrace();
        }

        for (Map.Entry<String, Object> entry : rawMapVarbinds.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            if (value.equals("int") || value.equals("i")) {
                mapVarbinds.put(key, "i");
            } else {
                mapVarbinds.put(key, "s");
            }
        }

        if (isCommandLine && rawTrapsReceivedPath.contains("(*).")) {
            String rawPath = rawTrapsReceivedPath.split("traps_received")[0];
            for (int i = 0; i < 100; i++) {
                String trapReceivedPath;
                if (i == 0) {
                    trapReceivedPath = rawPath + "traps_received.log";
                } else {
                    trapReceivedPath = rawPath + "traps_received" + i + ".log";
                }
                File file = new File(trapReceivedPath);
                if (file.exists()) {
                    trapsReceivedPaths.add(trapReceivedPath);
                }
            }
        } else if (isCommandLine && rawTrapsReceivedPath.contains(",")) {
            String[] rawPathArr = rawTrapsReceivedPath.split(",");
            trapsReceivedPaths = Arrays.asList(rawPathArr);
        } else if (isCommandLine) {
            trapsReceivedPaths.add(rawTrapsReceivedPath);
        }

        for (String path : trapsReceivedPaths) {

            File trapsReceivedFile = new File(path);
            String readableStr;
            String completeStr;
            BufferedReader bufferedReader;
            boolean selector;
            List<String> checkList = new ArrayList<>();

            try {
                bufferedReader = new BufferedReader(new FileReader(trapsReceivedFile));
                while (bufferedReader.ready()) {
                    readableStr = bufferedReader.readLine();
                    selector = true;
                    for (String oid : mapVarbinds.keySet()) {
                        if (readableStr.contains(oid)) {
                            if (selector) {
                                completeStr = trapBuilder(readableStr, mapVarbinds);
                                if (!checkList.contains(completeStr) && !completeStr.equals("")) {
                                    if (completeStr.contains("SET NETSNMP_PATH=C:\\usr\\bin\n")) {
                                        checkList.add(completeStr.replace("SET NETSNMP_PATH=C:\\usr\\bin\n", ""));
                                    } else {
                                        checkList.add(completeStr);
                                    }
                                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(batFile, true))) {
                                        bufferedWriter.write(completeStr + "\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                selector = false;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String trapBuilder(String receivedStr, Map<String, String> mapVarbinds) {

        List<String> values = new ArrayList<>();
        List<String> oids = new ArrayList<>();
        List<String> finalOids = new ArrayList<>();
        String ip = null;
        String trapVer = null;
        String trapOid = null;
        String type = null;

        for (String oid : mapVarbinds.keySet()) {
            if (receivedStr.contains(oid)) {

                Pattern pattern1 = Pattern.compile("(?<= - ).+?(?=\\/| :)|(?<= - ).+?(?= : )");
                Matcher matcher1 = pattern1.matcher(receivedStr);

                while (matcher1.find())
                    ip = matcher1.group();

                if (receivedStr.contains("INFORM")) {
                    type = "INFORM";
                } else {
                    type = "trap";
                }

                if (receivedStr.contains("V1TRAP")) {
                    trapVer = "v1";
                } else {
                    trapVer = "v2";
                }

                Pattern pattern2 = Pattern.compile("((?<=; |VBS\\[)1.3.6.1\\..+?(?= ))( =)(.+?)(?=;|\\])");
                Matcher matcher2 = pattern2.matcher(receivedStr);

                while (matcher2.find()) {
                    char[] chars = matcher2.group(1).toCharArray();
                    if (matcher2.group(1).equals(oid)) {
                        String finalOid = matcher2.group(1);
                        String oidForList = oid;
                        String value = matcher2.group(3).trim();
                        finalOids.add(finalOid);
                        values.add(value);
                        oids.add(oidForList);
                    } else if (matcher2.group(1).contains(oid) && ((matcher2.group(1).length()) > oid.length())) {
                        if (chars[oid.length()] == ('.')) {
                            String finalOid = matcher2.group(1);
                            String oidForList = oid;
                            String value = matcher2.group(3).trim();
                            finalOids.add(finalOid);
                            values.add(value);
                            oids.add(oidForList);
                        }
                    }
                }

                Pattern pattern = Pattern.compile("(?<=enterprise=).+?(?=,)|(?<=1.3.6.1.6.3.1.1.4.1.0 = ).+?(?=;)");
                Matcher matcher = pattern.matcher(receivedStr);

                while (matcher.find()) {
                    trapOid = matcher.group();
                }
            }
        }
        TrapProperty trap;
        StringBuilder stringBuilder = new StringBuilder();

        if (ip != null && trapOid != null && !oids.isEmpty() && !values.isEmpty() && versionList.contains(trapVer)) {
            trap = new TrapProperty(ip, trapVer, trapOid, oids, finalOids, values, type);

            String batFileName;
            if (trap.type.equals("INFORM")) {
                batFileName = trap.ip + "_" + trap.version + "_informs.bat";
            } else {
                batFileName = trap.ip + "_" + trap.version + "_traps.bat";
            }

            File testFile = new File(batFilePath + "\\" + batFileName);

            if (!testFile.exists()) {
                stringBuilder.append("SET NETSNMP_PATH=C:\\usr\\bin\n");
            }

            batFile = new File(batFilePath, batFileName);

            if (trap.version.equals("v1")) {
                stringBuilder.append("%NETSNMP_PATH%\\snmptrap -v 1 -c public ");
                stringBuilder.append(trap.ip + " ");
                stringBuilder.append(trap.trapOid + " ");
                stringBuilder.append(trap.ip + " 6 0 '55' ");
            } else if (trap.version.equals("v2") && trap.type.equals("trap")) {
                stringBuilder.append("%NETSNMP_PATH%\\snmptrap -v 2c -c public ");
                stringBuilder.append(trap.ip + " \"\" ");
                stringBuilder.append(trap.trapOid + " ");
            } else {
                stringBuilder.append("%NETSNMP_PATH%\\snmpinform -v 2c -c public ");
                stringBuilder.append(trap.ip + " \"\" ");
                stringBuilder.append(trap.trapOid + " ");
            }
            for (int i = 0; i < trap.oids.size(); i++) {
                if (!(trap.values.get(i).equals("")) && (mapVarbinds.get(trap.oids.get(i)).equals("i"))) {
                    stringBuilder.append(trap.finalOids.get(i) + " ");
                    stringBuilder.append(mapVarbinds.get(trap.oids.get(i)) + " ");
                    stringBuilder.append(trap.values.get(i) + " ");
                } else if (!(trap.values.get(i).equals(""))) {
                    stringBuilder.append(trap.finalOids.get(i) + " ");
                    stringBuilder.append(mapVarbinds.get(trap.oids.get(i)) + " ");
                    stringBuilder.append("\"" + trap.values.get(i) + "\" ");
                }
            }
        }
        return stringBuilder.toString();
    }
}
