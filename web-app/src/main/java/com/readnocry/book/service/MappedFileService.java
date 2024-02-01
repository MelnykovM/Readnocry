package com.readnocry.book.service;

import com.readnocry.entity.BookMetaData;

import java.io.IOException;

public interface MappedFileService {

    String readNextPage(BookMetaData bookMetaData) throws IOException;

    String readPreviousPage(BookMetaData bookMetaData) throws IOException;

    String readRandomPage(BookMetaData bookMetaData, int pageNumber) throws IOException;
}
