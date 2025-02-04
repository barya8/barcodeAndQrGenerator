package com.company.service;

import com.company.constants.Constants;
import com.company.interfaces.DatabaseCallback;
import com.company.model.QrData;
import com.google.firebase.database.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class FirebaseRealtimeDbService {

    private final DatabaseReference databaseReference;

    public FirebaseRealtimeDbService() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Retrieve and process all data from the Firebase Realtime Database.
     *
     * @return A CompletableFuture containing a Map with the relevant data.
     */
    public CompletableFuture<Map<String, Object>> getAllData() {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                log.info("inside the database reference");
                Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                future.complete(data != null ? data : new HashMap<>());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Error fetching data: " + databaseError.getMessage()));
            }
        });

        return future;
    }

    public CompletableFuture<Map<String, Object>> getDataByClient(String apiKey) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        databaseReference.child(Constants.DBKeys.QRS).child(apiKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                log.info("inside the database reference");
                if (dataSnapshot.getValue() == null) {
                    future.completeExceptionally(new Exception("No data found for API Key: " + apiKey));
                    return;
                }
                List<Object> listData = (List<Object>) dataSnapshot.getValue();
                Map<String, Object> mapData = new HashMap<>();
                for (int i = 0; i < listData.size(); i++) {
                    mapData.put(String.valueOf(i), listData.get(i));
                }
                future.complete(mapData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Error fetching data: " + databaseError.getMessage()));
            }
        });

        return future;
    }

    /**
     * Write data to a specific node in Firebase Realtime Database.
     *
     * @param data the data to be written
     */
    public void InsertData(String apiKey, QrData data) {
        databaseReference.child(Constants.DBKeys.QRS).child(apiKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Integer> counterList = (ArrayList<Integer>) dataSnapshot.getValue();
                if (counterList == null) {
                    counterList = new ArrayList<>();
                }

                Integer newKey = counterList.size();
                databaseReference.child(Constants.DBKeys.QRS).child(apiKey).child(newKey.toString()).setValueAsync(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancellation
            }
        });
    }

    public void updateData(String apiKey, Integer id, QrData qrData, DatabaseCallback callback) {
        DatabaseReference qrDataRef = databaseReference.child(Constants.DBKeys.QRS).child(apiKey).child(String.valueOf(id));
        qrDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Data exists; proceed with the update
                    qrDataRef.setValueAsync(qrData);
                    callback.onSuccess();
                } else {
                    // Data does not exist; handle the error
                    callback.onFailure(new Exception("QR Data not found for id: " + id));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancellation
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void deleteData(String apiKey, Integer id, DatabaseCallback callback) {
        DatabaseReference qrDataRef = databaseReference.child(Constants.DBKeys.QRS).child(apiKey).child(String.valueOf(id));
        qrDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Data exists; proceed with the update
                    qrDataRef.removeValueAsync();
                    callback.onSuccess();
                } else {
                    // Data does not exist; handle the error
                    callback.onFailure(new Exception("QR Data not found for id: " + id));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancellation
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void deleteAllData(String apiKey) {
        databaseReference.child(Constants.DBKeys.QRS).child(apiKey).removeValueAsync();
    }

    public CompletableFuture<Integer> getCounterListSize(String apiKey) {
        CompletableFuture<Integer> futureSize = new CompletableFuture<>();

        databaseReference.child(Constants.DBKeys.QRS).child(apiKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Integer> counterList = (ArrayList<Integer>) dataSnapshot.getValue();
                if (counterList == null) {
                    futureSize.complete(0);
                } else {
                    futureSize.complete(counterList.size());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                futureSize.completeExceptionally(databaseError.toException());
            }
        });

        return futureSize;
    }

}