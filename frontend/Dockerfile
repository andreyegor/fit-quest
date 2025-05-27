FROM node:24-slim AS builder
WORKDIR /app
COPY ./package.json ./
RUN npm install
COPY ./src ./src
COPY ./public ./public
COPY ./tsconfig.json ./
COPY ./vite.config.ts ./
RUN npm run build

FROM nginx:alpine-slim
COPY --from=builder /app/dist /usr/share/nginx/html
