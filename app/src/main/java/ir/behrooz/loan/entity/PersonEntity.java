package ir.behrooz.loan.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
@Entity(nameInDb = "Person")
public class PersonEntity {

    @Id(autoincrement = true)
    private Long id;
    private String nationalCode;
    private String name;
    private String family;
    private String phone;
    private Long cashId;
    @Generated(hash = 1751160242)
    public PersonEntity(Long id, String nationalCode, String name, String family,
            String phone, Long cashId) {
        this.id = id;
        this.nationalCode = nationalCode;
        this.name = name;
        this.family = family;
        this.phone = phone;
        this.cashId = cashId;
    }
    @Generated(hash = 69356185)
    public PersonEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNationalCode() {
        return this.nationalCode;
    }
    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFamily() {
        return this.family;
    }
    public void setFamily(String family) {
        this.family = family;
    }
    public String getPhone() {
        return this.phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public Long getCashId() {
        return this.cashId;
    }
    public void setCashId(Long cashId) {
        this.cashId = cashId;
    }
}