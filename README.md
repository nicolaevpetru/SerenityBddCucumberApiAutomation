# BDD API Automation Project

A Behavior-Driven Development (BDD) style API automation framework built with **Serenity BDD**, **Cucumber**, and **JUnit 5**. This framework allows you to write feature files in a human-readable format while leveraging Serenity to generate comprehensive test reports.

---

## Table of Contents
1. [Overview](#overview)
2. [Key Features](#key-features)
3. [Project Structure](#project-structure)
4. [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
5. [Running the Tests](#running-the-tests)
    - [Via Maven Commands](#via-maven-commands)
    - [Via IDE](#via-ide)
6. [Configuration and Environments](#configuration-and-environments)
7. [Encryption with Jasypt](#encryption-with-jasypt)
8. [Serenity Reports](#serenity-reports)
9. [Writing and Organizing Tests](#writing-and-organizing-tests)
10. [Contributing](#contributing)
11. [License](#license)

---

## Overview

This project automates API tests using a **BDD** approach with **Cucumber** and **Serenity**. It enables teams to:

- Write human-readable test scenarios in `.feature` files.
- Store test data and expected values in data tables for clarity and maintainability.
- Generate rich, visual test reports with Serenity.
- Utilize best practices with parameterized endpoints, dynamic payload creation, and a flexible approach to query parameters.

The framework simplifies API testing by handling authentication, requests, and validations through shared libraries and steps.

---

## Key Features

1. **Serenity BDD Integration**  
   Generates detailed test reports (HTML, JSON) with step-by-step visualizations.

2. **Cucumber BDD**  
   Allows creating test scenarios in a Gherkin (`.feature`) syntax, making tests easier to understand for both technical and non-technical stakeholders.

3. **JUnit 5**  
   Offers modern testing capabilities, including parameterized tests, better readability, and extensible test lifecycle methods.

4. **Rest-Assured**  
   Simplifies HTTP request construction and response validation for RESTful API testing.

5. **Jasypt Integration**  
   Manages sensitive data by providing encryption and decryption support within the project.

6. **Modular and Extensible**  
   Provides an easy-to-extend approach to handle additional APIs, new authentication methods, or complex test data requirements.

---

## Project Structure

Below is a high-level overview of the project:

- **`CommonSteps.java`**  
  Contains step definitions for constructing requests, sending them to APIs, verifying responses, and storing dynamic data.

- **`ApiRequestManagerService`** (under `utilities` package)  
  Manages all HTTP requests (GET, POST, PUT, DELETE) and query parameters.

- **`ObjectStore.java`** (under `utilities` package)  
  A shared store for saving data (e.g., response fields or generated values) across test steps and scenarios.

- **`ConfigManager.java`** (under `utilities` package)  
  Handles configuration values from property files or environment variables.

---

## Getting Started

### Prerequisites

1. **Java 17** (as indicated in the `pom.xml`)
    - Ensure JDK 17 is installed.
    - Check version with: `java -version`
2. **Maven 3.8+**
    - Ensure Maven is installed and accessible from the command line.
    - Check version with: `mvn -v`
3. **Git** (optional but recommended)
    - For cloning and managing your project repository.

