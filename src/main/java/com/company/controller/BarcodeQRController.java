package com.company.controller;


import com.company.interfaces.DatabaseCallback;
import com.company.model.ApiResponse;
import com.company.model.ParsedUrl;
import com.company.model.ServiceResult;
import com.company.service.BarcodeQRService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.sql.Date;
import java.util.concurrent.CountDownLatch;

@Log4j2
@RestController
@RequestMapping("/api/barcodes")
public class BarcodeQRController {

    @Autowired
    private BarcodeQRService barcodeQRService;

    // Generate QR Code
    @PostMapping("/generateQRCode")
    public ResponseEntity<byte[]> generateQRCode(@RequestParam String text,
                                                 @RequestParam Integer type,
                                                 @RequestParam(required = false) Integer size,
                                                 @RequestParam(required = false) String errorCorrection,
                                                 @RequestParam(required = false, defaultValue = "true") boolean valid,
                                                 @RequestParam(required = false) Date startDate,
                                                 @RequestParam(required = false) Date endDate,
                                                 @RequestHeader("x-api-key") String apiKey) {
        try {
            byte[] qrCodeImage = barcodeQRService.generateQRCodeAndWriteToDB(apiKey, text, size,
                    errorCorrection, valid, startDate, endDate, type);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/png");
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Generate Barcode
//    @GetMapping("/generateBarcode/{text}")
//    public ResponseEntity<byte[]> generateBarcode(@PathVariable String text) {
//        try {
//            byte[] barcodeImage = barcodeQRService.generateBarcode(text);
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Content-Type", "image/png");
//            return new ResponseEntity<>(barcodeImage, headers, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    // Generate QR Code with logo
    @PostMapping("/generateQRCodeWithLogo")
    public ResponseEntity<byte[]> generateQRCodeWithLogo(@RequestParam String text,
                                                         @RequestParam(required = false) Integer size,
                                                         @RequestParam(required = false) String errorCorrection,
                                                         @RequestParam(required = false) boolean valid,
                                                         @RequestParam(required = false) Date startDate,
                                                         @RequestParam(required = false) Date endDate,
                                                         @RequestHeader("x-api-key") String apiKey) {
        try {
            byte[] qrCodeImage = barcodeQRService.generateQRCodeWithLogo(text, size, errorCorrection);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/png");
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/qrcode/check")
    public ResponseEntity<ServiceResult> qrCodeCheck(@RequestParam MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            barcodeQRService.isQRCodeReadable(image);
            return ResponseEntity.ok(ServiceResult.builder()
                    .returnCode("0")
                    .returnMessage("Valid QR code")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ServiceResult.builder()
                    .returnCode("99")
                    .returnMessage("Invalid QR code")
                    .build());
        }
    }

//    @PostMapping("/qrcode/scan")
//    public ResponseEntity qrCodeScan(@RequestParam MultipartFile file) {
//        try {
//            BufferedImage image = ImageIO.read(file.getInputStream());
//            barcodeQRService.isQRCodeReadable(image);
//            return new ResponseEntity(HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PutMapping("/updateQrById")
    public ResponseEntity updateData(@RequestParam Integer id,
                                     @RequestParam Integer type,
                                     @RequestParam String text,
                                     @RequestParam(required = false) Integer size,
                                     @RequestParam(required = false) String errorCorrection,
                                     @RequestParam(required = false) boolean valid,
                                     @RequestParam(required = false) Date startDate,
                                     @RequestParam(required = false) Date endDate,
                                     @RequestHeader("x-api-key") String apiKey) {
        try {
            // Create a latch with 1 count to wait for callback
            CountDownLatch latch = new CountDownLatch(1);
            final ResponseEntity<ServiceResult>[] response = new ResponseEntity[1];
            barcodeQRService.generateQRCodeAndUpdateDatabase(apiKey, text, size, errorCorrection,
                    id, valid, startDate, endDate, type, new DatabaseCallback() {
                        @Override
                        public void onSuccess() {
                            // Return a success response
                            response[0] = ResponseEntity.status(HttpStatus.OK).body(ServiceResult.builder()
                                    .returnCode("0")
                                    .returnMessage("QR Data updated successfully")
                                    .build());
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Return an error response
                            response[0] = ResponseEntity.status(HttpStatus.NOT_FOUND).body(ServiceResult.builder().
                                    returnCode("99").
                                    returnMessage(e.getMessage())
                                    .build());
                            latch.countDown();
                        }
                    });
            // Wait for the callback to complete (either success or failure)
            latch.await();
            // If no response is set, return internal server error
            if (response[0] == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ServiceResult.builder()
                        .returnCode("98")
                        .returnMessage("An unexpected error occurred.")
                        .build());
            }

            return response[0];
        } catch (Exception e) {
            // Return an internal server error response
            log.error("Error deleting QR data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ServiceResult.builder()
                    .returnCode("97")
                    .returnMessage("An error occurred: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/read")
    public ResponseEntity<?> readQRCode(@RequestParam("file") MultipartFile file) {
        try {
            ParsedUrl parsedUrl = barcodeQRService.readAndParseQRCode(file);
            return ResponseEntity.ok(ApiResponse.builder()
                    .serviceResult(ServiceResult.builder()
                            .returnCode("0")
                            .returnMessage("success")
                            .build())
                    .data(parsedUrl)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .serviceResult(ServiceResult.builder()
                                    .returnCode("99")
                                    .returnMessage("Invalid QR Failed to read QR code")
                                    .build())
                            .build());
        }
    }
}
