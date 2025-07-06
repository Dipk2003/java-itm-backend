# iTech Chatbot Feature

## Overview
The iTech chatbot is an intelligent recommendation system that helps users find the best vendors and products on the platform. It prioritizes premium vendors (Diamond, Platinum, Gold) and provides personalized recommendations based on user queries.

## Features

### 🤖 **Smart Vendor Recommendations**
- Automatically recommends premium vendors first
- Considers vendor performance scores
- Matches products and services based on user queries
- Provides detailed vendor information including contact details

### 🔍 **Intelligent Query Processing**
- Product-based queries: "I need electronics", "Looking for furniture"
- Service-based queries: "Who provides web development?", "Find marketing services"
- Category matching and keyword extraction
- Natural language understanding

### 💎 **Premium Vendor Prioritization**
Vendors are ranked in the following order:
1. **Diamond** - Highest premium tier
2. **Platinum** - Second tier premium
3. **Gold** - Third tier premium
4. **Basic** - Standard tier

### 📊 **Performance-Based Ranking**
- Integrates with vendor performance scores
- Considers total leads generated
- Factors in vendor reliability metrics

## API Endpoints

### Public Endpoints (No Authentication Required)

#### Start Chat Session
```
POST /api/chatbot/start-session
```
Initializes a new chat session and returns a welcome message.

#### Send Message
```
POST /api/chatbot/chat
```
**Request Body:**
```json
{
  "message": "I need electronics products",
  "sessionId": "uuid-session-id",
  "userId": 123 // Optional: if user is logged in
}
```

**Response:**
```json
{
  "response": "Here are the top recommended vendors for your product query...",
  "sessionId": "uuid-session-id",
  "hasRecommendations": true,
  "recommendations": [
    {
      "vendorId": 1,
      "vendorName": "TechCorp Solutions",
      "vendorEmail": "contact@techcorp.com",
      "vendorPhone": "+1234567890",
      "vendorType": "DIAMOND",
      "performanceScore": 95.5,
      "products": ["Laptop", "Mobile", "Accessories"],
      "categories": ["Electronics", "Computers"],
      "reason": "Premium DIAMOND vendor with 15 products across 3 categories"
    }
  ]
}
```

#### Get Chat History
```
GET /api/chatbot/history/{sessionId}
```
Returns the conversation history for a specific session.

#### Health Check
```
GET /api/chatbot/health
```
Returns chatbot service status.

### Admin Endpoints (Admin Authentication Required)

#### Get Analytics
```
GET /admin/chatbot/analytics
```
Returns chatbot usage analytics including total messages, recent activity, and unique sessions.

#### Get All Conversations
```
GET /admin/chatbot/conversations?page=0&size=20&sortBy=createdAt&sortDir=desc
```
Returns paginated list of all chat conversations.

#### Get Specific Conversation
```
GET /admin/chatbot/conversation/{sessionId}
```
Returns complete conversation history for a specific session.

#### Delete Conversation
```
DELETE /admin/chatbot/conversation/{sessionId}
```
Deletes a specific conversation and all its messages.

#### Get Recent Queries
```
GET /admin/chatbot/recent-queries?limit=10
```
Returns recent user queries for analysis.

## Query Types and Examples

### Product Queries
- "I need electronics products"
- "Looking for furniture items"
- "Show me available laptops"
- "What products do you have in stock?"

### Service Queries
- "Who provides web development services?"
- "Find marketing experts"
- "I need professional photography services"
- "Which companies offer consulting?"

### General Queries
- "Hello" / "Hi" - Greeting responses
- "Help" / "What can you do?" - Help information
- General conversation - Default helpful responses

## Recommendation Algorithm

### 1. **Keyword Extraction**
- Removes stop words (the, and, or, etc.)
- Filters short words (less than 3 characters)
- Extracts meaningful keywords from user queries

### 2. **Product/Service Matching**
- Searches product names, descriptions, and categories
- Matches vendor specializations
- Considers category relationships

### 3. **Vendor Prioritization**
```
Priority Order:
1. Vendor Type (Diamond > Platinum > Gold > Basic)
2. Performance Score (Higher scores first)
3. Product Diversity (More products = better ranking)
4. Category Expertise (Multiple categories = higher score)
```

### 4. **Response Generation**
- Limits to top 5 recommendations
- Provides detailed vendor information
- Includes recommendation reasoning
- Offers follow-up suggestions

## Database Schema

### ChatbotMessage Table
```sql
CREATE TABLE chatbot_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255),
    user_message TEXT,
    bot_response TEXT,
    user_ip VARCHAR(255),
    created_at TIMESTAMP,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

## Integration with Existing Features

### User Management
- Links conversations to registered users
- Tracks anonymous sessions
- Maintains user context across conversations

### Vendor Ranking System
- Integrates with VendorRanking table
- Uses performance scores for recommendations
- Considers lead generation metrics

### Product Catalog
- Searches across all products
- Matches by category relationships
- Considers product availability

## Security Considerations

### Public Access
- Chatbot endpoints are publicly accessible
- No sensitive information exposed
- Rate limiting recommended for production

### Data Privacy
- IP addresses logged for analytics
- User messages stored for improvement
- Admin-only access to conversation data

### Authentication
- Admin endpoints require ADMIN role
- User context optional but recommended
- Session-based conversation tracking

## Deployment Notes

### Database Migration
The chatbot feature will automatically create the `chatbot_message` table when the application starts with `spring.jpa.hibernate.ddl-auto=update`.

### Configuration
No additional configuration required. The chatbot uses existing database and security configurations.

### Monitoring
- Health check endpoint available
- Analytics dashboard for admins
- Conversation logging for debugging

## Example Usage Scenarios

### Scenario 1: New User Looking for Products
```
User: "Hi, I need a laptop for gaming"
Bot: Recommends premium vendors with gaming laptops
Result: User gets contact info for top-rated computer vendors
```

### Scenario 2: Business Looking for Services
```
User: "Who provides digital marketing services?"
Bot: Lists service providers specializing in marketing
Result: Business connects with marketing agencies
```

### Scenario 3: Vendor Discovery
```
User: "Show me premium electronics vendors"
Bot: Prioritizes Diamond/Platinum vendors in electronics
Result: User sees highest-tier vendors first
```

## Future Enhancements

### Planned Features
- Location-based vendor recommendations
- Price range filtering
- Multi-language support
- Integration with vendor availability
- Real-time vendor status updates
- Advanced analytics and reporting

### AI Improvements
- Machine learning for better query understanding
- Sentiment analysis for user satisfaction
- Personalized recommendations based on history
- Automated vendor performance updates

## Support and Maintenance

### Monitoring
- Regular review of conversation logs
- Analysis of common user queries
- Performance monitoring of recommendation accuracy

### Updates
- Periodic review of recommendation algorithm
- Vendor ranking criteria adjustments
- Response template improvements
- Analytics dashboard enhancements
