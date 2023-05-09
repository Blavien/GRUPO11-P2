import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUtils {

    public static final String REQUEST_REGEX = "GET : (\\w+.txt)";
    public static final String SERVER_PATH_FILE_FORMAT = "%s/%s";
    public static final String HANDSHAKE_SIGNAL = "server/Registry/handshake_signal.txt";

    public static final String REGISTRY_PATH = "server/Registry/registry.txt";
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
        System.out.println(splitMessage[1]);
        requestInfo.add(splitMessage[1]);

        //Splits to get the request file
        System.out.println(splitMessage[3]);
        requestInfo.add(splitMessage[3]);
        return requestInfo;
    }
    public static void newClientRegister(String client_name) throws IOException {
        File file = new File(REGISTRY_PATH);
        int request_counter = 0; //For the first request
        boolean userFound = false;


        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(" : ");
            if (parts[0].equals(client_name)) {
                request_counter = Integer.parseInt(parts[1]);
                userFound = true;
                break;
            }
        }
        br.close();
        //User já existente
        String data = client_name + " : " + 0;

        if (!userFound) {   //New user, fica a 0
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(data);
            out.close();
        }


    }
    public static void registerRequests(ArrayList<String> request) throws IOException {
        String client_name = request.get(0);
        File file = new File(REGISTRY_PATH);
        int request_counter = 0; //For the first request
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
    }
    public static void emptyRegistry () throws Exception{
        //Limpa o ficheiro de execuções do programa anteriores
        new PrintWriter(REGISTRY_PATH).close();
    }
    public static int requestLimit (String request) throws IOException{
        String client_name = request;

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
                    userFound = true;
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
    public static void writeNumberToFile(int number, String filename) throws Exception {
        File file = new File(filename);
        FileWriter writer = new FileWriter(file);
        writer.write(Integer.toString(number));
        writer.close();
    }
    public static int readNumberFromFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("File number.txt does not exist");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            return Integer.parseInt(line.trim());
        } catch (Exception e) {
            return 0; // or any other default value
        }
    }
}
