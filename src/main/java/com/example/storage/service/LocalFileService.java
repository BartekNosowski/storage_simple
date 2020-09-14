package com.example.storage.service;

import com.example.storage.exceptions.ConfigPropertyException;
import com.example.storage.model.LocalFile;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.example.storage.config.Constans.*;


@Service
public class LocalFileService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileService.class);

//    @Value("${files.path}")
    private String uploads = "C:\\Users\\User\\IdeaProjects\\storage\\files\\";

    public LocalFileService() {
        try {
            createContextDirectory();
        } catch (ConfigPropertyException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createContextDirectory() throws ConfigPropertyException, IOException {
        if (Strings.isBlank(this.uploads)) {
            logger.error("Cannot get path to files");
            throw new ConfigPropertyException("Cannot get path to files");
        }

        Path path = Paths.get(uploads);
        if (Files.notExists(path)) {
            try {
                logger.info("Try to create directory: {}", path);
                Files.createDirectories(path);
            } catch (IOException e) {
                logger.error("Cannot create directory: {}. Exception: {}", path, e.getMessage());
                throw new IOException (e.getMessage());
            }
        }
    }
    public ResponseEntity<?> getFile(String fileName){
        UrlResource resource;
        Path path = Paths.get(uploads+ fileName);

        try {
            resource = new UrlResource(path.toUri());
        }
        catch (MalformedURLException e){
            logger.error("Cannot get file: {}",e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        File targetFile;
        try {
            targetFile = resource.getFile();
        } catch (IOException e) {
            logger.error("Cannot get file: {}",e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        String contentType;
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            logger.error("Cannot get file: {}",e.getMessage());
            return ResponseEntity
                    .ok()
                    .body(e.getMessage());
        }
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=\""+targetFile.getName()+"\"")
                .header("Cookie","ciastkoApp=xxxvvv12345;idVisit=abcdefgh")
                .contentLength(targetFile.length())
                .body(resource);
    }

    public List<LocalFile> getFiles() {
        Stream<Path> files;
        try {
            logger.info("try to get all files");
            files = Files.walk(Paths.get(this.uploads))
                    .filter(Files::isRegularFile);
        } catch (IOException e){
            logger.error("Cannot get files: {}", e.getMessage());
            return null;
        }

        List<LocalFile> localFiles = new ArrayList<>();
        files.forEach(
                f -> {
                    BasicFileAttributes bs;
                    try {
                        bs = Files.readAttributes(f,BasicFileAttributes.class);
                    }
                    catch (IOException e){
                        logger.error("cannot get read file: {}", e.getMessage());
                        return;
                    }
                    String downloadUri = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path(DOWNLOAD_URI) // sciezka do konkretnego end-pointa
                            .path(f.getFileName().toString())
                            .toUriString();

                    // todo dorobić ścieżkę do kasowania pliku
                    String deleteUri = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path(DELETE_URI) // sciezka do konkretnego end-pointa
                            .path(f.getFileName().toString())
                            .toUriString();

                    LocalFile localFile = new LocalFile();
                    localFile.setName(f.getFileName().toString());
                    localFile.setCreationTime(bs.creationTime().toString());
                    localFile.setLastModified(bs.lastModifiedTime().toString());
                    localFile.setSize(bs.size());
                    localFile.setDownloadUri(downloadUri);
                    localFile.setDeleteUri(deleteUri);
                    try {
                        localFile.setFileType(Files.probeContentType(f.toAbsolutePath()));
                    } catch (IOException e) {
                        logger.error("Error while getting probeContentType: {}",e.getMessage());
                        return;
                    }
                    localFiles.add(localFile);
                }
        );
    return localFiles;
    }
    public ResponseEntity<String> deleteFile(String filename){
        File file = new File(uploads + filename);
        if (file.delete()){
            logger.info("Deleted file: {}"+file.getName());
            return new ResponseEntity<>("Deleted file: {}"+ file.getName(),HttpStatus.OK);
        }
        logger.error("File not found: {}"+file.getName());
        return new ResponseEntity<>("File not found: {}"+ file.getName(),HttpStatus.NOT_FOUND);
    }

}
