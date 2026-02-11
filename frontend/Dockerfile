# Build stage
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . ./
RUN npm run build

# Production stage
FROM nginx:alpine
# Copy your custom config
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copy the Angular build output
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
