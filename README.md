# FILO

Unification solver for the description logic **FL₀**.

---

## Overview

**FILO** is a tool for solving unification problems in the description logic FL₀. It is an application written in Java using OWL API and Maven for dependency management. As for now it is a standalone application (`FILO.jar`). The application can be easily opened by double-clicking the file (on Windows) or by running the command `java -jar Filo.jar` in the terminal (on Linux). The compiled file is available [here](https://unifdl.cs.uni.opole.pl/unificator-app-for-the-description-logic-fl_0/).

The pipeline of the project is organized into three main stages:

1. **Basic Processing** (`Filo-basis`)  
2. **Formula Flattening** (`Filo-flattening`)  
3. **Unification Algorithm** (`Filo-unifier`)  

<!--It supports computing most general unifiers, verifying correctness and minimality of solutions, and integrates easily with external ontology-processing tools.-->

---

## Features

- Supports core FL₀ constructors: conjunction, disjunction, role value restrictions, top constructor
- Automatic flattening of nested concept expressions  
- Efficient implementation of the unification algorithm  
- Extensible framework for adding new heuristics or logic fragments  

---

## Installation

1. Make sure you have installed:
- Java Development Kit (JDK) 8 or higher
- Maven
- Git (with configured SSH access)

2.  Clone the repository:
```
git clone git@github.com:barbmor/FILO.git
```
3.  Navigate to the project directory:
```
cd FILO
```
4.  Build the project using Maven:
```
mvn clean install
```

## Tutorial

The main application window, shown in the image below, is divided into three sections. The input section allows the user to select a predefined test from a dropdown menu or to choose an ontology file with a .owx or .owl extension. The output section displays the results of the unification process, including any discovered unifiers and relevant messages. The options panel provides controls for setting the log level, as well as buttons for saving the log file ("Save log file") and displaying diagnostic data ("Show statistics"). The example below illustrates the application interface together with the statistics window.

<img src="https://github.com/user-attachments/assets/3ac478ac-b002-49a5-ab38-8f9d03a1d4a6"
     alt="FiloMainWindowWithStatistics"
     width="70%" />

---

## Authors

Michał Henne,
[Sławomir Kost](https://ii.wmfi.uni.opole.pl/pracownicy/slawomir-kost/),
Dariusz Marzec,
[Barbara Morawska](https://ii.wmfi.uni.opole.pl/pracownicy/barbara-morawska)
