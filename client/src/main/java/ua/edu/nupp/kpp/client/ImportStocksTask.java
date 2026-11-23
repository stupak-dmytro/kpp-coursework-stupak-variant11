package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Фоновий імпорт залишків із CSV: читання файлу, розбір рядків, передавання на сервер через HTTP-клієнт
public class ImportStocksTask extends Task<ImportResult> {

    private static final Logger LOG = Logger.getLogger(ImportStocksTask.class.getName());

    private final HttpWarehouseClient api;
    private final Path path;

    public ImportStocksTask(HttpWarehouseClient api, Path path) {
        this.api = api;
        this.path = path;
    }

    private record Row(int number, String text) {
    }

    @Override
    protected ImportResult call() throws Exception {
        LOG.info("Початок імпорту CSV: файл " + path.getFileName());

        List<Row> rows;
        try {
            rows = readRows();
        } catch (IllegalArgumentException e) {
            LOG.warning("Імпорт не розпочато: " + e.getMessage());
            throw e;
        }

        int total = rows.size();
        if (total == 0) {
            // Файл містить лише заголовок: це нормальний граничний випадок, а не помилка
            updateProgress(1, 1);
            updateMessage("Немає рядків для імпорту");
            LOG.info("Завершення імпорту: у файлі немає рядків даних");
            return new ImportResult(0, List.of());
        }

        int imported = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            if (isCancelled()) {
                break;
            }
            Row row = rows.get(i);
            try {
                Stock stock = CsvStockReader.parse(row.text(), row.number());
                api.addStock(stock);
                imported++;
            } catch (ServerUnavailableException e) {
                // Сервер недоступний: імпорт зупиняється, а не вдає «успішний» результат
                LOG.log(Level.SEVERE, "Імпорт перервано: сервер недоступний (оброблено " + i + " з "
                        + total + ", імпортовано " + imported + ")", e);
                throw e;
            } catch (IllegalArgumentException e) {
                // некоректний рядок CSV
                errors.add(e.getMessage());
                LOG.warning(e.getMessage());
            } catch (RuntimeException e) {
                // сервер відповів помилкою (наприклад, неіснуючий товар або недопустиме значення)
                String reason = "Рядок " + row.number() + ": сервер відхилив запис (" + e.getMessage() + ")";
                errors.add(reason);
                LOG.warning(reason);
            }
            updateProgress(i + 1, total);
            updateMessage("Оброблено " + (i + 1) + " з " + total);
        }

        LOG.info("Завершення імпорту: імпортовано " + imported + ", пропущено " + errors.size()
                + " із " + total + " рядків");
        return new ImportResult(imported, errors);
    }

    // Зчитує файл, перевіряє заголовок і повертає непорожні рядки даних разом із номерами рядків
    private List<Row> readRows() {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Не вдалося прочитати CSV-файл: " + path);
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV-файл порожній");
        }
        CsvStockReader.checkHeader(lines.get(0));

        List<Row> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String text = lines.get(i).trim();
            if (!text.isEmpty()) {
                rows.add(new Row(i + 1, text));
            }
        }
        return rows;
    }
}
