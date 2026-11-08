package ua.edu.nupp.kpp.server;

public final class RiskCalculator {

    // Тривалість постачання (днів), починаючи з якої ця складова ризику вважається максимальною (100)
    private static final double MAX_LEAD_TIME_DAYS = 14.0;

    private RiskCalculator() {
    }

    // F1 — дефіцит запасу: яку частину мінімального рівня становить нестача (0..100)
    public static double factor1(double quantity, double minLevel) {
        if (!(quantity >= 0) || !(minLevel > 0)) {
            throw new IllegalArgumentException("Некоректні дані для розрахунку F1");
        }
        return round2(clamp((minLevel - quantity) / minLevel * 100.0));
    }

    // F2 — інтенсивність споживання: яку частину мінімального рівня витрачають за добу (0..100)
    public static double factor2(double dailyConsumption, double minLevel) {
        if (!(dailyConsumption >= 0) || !(minLevel > 0)) {
            throw new IllegalArgumentException("Некоректні дані для розрахунку F2");
        }
        return round2(clamp(dailyConsumption / minLevel * 100.0));
    }

    // F3 — ризик затримки постачання: середнє ненадійності постачальника та тривалості постачання (0..100)
    public static double factor3(int leadTimeDays, double reliability) {
        if (leadTimeDays < 0 || !(reliability >= 0 && reliability <= 100)) {
            throw new IllegalArgumentException("Некоректні дані для розрахунку F3");
        }
        double unreliability = 100.0 - reliability;
        double leadTimePart = clamp(leadTimeDays / MAX_LEAD_TIME_DAYS * 100.0);
        return round2(0.5 * unreliability + 0.5 * leadTimePart);
    }

    // I = 0,40·F1 + 0,35·F2 + 0,25·F3, округлення до двох знаків
    public static double calculate(double f1, double f2, double f3) {
        if (!inRange(f1) || !inRange(f2) || !inRange(f3)) {
            throw new IllegalArgumentException("F1, F2 і F3 мають бути в діапазоні 0..100");
        }
        return round2(0.40 * f1 + 0.35 * f2 + 0.25 * f3);
    }

    private static boolean inRange(double v) {
        return v >= 0 && v <= 100;
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(100.0, v));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
