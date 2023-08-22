# EcomAPI
# Relationships:
1. Each User can have one Cart.
2. Each Cart can belong to only one User.
3. Each Cart can have multiple CartItems.
4. Multiple CartItems can belong to one Cart.
5. Each CartItem contains one product.
6. Each product can belong to more than one CartItems.
7. Each product contains one or many ProductStocks.
8. Each ProductStock belongs to one Product.
9. Each Sale belongs to one Customer only.
10. Each Customer can have multiple Sales.
11. Each Sale contains one or many SaleItems.
12. Each SaleItem belongs to only one Sale.
13. Each Customer is also a User.
14. Each User is not a Customer until one makes a sale.
15. Each Customer can create multiple reviews.
16. Each Product can have multiple reviews.

# Functionality so far:
1. User can sign up using a Signup endpoint. The payload for signup is validated.
2. User can log in with his/her credentials and is provided an access token for accessing the rest of the protected endpoints.
3. Each access token expires within 20 minutes.
4. User can log out using the logout endpoint such that the user's access token is blacklisted.
5. Before accessing each endpoint a custom annotation checks if the incoming access token is blacklisted or not, if yes, the request is declined.
6. Each user can reset his/her password using the password reset endpoint, such that an email is sent that contains a link to update/reset the password.
7. Products can be added/created using the create product endpoint. This endpoint can be accessed by admin only.
8. Products can be updated using the update product endpoint. This endpoint can be accessed by admin only.
9. List of all products can be retrieved using the getAllProducts endpoint. This endpoint can be accessed by admin only.
10. List of products with their stock summary such that the weighted average price and total number of units for each product can be retrieved using products-with-stock-summary endpoint.
11. Products can be deleted using deleteProduct endpoint. This endpoint can be accessed by admin only.
12. A stock entity for a product can be created using createStock endpoint. This endpoint can be accessed by admin only.
13. A stock entity can be updated using updateStock endpoint. This endpoint can be accessed by admin only.
14. A stock entity for a product can be retrieved using getStock endpoint. This endpoint can be accessed by admin only.
15. A list of all stocks for all products can be retrieved using getAllStocks endpoint. This endpoint can be accessed by admin only.
16. A stock entity for a product can be deleted using deleteStock endpoint. This endpoint can be accessed by admin only.
17. List of all stock entities for a specific product can be retrieved using getStocksByProduct endpoint. This endpoint can be accessed by admin only.
18. All stock entities for a specific product can be deleted using deleteAllStockByProduct endpoint. This endpoint can be accessed by admin only.
19. A cart item for a user can be created using createCartItem endpoint. If the same endpoint is hit repeatedly for the same product by the same user, the quantity adds up.
20. List of all cart items in a cart of user can be retrieved using getAllCartItemsByCart endpoint.
21. A cart item can be updated using updateCartItem endpoint while providing the updated quantity and cart item id.
22. A cart item can be removed from the cart using deleteCartItemById endpoint.
23. A cart for a user can be emptied using emptyCart endpoint.
24. User can checkout using the checkout endpoint such that all the cart items in the cart for that user make sale items which in turn make a sale.
25. A user can see sales made under his/her name using the getAllSaleItemsBySale endpoint.
26. A user can delete his/her specific order via deleteAllSaleItemsforSale endpoint.
27. **A user can now purchase a product only if payment via stripe is successful.**
28. Now payment mode is also set for a Sale after successful payment.
29. An admin can get list of all users using getAllUsers endpoint. This endpoint can be accessed by admin only.
30. An admin can get list of all customers using getAllCustomerProfiles endpoint. This endpoint can be accessed by admin only.
31. An admin can get list of all the Sales of all the Customers using getAllSales endpoint. This endpoint can be accessed by admin only.
32. An admin can get all Sales of a specific Customer by using getAllSaleItemsBySaleForAdmin endpoint. This endpoint can be accessed by admin only.
33. An admin can delete a Sale of a Customer by using deleteAllSaleItemsforSaleForAdmin endpoint. This endpoint can be accessed by admin only.
34. An admin can update the Status of a Sale using updateSaleStatus endpoint. This endpoint can be accessed by admin only.
35. Now a Customer gets an email upon making a Sale.
36. Product can be queried via the GraphQL endpoint. It provides all products or a single product using the product ID.
37. ProductStock can be queried via the GraphQL endpoint. It provides all stocks, a single stock by ID, or stocks for a product.
38. CustomerProfile can be queried via the GraphQL endpoint. It provides the customer profile by customer ID, email, or all customer profiles.
39. Sale can be queried via the GraphQL endpoint. It provides the sale by sale ID, sales by customer ID, or all sales.
40. SaleItem can be queried via the GraphQL endpoint. It provides the sale item by sale item ID, sale items by sale ID, or all sale items.
41. Client side can get server sent events if weighted average price changes for a product by subscribing to "/sse/price-update" endpoint.
42. Now a customer can create a review for a product using the createReview endpoint. Customer can only create a review for a product if he/she buys the product.
43. A customer can create only one review per purchased product.
44. An admin can get all reviews of all products by using getAllReviews endpoint. This endpoint is restricted to admin only.
45. A customer can find his/her review for a product using the getReviewByProductIdAndCustomer endpoint.
46. An admin/user can find all the reviews for a specific product using getProductReviews endpoint.
47. A customer can find all his/her reviews using getCustomerReviews endpoint.
48. A customer can update his/her review using updateReview endpoint.
49. A customer can delete his/her review using deleteReview endpoint.
50. An admin can delete a review using deleteReviewForAdmin endpoint. This endpoint is restricted to admin only.
51. Products similar to a specific product can be found using findSimilarProducts endpoint.
52. Now products-with-stock-summary endpoint supports sorting by various parameters:
    - `lowToHigh`: Sort by weighted average price low to high.
    - `highToLow`: Sort by product name high to low.
    - `nameAsc`: Sort by product name A-Z.
    - `nameDesc`: Sort by product name Z-A.
    - `mostReviewed`: Sort by most reviewed products (descending order).
    - `topRated`: Sort by top rated products (descending order of average rating).
    - `bestSelling`: Sort by best selling products (descending order of total units sold).
    - `oldestFirst`: Sort by stock latest date( descending order of latest stock date).
    - `recentFirstFirst`: Sort by stock latest date.
