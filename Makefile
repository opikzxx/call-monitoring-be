.PHONY: quick env up down logs test

quick: env test up

env:
	test -f .env || cp .env.example .env

up: env
	docker compose -f docker-compose.dev.yml up -d

down:
	docker compose -f docker-compose.dev.yml down

logs:
	docker compose -f docker-compose.dev.yml logs -f app

test:
	./gradlew test
