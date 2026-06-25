package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
// import java.util.List;
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
      
      // 1. REGISTRATION MODE (Saves the manually added student details directly into 'elements' table)
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
// package com.example.demo;
//
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.client.RestClient;
// import java.util.List;
// import org.springframework.web.util.UriComponentsBuilder;
// import java.util.Map;
//
// @RestController
// @RequestMapping("/api/elements")
// public class ElementController {
//
//   private final ElementRepository repository;
//   private final StudentRepository studentRepository;
//   private final RestClient restClient = RestClient.create();
//
//   public ElementController(ElementRepository repository, StudentRepository studentRepository) {
//     this.repository = repository;
//     this.studentRepository = studentRepository;
//   }
//
//   // Endpoint 1: Call external API on demand and save to Postgres
//
//   // HACK: ========PLACEHOLDER URL USED TEMPORARILY INPLACE OF API URL ============
//
// @PostMapping("/fetch-external")
// public String fetchAndSaveStudents(
//     @RequestParam String startIndex,
//     @RequestParam int limitAmount) {
//
//     // Safely build the URL using the correct query parameters
//       String schoolApiUrl = org.springframework.web.util.UriComponentsBuilder
//           .fromUriString("https://typicode.com") // TODO: Placeholder endpoint for UITS url
//           .queryParam("start", startIndex)
//           .queryParam("limit", limitAmount)
//           .toUriString();
//
//       try {
//           Element[] externalStudents = restClient.get()
//               .uri(schoolApiUrl)
//               .retrieve()
//               .body(Element[].class);
//
//           if (externalStudents != null && externalStudents.length > 0) {
//               // Save all fetched students directly to your database
//               repository.saveAll(java.util.Arrays.asList(externalStudents));
//
//               // Return a success message to the frontend, NOT the database payload
//               return "Successfully imported " + externalStudents.length + " records into the database.";
//           }
//       } catch (Exception e) {
//           return "Failed to fetch from school API: " + e.getMessage();
//       }
//
//       return "No records were found to import.";
//   }
//
//
//   // Endpoint 2: Receive NFC code, compare, and mark checked
//
//
//
//   @PostMapping("/verify-nfc")
//   public String handleNfcTraffic(@RequestBody java.util.Map<String, Object> payload) {
//
//       // 1. REGISTRATION MODE (If full details are sent)
//       if (payload.containsKey("fullName") && payload.containsKey("indexNumber")) {
//           Student newStudent = new Student();
//
//           newStudent.setIndexNumber((String) payload.get("indexNumber"));
//           newStudent.setReferenceNumber((String) payload.get("referenceNumber"));
//           newStudent.setFullName((String) payload.get("fullName"));
//           newStudent.setNfcCode((String) payload.get("incomingNfc")); 
//           newStudent.setChecked(false); 
//
//           repository.save(newStudent);
//           return "Registration Successful: Saved " + newStudent.getFullName() + " to database.";
//       }
//
//       // 2. ATTENDANCE LOGGING MODE (If only ID and NFC are sent)
//       if (payload.containsKey("incomingNfc") && payload.containsKey("indexNumber")) {
//           String indexNumber = (String) payload.get("indexNumber");
//           String incomingNfc = (String) payload.get("incomingNfc");
//
//           // Use indexNumber to lookup because it's marked as @Id
//           Student student = repository.findById(indexNumber)
//               .orElseThrow(() -> new RuntimeException("Student profile not found."));
//
//           if (student.getNfcCode() != null && student.getNfcCode().equals(incomingNfc)) {
//               student.setChecked(true); 
//               repository.save(student);
//               return "NFC Verified and Attendance Marked for " + student.getFullName() + "!";
//           }
//           return "NFC Verification Failed! Card mismatch.";
//       }
//
//       return "Error: Invalid JSON payload structure.";
//   }
//
//
// }
