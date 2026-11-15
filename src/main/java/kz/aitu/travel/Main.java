// Owner: Member C
package kz.aitu.travel;

import kz.aitu.travel.config.AppContext;

/** Application launcher; composition is delegated to AppContext. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        AppContext.run(args, System.in, System.out);
    }
}
