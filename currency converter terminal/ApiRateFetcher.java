import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiRateFetcher implements RateFetcher {
    private static final String API_KEY = "7c674f6795c3cf7198ef16d6";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    @Override
    public double getRate(String baseCurrency, String targetCurrency) {
        Map<String, Double> rates = getAllRates(baseCurrency);
        if (rates.containsKey(targetCurrency)) {
            return rates.get(targetCurrency);
        } else {
            return -1;
        }
    }

    @Override
    public Map<String, Double> getAllRates(String baseCurrency) {
        Map<String, Double> ratesMap = new LinkedHashMap<>();
        try {
            String urlStr = BASE_URL + API_KEY + "/latest/" + baseCurrency;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            String json = response.toString();
            String key = "\"conversion_rates\":";
            int idx = json.indexOf(key);
            if (idx == -1) return ratesMap;

            int start = json.indexOf("{", idx + key.length());
            int end = json.indexOf("}", start);
            String ratesJson = json.substring(start + 1, end);

            String[] entries = ratesJson.split(",");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String code = parts[0].trim().replaceAll("\"", "");
                    String valStr = parts[1].trim();
                    try {
                        double v = Double.parseDouble(valStr);
                        ratesMap.put(code, v);
                    } catch (NumberFormatException nfe) {
                        // skip invalid
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching rates: " + e.getMessage());
        }
        return ratesMap;
    }
}
