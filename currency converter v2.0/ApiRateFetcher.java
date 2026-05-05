import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiRateFetcher {
    private static final String API_KEY = "7c674f6795c3cf7198ef16d6";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    public double getRate(String baseCurrency, String targetCurrency) {
        Map<String, Double> rates = getAllRates(baseCurrency);
        if (rates.containsKey(targetCurrency)) {
            return rates.get(targetCurrency);
        } else {
            return -1;
        }
    }

    public Map<String, Double> getAllRates(String baseCurrency) {
        Map<String, Double> ratesMap = new LinkedHashMap<>();
        try {
            String urlStr = BASE_URL + API_KEY + "/latest/" + baseCurrency;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            String json = response.toString();

            // Find the "conversion_rates" object
            String key = "\"conversion_rates\":";
            int idx = json.indexOf(key);
            if (idx == -1) return ratesMap;

            int start = json.indexOf("{", idx + key.length());
            if (start == -1) return ratesMap;
            int end = start;
            int braceCount = 0;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        end = i;
                        break;
                    }
                }
            }
            if (end <= start) return ratesMap;

            String ratesJson = json.substring(start + 1, end); // inside braces

            // Split by commas and parse entries
            String[] entries = ratesJson.split(",");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length >= 2) {
                    String code = parts[0].trim().replaceAll("\"", "");
                    StringBuilder valBuilder = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        if (i > 1) valBuilder.append(":");
                        valBuilder.append(parts[i]);
                    }
                    String valStr = valBuilder.toString().trim();
                    valStr = valStr.replaceAll("[^0-9.\\-eE]", "");
                    try {
                        double v = Double.parseDouble(valStr);
                        ratesMap.put(code, v);
                    } catch (NumberFormatException nfe) {
                        // skip invalid
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching all rates: " + e.getMessage());
        }
        return ratesMap;
    }
}
