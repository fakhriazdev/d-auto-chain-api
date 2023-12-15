package com.danamon.autochain.service.impl;

import com.danamon.autochain.entity.CompanyFile;
import com.danamon.autochain.repository.CompanyFileRepository;
import com.danamon.autochain.service.CompanyFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyFileServiceImpl implements CompanyFileService {
    private final CompanyFileRepository companyFileRepository;
    private final List<String> contentTypes = List.of("application/pdf");
    private final Path directoryPath = Paths.get("/home/user/Java/company_file");
    private final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    @Override
    public CompanyFile createFile(MultipartFile multipartFile) {
        try {
            saveValidation(multipartFile);

            String filename = String.format("%d_%s", System.currentTimeMillis(), multipartFile.getOriginalFilename());

            Path filePath = directoryPath.resolve(filename);
            Files.copy(multipartFile.getInputStream(), filePath);

            CompanyFile companyFile = CompanyFile.builder()
                    .name(filename)
                    .contentType(multipartFile.getContentType())
                    .size(multipartFile.getSize())
                    .path(filePath.toString())
                    .build();

            return companyFileRepository.save(companyFile);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Resource findByPath(String path) {
        try {
            Path filepath = Paths.get(path);
            return new UrlResource(filepath.toUri());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public CompanyFile findById(String id) {
        return companyFileRepository.findById(id).orElseThrow(() -> new RuntimeException("company file not found"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteFile(CompanyFile companyFile) {
        try {
            Path filepath = Paths.get(companyFile.getPath());
            if (!Files.exists(filepath))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found");
            Files.delete(filepath);
            companyFileRepository.delete(companyFile);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void saveValidation(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        if (multipartFile.getSize() > MAX_FILE_SIZE)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must be under 2 MB");
        if (!contentTypes.contains(multipartFile.getContentType()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid content type");
        if (!Files.exists(directoryPath))
            Files.createDirectory(directoryPath);
    }
}
