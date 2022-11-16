package com.example.databasecustomfieldsencrypter.controller;

import com.example.databasecustomfieldsencrypter.domain.Employee;
import com.example.databasecustomfieldsencrypter.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class EmployeeController {

  @Autowired
  private EmployeeRepository employeeRepository;

  @PostMapping("/create")
  public ResponseEntity<Employee> create() {

    Employee secretString = employeeRepository.save(new Employee("1", "SecretString"));

    return new ResponseEntity<>(secretString, HttpStatus.OK);
  }

  @GetMapping("/create")
  public ResponseEntity<Employee> getEmp() {

    Optional<Employee> byId = employeeRepository.findById("1");
    return new ResponseEntity<>(byId.get(), HttpStatus.OK);
  }
}
