import java.util.Map;

public interface RateFetcher {
    double getRate(String baseCurrency, String targetCurrency);
    Map<String, Double> getAllRates(String baseCurrency);
}
