# API Gateway with Authentication + Rate Limiting
This project implements an API Gateway in spring boot that provides authentication and rate limiting features.
It uses a JWT-based authentication mechanism and integrates with a Redis cache for rate limiting.

## Tech Stack
1. Java 21
2. Spring Boot + Web 3.5.4
3. Redis 8.0.2
4. Spring Data Redis
5. Spring Security
6. JWT (Java JWT)
7. Lombok 
8. JUNIT 5 - For Unit Testing
9. Apache JMeter - For Concurrency Testing


## High Level Design
![High Level Design](doc/HLD.drawio.png)
- For simplicity purpose, API gateway store users in memory. Following users are present by default:
  - Username: `johndoe` Password: `johndoe`
  - Username: `janedoe` Password `janedoe`
- There are two un-authenticated endpoints (highlighted in green) which are used to generate JWT tokens:
  - `/token` - Returns access token and refresh token for the user
  - `/token-refresh` - Returns new access and refresh for the user using refresh token
  - These endpoints are rate limited to 100 requests per minute using client IP address as the key.
- Pass through APIs are any paths which are authenticated (highlighted in blue)
- Authenticated APIs requires a valid JWT access token in the `Authorization` header.
- Authenticated APIs are rate limited to 100 requests per minute using username as the key.
- Username for authenticated APIs is extracted from valid JWT access token.
- For rate limiting, token bucket based algorithm is used.
- Rate limiting is implemented at userId/clientIp + API level.
- Rate limiting is also applied to un-authenticated endpoints to prevent abuse.

## Test Results
### Unit Tests
Unit tests are provided for the application using JUNIT 5.
You can run the tests using the following command:
```bash
./gradlew clean test
```
Jacoco code coverage report will be generated in `build/reports/jacoco/test/html/index.html`.
### Concurrency Tests
Concurrency tests are provided to test the rate limiting feature for concurrent bursts of requests for same user
or from same client IP address. You can run the tests present in `perf_tests` folder using Apache JMeter.
<br><br>
The results of the concurrency tests are available in `perf_tests/burst_results.csv` file. In these tests the rate limit
was set to 5 request per minute and concurrent burst of 7 requests were sent for same user or from same client IP address.
The results show that the rate limiting is working as expected and the requests are being throttled after 5 requests.
<br><br>
Please see following table results of concurrency tests:

|timeStamp  |label               |responseCode|threadName                                 |success|
|-----------|--------------------|------------|-------------------------------------------|-------|
|1.75347E+12|Create Token        |429         |Un-authenticted User Burst Test 1-1        |FALSE  |
|1.75347E+12|Create Token        |429         |Un-authenticted User Burst Test 1-5        |FALSE  |
|1.75347E+12|Create Token        |201         |Un-authenticted User Burst Test 1-6        |TRUE   |
|1.75347E+12|Create Token        |201         |Un-authenticted User Burst Test 1-7        |TRUE   |
|1.75347E+12|Create Token        |201         |Un-authenticted User Burst Test 1-3        |TRUE   |
|1.75347E+12|Create Token        |201         |Un-authenticted User Burst Test 1-2        |TRUE   |
|1.75347E+12|Create Token        |201         |Un-authenticted User Burst Test 1-4        |TRUE   |
|||||
|1.75347E+12|Authenticated API -1|200         |Authenticated User Burst Test - API -1  2-6|TRUE   |
|1.75347E+12|Authenticated API -1|200         |Authenticated User Burst Test - API -1  2-3|TRUE   |
|1.75347E+12|Authenticated API -1|200         |Authenticated User Burst Test - API -1  2-4|TRUE   |
|1.75347E+12|Authenticated API -1|429         |Authenticated User Burst Test - API -1  2-2|FALSE  |
|1.75347E+12|Authenticated API -1|429         |Authenticated User Burst Test - API -1  2-5|FALSE  |
|1.75347E+12|Authenticated API -1|200         |Authenticated User Burst Test - API -1  2-1|TRUE   |
|1.75347E+12|Authenticated API -1|200         |Authenticated User Burst Test - API -1  2-7|TRUE   |
|||||
|1.75347E+12|Authenticated API -2|200         |Authenticated User Burst Test - API -2 3-6 |TRUE   |
|1.75347E+12|Authenticated API -2|200         |Authenticated User Burst Test - API -2 3-5 |TRUE   |
|1.75347E+12|Authenticated API -2|200         |Authenticated User Burst Test - API -2 3-7 |TRUE   |
|1.75347E+12|Authenticated API -2|429         |Authenticated User Burst Test - API -2 3-2 |FALSE  |
|1.75347E+12|Authenticated API -2|429         |Authenticated User Burst Test - API -2 3-1 |FALSE  |
|1.75347E+12|Authenticated API -2|200         |Authenticated User Burst Test - API -2 3-3 |TRUE   |
|1.75347E+12|Authenticated API -2|200         |Authenticated User Burst Test - API -2 3-4 |TRUE   |

## Running the Application
### Using Docker
Docker compose file is provided to run the application along with Redis.
This is the recommended way to run the application for development and testing purposes.
1. Make sure you have Docker installed and running.
2. Run docker compose command to start the application and Redis:
   ```bash
   docker compose up -d
   ```
3. The application will be available at `http://localhost:8080`. Redis will be available at `redis://localhost:6379`.

### Configuration
You can configure the application by changing command line arguments in `docker-compose.yml` file.
- `application.secret`: Secret key used for signing JWT tokens
- `application.jwtAccessTokenExpirationMs`: Access token expiration time in milliseconds (default: 10 minutes)
- `application.jwtRefreshTokenExpirationMs`: Refresh token expiration time in milliseconds (default: 20 minutes)
- `application.maxReqPerMinute`: Maximum number of requests per minute for authenticated APIs (default: 100)

### Postman Collection
A Postman collection is provided to test the API Gateway.
You can import the collection from `postman/API gateway.postman_collection.json` file.


