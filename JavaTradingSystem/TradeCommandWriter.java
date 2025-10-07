
import java.io.FileWriter;
import java.io.IOException;

public class TradeCommandWriter {
    public void writeCommand(String command) {
        String filePath = "/Users/apple/Library/Application Support/net.metaquotes.wine.metatrader4/drive_c/Program Files (x86)/MetaTrader 4/MQL4/Files/commands.txt";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
