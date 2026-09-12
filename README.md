# Warehouse Management System

[![build](https://github.com/Jarru01/warehouse-management-system/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Jarru01/warehouse-management-system/actions/workflows/build.yml)

A Java-based console application modeling a role-based warehouse management
workflow. Built on strict Object-Oriented Programming principles, it features
role-based terminal access, package-structured logic, storage capacity
tracking, and state persistence via a documented JSON save file.

## 🚀 Features

- **Role-Based Access Control** — distinct terminal interfaces and actions for three system actors:
  - **Director (`Riaditel`)** — HR tasks: hiring and dismissing warehouse workers, managing racks.
  - **Worker (`Pracovnik`)** — navigates storage rooms, adjusts inventory, handles goods inside racks.
  - **Customer (`Zakaznik`)** — deposits items and retrieves them by unique identifier.
- **Storage Capacity Management** — large warehouses (`VelkySklad`) with expandable racks vs. small warehouses (`MalySklad`) with rigid fixed capacity ceilings.
- **State Persistence** — the current state (rooms, items, employees, users) is saved as JSON to `sklad.dat` after every action and restored on startup. Saves are written atomically and an unreadable save file is discarded in favor of a fresh state.
- **Modular Architecture** — domain entities, interfaces, file handling, and console terminals are decoupled into functional packages.

## 📖 Domain Glossary

The code uses Slovak domain names. Quick reference:

| Identifier | Meaning |
|---|---|
| `Sklad` | warehouse |
| `miestnosti` | rooms |
| `Regal` | storage rack |
| `Tovar` | goods / item |
| `osoby` | people |
| `terminaly` | interactive console terminals |
| `pracaSoSuborom` | file handling |

## 🔑 Login

| Actor | Identification |
|---|---|
| Director (`Riaditel`) | ID `123` (default director defined in `Sklad`) |
| Worker (`Pracovnik`) | Any ID created by the director; three attempts allowed |
| Customer (`Zakaznik`) | ID, first name and last name; enter `ukonci` to return to the main menu |

## 📁 Architecture & Packages

| Package | Key components | Responsibility |
|---|---|---|
| *(root)* | `Main` | bootstrap, configuration loading, login menu |
| `sklad` | `Sklad`, `IIdentifikovatelny`, `VytvaracTovaru`, `Validacia` | central warehouse state, ID schema, console item creation, input validation |
| `sklad.miestnosti` | `ISkladovaMiestnost`, `Miestnost`, `VelkySklad`, `MalySklad` | room layouts, employee directories, capacity rules |
| `sklad.osoby` | `Osoba`, `Pracovnik`, `Riaditel`, `Zakaznik` | personnel and external actor models |
| `sklad.predmety` | `Regal`, `Tovar` | racks and stored goods |
| `sklad.terminaly` | `ITerminal`, `Vstup`, `TerminalPracovnika`, `TerminalRiaditela`, `TerminalZakaznika` | privilege-mapped console workflows, shared console input |
| `sklad.pracaSoSuborom` | `CitacSuboru`, `ZapisovacSuboru`, `SkladMapper`, `SkladData` and nested DTOs | documented JSON persistence: mapping, load, atomic write, invalid-file recovery |

## 🛠️ Build

Requires **JDK 21** — the Gradle build targets a Java 21 toolchain.

```bash
./gradlew build        # compile and run tests
./gradlew run          # start the application
./gradlew jar          # build a runnable jar
./gradlew javadoc      # generate API documentation
```

On Windows use `gradlew.bat` instead of `./gradlew`. The runnable jar is
written to `build/libs/warehouse-management-system-1.0.0.jar`.

Alternatively, open the project in IntelliJ IDEA as a Gradle project and run
`sk.uniza.fri.Main` directly.

## ✅ Tests

```bash
./gradlew test         # run the JUnit 5 test suite
```

The suite covers domain rules (racks, rooms, workers, picking/placing
goods), validation, role terminals, the item creator, and persistence
round-trips including corruption recovery.

## ▶️ Run

```bash
./gradlew run
# or
java -jar build/libs/warehouse-management-system-1.0.0.jar
```

## 💾 Persistence

On every change the application saves the warehouse state as UTF-8 JSON to a
file named `sklad.dat` next to the program and restores it automatically on
startup. Saves are written to a temporary file first and then moved into place,
so an interrupted save cannot destroy existing data. The format is documented
in [`docs/format-suboru.md`](docs/format-suboru.md). If the save file is
unreadable or uses an unsupported version, it is deleted and the system starts
from a fresh state. Delete `sklad.dat` to reset the system manually.

## ℹ️ Notes

`src/` is the single source of truth. Build output, generated documentation,
and save files are not committed; generate API documentation with
`./gradlew javadoc`.
