package com.readnocry.book.service;

public interface BookService {

    String getPageContent(Long bookMetaDataId, int pageNum);

    String getNextPageContent(Long bookMetaDataId);

    String getPreviousPageContent(Long bookMetaDataId);
}
