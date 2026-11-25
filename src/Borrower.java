import java.time.LocalDate;
import java.time.LocalDateTime;

public class Borrower {

    private int id;
    private String fullName;
    private String phone;
    private String address;
    private String idNumber;
    private LocalDate dateRegistered;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for registering a new borrower
    public Borrower(String fullName, String phone, String address, String idNumber) {
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.idNumber = idNumber;
        this.dateRegistered = LocalDate.now();
        this.status = "active";
    }

    // Constructor when loading from database
    public Borrower(int id, String fullName, String phone, String address, String idNumber,
                    LocalDate dateRegistered, String status,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.idNumber = idNumber;
        this.dateRegistered = dateRegistered;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public LocalDate getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(LocalDate dateRegistered) { this.dateRegistered = dateRegistered; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Borrower #" + id + " | " + fullName + " | " + phone + " | " + status;
    }
}
