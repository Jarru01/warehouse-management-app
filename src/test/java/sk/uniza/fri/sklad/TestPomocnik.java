package sk.uniza.fri.sklad;

import sk.uniza.fri.sklad.terminaly.Vstup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class TestPomocnik {
    private TestPomocnik() {
    }

    public static Vstup vstup(String... riadky) {
        String data = String.join(System.lineSeparator(), riadky) + System.lineSeparator();
        return new Vstup(new Scanner(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))));
    }

    public static String zachytVystup(Runnable akcia) {
        PrintStream povodny = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            akcia.run();
        } finally {
            System.setOut(povodny);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
