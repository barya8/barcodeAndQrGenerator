package com.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedUrl {
    private String fullUrl;
    private String protocol;
    private String host;
    private String path;
    private Map<String, String> queryParams;

}