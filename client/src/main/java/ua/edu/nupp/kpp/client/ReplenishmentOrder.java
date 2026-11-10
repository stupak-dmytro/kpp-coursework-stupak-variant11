package ua.edu.nupp.kpp.client;

public class ReplenishmentOrder {

    private Long id;
    private long stockId;
    private long supplierId;
    private double orderedQuantity;
    private OrderState state;
    private double f1;
    private double f2;
    private double f3;
    private double priorityIndex;
    private String createdAt;

    public ReplenishmentOrder() {
    }

    public ReplenishmentOrder(Long id,
                              long stockId,
                              long supplierId,
                              double orderedQuantity,
                              OrderState state,
                              double f1,
                              double f2,
                              double f3,
                              double priorityIndex,
                              String createdAt) {
        this.id = id;
        this.stockId = stockId;
        this.supplierId = supplierId;
        this.orderedQuantity = orderedQuantity;
        this.state = state;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.priorityIndex = priorityIndex;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getStockId() {
        return stockId;
    }

    public void setStockId(long stockId) {
        this.stockId = stockId;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public double getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(double orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getPriorityIndex() {
        return priorityIndex;
    }

    public void setPriorityIndex(double priorityIndex) {
        this.priorityIndex = priorityIndex;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
