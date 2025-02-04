package com.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrData {
    private String url;
    private Integer size;
    private String errorCorrection;
    private String base64Image;
    private String startDate;
    private String endDate;
    private boolean isScanned;
    private Integer type;
}
