package com.example.appweather;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for providing pre-translated Nepali phrases for weather descriptions.
 * This ensures accurate local terminology for the Nepali language (Locale 'ne').
 */
public class NepaliTranslator {

    private static final Map<String, String> NEPALI_PHRASES = new HashMap<>();

    static {
        // --- 1. Current Temperature Phrase ---
        // Base phrase for the sentence structure.
        NEPALI_PHRASES.put("temp_base", "अहिलेको तापमान  "); // Current temperature is:

        // --- 2. Common Weather Conditions (English Key -> Nepali Value) ---
        NEPALI_PHRASES.put("clear sky", "  आकाश सफा छ।"); // Clear sky
        NEPALI_PHRASES.put("few clouds", "  थोरै बादल लागेको छ।"); // Few clouds
        NEPALI_PHRASES.put("scattered clouds", "  छरिएका बादलहरू छन्।"); // Scattered clouds
        NEPALI_PHRASES.put("broken clouds", "  बादल फाटेको छ।"); // Broken clouds
        NEPALI_PHRASES.put("overcast clouds", "  पूरै बादल लागेको छ।"); // Overcast clouds
        NEPALI_PHRASES.put("light rain", "  हल्का पानी परिरहेको छ।"); // Light rain
        NEPALI_PHRASES.put("moderate rain", "  मध्यम पानी परिरहेको छ।"); // Moderate rain
        NEPALI_PHRASES.put("heavy intensity rain", "  धेरै जोडले पानी परिरहेको छ।"); // Heavy intensity rain
        NEPALI_PHRASES.put("snow", "ह  िउँ परिरहेको छ।"); // Snow
        NEPALI_PHRASES.put("light snow", "  हल्का हिउँ परिरहेको छ।"); // Light snow
        NEPALI_PHRASES.put("mist", "  कुहिरो लागेको छ।"); // Mist
        NEPALI_PHRASES.put("fog", "  बाक्लो कुहिरो लागेको छ।"); // Fog
        NEPALI_PHRASES.put("thunderstorm", "  चट्याङसहितको आँधीबेहरी चलिरहेको छ।"); // Thunderstorm
        NEPALI_PHRASES.put("drizzle", "स  िमसिमे पानी परिरहेको छ।"); // Drizzle
        NEPALI_PHRASES.put("rain and snow", "  पानी र हिउँ परिरहेको छ।"); // Rain and snow
    }

    /**
     * Finds the Nepali phrase for an English weather description.
     */
    public static String getConditionPhrase(String englishDescription) {
        String key = englishDescription.toLowerCase(Locale.ROOT);
        return NEPALI_PHRASES.getOrDefault(key, "मौसमको जानकारी उपलब्ध छ।"); // Default: Weather information available.
    }

    /**
     * Builds the complete spoken sentence for the current weather.
     */
    public static String buildFullNepaliPhrase(String temperature, String englishDescription) {
        String tempBase = NEPALI_PHRASES.get("temp_base");
        String condition = getConditionPhrase(englishDescription);

        // Sentence structure: [Current temperature is] [15] [degrees Celsius], [Condition].
        return tempBase + " " + temperature + " डिग्री सेल्सियस अनि , " + condition;
    }
}