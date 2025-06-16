# Standalone Build and Execution environment for the RoCo Gui React frontend.
# docker build -t frontend .
# docker run -p 3000:3000 -e VITE_BACKEND_URL=https://backend:8080 frontend

FROM node:18-alpine

WORKDIR /app

COPY package.json package-lock.json ./
RUN apk add --no-cache python3 make g++ \
 && npm ci \
 && apk del python3 make g++

RUN npm i -g serve

#COPY package.json .
#COPY package.json package-lock.json ./
#RUN npm ci
#RUN npm install


COPY . .

RUN npm run build

EXPOSE 3000

CMD [ "serve", "-s", "dist" ]
#CMD [ "npm", "run", "dev", "--host 0.0.0.0" ]