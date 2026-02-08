# 1. Java 22 official image
FROM eclipse-temurin:22-jdk

# 2. App ke liye working directory
WORKDIR /app

# 3. Saara project container me copy karo
COPY . .

# 4. Maven wrapper ko executable banao
RUN chmod +x mvnw

# 5. Project build karo (jar banega)
RUN ./mvnw clean package -DskipTests

# 6. Spring Boot port
EXPOSE 8080

# 7. App start command
CMD ["java", "-jar", "target/*.jar"]
