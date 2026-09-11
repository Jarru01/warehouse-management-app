package sk.uniza.fri.sklad;

/**
 * Pomocna trieda so statickymi metodami na overovanie textovych vstupov.
 * @author Juraj
 */
public final class Validacia {
    private Validacia() {
    }

    /**
     * Overi, ci je vstup neprzadny a obsahuje iba cislice.
     * @param vstup overovany vstup
     * @return true ak je vstup platne cislo
     */
    public static boolean jeCislo(String vstup) {
        return vstup != null && !vstup.isEmpty() && vstup.chars().allMatch(Character::isDigit);
    }

    /**
     * Overi, ci je vstup neprzadny a obsahuje iba pismena.
     * @param vstup overovany vstup
     * @return true ak je vstup platny text
     */
    public static boolean jeText(String vstup) {
        return vstup != null && !vstup.isEmpty() && vstup.chars().allMatch(Character::isLetter);
    }

    /**
     * Overi, ci je vstup neprzadny a obsahuje iba pismena alebo cislice.
     * @param vstup overovany vstup
     * @return true ak je vstup platny nazov
     */
    public static boolean jeNazov(String vstup) {
        return vstup != null && !vstup.isEmpty() && vstup.chars().allMatch(Character::isLetterOrDigit);
    }

    /**
     * Overi, ci je vstup platne kladne cele cislo.
     * @param vstup overovany vstup
     * @return true ak je vstup kladne cislo
     */
    public static boolean jeKladneCislo(String vstup) {
        if (!jeCislo(vstup)) {
            return false;
        }
        try {
            return Integer.parseInt(vstup) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
