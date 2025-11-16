#!/bin/bash

# chmod +x dev-init.sh

set -e
docker network create smart-payments
sleep 15

# ==== COLORS ====
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
BLUE="\033[34m"
RESET="\033[0m"
BOLD="\033[1m"

# ==== ENTER ON ROOT .DEV FOLDER ====
cd .dev

# ==== FUNCTION TO CHECK IF CONTAINERS ARE RUNNING ====
check_containers() {
    local service="$1"

    echo -e "${YELLOW}🔍 Checking containers for service '${service}'...${RESET}"
    sleep 2

    FAILED=$(docker compose ps --services --filter "status=exited")

    if [ -z "$FAILED" ]; then
        echo -e "${GREEN}✔ All containers for '${service}' are UP.${RESET}"
    else
        echo -e "${RED}❌ Some containers failed in '${service}':${RESET}"
        echo "$FAILED"
        exit 1
    fi
}

# ==== FUNCTION: WAIT UNTIL ALL CONTAINERS ARE HEALTHY (if healthcheck exists) ====
wait_for_health() {
    local service="$1"

    echo -e "${YELLOW}⏳ Waiting for healthy containers in '${service}'...${RESET}"

    # Loop until all containers report healthy or until timeout
    for i in {1..30}; do
        UNHEALTHY=$(docker compose ps --format json | jq -r '.[] | select(.Health=="unhealthy" or .Health=="starting")')

        if [ -z "$UNHEALTHY" ]; then
            echo -e "${GREEN}✔ Containers in '${service}' are healthy.${RESET}"
            return 0
        fi

        sleep 2
    done

    echo -e "${RED}❌ Containers in '${service}' did not become healthy in time.${RESET}"
    exit 1
}

# =====================
# ROOT
# =====================

echo -e "${BLUE}${BOLD}🚀 Starting root docker compose...${RESET}"
docker compose -p smart-payments up -d
echo -e "${GREEN}✔ Root docker compose started.${RESET}"

check_containers "root"
wait_for_health "root"

cd ..

# =====================
# AUTHENTICATOR
# =====================

echo ""
echo -e "${BLUE}${BOLD}➡ Entering authenticator/.dev...${RESET}"
cd authenticator/.dev

echo -e "${BLUE}${BOLD}🚀 Starting authenticator docker compose...${RESET}"
docker compose -p smart-payments up -d
echo -e "${GREEN}✔ Authenticator started.${RESET}"

check_containers "authenticator"
wait_for_health "authenticator"

cd ..

echo ""
echo -e "${GREEN}${BOLD}🎉 All service dependencies have been successfully started!${RESET}"
