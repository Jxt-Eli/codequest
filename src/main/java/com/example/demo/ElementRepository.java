package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ElementRepository extends JpaRepository<Element, String> {
  List<Element> findByCheckedFalse();
}
