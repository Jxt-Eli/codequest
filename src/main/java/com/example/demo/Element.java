package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "elements")
public class Element {
  @Id
  private String id;
  private String nfcCode;

  private boolean checked;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