53. Now a user can search for products using key-words via searchProductListByKeyword endpoint.
54. Now products-with-stock-summary endpoint supports pagination i-e returns paginated data.
55. New functionality added:
    - The `get-chat-room` endpoint allows users to retrieve their associated chat room. If a chat room doesn't exist for the user, an admin is randomly assigned, creating a new chat room.
56. **Enhanced Password Security:**
    - We have implemented enhanced password security by encrypting user passwords using BCrypt, a strong cryptographic hashing algorithm.
    - When users register, we hash and salt their passwords using BCrypt before storing them in the database.
    - During user authentication, we securely compare hashed passwords using BCrypt to ensure maximum password security.
57. **Sales Analysis Report:**
	-`reports/sales-analysis` endpoint provides sales analysis report.
    - Added a new class, `SalesAnalysisReport`, to store and retrieve sales analysis data.
    - The class includes the following fields:
      - `totalSales`: The total sales amount.
      - `saleItems`: A list of sale items.
      - `totalUnitsSold`: The total number of units sold.
      - `productWithMaxUnits`: The product with the maximum units sold.
      - `productWithMinUnits`: The product with the minimum units sold.
      - `revenueByDate`: A map of revenue by date.
      - `dateWithHighestRevenue`: The date with the highest revenue.
      - `dateWithLowestRevenue`: The date with the lowest revenue.
      - `dateWithMostUnitsBought`: The date with the most units bought.
      - `dateWithLeastUnitsBought`: The date with the least units bought.
      - `totalRevenueByProduct`: A map of total revenue by product.
      - `unitsBoughtByProduct`: A map of units bought by product.
      - `productWithHighestRevenue`: The product that generated the highest revenue.
      - `productWithLowestRevenue`: The product that generated the lowest revenue.
      - `mostReviewedProduct`: The product that was reviewed the most.
      - `leastReviewedProduct`: The product that was reviewed the least.
      - `productWithHighestRating`: The product with the highest rating.
      - `productWithLowestRating`: The product with the lowest rating.
    - Implemented a controller method, `generateSalesAnalysisReport`, that calculates the sales analysis data based on the provided start and end dates.
    - The calculated data includes the total sales amount, sale items, total units sold, product with the maximum units sold, product with the minimum units sold, revenue by date, date with the highest revenue, date with the lowest revenue, date with the most units bought, date with the least units bought, total revenue by product, units bought by product, product with the highest revenue, product with the lowest revenue, most reviewed product, least reviewed product, product with the highest rating, and product with the lowest rating.
    - The calculated data is populated in an instance of `SalesAnalysisReport` and returned in the response for further analysis or display.
    - The `saleItems` list can be sorted based on a specified parameter by passing the request parameter `sortSaleItemsBy`. The supported parameters for sorting are:
      - `"date"`: Sorts the sale items by the sale date and time in ascending order.
      - `"totalAmount"`: Sorts the sale items by the total amount in descending order.
      - `"subTotal"`: Sorts the sale items by the sub-total amount in ascending order.
      - `"unitsBought"`: Sorts the sale items by the number of units bought in ascending order.


 
