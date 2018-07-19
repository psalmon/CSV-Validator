import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.*;

public class CsvNormalizer {

    public File normalize(String path){

                String parentPath = Paths.get(path).getParent().toString();
                String fileName = Paths.get(path).getFileName().toString();
                String newPath = parentPath + "/new_" + fileName;

                try {

                    FileInputStream input = new FileInputStream(new File(path));
                    CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
                    decoder.onMalformedInput(CodingErrorAction.IGNORE);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(newPath));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

                    for (CSVRecord csvRecord : csvParser) {

                        if (csvRecord.get(0).equals("Timestamp")){//Don't process header
                            csvPrinter.printRecord(csvRecord);
                            continue;
                        }

                        String timeStamp = timestampStandardize(csvRecord.get(0));
                        String address = csvRecord.get(1);
                        String zip = zipCodeStandardize(csvRecord.get(2));
                        String fullName = nameStandardize(csvRecord.get(3));
                        String fooDuration = durationStandardize(csvRecord.get(4));
                        String barDuration = durationStandardize(csvRecord.get(5));
                        String totalDuration;
                        String notes = csvRecord.get(7);

                        //if timestamp, zip, or any of the durations contain unicode replacement, drop the column.

                        if(fooDuration == "" || barDuration == ""){
                            totalDuration = "";
                        }else{
                            totalDuration = String.valueOf(Float.valueOf(fooDuration) + Float.valueOf(barDuration));
                        }
                        
                        String[] recordArray = {
                                timeStamp, address, zip, fullName, fooDuration,
                                barDuration, totalDuration, notes
                        };

                        ArrayList<String> utf8RecordArray = new ArrayList<>();
                        for (String record : recordArray) {
                            utf8RecordArray.add(utf8Standardize(record));
                        }

                        csvPrinter.printRecord(utf8RecordArray);
                        csvPrinter.flush();
                    }
                    reader.close();

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

        return new File(newPath);

    }


    public String utf8Standardize(String recordString){

        try {
            byte bytes[] = recordString.getBytes("ISO-8859-1");
            recordString = new String(bytes, "UTF-8");
            return recordString;
        }catch(UnsupportedEncodingException e){
            System.out.println(e.getMessage());
        }
        return "";

    }

    public String timestampStandardize(String timeStamp){

        if(timeStamp.contains("�")){
            return "";
        }

        StringBuilder sb = new StringBuilder();

        DateTimeFormatter inboundFormat = DateTimeFormatter.ofPattern("M/d/yy h:mm:ss a");
        DateTimeFormatter outboundFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime ldt = LocalDateTime.parse(timeStamp, inboundFormat);
        ldt = ldt.plusHours(3);//pst to est

        sb.append(outboundFormat.format(ldt));

        return sb.toString();
    }

    public String zipCodeStandardize(String zipCode){

        if(zipCode.contains("�")){
            return "";
        }

        return String.format("%05d", Integer.parseInt(zipCode));

    }

    public String nameStandardize(String name){

        return name.toUpperCase();

    }

    public String durationStandardize(String duration) {

        if(duration.contains("�")){
            return "";
        }

        String[] hoursMinutes = duration.split(":");
        String[] secondsMillis = hoursMinutes[2].split("\\.");

        float hoursInSeconds = Integer.parseInt(hoursMinutes[0]) * 60 * 60;
        float minutesInSeconds = Integer.parseInt(hoursMinutes[1]) * 60;
        float seconds = Integer.parseInt(secondsMillis[0]);
        float millisecondsInSeconds= Integer.parseInt(secondsMillis[1]) * 0.001f;

        return String.valueOf(hoursInSeconds + minutesInSeconds + seconds + millisecondsInSeconds);

    }

}
