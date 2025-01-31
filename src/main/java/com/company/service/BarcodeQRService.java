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
import java.io.*;
import java.net.URL;
import java.sql.Date;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class BarcodeQRService {

    @Autowired
    private FirebaseRealtimeDbService firebaseRealtimeDbService;
    private static final int SIZE = 300;
    //    private static final int MAX_SCALE_ATTEMPTS = 5;
//    private static final int SCALE_STEP = 50;
    private static final ErrorCorrectionLevel DEFAULT_ERROR_CORRECTION = ErrorCorrectionLevel.M;

    // Path to the logo image (update path as needed)
    private static final String LOGO_PATH = "src/main/resources/static/logo.png";

    // Generate a QR code with an optional logo, error correction, and size
    public byte[] generateQRCodeWithLogo(String text, Integer size, String errorCorrection) throws Exception {
        byte[] qrCodeImage = generateQRCode(text, size, errorCorrection);
        BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrCodeImage));

        // Overlay logo image on the QR code
        BufferedImage overlayImage = ImageIO.read(new File(LOGO_PATH));
        BufferedImage qrWithLogo = embedLogoInsideQRCode(qrImage, overlayImage);

        // Keep scaling until the QR code is readable
//        while (!isQRCodeReadable(qrWithLogo)) {
//            // If not readable, scale up
//            width += SCALE_STEP;
//            height += SCALE_STEP;
//
//            // Regenerate QR with new size
//            qrCodeImage = generateQRCode(text, width, height, errorCorrection);
//            qrImage = ImageIO.read(new ByteArrayInputStream(qrCodeImage));
//            // Embed the logo again
//            qrWithLogo = embedLogoInsideQRCode(qrImage, overlayImage);
//        }
        if (isQRCodeReadable(qrWithLogo))
            return toByteArray(qrWithLogo);
        else throw new Exception("QR Code with logo is not readable");
    }

    // Generate a QR code from the provided text, with optional parameters
    public byte[] generateQRCodeAndWriteToDB(String apiKey, String text, Integer size, String errorCorrection,
                                             boolean valid, Date startDate, Date endDate, Integer type) {
        try {
            if (type == 2) {
                // Fetch counter size from Firebase
                CompletableFuture<Integer> futureSize = firebaseRealtimeDbService.getCounterListSize(apiKey);
                int id = futureSize.get(); // Wait for the result
                // Include the counter size in the QR text or use it for logging/debugging
                text = text + "?apiKey=" + apiKey + "&id=" + id + "&valid=" + valid + "&startDate="
                        + startDate + "&endDate=" + endDate;
            }
            int qrSize = (size != null) ? size : SIZE;
            ErrorCorrectionLevel correctionLevel = getErrorCorrectionLevel(errorCorrection);
            byte[] qrImageBytes = generateBarcodeOrQRCode(text, BarcodeFormat.QR_CODE, qrSize, correctionLevel);
            String base64QrImage = Base64.getEncoder().encodeToString(qrImageBytes);

            BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrImageBytes));

            if (isQRCodeReadable(qrImage)) {
                QrData qrData = new QrData(text, qrSize, errorCorrection, base64QrImage);
                firebaseRealtimeDbService.writeData(apiKey, qrData);
            }
            return qrImageBytes;
        } catch (Exception e) {
            return null;
        }

    }

    public void generateQRCodeAndUpdateDatabase(String apiKey, String text, Integer size, String errorCorrection, Integer id, boolean valid,
                                                Date startDate, Date endDate, Integer type, DatabaseCallback callback) throws Exception {
        if (type == 2) {
            // Include the counter size in the QR text or use it for logging/debugging
            text = text + "?apiKey=" + apiKey + "&id=" + id + "&valid=" + valid + "&startDate="
                    + startDate + "&endDate=" + endDate;
        }
        // Generate the QR code
        byte[] qrImageBytes = generateQRCode(text, size, errorCorrection);

        // Convert the QR code to a base64-encoded string
        String base64QrImage = Base64.getEncoder().encodeToString(qrImageBytes);

        BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrImageBytes));

        if (isQRCodeReadable(qrImage)) {


            // Create the QR data object
            QrData qrData = QrData.builder()
                    .url(text)
                    .size(size)
                    .errorCorrection(errorCorrection)
                    .base64Image(base64QrImage)
                    .build();

            // Update Firebase with the generated QR data
            firebaseRealtimeDbService.updateData(apiKey, id, qrData, callback);
        } else throw new Exception("The QR is not readable");
    }

    // Generate a QR code from the provided text, with optional parameters
    public byte[] generateQRCode(String text, Integer size, String errorCorrection) throws Exception {
        int qrSize = (size != null) ? size : SIZE;
        ErrorCorrectionLevel correctionLevel = getErrorCorrectionLevel(errorCorrection);

        return generateBarcodeOrQRCode(text, BarcodeFormat.QR_CODE, qrSize, correctionLevel);
    }

    // Generate a barcode from the provided text
//    public byte[] generateBarcode(String text) throws Exception {
//        return generateBarcodeOrQRCode(text, BarcodeFormat.CODE_128);
//    }

    // Common method for generating both barcodes and QR codes
    private byte[] generateBarcodeOrQRCode(String text, BarcodeFormat format, int size, ErrorCorrectionLevel errorCorrection) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        hints.put(EncodeHintType.MARGIN, 0); // Optional: to set margin to 0

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = multiFormatWriter.encode(text, format, size, size, hints);

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

    // Embed the logo inside the QR code
    private BufferedImage embedLogoInsideQRCode(BufferedImage qrImage, BufferedImage logoImage) {
        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();

        // Scale the logo to 20% of the QR code's size
        int logoWidth = qrWidth / 5;
        int logoHeight = qrHeight / 5;

        // Resize the logo
        Image scaledLogo = logoImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
        BufferedImage scaledLogoImage = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledLogoImage.createGraphics();
        g2d.drawImage(scaledLogo, 0, 0, null);
        g2d.dispose();

        // Create a copy of the QR code to modify
        BufferedImage qrWithLogo = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrWithLogo.createGraphics();
        g.drawImage(qrImage, 0, 0, null);

        // Add a white circular background for the logo
        int centerX = (qrWidth - logoWidth) / 2;
        int centerY = (qrHeight - logoHeight) / 2;

        g.setColor(Color.WHITE);
        g.fillOval(centerX - 10, centerY - 10, logoWidth + 20, logoHeight + 20); // Adjust padding around the logo

        // Draw the scaled logo on top of the circular background
        g.drawImage(scaledLogoImage, centerX, centerY, null);
        g.dispose();

        return qrWithLogo;
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

    public static String readQRCodeFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        BufferedImage bufferedImage = ImageIO.read(file);

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
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
