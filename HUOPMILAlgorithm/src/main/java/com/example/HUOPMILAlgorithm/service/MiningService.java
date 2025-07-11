package com.example.HUOPMILAlgorithm.service;

import com.example.HUOPMILAlgorithm.Algorithm.AlgoHUOMIL;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class MiningService {

    private final Path UPLOAD_DIR = Paths.get("upload");
    private final Path RESULT_DIR = Paths.get("result");

    public File runMining(MultipartFile file, String algorithm, double alpha, double beta) throws IOException {
        // Tạo thư mục nếu chưa có
        Files.createDirectories(UPLOAD_DIR);
        Files.createDirectories(RESULT_DIR);

        // Tạo tên file
        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "input.txt";

        String inputFileName = System.currentTimeMillis() + "_" + originalName;
        Path inputPath = UPLOAD_DIR.resolve(inputFileName);
        Path outputPath = RESULT_DIR.resolve("output_" + System.currentTimeMillis() + ".txt");

        // 🛠 Dùng Java NIO để ghi file upload vào thư mục đích
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, inputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Chạy thuật toán
        if ("HUOMIL".equalsIgnoreCase(algorithm)) {
            AlgoHUOMIL algo = new AlgoHUOMIL();
            algo.runAlgorithm(
                    inputPath.toString(),
                    outputPath.toString(),
                    (float) alpha,
                    (float) beta
            );
        }

        return outputPath.toFile();
    }
}
