# HealthSys Distribuido Backend

Monorepo backend do projeto HealthSys SaaS com Spring Boot, API Gateway, PostgreSQL, RabbitMQ, Redis e microsservicos.

## Estrutura

- `services/api-gateway`: entrada HTTP unica e roteamento para os servicos internos
- `services/identity-service`: autenticacao JWT, logout com revogacao e gestao de usuarios
- `services/patient-service`: cadastro, consulta, atualizacao e inativacao logica de pacientes
- `services/triage-service`: abertura, consulta e atualizacao de status de triagens
- `services/notification-service`: consumo de eventos RabbitMQ, persistencia e leitura de notificacoes
- `infra/docker-compose.yml`: ambiente local completo com PostgreSQL, RabbitMQ, Redis, backend e frontend
- `infra/postgres/init`: criacao idempotente dos bancos dos servicos
- `docs/`: documentacao tecnica e auditorias

## Escopo Implementado

- Semanas 1-2
  - estrutura do repositorio
  - modelagem inicial dos bancos
  - ambiente Docker Compose
  - PostgreSQL e RabbitMQ em containers
- Semanas 3-4
  - login com JWT
  - logout com revogacao de token
  - cadastro e listagem de usuarios
  - cadastro, consulta, atualizacao e inativacao de pacientes
  - API Gateway com rotas centrais
- Semanas 5-6
  - `triage-service` para fluxo de triagem
  - `notification-service` persistente para eventos assincronos
  - Redis para cache/rate limiter
  - eventos via RabbitMQ entre servicos
  - frontend buildado e servido pelo Compose

## Execucao Completa

Na raiz do backend:

```powershell
Set-Location C:\Users\felip\OneDrive\Desktop\Comp-Dist-Backend
docker compose -f .\infra\docker-compose.yml up -d --build
```

Esse comando sobe:

- PostgreSQL
- `postgres-init`
- RabbitMQ
- Redis
- `identity-service`
- `patient-service`
- `triage-service`
- `notification-service`
- `api-gateway`
- frontend

URLs:

- Frontend: `http://localhost:4173`
- API Gateway: `http://localhost:8080`
- RabbitMQ UI: `http://localhost:15672`

Credenciais padrao:

- admin: `admin@healthsys.local`
- senha: `Admin@123`
- RabbitMQ: `healthsys` / `healthsys`

## Ajuste do PostgreSQL

O Compose inclui o servico `postgres-init`. Ele roda depois do PostgreSQL ficar saudavel e antes dos microsservicos, executando `infra/postgres/init/01-create-databases.sql`.

Esse script e idempotente: cria apenas os bancos ausentes. Isso resolve tanto instalacao limpa quanto ambiente com volume antigo, onde o init padrao do container PostgreSQL nao roda novamente.

Bancos criados/verificados:

- `healthsys_identity`
- `healthsys_patient`
- `healthsys_triage`
- `healthsys_notification`

## Endpoints via Gateway

Autenticacao:

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`

Usuarios:

- `GET /api/users`
- `POST /api/users`

Pacientes:

- `GET /api/patients`
- `GET /api/patients/{id}`
- `POST /api/patients`
- `PUT /api/patients/{id}`

Triagens:

- `GET /api/triages`
- `GET /api/triages/{id}`
- `GET /api/triages/patient/{patientId}`
- `POST /api/triages`
- `PUT /api/triages/{id}/status`

Notificacoes:

- `GET /api/notifications`
- `GET /api/notifications?unread=true`
- `GET /api/notifications/{id}`
- `PUT /api/notifications/{id}/read`

## Testes e Validacao

Backend:

```powershell
mvn test -q
```

Frontend:

```powershell
Set-Location C:\Users\felip\OneDrive\Desktop\Comp-Dist-Fronted
npm.cmd run typecheck
npm.cmd run build
```

Smoke real, com a stack de pe:

```powershell
Set-Location C:\Users\felip\OneDrive\Desktop\Comp-Dist-Backend
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-weeks-1-4.ps1
```

Observacao: apos `docker compose up -d --build`, aguarde os servicos Spring Boot terminarem a inicializacao antes de executar chamadas manuais. O container pode aparecer como `Up` antes de o endpoint estar pronto.

## Deploy em Nuvem

O workflow `.github/workflows/cd.yml` executa o deploy em Azure AKS a cada push na `main`.

O CD:

- builda e publica imagens multi-arch no GitHub Container Registry
- aplica os manifests Kubernetes no namespace `healthsys`
- inicializa/verifica os bancos PostgreSQL de forma idempotente
- faz rollout de `identity-service`, `patient-service`, `triage-service`, `notification-service`, `api-gateway`, `frontend`, `prometheus` e `grafana`

Recursos aplicados:

- PostgreSQL
- RabbitMQ
- Redis
- microsservicos Spring Boot
- frontend React servido por Nginx
- Prometheus
- Grafana
- Ingress para frontend/API e dashboards

## Fora de Escopo Nesta Entrega

- prontuario eletronico completo
- triagem completa alem do fluxo inicial implementado
- operacao offline
- analytics hospitalar
- QR Code
- monitoramento hospitalar completo
- educacao em saude
- Terraform
- ELK Stack
- funcionalidades opcionais do documento base
