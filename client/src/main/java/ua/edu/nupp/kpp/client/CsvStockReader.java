package ua.edu.nupp.kpp.client;

// Розбір CSV-файлу залишків товарів: формат product_id;quantity;min_level;daily_consumption
public final class CsvStockReader {

    public static final String HEADER = "product_id;quantity;min_level;daily_consumption";

    private static final String SEPARATOR = ";";
    private static final int COLUMNS = 4;

    private CsvStockReader() {
    }

    // Перевіряє заголовок файлу; BOM на початку файлу ігнорується
    static void checkHeader(String line) {
        String normalized = line == null ? "" : line.replace("\uFEFF", "").trim();
        if (!HEADER.equals(normalized)) {
            throw new IllegalArgumentException("Некоректний заголовок CSV-файлу: очікується " + HEADER);
        }
    }

    // Розбирає один рядок даних. Некоректний рядок не створює модель
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

    // Приймає і крапку, і кому як десятковий роздільник
    private static double toDouble(String value) {
        return Double.parseDouble(value.trim().replace(',', '.'));
    }
}
