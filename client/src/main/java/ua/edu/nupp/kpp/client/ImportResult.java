package ua.edu.nupp.kpp.client;

import java.util.List;

// Підсумок імпорту: кількість збережених рядків і описи пропущених
public record ImportResult(int imported, List<String> errors) {

    public int skipped() {
        return errors.size();
    }
}
