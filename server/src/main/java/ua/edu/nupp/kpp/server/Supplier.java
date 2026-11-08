package ua.edu.nupp.kpp.server;

public class Supplier {

    private Long id;
    private String name;
    private String contactEmail;
    private int leadTimeDays;
    private double reliability;

    public Supplier() {
    }

    public Supplier(Long id,
                    String name,
                    String contactEmail,
                    int leadTimeDays,
                    double reliability) {
        this.id = id;
        this.name = name;
        this.contactEmail = contactEmail;
        this.leadTimeDays = leadTimeDays;
        this.reliability = reliability;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public int getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(int leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public double getReliability() {
        return reliability;
    }

    public void setReliability(double reliability) {
        this.reliability = reliability;
    }
}
