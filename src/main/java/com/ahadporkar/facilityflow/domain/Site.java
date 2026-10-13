package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "site")
public class Site extends BaseEntity {
    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    public Site() {}

    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String value) { this.postalCode = value; }
    public String getContactName() { return contactName; }
    public void setContactName(String value) { this.contactName = value; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String value) { this.contactEmail = value; }
}
