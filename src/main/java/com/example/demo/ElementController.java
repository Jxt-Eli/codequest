package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Arrays;
import java.util.List;

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

  // NOTE: Endpoint 1: Call external API on demand and save to the 'students'
  // table
  @PostMapping("/fetch-external")
  public String fetchAndSaveStudents(
      @RequestParam String startIndex,
      @RequestParam int limitAmount) {

    // FIX: ==========Replace with UITS URL=============
    String schoolApiUrl = UriComponentsBuilder
        .fromUriString("https://typicode.com") // TODO: Placeholder endpoint for UITS URL
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

  // NOTE: Endpoint 2: Receive NFC code, compare, and mark checked (smart
  // multipurpose endpoint)
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

  // NOTE: Endpoint 3: Pull full names of all unchecked students
  @GetMapping("/unchecked")
  public List<String> getUncheckedStudentNames() {
    // 1. Get all element rows where checked is false
    List<Element> uncheckedElements = repository.findByCheckedFalse();

    // 2. Map those records to their actual full names from the students registry
    return uncheckedElements.stream()
        .map(element -> {
          return studentRepository.findById(element.getIndexNumber())
              .map(Student::getFullName)
              .orElse("Unknown Student (" + element.getIndexNumber() + ")");
        })
        .toList();
  }

  // NOTE: Endpoint 4: Backup confirmation using Index Number (Manual Override)
  @PostMapping("/{indexNumber}/check-backup")
  public String backupCheck(@PathVariable String indexNumber) {

    // 1. Verify the student actually exists in the core registration registry
    Student studentProfile = studentRepository.findById(indexNumber)
        .orElseThrow(() -> new RuntimeException("Student not found in registry with index: " + indexNumber));

    // 2. Fetch their daily record or initialize a blank one if they haven't scanned
    // yet
    Element attendanceRecord = repository.findById(indexNumber)
        .orElse(new Element());

    // 3. Force the verification parameters manually
    attendanceRecord.setIndexNumber(indexNumber);
    attendanceRecord.setChecked(true); // Explicitly mark them present

    repository.save(attendanceRecord);

    return "Manual Backup Success: Attendance marked for " + studentProfile.getFullName() + "!";
  }
}
