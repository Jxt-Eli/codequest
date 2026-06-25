package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
  
  @Id
  private String indexNumber;
  private String referenceNumber;
  private String fullName;
  private String nfcCode;
  private boolean checked = false;


  public String getIndexNumber() {
    return indexNumber;
  }

  public void setIndexNumber(String indexNumber) {
    this.indexNumber = indexNumber;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(String referenceNumber) {
    this.referenceNumber = referenceNumber;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getNfcCode() {
    return nfcCode;
  }

  public void setNfcCode(String nfcCode) {
    this.nfcCode = nfcCode;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }
}

