# QQQ Business Platform - Test and Validation Commands
# Usage: make <target>

.PHONY: help test test-quick test-full test-infrastructure test-api test-frontend clean-test start stop restart logs build status

# Default target
help:
	@echo "🚀 QQQ Business Platform Test Suite"
	@echo "=================================="
	@echo "Available commands:"
	@echo ""
	@echo "  make test           - Run quick validation tests"
	@echo "  make test-full      - Run comprehensive Python test suite"
	@echo "  make test-dashboard - Open browser-based test dashboard"
	@echo "  make test-infra     - Test only infrastructure services"
	@echo "  make test-api       - Test only API endpoints"
	@echo "  make test-frontend  - Test only frontend pages"
	@echo ""
	@echo "  make start          - Start all platform services"
	@echo "  make stop           - Stop all services"
	@echo "  make restart        - Restart all services"
	@echo "  make build          - Build all Docker images"
	@echo "  make logs           - Show service logs"
	@echo "  make status         - Show service status"
	@echo ""
	@echo "  make clean          - Clean up Docker resources"
	@echo "  make clean-all      - Clean everything (containers, images, volumes)"
	@echo ""
	@echo "Access Points:"
	@echo "  Frontend:    http://localhost:13000"
	@echo "  Backend API: http://localhost:18080/api"
	@echo "  Keycloak:    http://localhost:18081/admin (admin/admin)"
	@echo "  Mailpit:     http://localhost:8025"
	@echo "  MinIO:       http://localhost:19001 (minio_admin/minio_admin_password)"

# Quick test using shell script
test:
	@echo "🧪 Running QQQ Business Platform Quick Tests..."
	@chmod +x tests/quick-test.sh
	@bash tests/quick-test.sh

# Comprehensive Python test suite
test-full:
	@echo "🔬 Running Comprehensive Test Suite..."
	@python3 tests/platform-validation.py

# Open browser-based test dashboard
test-dashboard:
	@echo "🌐 Opening Test Dashboard..."
	@echo "Dashboard available at: file://$(PWD)/tests/dashboard.html"
	@if command -v xdg-open > /dev/null 2>&1; then \
		xdg-open tests/dashboard.html; \
	elif command -v open > /dev/null 2>&1; then \
		open tests/dashboard.html; \
	elif command -v start > /dev/null 2>&1; then \
		start tests/dashboard.html; \
	else \
		echo "Please open tests/dashboard.html in your browser"; \
	fi

# Test infrastructure only
test-infra:
	@echo "🏗️ Testing Infrastructure Services..."
	@echo "PostgreSQL:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:18080/api/public/v1/health || echo "Failed"
	@echo "Keycloak:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:18081/realms/qqq-platform/.well-known/openid_configuration || echo "Failed"
	@echo "Mailpit:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8025 || echo "Failed"
	@echo "MinIO:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:19001 || echo "Failed"

# Test API endpoints only
test-api:
	@echo "🔌 Testing API Endpoints..."
	@echo "Public Health:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:18080/api/public/v1/health || echo "Failed"
	@echo "Public Content:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:18080/api/public/v1/content/posts || echo "Failed"
	@echo "Member API (should be 401/403):" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:18080/api/member/v1/profile || echo "Failed"
	@echo "Staff API (should be 401/403):" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:18080/api/staff/v1/users || echo "Failed"

# Test frontend only
test-frontend:
	@echo "🌐 Testing Frontend Pages..."
	@echo "Homepage:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:13000 || echo "Failed"
	@echo "Login:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:13000/login || echo "Failed"
	@echo "Register:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:13000/register || echo "Failed"
	@echo "Blog:" && curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:13000/blog || echo "Failed"

# Platform management commands
start:
	@echo "🚀 Starting QQQ Business Platform..."
	docker-compose -f deploy/compose/docker-compose.yml up -d
	@echo "✅ Platform starting! Use 'make status' to check progress"
	@echo "🌐 Frontend will be available at: http://localhost:13000"
	@echo "🔌 API will be available at: http://localhost:18080/api"

stop:
	@echo "🛑 Stopping QQQ Business Platform..."
	docker-compose -f deploy/compose/docker-compose.yml down

restart: stop start

build:
	@echo "🏗️ Building QQQ Business Platform images..."
	docker-compose -f deploy/compose/docker-compose.yml build --no-cache

logs:
	@echo "📋 Showing service logs..."
	docker-compose -f deploy/compose/docker-compose.yml logs --tail=50 -f

status:
	@echo "📊 QQQ Business Platform Status"
	@echo "=============================="
	@docker-compose -f deploy/compose/docker-compose.yml ps
	@echo ""
	@echo "🔗 Service URLs:"
	@echo "Frontend:     http://localhost:13000"
	@echo "Backend API:  http://localhost:18080/api/public/v1/health"
	@echo "Keycloak:     http://localhost:18081/admin"
	@echo "Mailpit:      http://localhost:8025"
	@echo "MinIO:        http://localhost:19001"

# Cleanup commands
clean:
	@echo "🧹 Cleaning up QQQ Business Platform..."
	docker-compose -f deploy/compose/docker-compose.yml down --volumes
	docker system prune -f

clean-all:
	@echo "🧹 Deep cleaning QQQ Business Platform..."
	docker-compose -f deploy/compose/docker-compose.yml down --volumes --rmi all
	docker system prune -af

# Development helpers
dev-setup:
	@echo "🛠️ Setting up development environment..."
	@if ! command -v docker > /dev/null 2>&1; then \
		echo "❌ Docker is required but not installed"; \
		exit 1; \
	fi
	@if ! command -v docker-compose > /dev/null 2>&1; then \
		echo "❌ Docker Compose is required but not installed"; \
		exit 1; \
	fi
	@echo "✅ Docker and Docker Compose are available"
	@echo "✅ Development environment ready"

# Wait for services to be ready
wait-ready:
	@echo "⏳ Waiting for services to be ready..."
	@timeout=60; \
	while [ $$timeout -gt 0 ]; do \
		if curl -s -f http://localhost:13000 > /dev/null 2>&1 && \
		   curl -s -f http://localhost:18080/api/public/v1/health > /dev/null 2>&1; then \
			echo "✅ Services are ready!"; \
			exit 0; \
		fi; \
		echo "⏳ Still waiting... ($$timeout seconds remaining)"; \
		sleep 5; \
		timeout=$$((timeout - 5)); \
	done; \
	echo "❌ Services did not become ready in time"; \
	exit 1

# Full workflow: build, start, wait, test
full-test: build start wait-ready test

# Quick start for demo
demo:
	@echo "🎬 Starting QQQ Business Platform Demo..."
	@make start
	@echo "⏳ Waiting 30 seconds for services to initialize..."
	@sleep 30
	@make test-dashboard
	@echo ""
	@echo "🎉 Demo Ready!"
	@echo "📱 Test Dashboard: file://$(PWD)/tests/dashboard.html"
	@echo "🌐 Main Site: http://localhost:13000"
	@echo "🔌 API Docs: http://localhost:18080/swagger-ui.html"
	@echo "🔐 Keycloak: http://localhost:18081/admin (admin/admin)"