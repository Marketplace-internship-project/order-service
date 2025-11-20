#!/bin/bash


GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${GREEN}=== Marketplace Quick Start ===${NC}"

if [ -f .env.example ] && [ ! -f .env ]; then
    echo -e "${GREEN}[1/4] Creating .env file from .env.example...${NC}"
    cp .env.example .env
else
    echo -e "${GREEN}[1/4] .env file already exists.${NC}"
fi

cd ..

if [ ! -d "user-service" ]; then
    echo -e "${GREEN}[2/4] Cloning User Service...${NC}"
    git clone https://github.com/Marketplace-internship-project/user-service.git
else
    echo -e "${GREEN}[2/4] User Service directory exists. Skipping clone.${NC}"
fi


#if [ ! -d "authentication-service" ]; then
#    echo -e "${GREEN}[3/4] Cloning Authentication Service...${NC}"
#    git clone https://github.com/Marketplace-internship-project/authentication-service.git
#else
#    echo -e "${GREEN}[3/4] Authentication Service directory exists. Skipping clone.${NC}"
#fi

cd order-service

echo -e "${GREEN}[4/4] Starting all services with Docker Compose...${NC}"
docker-compose up --build