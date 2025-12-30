package com.boombet.boombet_backend.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class AzureBlobService {

    private final BlobServiceClient blobServiceClient;

    public AzureBlobService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    /**
     * Sube un archivo a un contenedor específico de Azure y retorna la URL pública.
     * @param file El archivo a subir.
     * @param containerName El nombre del contenedor (ej: "publicidades", "usuarios").
     */
    public String uploadFile(MultipartFile file, String containerName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            if (!containerClient.exists()) {
                containerClient.create();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID().toString() + extension;

            BlobClient blobClient = containerClient.getBlobClient(fileName);
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());

            blobClient.upload(file.getInputStream(), file.getSize());
            blobClient.setHttpHeaders(headers);

            return blobClient.getBlobUrl();

        } catch (IOException e) {
            throw new RuntimeException("Error subiendo archivo al contenedor " + containerName, e);
        }
    }

    /**
     * Borra un archivo de un contenedor específico.
     * @param mediaUrl La URL completa del archivo.
     * @param containerName El nombre del contenedor donde está el archivo.
     */
    public void deleteBlob(String mediaUrl, String containerName) {
        if (mediaUrl == null || mediaUrl.isEmpty()) return;

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            String blobName = extractBlobNameFromUrl(mediaUrl);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (blobClient.exists()) {
                blobClient.delete();
                System.out.println(">>> 🗑️ Archivo eliminado de " + containerName + ": " + blobName);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error borrando blob de " + containerName + ": " + mediaUrl, e);
        }
    }

    private String extractBlobNameFromUrl(String url) {
        try {
            return url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return url;
        }
    }


}