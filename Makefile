up:
	docker compose up --build -d

down:
	docker compose down

reset:
	docker compose down -v
	docker compose up --build -d

restart:
	docker compose down
	docker compose up --build -d

logs:
	docker compose logs -f

ps:
	docker compose ps

app-logs:
	docker compose logs -f app

db-shell:
	docker compose exec postgres psql -U postgres -d recruitment_platform

mail:
	open http://localhost:8025

swagger:
	open http://localhost:8080/swagger-ui/index.html