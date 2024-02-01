package com.readnocry.entity.enums;

public enum PageSize {

    MIN(1200),
    XS(1600),
    S(2000),
    M(2400),
    L(2800),
    XL(3200),
    XXL(3600),
    XXXL(4000),
    MAX(4400);

    private final int bytePageSize;

    PageSize(int bytePageSize) {
        this.bytePageSize = bytePageSize;
    }

    public int getBytePageSize() {
        return bytePageSize;
    }
}
