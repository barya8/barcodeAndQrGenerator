package com.company.service;

import com.company.interfaces.DatabaseCallback;
import com.company.model.ParsedUrl;
import com.company.model.QrData;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class QRService {

    @Autowired
    private FirebaseRealtimeDbService firebaseRealtimeDbService;
    private static final int SIZE = 300;
    private static final ErrorCorrectionLevel DEFAULT_ERROR_CORRECTION = ErrorCorrectionLevel.M;

    public byte[] generateQRCodeAndWriteToDB(String apiKey, String url, Integer size, String errorCorrection, boolean isScanned,
                                             String startDate, String endDate, Integer type) throws Exception {
        if ((type == 2) || (type == 4)) {
            // Fetch counter size from Firebase
            CompletableFuture<Integer> futureSize = firebaseRealtimeDbService.getCounterListSize(apiKey);
            int id = futureSize.get();
            url = url + "?apiKey=" + apiKey + "&id=" + id + "&isScanned=" + isScanned + "&startDate="
                    + startDate + "&endDate=" + endDate;
        }

        int qrSize = (size != null) ? size : SIZE;
        ErrorCorrectionLevel correctionLevel = getErrorCorrectionLevel(errorCorrection);

        // Generate the QR code
        byte[] qrImageBytes = generateQRCode(url, qrSize, correctionLevel);

        // Convert the QR code to a base64-encoded string
        String base64QrImage = Base64.getEncoder().encodeToString(qrImageBytes);

        BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrImageBytes));

        if (isQRCodeReadable(qrImage)) {
            // Create the QR data object
            QrData qrData = QrData.builder()
                    .url(url)
                    .size(qrSize)
                    .errorCorrection(correctionLevel.toString())
                    .base64Image(base64QrImage)
                    .isScanned(isScanned)
                    .startDate(startDate)
                    .endDate(endDate)
                    .type(type)
                    .build();
            // Update Firebase with the generated QR data
            firebaseRealtimeDbService.InsertData(apiKey, qrData);
            return qrImageBytes;
        } else throw new Exception("The QR is not readable");
    }

    public void generateQRCodeAndUpdateDatabase(String apiKey, String url, Integer size, String errorCorrection, Integer id, boolean isScanned,
                                                String startDate, String endDate, Integer type, DatabaseCallback callback) throws Exception {
        if ((type == 2) || (type == 4)) {
            // Include the counter size in the QR text or use it for logging/debugging
            url = url + "?apiKey=" + apiKey + "&id=" + id + "&isScanned=" + isScanned + "&startDate="
                    + startDate + "&endDate=" + endDate;
        }
        int qrSize = (size != null) ? size : SIZE;
        ErrorCorrectionLevel correctionLevel = getErrorCorrectionLevel(errorCorrection);

        // Generate the QR code
        byte[] qrImageBytes = generateQRCode(url, qrSize, correctionLevel);

        // Convert the QR code to a base64-encoded string
        String base64QrImage = Base64.getEncoder().encodeToString(qrImageBytes);

        BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrImageBytes));

        if (isQRCodeReadable(qrImage)) {
            // Create the QR data object
            QrData qrData = QrData.builder()
                    .url(url)
                    .size(qrSize)
                    .errorCorrection(correctionLevel.toString())
                    .base64Image(base64QrImage)
                    .isScanned(isScanned)
                    .startDate(startDate)
                    .endDate(endDate)
                    .type(type)
                    .build();

            // Update Firebase with the generated QR data
            firebaseRealtimeDbService.updateData(apiKey, id, qrData, callback);
        } else throw new Exception("The QR is not readable");
    }

    // Common method for generating QR codes
    private byte[] generateQRCode(String text, int size, ErrorCorrectionLevel errorCorrection) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        hints.put(EncodeHintType.MARGIN, 0); // Optional: to set margin to 0

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage bufferedImage = toBufferedImage(bitMatrix);
        return toByteArray(bufferedImage);
    }

    // Convert BitMatrix to BufferedImage
    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE); // Background color
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK); // Barcode color

        // Draw the matrix
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        return image;
    }

    public boolean isQRCodeReadable(BufferedImage qrImage) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(qrImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        new MultiFormatReader().decode(bitmap, hints);
        return true;  // Decoding successful
    }

    // Convert BufferedImage to byte array
    private byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    // Helper method to map error correction level from query parameter
    private ErrorCorrectionLevel getErrorCorrectionLevel(String errorCorrection) {
        if (errorCorrection == null) {
            return DEFAULT_ERROR_CORRECTION;
        }

        switch (errorCorrection.toUpperCase()) {
            case "L":
                return ErrorCorrectionLevel.L; // ~7% recovery
            case "M":
                return ErrorCorrectionLevel.M; // ~15% recovery
            case "Q":
                return ErrorCorrectionLevel.Q; // ~25% recovery
            case "H":
                return ErrorCorrectionLevel.H; // ~30% recovery
            default:
                return DEFAULT_ERROR_CORRECTION; // Default if unrecognized
        }
    }

    public static ParsedUrl readAndParseQRCode(MultipartFile file) throws Exception {
        String qrData = readQRCodeFromStream(file.getInputStream());
        return parseUrl(qrData);
    }

    public static String readQRCodeFromStream(InputStream inputStream) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

    private static ParsedUrl parseUrl(String urlString) throws Exception {
        URL url = new URL(urlString);

        String protocol = url.getProtocol();
        String host = url.getHost();
        String path = url.getPath();
        String query = url.getQuery();

        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";
                queryParams.put(key, value);
            }
        }

        return new ParsedUrl(urlString, protocol, host, path, queryParams);
    }
}
