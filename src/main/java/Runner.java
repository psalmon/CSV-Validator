import java.io.*;
import java.util.Scanner;

public class Runner {

    public static void main(String[] args){
        String path;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please provide the path of your .csv file.\n");
        path = scanner.next();

        CsvNormalizer normalizer = new CsvNormalizer();
        File csvOut = normalizer.normalize(path);

        try{
            FileInputStream input = new FileInputStream(csvOut);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }catch(FileNotFoundException e){
            System.out.println(e.getMessage());
        }

    }
}
