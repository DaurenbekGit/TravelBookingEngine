// Owner: Member A
package kz.aitu.travel.supplier.adapter;

import java.math.BigDecimal;

/** One deliberately fixed simulation rate; not a live exchange-rate feed. */
public final class SupplierRates {
    public static final BigDecimal USD_TO_KZT = new BigDecimal("500");
    public static final BigDecimal TIYN_TO_KZT = new BigDecimal("0.01");
    private SupplierRates() { }
}
