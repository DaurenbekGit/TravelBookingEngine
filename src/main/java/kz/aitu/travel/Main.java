
package kz.aitu.travel;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import kz.aitu.travel.config.AppContext;

/** Application launcher; composition is delegated to AppContext. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        PrintStream utf8Output = new PrintStream(
                System.out,
                true,
                StandardCharsets.UTF_8
        );

        AppContext.run(args, System.in, utf8Output);
    }
}
