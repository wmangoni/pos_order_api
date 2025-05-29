#!/bin/bash

# Base URLs
MENU_SERVICE_URL="http://localhost:8081"
ORDER_SERVICE_URL="http://localhost:8082"

echo "Restaurant POS API Test Script"
echo "--------------------------------"

# --- Menu Service ---
echo "\n--- Menu Service Tests ---"

# 1. Create Menu Item: Pizza
echo "\n1. Creating Menu Item: Pizza..."
PIZZA_PAYLOAD='{
  "name": "Pizza",
  "description": "Delicious cheese pizza",
  "price": 9.99
}'
PIZZA_RESPONSE=$(curl -s -X POST "${MENU_SERVICE_URL}/menu-items" \
  -H "Content-Type: application/json" \
  -d "$PIZZA_PAYLOAD")
echo "Response:"
echo "$PIZZA_RESPONSE" | jq . # Requires jq to be installed for pretty print
PIZZA_ID=$(echo "$PIZZA_RESPONSE" | jq -r '.id')
echo "Pizza ID: $PIZZA_ID"

# 2. Create Menu Item: Soda
echo "\n2. Creating Menu Item: Soda..."
SODA_PAYLOAD='{
  "name": "Soda",
  "description": "Refreshing soda",
  "price": 1.99
}'
SODA_RESPONSE=$(curl -s -X POST "${MENU_SERVICE_URL}/menu-items" \
  -H "Content-Type: application/json" \
  -d "$SODA_PAYLOAD")
echo "Response:"
echo "$SODA_RESPONSE" | jq .
SODA_ID=$(echo "$SODA_RESPONSE" | jq -r '.id')
echo "Soda ID: $SODA_ID"

# 3. Get All Menu Items
echo "\n3. Getting All Menu Items (limit=5, offset=0)..."
curl -s -X GET "${MENU_SERVICE_URL}/menu-items?limit=5&offset=0" | jq .

# 4. Get Menu Item by ID (Pizza)
echo "\n4. Getting Menu Item: Pizza (ID: $PIZZA_ID)..."
curl -s -X GET "${MENU_SERVICE_URL}/menu-items/${PIZZA_ID}" | jq .

# 5. Update Menu Item (Pizza)
echo "\n5. Updating Menu Item: Pizza (ID: $PIZZA_ID)..."
UPDATED_PIZZA_PAYLOAD='{
  "name": "Large Pizza",
  "description": "Cheese pizza with extra toppings",
  "price": 11.99
}'
curl -s -X PUT "${MENU_SERVICE_URL}/menu-items/${PIZZA_ID}" \
  -H "Content-Type: application/json" \
  -d "$UPDATED_PIZZA_PAYLOAD" | jq .

# --- Order Service ---
echo "\n\n--- Order Service Tests ---"

# Check if PIZZA_ID and SODA_ID are valid (not "null")
if [ "$PIZZA_ID" == "null" ] || [ "$SODA_ID" == "null" ]; then
  echo "\nError: Could not retrieve valid Pizza ID or Soda ID. Aborting Order Service tests."
  echo "Please ensure Menu Service is running and items were created successfully."
  # 6. Delete Menu Items (Cleanup from Menu Service if Order tests abort)
  if [ "$PIZZA_ID" != "null" ]; then
    echo "\nAttempting to delete Pizza (ID: $PIZZA_ID)..."
    curl -s -X DELETE "${MENU_SERVICE_URL}/menu-items/${PIZZA_ID}" | jq .
  fi
  if [ "$SODA_ID" != "null" ]; then
    echo "\nAttempting to delete Soda (ID: $SODA_ID)..."
    curl -s -X DELETE "${MENU_SERVICE_URL}/menu-items/${SODA_ID}" | jq .
  fi
  exit 1
fi

# 1. Create Order
echo "\n1. Creating Order..."
ORDER_PAYLOAD=$(cat <<EOF
{
  "customer": {
    "fullName": "William Mangoni",
    "address": "123 Main St, Anytown, USA",
    "email": "william@gmail.com"
  },
  "orderItems": [
    { "productId": "${PIZZA_ID}", "quantity": 2 },
    { "productId": "${SODA_ID}", "quantity": 1 }
  ]
}
EOF
)
echo "Order Payload:"
echo "$ORDER_PAYLOAD" | jq .

ORDER_RESPONSE=$(curl -s -X POST "${ORDER_SERVICE_URL}/orders" \
  -H "Content-Type: application/json" \
  -d "$ORDER_PAYLOAD")
echo "Response:"
echo "$ORDER_RESPONSE" | jq .
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.id')
echo "Order ID: $ORDER_ID"

# Check if ORDER_ID is valid
if [ "$ORDER_ID" == "null" ]; then
  echo "\nError: Could not create order or retrieve valid Order ID. Aborting further Order Service tests."
  # Attempt cleanup for menu items
  echo "\nAttempting to delete Pizza (ID: $PIZZA_ID)..."
  curl -s -X DELETE "${MENU_SERVICE_URL}/menu-items/${PIZZA_ID}" | jq .
  echo "\nAttempting to delete Soda (ID: $SODA_ID)..."
  curl -s -X DELETE "${MENU_SERVICE_URL}/menu-items/${SODA_ID}" | jq .
  exit 1
fi

# 2. Get Order by ID
echo "\n2. Getting Order by ID (ID: $ORDER_ID)..."
curl -s -X GET "${ORDER_SERVICE_URL}/orders/${ORDER_ID}" | jq .

# 3. Update Order Status
echo "\n3. Updating Order Status to PREPARING (ID: $ORDER_ID)..."
STATUS_UPDATE_PAYLOAD='{
  "status": "PREPARING"
}'
curl -s -X PATCH "${ORDER_SERVICE_URL}/orders/${ORDER_ID}/status" \
  -H "Content-Type: application/json" \
  -d "$STATUS_UPDATE_PAYLOAD" | jq .
echo "Check Order Service logs for RabbitMQ message consumption and simulated notification."

# Wait a moment for RabbitMQ processing (optional)
sleep 2

# 4. Get Order by ID (to see updated status)
echo "\n4. Getting Order by ID (ID: $ORDER_ID) again to check status..."
curl -s -X GET "${ORDER_SERVICE_URL}/orders/${ORDER_ID}" | jq .

# 5. Get Order History
echo "\n5. Getting Order History (limit=5, offset=0)..."
curl -s -X GET "${ORDER_SERVICE_URL}/orders?limit=5&offset=0" | jq .


# --- Cleanup ---
echo "\n\n--- Cleanup: Deleting created items ---"

# Delete Order (if an endpoint existed, not specified in requirements)
# echo "\nDeleting Order (ID: $ORDER_ID)..."
# curl -X DELETE "${ORDER_SERVICE_URL}/orders/${ORDER_ID}"

# Delete Menu Items
echo "\nDeleting Pizza (ID: $PIZZA_ID)..."
curl -s -X DELETE "${MENU_SERVICE_URL}/menu-items/${PIZZA_ID}" | jq .

echo "\nDeleting Soda (ID: $SODA_ID)..."
curl -s -X DELETE "${MENU_SERVICE_URL}/menu-items/${SODA_ID}" | jq .

echo "\n\nScript finished."
echo "Don't forget to check the logs of menu-service, order-service, and rabbitmq (via docker logs <container_name> or management UI)."
