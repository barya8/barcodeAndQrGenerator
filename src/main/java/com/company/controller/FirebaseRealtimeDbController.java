package com.company.controller;

import com.company.interfaces.DatabaseCallback;
import com.company.model.ApiResponse;
import com.company.model.ServiceResult;
import com.company.service.BarcodeQRService;
import com.company.service.FirebaseRealtimeDbService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Log4j2
@RestController
@RequestMapping("/api/firebase")
public class FirebaseRealtimeDbController {
    @Autowired
    private BarcodeQRService barcodeQRService;
    @Autowired
    private FirebaseRealtimeDbService firebaseRealtimeDbService;


    @GetMapping("/getAllData")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> getAllData() {
        return firebaseRealtimeDbService.getAllData().thenApply(data -> {
            ServiceResult serviceResult = ServiceResult.builder()
                    .returnCode("0")
                    .returnMessage("Data retrieved successfully")
                    .build();
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(serviceResult, data);
            return ResponseEntity.ok(response);
        }).exceptionally(e -> {
            log.error("Error retrieving all data", e);
            ServiceResult serviceResult = new ServiceResult("99", "Error retrieving all data: " + e.getMessage());
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(serviceResult, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        });
    }

    @GetMapping("/getDataByClient")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> getDataByClient(
            @RequestHeader("x-api-key") String apiKey) {
        return firebaseRealtimeDbService.getDataByClient(apiKey).thenApply(data -> {
            ServiceResult serviceResult = ServiceResult.builder()
                    .returnCode("0")
                    .returnMessage("Data retrieved successfully")
                    .build();
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(serviceResult, data);
            return ResponseEntity.ok(response);
        }).exceptionally(e -> {
            log.error("Error retrieving data for client", e);
            ServiceResult serviceResult = new ServiceResult("99", "Error retrieving data for client:" + e.getMessage());
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(serviceResult, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        });
    }

    @DeleteMapping("/deleteQrById")
    public ResponseEntity deleteData(@RequestParam Integer id,
                                     @RequestHeader("x-api-key") String apiKey) {
        try {
            // Create a latch with 1 count to wait for callback
            log.info("check if the id is already exist");
            CountDownLatch latch = new CountDownLatch(1);
            final ResponseEntity<ServiceResult>[] response = new ResponseEntity[1];
            firebaseRealtimeDbService.deleteData(apiKey, id, new DatabaseCallback() {
                @Override
                public void onSuccess() {
                    // Return a success response
                    response[0] = ResponseEntity.status(HttpStatus.OK).body(ServiceResult.builder()
                            .returnCode("0")
                            .returnMessage("The QR deleted successfully")
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
            log.error("Error deleting QR data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ServiceResult.builder()
                    .returnCode("97")
                    .returnMessage("Failed to delete QR data: " + e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<ServiceResult> deleteAllData(@RequestHeader("x-api-key") String apiKey) {
        try {
            firebaseRealtimeDbService.deleteAllData(apiKey);
            return ResponseEntity.ok(ServiceResult.builder()
                    .returnCode("0")
                    .returnMessage("All QR Data deleted successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deleting all QR data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ServiceResult.builder()
                    .returnCode("99")
                    .returnMessage("Failed to delete all QR data: " + e.getMessage())
                    .build());
        }
    }
}
