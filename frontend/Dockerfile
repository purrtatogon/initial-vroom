# Multi-stage build: first stage compiles Angular, second stage serves it with nginx

# Build stage: install deps and compile the Angular app to static files
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files first for better Docker layer caching
COPY package*.json ./
RUN npm ci

COPY . ./
RUN npm run build

# Production stage: nginx serves the static files and proxies API/WebSocket to backend
FROM nginx:alpine
# Replace the default nginx config with ours (handles /api/ and /vroom-ws/ proxying)
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copy the compiled Angular output into nginx's serve directory
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
