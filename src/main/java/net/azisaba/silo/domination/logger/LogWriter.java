package net.azisaba.silo.domination.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogWriter {

    private final File logFile;

    public LogWriter(File logFile) {
        this.logFile = logFile;

        if ( !this.logFile.exists() ) {
            try {
                this.logFile.getParentFile().mkdirs();
                this.logFile.createNewFile();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public void writeLine(String data) {

        if ( !logFile.exists() ) {
            return;
        }

        String str = read();
        str += getDate() + " " + data;
        writeText(str);
    }

    private void writeText(String data) {
        try {
            FileWriter fw = new FileWriter(logFile);
            fw.write(data);
            fw.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private String read() {
        String data = null;
        StringBuilder sb = new StringBuilder();

        String lineSeparator = System.getProperty("line.separator");

        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            while ( (data = br.readLine()) != null ) {
                sb.append(data);
                sb.append(lineSeparator);
            }
            data = sb.toString();
            br.close();
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return data;
    }

    private String getDate() {

        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("[1] h/m/s S");

        sdf.applyPattern("[MM/dd HH:mm:ss]");
        return sdf.format(cal.getTime());
    }

    public String getEndOfLine() {
        return "\r\n";
    }

}