# Price calculation and stock updates:
As each product can have multiple stocks and each stock can come up with different dates and prices for each unit, I have used the weighted average price for each product, this price updates itself as new stock comes, the present stock gets updated, the stock gets deleted or stock is consumed for making a sale item. These updates are performed using the ***RabbitMQ***.

# Sale, sale items, cart items, and cart:
Whenever a checkout is performed by a user, cart items are used to make sale items. Each sale item is associated with one oldest available product stock and the quantity of that stock is reduced for that product according to the quantity of units a user buys. This sends a message to the listener which updates the weighted average price and subtotal on each related cart item. Also, the total for the cart is updated.

# Out-of-stock exception:
A user is presented with a list of products where total stocks are not equal to zero. If a user tries to buy a quantity of a product that is greater than the total available stock for that product, an exception is thrown that notifies the user of stock is not available for that product.

# GraphQL:
To retrieve data from the server, clients can send GraphQL queries to graphql endpoint.
The GraphQL endpoint exposes the following methods:

- `products`: Retrieves all products.
- `product`: Retrieves a single product by ID.
- `productStock`: Retrieves a single product stock by ID.
- `allProductStocks`: Retrieves all product stocks.
- `productStocks`: Retrieves product stocks for a specific product.
- `getCustomerProfileById`: Retrieves the customer profile by customer ID.
- `getCustomerProfileByEmail`: Retrieves the customer profile by email.
- `getAllCustomerProfiles`: Retrieves all customer profiles.
- `getSaleById`: Retrieves the sale by sale ID.
- `getSalesByCustomerId`: Retrieves sales by customer ID.
- `getAllSales`: Retrieves all sales.
- `getSaleItemById`: Retrieves the sale item by sale item ID.
- `getAllSaleItemsBySaleId`: Retrieves sale items by sale ID.
- `getAllSaleItems`: Retrieves all sale items.

These methods allow users to query and retrieve the required data from the GraphQL endpoint using the specified parameters.

# Server Sent Events:
Subscribers can get SSE when weighted average price changes.

# Similar products:
Jaccard Index is calculated between two sets that originate from json attribute nodes of products. If index is greater or equal to 0.5 that product is added to the list and list is returned.

# Dockerization and Container Orchestration

## Dockerized REST API with PostgreSQL and RabbitMQ

This project has been Dockerized to simplify deployment and management. The Docker containers provide isolation and consistency across different environments. The `docker-compose.yml` file is used to define and manage the services that compose the application stack.

The following components are part of the Dockerized setup:

- **REST API**: The main application that serves the REST API endpoints.
- **PostgreSQL**: A powerful open-source relational database for storing data.
- **RabbitMQ**: A message broker that facilitates communication between different parts of your application.

# Product Search:
The keyword is tokenized and converted to a set. Similarly attribute json nodes of all products are converted to sets, if keyword set is subset of any json node set, that product will be added to the list and returned.

## Integrating with TalkJS for Customer/User Support Chat

TalkJS is a powerful tool that allows you to easily integrate real-time chat functionality into your application. By utilizing the data provided by our new endpoint `getChatRoomForUser`, you can seamlessly integrate customer/user support chat using TalkJS. Here's how you can achieve this:

1. **Retrieving Chat Room Data**
   - Utilize the `getChatRoomForUser` endpoint to retrieve the chat room associated with the user.
   - If a chat room doesn't exist for the user, the endpoint will randomly assign an admin and create a new chat room.

2. **TalkJS User and Admin Setup**
   - Map the retrieved user data to the appropriate TalkJS user object. This will include the user's ID, display name, and any other relevant information.

3. **Initializing the Chat Interface**
   - Using TalkJS's JavaScript SDK, initialize the chat interface on the user's page. Provide the user's information to identify them in the chat.

4. **Support Staff as Admins**
   - Since the chat room is associated with both the user and the admin, you can map the admin's data to a TalkJS admin user object.
   - Assign the admin user as the support staff in the chat room.

5. **Real-time Communication**
   - Users and support staff can now communicate in real-time using the TalkJS chat interface.
   - Messages, updates, and notifications are seamlessly managed by TalkJS.

6. **Enhanced Customer/User Support**
   - With TalkJS, you can provide enhanced customer/user support by facilitating direct and instant communication between users and support staff.
   - Resolve issues, answer queries, and provide assistance more effectively.

By integrating TalkJS with the data from our new endpoint, you can elevate your application's user experience by offering a feature-rich customer/user support chat system. Refer to the TalkJS documentation for more details on how to integrate their SDK and customize the chat interface to match your application's look and feel.

---

Feel free to customize and expand upon these steps to fit your specific application's requirements and integration approach.
