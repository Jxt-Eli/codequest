package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import java.util.List;

@RestController
@RequestMapping("/api/elements")
public class ElementController {

  private final ElementRepository repository;
  private final StudentRepository studentRepository;
  private final RestClient restClient = RestClient.create();

  public ElementController(ElementRepository repository, StudentRepository studentRepository) {
    this.repository = repository;
    this.studentRepository = studentRepository;
  }

  // Endpoint 1: Call external API on demand and save to Postgres

  // Replace your old /fetch-external endpoint with this:
  @PostMapping("/fetch-external")
  public String fetchAndSaveStudents(
      @RequestParam String startIndex,
      @RequestParam int limitAmount) {

    // 1. Build the school API URL dynamically with parameters
    String schoolApiUrl = "https://typicode.com" + startIndex + "&limit=" + limitAmount;

    // 2. Fetch data. Spring automatically converts the JSON array into a Java Array
    // of Students!
    Student[] externalStudents = restClient.get()
        .uri(schoolApiUrl)
        .retrieve()
        .body(Student[].class); // Magic: converts JSON array to Student[]

    if (externalStudents != null) {
      for (Student student : externalStudents) {
        // 3. Save each student into your Postgres database
        studentRepository.save(student);
      }
      return "Successfully synced " + externalStudents.length + " students!";
    }

    return "No student records retrieved.";
  }

  // Endpoint 2: Receive NFC code, compare, and mark checked
  @PostMapping("/verify-nfc")
  public String verifyNfc(@RequestParam String id, @RequestParam String incomingNfc) {
    Element element = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Element not found"));

    if (element.getNfcCode() != null && element.getNfcCode().equals(incomingNfc)) {
      element.setChecked(true);
      repository.save(element);
      return "NFC Verified and Element Checked!";
    }
    return "NFC Verification Failed!";
  }

  // Endpoint 3: Pull all unchecked elements
  @GetMapping("/unchecked")
  public List<Element> getUnchecked() {
    return repository.findByCheckedFalse();
  }

  // Endpoint 4: Backup confirmation using Unique ID
  @PostMapping("/{id}/check-backup")
  public Element backupCheck(@PathVariable String id) {
    Element element = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Element not found"));

    element.setChecked(true);
    return repository.save(element);
  }
}
