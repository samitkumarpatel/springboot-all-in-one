package com.example.all_in_one.models;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class Address extends Contact {
    public Address() {}
    //ContactType contactType;
    String address1;
    String address2;
}
