# Builds the Next.js app for the staging preview (backend/compose.prod.yaml).
#
# Stage 1 compiles the production bundle; stage 2 serves it with `next start`.
# Nothing here changes how local development works — `npm run dev` still reads
# frontend/.env as it always has.

FROM node:24-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# The browser reaches the API through the same origin the page came from
# (nginx proxies /api), so the baked-in base URL is empty and client.ts falls
# back to relative requests. An unset or "http://localhost:8080" value here
# would point a reviewer's browser at a server that only exists on one machine.
ARG NEXT_PUBLIC_API_BASE_URL=
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL

RUN npm run build

FROM node:24-alpine AS run
WORKDIR /app
ENV NODE_ENV=production

COPY package.json package-lock.json ./
RUN npm ci --omit=dev && npm cache clean --force

COPY --from=build --chown=node:node /app/.next ./.next
COPY --from=build --chown=node:node /app/public ./public
COPY --from=build --chown=node:node /app/next.config.ts ./next.config.ts

USER node
EXPOSE 3000

CMD ["npm", "run", "start"]
