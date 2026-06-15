package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String> {
  // This gives you save(), findById(), and delete() for students automatically!
}
