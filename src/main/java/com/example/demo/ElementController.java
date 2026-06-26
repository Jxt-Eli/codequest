package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/elements")
public class ElementController {

  private final ElementRepository repository; // Maps to elements table
  private final StudentRepository studentRepository; // Maps to students table
  private final RestClient restClient = RestClient.create();

  public ElementController(ElementRepository repository, StudentRepository studentRepository) {
    this.repository = repository;
    this.studentRepository = studentRepository;
  }

  // Endpoint 1: Call external API on demand and save to the 'students' table
  @PostMapping("/fetch-external")
  public String fetchAndSaveStudents(
      @RequestParam String startIndex,
      @RequestParam int limitAmount) {

    String schoolApiUrl = UriComponentsBuilder
        .fromUriString("https://typicode.com") // Placeholder endpoint
        .queryParam("start", startIndex)
        .queryParam("limit", limitAmount)
        .toUriString();

    try {
      // Realigned: Converts JSON to Student array instead of Element array
      Student[] externalStudents = restClient.get()
          .uri(schoolApiUrl)
          .retrieve()
          .body(Student[].class);

      if (externalStudents != null && externalStudents.length > 0) {
        // Realigned: Saves explicitly to the studentRepository (students table)
        studentRepository.saveAll(Arrays.asList(externalStudents));
        return "Successfully imported " + externalStudents.length + " student records into the registry.";
      }
    } catch (Exception e) {
      return "Failed to fetch from school API: " + e.getMessage();
    }

    return "No records were found to import.";
  }

  // Endpoint 2: Receive NFC code, compare, and mark checked
  @PostMapping("/verify-nfc")
  public String handleNfcTraffic(@RequestBody Map<String, Object> payload) {

    // 1. REGISTRATION MODE (Saves the manually added student details directly into
    // 'elements' table)
    if (payload.containsKey("fullName") && payload.containsKey("indexNumber")) {
      Element newElement = new Element();

      // Realigned: Setting values and saving to 'repository' (elements table)
      newElement.setIndexNumber((String) payload.get("indexNumber"));
      newElement.setNfcCode((String) payload.get("incomingNfc"));
      newElement.setChecked(false);

      repository.save(newElement);
      return "Registration Successful: Added student card data to elements table.";
    }

    // 2. ATTENDANCE LOGGING MODE (Compares and updates the 'elements' table)
    if (payload.containsKey("incomingNfc") && payload.containsKey("indexNumber")) {
      String indexNumber = (String) payload.get("indexNumber");
      String incomingNfc = (String) payload.get("incomingNfc");

      // Realigned: Looks up directly inside the 'elements' table
      Element element = repository.findById(indexNumber)
          .orElseThrow(() -> new RuntimeException("Student record not found in elements table."));

      if (element.getNfcCode() != null && element.getNfcCode().equals(incomingNfc)) {
        element.setChecked(true);
        repository.save(element);
        return "NFC Verified and Attendance Marked!";
      }
      return "NFC Verification Failed! Card mismatch.";
    }

    return "Error: Invalid JSON payload structure.";
  }
}
