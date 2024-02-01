package com.readnocry.book.fileutils;

import lombok.Getter;

import java.nio.MappedByteBuffer;

@Getter
public class MappedFileBlock {

    private final MappedByteBuffer buffer;
    private final long start;
    private final long end;

    public MappedFileBlock(MappedByteBuffer buffer, long start, long end) {
        this.buffer = buffer;
        this.start = start;
        this.end = end;
    }

    public boolean isValidForPage(int pageNumber, int pageSize) {
        long pageStart = (long) pageNumber * pageSize;
        return pageStart >= start && pageStart < end;
    }
}
