# ==========================================
# STAGE 1: Build the React app
# ==========================================
# Use a lightweight Node image
FROM node:18-alpine AS builder

# Set the working directory
WORKDIR /app

# Copy package.json and install dependencies
COPY package*.json ./
RUN npm install

# Copy the rest of the code and build the project
COPY . .
RUN npm run build

# ==========================================
# STAGE 2: Serve the app with Nginx
# ==========================================
# Use a tiny Nginx image
FROM nginx:alpine

# Copy the built React files from the 'builder' stage into Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80 (the default HTTP port)
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]