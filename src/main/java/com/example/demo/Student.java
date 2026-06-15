package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
  @Id
  private String indexNumber;
  private String refNumber;

  private String fullName;

  public String indexNumber() {
    return indexNumber;
  }

  public void indexNumber(String indexNumber) {
    this.indexNumber = indexNumber;
  }

  public String refNumber() {
    return refNumber;
  }

  public void refNumber(String refNumber) {
    this.refNumber = refNumber;
  }

  public String fullName() {
    return fullName;
  }

  public void fullName(String fullName) {
    this.fullName = fullName;
  }

}
