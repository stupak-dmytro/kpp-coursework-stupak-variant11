package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Початок CSV-імпорту: послідовне читання одного рядка залишків без фонових потоків
public final class CsvStockReader {

    private static final String SEPARATOR = ";";
    private static final int COLUMNS = 4;

    private CsvStockReader() {
    }

    // Читає файл, пропускає заголовок і розбирає перший рядок даних
    public static Stock readFirstRow(Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Не вдалося прочитати CSV-файл: " + path);
        }
        // перший рядок файлу є заголовком, тому починаємо з другого
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                return parse(line, i + 1);
            }
        }
        throw new IllegalArgumentException("У CSV-файлі немає рядків даних");
    }

    // Формат рядка: product_id;quantity;min_level;daily_consumption
    static Stock parse(String line, int lineNumber) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != COLUMNS) {
            throw new IllegalArgumentException("Рядок " + lineNumber + ": очікується стовпців "
                    + COLUMNS + ", а знайдено " + parts.length);
        }
        try {
            return new Stock(
                    null,
                    Long.parseLong(parts[0].trim()),
                    toDouble(parts[1]),
                    toDouble(parts[2]),
                    toDouble(parts[3]),
                    null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Рядок " + lineNumber + ": некоректне числове значення");
        }
    }

    private static double toDouble(String value) {
        return Double.parseDouble(value.trim().replace(',', '.'));
    }
}
