# Use Node.js 16 as base image
FROM node:16-alpine

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# If .env.docker exists, use it for the build
RUN if [ -f .env.docker ]; then cp .env.docker .env; fi

# Build the application
RUN npm run build

# Install serve to serve the static files
RUN npm install -g serve

# Expose port 3000
EXPOSE 3000

# Serve the application
CMD ["serve", "-s", "build", "-l", "3000"]