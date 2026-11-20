package ua.edu.nupp.kpp.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvStockReaderTest {

    private static final double DELTA = 0.001;

    @Test
    void parsesValidLine() {
        Stock stock = CsvStockReader.parse("1;85.0;150.0;40.0", 2);
        assertNull(stock.getId());
        assertEquals(1L, stock.getProductId());
        assertEquals(85.0, stock.getQuantity(), DELTA);
        assertEquals(150.0, stock.getMinLevel(), DELTA);
        assertEquals(40.0, stock.getDailyConsumption(), DELTA);
        assertNull(stock.getRecordedAt());
    }

    @Test
    void acceptsDecimalCommaAndSpaces() {
        Stock stock = CsvStockReader.parse(" 2 ; 420,5 ; 300.0 ; 55 ", 3);
        assertEquals(2L, stock.getProductId());
        assertEquals(420.5, stock.getQuantity(), DELTA);
    }

    @Test
    void rejectsWrongColumnCount() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> CsvStockReader.parse("3;22.0;100.0", 4));
        assertTrue(e.getMessage().contains("Рядок 4"));
        assertTrue(e.getMessage().contains("знайдено 3"));
    }

    @Test
    void rejectsNonNumericValue() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> CsvStockReader.parse("2;abc;300.0;55.0", 5));
        assertTrue(e.getMessage().contains("некоректне числове значення"));
    }

    @Test
    void rejectsNonIntegerProductId() {
        assertThrows(IllegalArgumentException.class,
                () -> CsvStockReader.parse("1.5;10.0;100.0;5.0", 6));
    }

    @Test
    void acceptsCorrectHeader() {
        assertDoesNotThrow(() -> CsvStockReader.checkHeader(CsvStockReader.HEADER));
    }

    @Test
    void ignoresByteOrderMarkInHeader() {
        assertDoesNotThrow(() -> CsvStockReader.checkHeader("\uFEFF" + CsvStockReader.HEADER));
    }

    @Test
    void rejectsWrongHeader() {
        assertThrows(IllegalArgumentException.class,
                () -> CsvStockReader.checkHeader("id;quantity"));
    }

    @Test
    void importResultCountsSkippedRows() {
        ImportResult result = new ImportResult(2, List.of("Рядок 3: помилка", "Рядок 4: помилка"));
        assertEquals(2, result.imported());
        assertEquals(2, result.skipped());
    }
}
