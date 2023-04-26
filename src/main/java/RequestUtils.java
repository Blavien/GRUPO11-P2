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

        // Se exisitir ele vai lá procurar o client name e incrementar o counter
        if (file.exists()) {
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
    public boolean requestLimit (String request) throws IOException{
        String client_name = request;

        File file = new File(REGISTRY_PATH);
        boolean userFound = false;
        int requestCounter = 0;

        // Search for client name and update request counter
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(client_name)) {
                    requestCounter = Integer.parseInt(parts[1]);
                    userFound = true;
                    break;
                }
            }
            br.close();
        }

        // Check if request limit reached
        if (userFound && requestCounter == 5) {
            return true;
        } else {
            return false;
        }
    }
    public void resetRequestCounter(String username) throws IOException {
        File file = new File(REGISTRY_PATH);
        boolean userFound = false;
        List<String> lines = new ArrayList<>();

        // Search for the user in the file and replace the request counter with 1
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(username)) {
                    lines.add(parts[0] + " : 1");
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

}
