package ua.edu.nupp.kpp.server;

public class Stock {

    private Long id;
    private long productId;
    private double quantity;
    private double minLevel;
    private double dailyConsumption;
    private String recordedAt;

    public Stock() {
    }

    public Stock(Long id,
                 long productId,
                 double quantity,
                 double minLevel,
                 double dailyConsumption,
                 String recordedAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.minLevel = minLevel;
        this.dailyConsumption = dailyConsumption;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(double minLevel) {
        this.minLevel = minLevel;
    }

    public double getDailyConsumption() {
        return dailyConsumption;
    }

    public void setDailyConsumption(double dailyConsumption) {
        this.dailyConsumption = dailyConsumption;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }
}
