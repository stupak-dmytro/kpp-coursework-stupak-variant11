package ua.edu.nupp.kpp.client;

public class Product {

    private Long id;
    private String sku;
    private String name;
    private String unit;

    public Product() {
    }

    public Product(Long id,
                   String sku,
                   String name,
                   String unit) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
