package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Фоновий імпорт залишків із CSV: читання файлу, розбір рядків, передавання на сервер через HTTP-клієнт
public class ImportStocksTask extends Task<ImportResult> {

    private static final Logger LOG = Logger.getLogger(ImportStocksTask.class.getName());

    // Невелика пауза між рядками, щоб хід імпорту було видно на ProgressBar
    private static final long DELAY_MS = 150;

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
        List<Row> rows = readRows();
        int total = rows.size();
        int imported = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            if (isCancelled()) {
                break;
            }
            Row row = rows.get(i);
            try {
                Stock stock = CsvStockReader.parse(row.text(), row.number());
                try {
                    api.addStock(stock);
                    imported++;
                } catch (RuntimeException e) {
                    String reason = "Рядок " + row.number() + ": сервер відхилив запис (" + e.getMessage() + ")";
                    errors.add(reason);
                    LOG.warning(reason);
                }
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
                LOG.warning(e.getMessage());
            }
            updateProgress(i + 1, total);
            updateMessage("Оброблено " + (i + 1) + " з " + total);
            try {
                Thread.sleep(DELAY_MS);
            } catch (InterruptedException e) {
                if (isCancelled()) {
                    break;
                }
                Thread.currentThread().interrupt();
            }
        }
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
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("У CSV-файлі немає рядків даних");
        }
        return rows;
    }
}
