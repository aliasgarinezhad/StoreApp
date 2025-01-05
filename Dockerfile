# Stage 1: Build the application
FROM gradle:jdk21-jammy AS build-stage

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and configuration files first to leverage Docker cache
COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts /app/


# Download dependencies to speed up subsequent builds
RUN ./gradlew wasmJsBrowserDistribution --no-daemon --stacktrace --info || true

# Copy the entire project
COPY . .

# Build the project
RUN ./gradlew wasmJsBrowserDistribution --no-daemon --stacktrace --info

# Stage 2: Prepare the runtime image
FROM nginx:1.26-alpine3.20-perl AS runtime-stage

# Set working directory inside Nginx
WORKDIR /var/www/html

# Copy the built files from the build stage
COPY --from=build-stage /app/composeApp/build/dist/wasmJs/productionExecutable ./vendor_app
RUN mkdir -p ./vendor_app/js
COPY nginx/js ./vendor_app/js
COPY nginx/barcode-scanner.html ./vendor_app
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/vendor_app.conf /etc/nginx/conf.d/vendor_app.conf
RUN rm -rf /etc/nginx/conf.d/default.conf

# Expose default Nginx port
EXPOSE CONTAINER_PORT

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]
