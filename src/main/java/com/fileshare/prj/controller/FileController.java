package com.fileshare.prj.controller;
import com.fileshare.prj.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename){
        try{
            Path filePath= fileService.getVideopath(filename);
            Resource resource=new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }else {
                throw new RuntimeException("File not found: " + filename);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while downloading file: " + e.getMessage());
        }
    }
    @GetMapping("/video/{filename}")
    public ResponseEntity<byte[]> streamVideo(@PathVariable String filename, @RequestHeader(value="Range", required = false) String rangeHeader){
        try{
            Path filePath= fileService.getVideopath(filename);
            RandomAccessFile videoFile=new RandomAccessFile(filePath.toFile(),"r");
            long fileLength=videoFile.length();
            byte[]data;

            if(rangeHeader==null){
                data=new byte[(int)fileLength];
                videoFile.readFully(data);
                videoFile.close();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
                        .body(data);
            }else{
                HttpRange range=HttpRange.parseRanges(rangeHeader).get(0);
                long start=range.getRangeStart(fileLength);
                System.out.println("Start==> "+start+"\n");
                long end=range.getRangeEnd(fileLength);
                System.out.println("End==> "+end+"\n");
                long contentLength=end -start+1;
                System.out.println("contentLength==> "+contentLength+"\n");
                videoFile.seek(start);
                data=new byte[(int)contentLength];
                videoFile.readFully(data);
                videoFile.close();

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                        .body(data);
            }

        }  catch (IOException e) {
            throw new RuntimeException("Error while streaming video file: " + e.getMessage(), e);
        }

    }
}
