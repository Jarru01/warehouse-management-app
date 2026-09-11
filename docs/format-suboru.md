# Save File Format (`sklad.dat`)

The warehouse state is stored as a UTF-8 JSON document named `sklad.dat` next
to the executable. The top-level object carries a `verzia` (version) field; the
current version is `1`. Object references are stored by ID (never nested), so
the document is acyclic and human-readable.

Writes are atomic: the JSON is first written to `sklad.dat.tmp` and then moved
over `sklad.dat`. An unreadable file or a file with an unsupported `verzia` is
deleted and replaced by a fresh state.

## Example

```json
{
  "verzia": 1,
  "riaditel": { "id": "123", "meno": "Juraj", "priezvisko": "Solensky" },
  "miestnosti": [
    {
      "kluc": "skladTovaru",
      "typ": "velky",
      "regale": [ { "kapacita": 20, "tovar": ["100"] } ]
    },
    {
      "kluc": "prijemTovaru",
      "typ": "maly",
      "regale": [ { "kapacita": 5, "tovar": [] } ]
    },
    {
      "kluc": "vydajTovaru",
      "typ": "maly",
      "regale": [ { "kapacita": 5, "tovar": [] } ]
    }
  ],
  "tovar": [
    {
      "id": "100",
      "nazov": "Tovar100",
      "vaha": 500,
      "odosielatel": { "id": "10", "meno": "Jan", "priezvisko": "Novy" },
      "prijemca": { "id": "20", "meno": "Peter", "priezvisko": "Novak" }
    }
  ],
  "pracovnici": [
    {
      "id": "5",
      "meno": "Peter",
      "priezvisko": "Novak",
      "miestnost": "skladTovaru",
      "drzanyTovar": null
    }
  ]
}
```

## Field reference

| Field | Type | Meaning |
|---|---|---|
| `verzia` | integer | Format version. Currently `1`. |
| `riaditel` | object | Director: `id`, `meno`, `priezvisko`. |
| `miestnosti` | array | Rooms. `kluc` is the unique room key, `typ` is `velky` or `maly`. |
| `miestnosti[].regale` | array | Racks. `kapacita` is the slot count, `tovar` lists item IDs in slot order. |
| `tovar` | array | Every item exactly once: `id`, `nazov`, `vaha` (grams), `odosielatel`, `prijemca`. |
| `pracovnici` | array | Workers: `id`, `meno`, `priezvisko`, `miestnost` (room key), `drzanyTovar` (item ID or `null`). |

## Invariants

- `miestnosti` must contain `skladTovaru` (type `velky`), `prijemTovaru`
  (type `maly`) and `vydajTovaru` (type `maly`).
- A `maly` room has exactly one rack and its `kapacita` is fixed at `5`.
- Item IDs are unique across `tovar`; every ID listed in a rack or in
  `drzanyTovar` must exist in `tovar`.
- Worker IDs are unique; `miestnost` must reference an existing room key.
- An item is either in a rack or held by a worker, never both.

## Versioning

Any breaking change to the schema increments `verzia`; the loader rejects
unknown versions (the file is deleted and a fresh state starts). Additive
changes with sensible defaults may keep the version and rely on Jackson's
unknown-property tolerance.
