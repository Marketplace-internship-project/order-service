Order service
---
Service for manage customers' orders

---
# Quick start

Clone this repository (if you haven't already):
```
git clone -b dev https://github.com/Marketplace-internship-project/order-service.git
cd order-service
```

Run the setup script for your OS:

Linux / macOS / Git Bash:
```
chmod +x setup.sh
./setup.sh
```



The script will:

- Create .env from .env.example.
- Clone sibling repositories (user-service, authentication-service) into the parent directory.
- Build and start all containers using docker-compose.

Manual Setup (if scripts fail)

If you prefer to do it manually:

Ensure your directory structure looks like this:

```
/workspace
├── order-service        (this repo)
├── user-service         (cloned from git)
```

Inside order-service, create .env file:
```
cp .env.example .env
```

Run docker compose:
```
docker-compose up --build
```
