# Restaurant POS System - Backend Services

This project implements a microservice-based backend for a restaurant Point of Sale (POS) system. It consists of two main services:

1.  **Menu Service**: Manages the restaurant's menu items.
2.  **Order Service**: Handles customer orders, status updates, and notifications via RabbitMQ.

Both services are built using Java Spring Boot and use MongoDB for data storage. Docker is used for containerization and `docker-compose` for orchestration.

## Project Structure

```
restaurant-pos-system/
├── menu-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/menuservice/
│       │   │   ├── MenuServiceApplication.java
│       │   │   ├── model/MenuItem.java
│       │   │   ├── dto/MenuItemRequestDTO.java
│       │   │   ├── dto/MenuItemResponseDTO.java
│       │   │   ├── dto/PaginatedMenuItemResponseDTO.java
│       │   │   ├── repository/MenuItemRepository.java
│       │   │   ├── service/MenuItemService.java
│       │   │   └── controller/MenuItemController.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/example/menuservice/
│               └── controller/MenuItemControllerTest.java // Basic Test
├── order-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/orderservice/
│       │   │   ├── OrderServiceApplication.java
│       │   │   ├── model/
│       │   │   │   ├── Order.java
│       │   │   │   ├── OrderItem.java
│       │   │   │   └── Customer.java
│       │   │   ├── dto/
│       │   │   │   ├── CreateOrderRequestDTO.java
│       │   │   │   ├── OrderItemRequestDTO.java
│       │   │   │   ├── CustomerDTO.java
│       │   │   │   ├── OrderResponseDTO.java
│       │   │   │   ├── OrderItemResponseDTO.java
│       │   │   │   ├── OrderStatusUpdateDTO.java
│       │   │   │   ├── PaginatedOrderResponseDTO.java
│       │   │   │   └── MenuItemDetailsDTO.java // For fetching from MenuService
│       │   │   ├── repository/OrderRepository.java
│       │   │   ├── service/
│       │   │   │   ├── OrderService.java
│       │   │   │   ├── RabbitMQSender.java
│       │   │   │   └── RabbitMQConsumer.java
│       │   │   ├── config/
│       │   │   │   ├── RabbitMQConfig.java
│       │   │   │   └── RestTemplateConfig.java
│       │   │   └── controller/OrderController.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/example/orderservice/
│               └── service/OrderServiceTest.java // Basic Test
├── docker-compose.yml
├── curl_requests.sh
├── postman_collection.json
└── README.md
```


## Prerequisites

* Java 23
* Maven 3.6.3 or later
* Docker
* Docker Compose

## Tech Stack

* Java 23
* Spring Boot 3.2.5
* Spring Data MongoDB
* Spring AMQP (RabbitMQ)
* MongoDB 8.0
* RabbitMQ 3.13.7
* Docker & Docker Compose
* Lombok

## Setup and Running the Application

1.  **Clone the Repository (or create the files as provided):**
    If this code is in a Git repository, clone it. Otherwise, create the directory structure and files as detailed in the subsequent sections.

2.  **Build the Services:**
    Navigate to the root directory of each service (`menu-service` and `order-service`) and run:
    ```bash
    mvn clean package -DskipTests
    ```
    This will build the JAR files for each service.

3.  **Run using Docker Compose:**
    From the root directory of the project (`restaurant-pos-system/`), run:
    ```bash
    docker-compose up --build
    ```
    This command will:
    * Build Docker images for `menu-service` and `order-service`.
    * Start containers for `menu-service`, `order-service`, `mongodb`, and `rabbitmq`.

    The services will be available at:
    * Menu Service: `http://localhost:8081`
    * Order Service: `http://localhost:8082`
    * RabbitMQ Management UI: `http://localhost:15672` (guest/guest)

4.  **Testing with Curl:**
    Use the provided `curl_requests.sh` script or individual curl commands to interact with the APIs. Make sure the services are running.
    ```bash
    chmod +x curl_requests.sh
    ./curl_requests.sh
    ```

    Or you can test manually by postman importing collection (/postman_collection.json) on the root of project

## API Endpoints

Refer to the problem description or the `curl_requests.sh` file for detailed API endpoints and payloads.

### Menu Service (Port 8081)

* `POST /menu-items`: Create a new menu item.
* `PUT /menu-items/{id}`: Update an existing menu item.
* `DELETE /menu-items/{id}`: Delete a menu item.
* `GET /menu-items?limit=10&offset=0`: Get all menu items (paginated).
* `GET /menu-items/{id}`: Get a menu item by its ID.

### Order Service (Port 8082)

* `POST /orders`: Create a new order. (This service will call Menu Service to fetch item details).
* `PATCH /orders/{orderId}/status`: Update the status of an order. (This will publish a message to RabbitMQ).
* `GET /orders?limit=10&offset=0`: Get order history (paginated).
* `GET /orders/{orderId}`: Get an order by its ID.

## RabbitMQ Integration

* **Exchange:** `order.exchange` (Direct Exchange)
* **Queue:** `order.status.queue`
* **Routing Key:** `order.status.routingkey`

When an order's status is updated via `PATCH /orders/{orderId}/status`, the Order Service publishes a message to `order.exchange` with the routing key `order.status.routingkey`.
The Order Service also consumes messages from `order.status.queue`. Upon receiving a message, it simulates a customer notification by logging the order details and the new status.

## MongoDB Collections

* **Menu Service:** `menuItems` collection in the `menu_db` database.
* **Order Service:** `orders` collection in the `order_db` database.

## Important Notes

* The communication between Order Service and Menu Service (for fetching menu item details during order creation) is done via REST API calls. The Menu Service URL is configured in the Order Service's `application.properties`.
* Error handling is basic. For a production system, more robust error handling and validation would be needed.
* The tests provided are basic integration tests for controllers/services. More comprehensive unit and integration tests should be added for a production environment.
