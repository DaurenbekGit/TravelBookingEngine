// Owner: Abylay
package kz.aitu.travel.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

/** Immutable KZT amount; all monetary arithmetic rounds half up to tiyn. */
public record Money(BigDecimal amount) implements Comparable<Money> {
    public static final Money ZERO = Money.of("0");

    public Money {
        Objects.requireNonNull(amount, "Amount is required");
        if (amount.signum() < 0) throw new IllegalArgumentException("Price cannot be negative");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(String value) { return new Money(new BigDecimal(value)); }
    public Money plus(Money other) { return new Money(amount.add(other.amount)); }
    public Money times(BigDecimal factor) { return new Money(amount.multiply(factor)); }
    public Money times(long factor) { return times(BigDecimal.valueOf(factor)); }
    public Money percent(BigDecimal rate) { return times(rate.movePointLeft(2)); }
    public Money percent(int rate) { return percent(BigDecimal.valueOf(rate)); }
    public Money dividedBy(long divisor) {
        if (divisor <= 0) throw new IllegalArgumentException("Divisor must be positive");
        return new Money(amount.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP));
    }
    public Money minus(Money other) { return new Money(amount.subtract(other.amount)); }
    public Money min(Money other) { return compareTo(other) <= 0 ? this : other; }
    @Override public int compareTo(Money other) { return amount.compareTo(other.amount); }

    @Override public String toString() {
        var symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.##", symbols).format(amount) + " KZT";
    }
}
