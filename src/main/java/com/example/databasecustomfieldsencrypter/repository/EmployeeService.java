package com.example.databasecustomfieldsencrypter.repository;

import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

  public EmployeeService(
      EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }
  
}
