import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUtils {

    public static final String REQUEST_REGEX = "GET : (\\w+.txt)";
    public static final String SERVER_PATH_FILE_FORMAT = "%s/%s";
    private static final String REGISTRY_PATH = "server/Registry/registry.txt";
    private static final String HANDSHAKE_PATH = "server/Registry/hanshake.txt";
    private final int MAX_REQUESTS = 5;

    /**
     * Computes the absolute path of the file to read from the request.
     *
     * @param request the request from the client
     *
     * @return the path of the file to read
     */
    public static String getAbsoluteFilePath ( String request ) {
        try {
            String fileName = request;
            return String.format ( SERVER_PATH_FILE_FORMAT , Server.FILE_PATH , fileName );
        } catch ( IllegalArgumentException e ) {
            throw new IllegalArgumentException ( "Invalid request" );
        }
    }

    /**
     * Extracts the name of the file from the request.
     *
     * @param request the request from the client
     *
     * @return the name of the requested file
     */
    public static String getFileNameFromRequest ( String request ) {
        Pattern pattern = Pattern.compile ( REQUEST_REGEX );
        Matcher matcher = pattern.matcher ( request );
        boolean matchFound = matcher.find ( );
        if ( matchFound ) {
            return matcher.group ( 1 );
        }
        throw new IllegalArgumentException ( "Invalid request" );
    }

    public static ArrayList<String> splitRequest(String message) throws Exception{
        ArrayList<String> requestInfo = new ArrayList<>();
        String[] splitMessage =message.split(": ");
        //Splits to get the client name
        requestInfo.add(splitMessage[1]);
        //Splits to get the request file
        requestInfo.add(splitMessage[3]);
        return requestInfo;
    }
    public static void registerRequests(ArrayList<String> request) throws IOException {
        String client_name = request.get(0);
        File file = new File(REGISTRY_PATH);
        int request_counter = 1; //For the first request
        boolean userFound = false;

        if (!file.exists()){
            file.createNewFile();
        }else {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(client_name)) {
                    request_counter = Integer.parseInt(parts[1]) + 1;
                    userFound = true;
                    break;
                }
            }
            br.close();
        }

        String data = client_name + " : " + request_counter;

        if (userFound) {
            List<String> lines = Files.readAllLines(file.toPath());
            PrintWriter out = new PrintWriter(new FileWriter(file));
            for (String line : lines) {
                if (line.startsWith(client_name + " : ")) {
                    out.println(data);
                } else {
                    out.println(line);
                }
            }
            out.close();
        } else {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(data);
            out.close();
        }
    }
    public static void emptyRegistry (String file) throws Exception{
        //Limpa o ficheiro de execuções do programa anteriores
        new PrintWriter(file).close();
    }
    public static int getRequestLimit(String name) throws IOException{
        String client_name = name;

        File file = new File(REGISTRY_PATH);
        boolean userFound = false;
        int requestCounter = 0;

        if (!file.exists()){
            file.createNewFile();
        }else {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(client_name)) {
                    requestCounter = Integer.parseInt(parts[1]);
                    break;
                }
            }
            br.close();
        }
        return requestCounter;
    }
    public static void resetRequestCounter(String username) throws IOException {
        File file = new File(REGISTRY_PATH);
        boolean userFound = false;
        List<String> lines = new ArrayList<>();

        if (!file.exists()){
            file.createNewFile();
        }else {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(username)) {
                    lines.add(parts[0] + " : 0");
                    userFound = true;
                } else {
                    lines.add(line);
                }
            }
            br.close();
        }

        // Write the modified file
        if (userFound) {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            for (String line : lines) {
                out.println(line);
            }
            out.close();
        }
    }
    public static List<String> getUsernamesFromRegistry() throws IOException {
        List<String> usernames = new ArrayList<>();

        File file = new File(REGISTRY_PATH);
        if (!file.exists()){
            file.createNewFile();
        } else {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts.length >= 1) {
                    String username = parts[0];
                    usernames.add(username);
                }
            }
            br.close();
        }

        return usernames;
    }

    public static int readNumberFromFile() throws IOException {
        String filename = HANDSHAKE_PATH;
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int number = Integer.parseInt(reader.readLine());
        reader.close();
        return number;
    }
    public static void writeNumberToFile(int number) throws Exception {
        emptyRegistry (HANDSHAKE_PATH);
        String filename = HANDSHAKE_PATH;
        Writer writer = new FileWriter(filename);
        writer.write(Integer.toString(number));
        writer.close();
    }
}
