package com.fileshare.prj.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
@Service
public class FileService {
    private final Path fileStorageLocation= Paths.get("src/main/resources/static").toAbsolutePath().normalize();
    public Path getVideopath(String filename){
        return fileStorageLocation.resolve(filename).normalize();
    }
}
