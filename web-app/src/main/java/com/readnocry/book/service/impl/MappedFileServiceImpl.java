package com.readnocry.book.service.impl;

import com.readnocry.book.enums.ReadPageCommand;
import com.readnocry.book.fileutils.MappedFileBlock;
import com.readnocry.book.service.MappedFileService;
import com.readnocry.entity.BookMetaData;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.readnocry.book.enums.ReadPageCommand.*;

@Service
@Log4j
public class MappedFileServiceImpl implements MappedFileService {

    private ConcurrentHashMap<String, MappedFileBlock> bufferCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> memorizedPart = new ConcurrentHashMap<>();

    public String readNextPage(BookMetaData bookMetaData) throws IOException {
        log.info("Read next page content from book: " + bookMetaData);
        String filePath = bookMetaData.getFilePath();
        int pageNumber = bookMetaData.getPage() + 1;
        String uncroppedResult = readPage(bookMetaData, pageNumber);
        return cropResult(uncroppedResult, filePath, NEXT_PAGE);
    }

    public String readPreviousPage(BookMetaData bookMetaData) throws IOException {
        log.info("Read previous page content from book: " + bookMetaData);
        String filePath = bookMetaData.getFilePath();
        int pageNumber = bookMetaData.getPage() - 1;
        String uncroppedResult = readPage(bookMetaData, pageNumber);
        return cropResult(uncroppedResult, filePath, PREVIOUS_PAGE);
    }

    public String readRandomPage(BookMetaData bookMetaData, int pageNumber) throws IOException {
        log.info("Read random page content from book: " + bookMetaData + ". Page num: " + pageNumber);
        String filePath = bookMetaData.getFilePath();
        String uncroppedResult = readPage(bookMetaData, pageNumber);
        return cropResult(uncroppedResult, filePath, RANDOM_PAGE);
    }

    private String readPage(BookMetaData bookMetaData,
                            int pageNumber) throws IOException {
        log.info("Read page content from book: " + bookMetaData + ". Page num: " + pageNumber);
        String filePath = bookMetaData.getFilePath();
        int pageSize = bookMetaData.getPageSize();
        final int blockPages = 10;
        final long blockSize = (long) blockPages * pageSize;

        MappedFileBlock block = bufferCache.get(filePath);
        if (block == null || !block.isValidForPage(pageNumber, pageSize)) {
            block = mapFile(filePath, pageNumber, pageSize, blockSize);
            bufferCache.put(filePath, block);
        }
        MappedByteBuffer buffer = block.getBuffer();
        long pageStart = (long) pageNumber * pageSize - block.getStart();
        long pageEnd = Math.min(buffer.limit(), pageStart + pageSize);

        byte[] bytes = new byte[(int) (pageEnd - pageStart)];
        synchronized (buffer) {
            buffer.position((int) pageStart);
            buffer.get(bytes, 0, bytes.length);
        }
        return new String(bytes, Charset.forName(bookMetaData.getCharset()));
    }

    private String cropResult(String uncroppedResult,
                              String filePath,
                              ReadPageCommand command) {
        log.info("Crop result for book: " + filePath);
        int stringLength = uncroppedResult.length();
        if (stringLength >= 500) {
            String lastCharacters = uncroppedResult.substring(stringLength - 500);
            int lastIndex = -1;
            Pattern pattern = Pattern.compile("(?<!\\bMr|\\bMrs)\\b[.!?;]");
            Matcher matcher = pattern.matcher(lastCharacters);

            while (matcher.find()) {
                lastIndex = matcher.start();
            }

            String lastSentence = "";
            if (lastIndex != -1) {
                lastSentence = lastCharacters.substring(lastIndex + 1);
            }
            String croppedResult = uncroppedResult.substring(0, stringLength - lastSentence.length());
            return addAndMemorizeCutPart(croppedResult, filePath, command, lastSentence);
        } else {
            return uncroppedResult;
        }
    }

    private String addAndMemorizeCutPart(String croppedResult,
                                         String filePath,
                                         ReadPageCommand command,
                                         String remains) {
        String result = "";
        if (command.equals(NEXT_PAGE)) {
            result = memorizedPart.get(filePath) + croppedResult;
            memorizedPart.put(filePath, remains);
        }
        if (command.equals(PREVIOUS_PAGE) || command.equals(RANDOM_PAGE)) {
            result = croppedResult;
            memorizedPart.put(filePath, remains);
        }
        return result;
    }

    private MappedFileBlock mapFile(String filePath,
                                    int pageNumber,
                                    int pageSize,
                                    long blockSize) throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of(filePath), StandardOpenOption.READ)) {
            long start = (long) pageNumber * pageSize;
            start = start / blockSize * blockSize;
            long end = Math.min(channel.size(), start + blockSize);
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, start, end - start);
            return new MappedFileBlock(buffer, start, end);
        }
    }
}

