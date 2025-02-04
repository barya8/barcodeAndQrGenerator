package com.company.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI QRCodeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QR Code & Barcode API")
                        .description("API for generating, scanning, and managing QR codes and barcodes.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Bar Yaron")
                                .email("bar.yaron@s.afeka.ac.il"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("apiKey",
                                new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("x-api-key"))
                        .addSchemas("ServiceResult", new Schema<>()
                                .description("Standard API Response Format")
                                .addProperty("returnCode", new Schema<>().type("string").example("0"))
                                .addProperty("returnMessage", new Schema<>().type("string").example("Success")))
                        .addResponses("400", new ApiResponse()
                                .description("Bad Request")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                        .addResponses("401", new ApiResponse()
                                .description("Unauthorized - API key required"))
                        .addResponses("500", new ApiResponse()
                                .description("Internal Server Error")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult"))))))
                .paths(new Paths()
                        .addPathItem("/api/firebase/getAllData",
                                new PathItem().get(new Operation()
                                        .summary("Retrieve all data from Firebase Realtime DB")
                                        .description("Fetches all stored data from Firebase Realtime Database.")
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for client authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("Data retrieved successfully"))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error retrieving data")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                        )))
                        .addPathItem("/api/firebase/getDataByClient",
                                new PathItem().get(new Operation()
                                        .summary("Retrieve data by client API key")
                                        .description("Fetches stored data for a specific client using an API key.")
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for client authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("Data retrieved successfully"))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error retrieving data")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                        )))
                        .addPathItem("/api/firebase/deleteQrById",
                                new PathItem().delete(new Operation()
                                        .summary("Delete QR By id")
                                        .description("Deletes the data that stored in the specific id for the specific client")
                                        .addParametersItem(new Parameter()
                                                .name("id")
                                                .description("The id of the barcode ")
                                                .required(true)
                                                .example("1")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for client authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("Data deleted successfully")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("404", new ApiResponse().description("Not Found - Id not found for the API key")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error deleting data")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                        )))
                        .addPathItem("/api/firebase/deleteAll",
                                new PathItem().delete(new Operation()
                                        .summary("Delete All")
                                        .description("Deletes all the data in firebase for specific api key")
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for client authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("Data deleted successfully")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error deleting data")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                        )))
                        .addPathItem("/api/barcodes/generateQRCode",
                                new PathItem().post(new Operation()
                                        .summary("Generate a QR code")
                                        .description("Generates a QR code from the provided url.")
                                        .addParametersItem(new Parameter()
                                                .name("type")
                                                .description("The type of the qr:\n1-externally managed one time code\n" +
                                                        "2-self managed one time code\n3-externally managed multi times" +
                                                        "\n4- self managed multi times")
                                                .required(true)
                                                .example("2")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("url")
                                                .description("The value we want to make barcode from")
                                                .required(true)
                                                .example("https://www.example.com")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("size")
                                                .description("The size of the barcode")
                                                .example("150")
                                                .required(false)
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("errorCorrection")
                                                .description("The error correction of the barcode")
                                                .example("H")
                                                .required(false)
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("isScanned")
                                                .description("If the barcode is is scanned")
                                                .example("true")
                                                .required(false)
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("startDate")
                                                .description("The start date of the barcode format YYYYmmDD")
                                                .required(false)
                                                .example("20250101")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("endDate")
                                                .description("The end date of the barcode format YYYYmmDD")
                                                .required(false)
                                                .example("20260101")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("QR code generated successfully"))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error generating QR code"))
                                        )))
                        .addPathItem("/api/barcodes/updateQrById",
                                new PathItem().put(new Operation()
                                        .summary("Update QR By id")
                                        .description("Updates the data that stored in the specific id for the specific client")
                                        .addParametersItem(new Parameter()
                                                .name("id")
                                                .description("The id of the barcode ")
                                                .required(true)
                                                .example("1")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("type")
                                                .description("The type of the qr:\n1-externally managed one time code\n" +
                                                        "2-self managed one time code\n3-externally managed multi times" +
                                                        "\n4- self managed multi times")
                                                .required(true)
                                                .example("2")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("url")
                                                .description("The new value we want to make barcode from")
                                                .required(true)
                                                .example("https://www.example.com")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("size")
                                                .description("The size of the barcode ")
                                                .required(false)
                                                .example("150")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("errorCorrection")
                                                .description("The error correction of the barcode ")
                                                .required(false)
                                                .example("H")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("isScanned")
                                                .description("If the barcode is is scanned")
                                                .required(false)
                                                .example("true")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("startDate")
                                                .description("The start date of the barcode format YYYYmmDD")
                                                .required(false)
                                                .example("20250101")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("endDate")
                                                .description("The end date of the barcode format YYYYmmDD")
                                                .required(false)
                                                .example("20260101")
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for client authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("Data deleted successfully")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("404", new ApiResponse().description("Not Found - Id not found for the API key")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error deleting data")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                        )))
                        .addPathItem("/api/barcodes/read",
                                new PathItem().post(new Operation()
                                        .summary("Read QR Code")
                                        .description("Mark the QR Code as read")
                                        .addParametersItem(new Parameter()
                                                .name("file")
                                                .description("QR code")
                                                .required(true)
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for client authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("Data deleted successfully")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error deleting data")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult"))))))))
                        .addPathItem("/api/barcodes/qrcode/check",
                                new PathItem().post(new Operation()
                                        .summary("Check if the qr code is readable")
                                        .description("Check if the qr code is readable")
                                        .addParametersItem(new Parameter()
                                                .name("file")
                                                .description("QR code")
                                                .required(true)
                                                .in("query"))
                                        .addParametersItem(new Parameter()
                                                .name("x-api-key")
                                                .description("API Key for client authentication")
                                                .required(true)
                                                .in("header"))
                                        .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse().description("Data deleted successfully")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("401", new ApiResponse().description("Unauthorized - API key required")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                                .addApiResponse("500", new ApiResponse().description("Error deleting data")
                                                        .content(new Content().addMediaType("application/json",
                                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServiceResult")))))
                                        ))));

    }
}