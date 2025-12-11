// src/main/java/com/boombet/boombet_backend/service/AzureBlobService.java
package com.boombet.boombet_backend.service;

import org.springframework.stereotype.Service;


//Este servicio tambien puede servir para cuando implementemos las imagenes de perfil
@Service
public class AzureBlobService {

    // Aquí va la inyección del cliente de Azure (BlobServiceClient o BlobContainerClient)

    /**
     * Lógica para borrar un blob usando su URL/Path.
     */
    public void deleteBlob(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            // No hay archivo para borrar. Es una publicidad de solo texto (si fuera permitido)
            return;
        }

        // --- LÓGICA DE AZURE SDK AQUÍ ---
        try {
            // Ejemplo de cómo obtendrías el nombre del blob desde la URL
            // String blobName = extractBlobName(mediaUrl);

            // blobClient.deleteIfExists();
            System.out.println(">>> 🗑️ Borrando archivo de Azure: " + mediaUrl);

        } catch (Exception e) {
            // Es CRUCIAL que si Azure falla, lances una RuntimeException para que Spring
            // sepa que la operación debe fallar.
            throw new RuntimeException("Fallo la eliminación del BLOB: " + mediaUrl, e);
        }
    }
}