# Standalone Build and Execution environment for the RoCo Gui React frontend.
# docker build -t frontend .
FROM node:20-alpine AS build

#install npm
RUN apk add --no-cache nodejs npm

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
#Enter bash shell to run the build command
ENTRYPOINT ["/bin/bash"]

#RUN npm run build

# Production stage
#FROM nginx:alpine


# Copy built React app to Nginx html directory
#COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 80

#CMD ["nginx", "-g", "daemon off;"]
#ENTRYPOINT [ "/bin/bash" ]