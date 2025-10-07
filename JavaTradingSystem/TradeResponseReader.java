

public class TradeResponseReader {
    public String readResponse() {
        String filePath = "/Users/apple/Library/Application Support/net.metaquotes.wine.metatrader4/drive_c/Program Files (x86)/MetaTrader 4/MQL4/Files/response.txt";
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get(filePath));
        } catch (Exception e) {
            return "No response or error reading file.";
        }
    }
}
