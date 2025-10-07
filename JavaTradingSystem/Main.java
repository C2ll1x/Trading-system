
public class Main {
    public static void main(String[] args) {
        TradeSignalGenerator signalGen = new TradeSignalGenerator();
        TradeCommandWriter writer = new TradeCommandWriter();
        TradeResponseReader reader = new TradeResponseReader();

        String signal = signalGen.generateSignal(); // Example: "BUY"
        writer.writeCommand(signal);

        String response = reader.readResponse();
        System.out.println("MT Response: " + response);
    }
}
