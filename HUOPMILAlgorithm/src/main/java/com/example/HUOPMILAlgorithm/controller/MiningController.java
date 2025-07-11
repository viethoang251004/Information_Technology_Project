package com.example.HUOPMILAlgorithm.controller;

import com.example.HUOPMILAlgorithm.service.MiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
public class MiningController {

    @Autowired
    private MiningService miningService;

    @PostMapping("/mine")
    public ResponseEntity<Resource> mineHUOMIL(
            @RequestParam("file") MultipartFile file,
            @RequestParam("algorithm") String algorithm,
            @RequestParam("alpha") double alpha,
            @RequestParam("beta") double beta) throws IOException {

        File resultFile = miningService.runMining(file, algorithm, alpha, beta);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(resultFile));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=result.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}
