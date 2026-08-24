# Warehouse Management System

A Java-based console application modeling a role-based warehouse management
workflow. Built on strict Object-Oriented Programming principles, it features
role-based terminal access, package-structured logic, storage capacity
tracking, and state persistence via flat-file data streams.

## 🚀 Features

- **Role-Based Access Control** — distinct terminal interfaces and actions for three system actors:
  - **Director (`Riaditel`)** — HR tasks: hiring and dismissing warehouse workers.
  - **Worker (`Pracovnik`)** — navigates storage rooms, adjusts inventory, handles goods inside racks.
  - **Customer (`Zakaznik`)** — deposits items and retrieves them by unique identifier.
- **Storage Capacity Management** — large warehouses (`VelkySklad`) with expandable racks vs. small warehouses (`MalySklad`) with rigid fixed capacity ceilings.
- **State Persistence** — on exit, the current state (rooms, items, employees, users) is serialized to a text file and cleanly restored on the next launch.
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

## 📁 Architecture & Packages

| Package | Key components | Responsibility |
|---|---|---|
| *(root)* | `Main` | bootstrap, configuration loading, login menu |
| `sklad` | `Sklad`, `IIdentifikovatelny`, `VytvaracTovaru` | central warehouse state, ID schema, item-creation helper |
| `sklad.miestnosti` | `ISkladovaMiestnost`, `Miestnost`, `VelkySklad`, `MalySklad` | room layouts, employee directories, capacity rules |
| `sklad.osoby` | `Osoba`, `Pracovnik`, `Riaditel`, `Zakaznik` | personnel and external actor models |
| `sklad.predmety` | `Regal`, `Tovar` | racks and stored goods |
| `sklad.terminaly` | `ITerminal`, `Terminal[Actor]` | privilege-mapped console workflows |
| `sklad.pracaSoSuborom` | `CitacSuboru`, `ZapisovacSuboru` | low-level read/write persistence |

## 🛠️ Build

Requires **JDK 11+**.

> Note: `src/**/*.java` globs do not expand on Windows shells, so compile from a sources list instead.

**Windows (PowerShell):**
```powershell
Get-ChildItem -Recurse src -Filter *.java |
  ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -d out "@sources.txt"
jar cfe WarehouseSystem.jar sk.uniza.fri.Main -C out .
```

**Linux / macOS:**
```bash
find src -name "*.java" > sources.txt
javac -d out "@sources.txt"
jar cfe WarehouseSystem.jar sk.uniza.fri.Main -C out .
```

Alternatively, open the project in IntelliJ IDEA and run `sk.uniza.fri.Main` directly.

## ▶️ Run

```bash
java -jar WarehouseSystem.jar
```

## 💾 Persistence

On exit the application saves the warehouse state to a plain-text data file
next to the program and restores it automatically on startup. Delete the file
to reset the system to a fresh state.

## ℹ️ Notes

Generated `JavaDoc/` output and compiled artifacts are committed for
convenience; `src/` remains the single source of truth.
