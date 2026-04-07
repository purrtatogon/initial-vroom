# Multi-stage build: first stage compiles Angular, second stage serves it with nginx

# Build stage: install deps and compile the Angular app to static files
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files first for better Docker layer caching
COPY package*.json ./
RUN npm ci

COPY . ./
# NG_CONFIG selects the Angular build configuration:
#   "development" in Docker Compose (uses environment.ts → localhost URLs)
#   "production"  hosted builds      (uses environment.prod.ts → Render backend URLs)
ARG NG_CONFIG=production
RUN npx ng build --configuration $NG_CONFIG

# Production stage: nginx serves the static files
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
