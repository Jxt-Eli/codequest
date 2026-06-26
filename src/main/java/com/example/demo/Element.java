package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "elements")
public class Element {

  @Id // Required: Marks indexNumber as the primary key for the 'elements' table
  private String indexNumber;
  private String nfcCode;
  private boolean checked = false;

  // --- GETTERS & SETTERS ---

  public String getIndexNumber() {
    return indexNumber;
  }

  public void setIndexNumber(String indexNumber) {
    this.indexNumber = indexNumber;
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
