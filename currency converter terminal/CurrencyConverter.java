public class CurrencyConverter {
    private RateFetcher fetcher;

    public CurrencyConverter(RateFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public void convert(String base, String target, double amount) {
        double rate = fetcher.getRate(base, target);
        if (rate != -1) {
            double result = amount * rate;
            System.out.println("\n=== Conversion Result ===");
            System.out.println(amount + " " + base + " = " + result + " " + target);
            System.out.println("==========================");
            System.out.println("Conversion completed successfully!\n");
        } else {
            System.out.println("Conversion failed. Check currency codes or internet connection.");
        }
    }
}
