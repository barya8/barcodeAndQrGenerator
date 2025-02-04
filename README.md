# QR Code & Barcode API

This API provides functionality for generating, scanning, and managing QR codes and barcodes. It is built using Spring Boot and documented using Swagger (OpenAPI 3.0).

## Table of Contents

- [Features](#features)
- [Endpoints](#endpoints)
- [Setup](#setup)
  - [Secured Application Properties](#secured-application-properties)
- [Usage](#usage)
- [Authentication](#authentication)
- [Error Handling](#error-handling)
- [License](#license)

## Features

- Generate QR codes with customizable parameters (size, error correction, etc.).
- Retrieve, update, and delete QR code data stored in Firebase Realtime Database.
- Mark QR codes as read and check their readability.
- Secure API access using API keys.

## Endpoints

### Firebase Data Management

- **GET `/api/firebase/getAllData`**: Retrieve all data from Firebase Realtime DB.
- **GET `/api/firebase/getDataByClient`**: Retrieve data by client API key.
- **DELETE `/api/firebase/deleteQrById`**: Delete QR data by ID.
- **DELETE `/api/firebase/deleteAll`**: Delete all data for a specific API key.

### QR Code Generation & Management

- **POST `/api/barcodes/generateQRCode`**: Generate a QR code.
- **PUT `/api/barcodes/updateQrById`**: Update QR data by ID.
- **POST `/api/barcodes/read`**: Mark a QR code as read.
- **POST `/api/barcodes/qrcode/check`**: Check if a QR code is readable.

## Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-repo/your-project.git
   cd your-project
   ```

2. **Build the project**:
   ```bash
   ./mvnw clean install
   ```

3. **Configure the application properties**:
   The `application.properties` file contains sensitive information and must be configured before running the application. See the [Secured Application Properties](#secured-application-properties) section for details.

4. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the Swagger UI**:
   Open your browser and navigate to `http://localhost:8080/swagger-ui.html` to explore the API documentation.

### Secured Application Properties

The `application.properties` file contains sensitive configuration details such as Firebase credentials and API keys. To set up the application, create or update the `application.properties` file in the `src/main/resources` directory with the following properties:

```properties
# Firebase Configuration
firebase.database.url=YOUR_FIREBASE_DATABASE_URL
firebase.project.id=YOUR_FIREBASE_PROJECT_ID
firebase.private.key.id=YOUR_FIREBASE_PRIVATE_KEY_ID
firebase.private.key=YOUR_FIREBASE_PRIVATE_KEY
firebase.client.email=YOUR_FIREBASE_CLIENT_EMAIL
firebase.client.id=YOUR_FIREBASE_CLIENT_ID
firebase.client.x509.cert.url=YOUR_FIREBASE_CLIENT_X509_CERT_URL

# API Security
api.key=YOUR_API_KEY
```

#### Steps to Secure the Properties:
1. **Do not commit sensitive data**: Ensure the `application.properties` file is added to your `.gitignore` file to avoid exposing sensitive information in version control.
   ```gitignore
   src/main/resources/application.properties
   ```

2. **Use environment variables**: For enhanced security, you can use environment variables to inject sensitive data at runtime. Update the `application.properties` file to reference environment variables:
   ```properties
   # Firebase Configuration
   firebase.database.url=${FIREBASE_DATABASE_URL}
   firebase.project.id=${FIREBASE_PROJECT_ID}
   firebase.private.key.id=${FIREBASE_PRIVATE_KEY_ID}
   firebase.private.key=${FIREBASE_PRIVATE_KEY}
   firebase.client.email=${FIREBASE_CLIENT_EMAIL}
   firebase.client.id=${FIREBASE_CLIENT_ID}
   firebase.client.x509.cert.url=${FIREBASE_CLIENT_X509_CERT_URL}

   # API Security
   api.key=${API_KEY}
   ```

   Then, set the environment variables in your system or deployment environment:
   ```bash
   export FIREBASE_DATABASE_URL=your_firebase_database_url
   export FIREBASE_PROJECT_ID=your_firebase_project_id
   export FIREBASE_PRIVATE_KEY_ID=your_firebase_private_key_id
   export FIREBASE_PRIVATE_KEY=your_firebase_private_key
   export FIREBASE_CLIENT_EMAIL=your_firebase_client_email
   export FIREBASE_CLIENT_ID=your_firebase_client_id
   export FIREBASE_CLIENT_X509_CERT_URL=your_firebase_client_x509_cert_url
   export API_KEY=your_api_key
   ```

3. **Use a secrets manager**: For production environments, consider using a secrets manager (e.g., AWS Secrets Manager, HashiCorp Vault) to securely store and retrieve sensitive data.

## Usage

### Authentication
All endpoints require an API key for authentication. Include the API key in the request header as follows:
```http
x-api-key: YOUR_API_KEY
```

### Example Requests

#### Generate a QR Code
```http
POST /api/barcodes/generateQRCode?type=2&url=https://www.example.com&size=150&errorCorrection=H HTTP/1.1
Host: localhost:8080
x-api-key: YOUR_API_KEY
```

#### Retrieve All Data from Firebase
```http
GET /api/firebase/getAllData HTTP/1.1
Host: localhost:8080
x-api-key: YOUR_API_KEY
```

## Error Handling

The API uses a standard response format for errors:
```json
{
  "returnCode": "string",
  "returnMessage": "string"
}
```

Common error responses include:
- **400 Bad Request**: Invalid input parameters.
- **401 Unauthorized**: Missing or invalid API key.
- **404 Not Found**: Resource not found.
- **500 Internal Server Error**: Server-side error.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](https://www.apache.org/licenses/LICENSE-2.0) file for details.

---

For more information, contact [Bar Yaron](mailto:bar.yaron@s.afeka.ac.il).
```

### Key Additions:
1. **Secured Application Properties Section**: Explains how to configure sensitive properties and provides best practices for securing them.
2. **Environment Variables**: Demonstrates how to use environment variables for enhanced security.
3. **Secrets Manager Suggestion**: Recommends using a secrets manager for production environments.
