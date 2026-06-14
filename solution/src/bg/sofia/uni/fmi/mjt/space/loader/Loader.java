package bg.sofia.uni.fmi.mjt.space.loader;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface Loader<T> {

    List<T> load(Reader reader);

    // needing two delimiters for rockets file because we do not know if the del is , or |
    static String[] parseCSVLine(String line, char delimiter1, char delimiter2) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        int size = line.length();
        for (int i = 0; i < size; i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if ((c == delimiter1 || c == delimiter2) && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

}