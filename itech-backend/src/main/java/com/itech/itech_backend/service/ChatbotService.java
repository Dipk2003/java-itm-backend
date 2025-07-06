package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.ChatbotRequestDto;
import com.itech.itech_backend.dto.ChatbotResponseDto;
import com.itech.itech_backend.enums.VendorType;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatbotMessageRepository chatbotMessageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VendorRankingRepository vendorRankingRepository;

    public ChatbotResponseDto processMessage(ChatbotRequestDto request) {
        log.info("Processing chatbot message: {}", request.getMessage());
        
        String userMessage = request.getMessage().toLowerCase().trim();
        String sessionId = request.getSessionId();
        
        // Generate session ID if not provided
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        // Process the message and generate response
        ChatbotResponseDto response = generateResponse(userMessage, sessionId);
        
        // Save the conversation
        saveChatMessage(request, response);
        
        return response;
    }

    private ChatbotResponseDto generateResponse(String userMessage, String sessionId) {
        List<ChatbotResponseDto.VendorRecommendationDto> recommendations = new ArrayList<>();
        String responseText;
        
        // Check if user is asking about products or services
        if (isProductQuery(userMessage)) {
            recommendations = findRecommendedVendors(userMessage);
            responseText = generateProductResponse(userMessage, recommendations);
        } else if (isServiceQuery(userMessage)) {
            recommendations = findServiceProviders(userMessage);
            responseText = generateServiceResponse(userMessage, recommendations);
        } else if (isGreeting(userMessage)) {
            responseText = generateGreetingResponse();
        } else if (isHelpQuery(userMessage)) {
            responseText = generateHelpResponse();
        } else {
            responseText = generateDefaultResponse();
        }
        
        return ChatbotResponseDto.builder()
                .response(responseText)
                .sessionId(sessionId)
                .recommendations(recommendations)
                .hasRecommendations(!recommendations.isEmpty())
                .build();
    }

    private List<ChatbotResponseDto.VendorRecommendationDto> findRecommendedVendors(String query) {
        // Extract keywords from query
        List<String> keywords = extractKeywords(query);
        
        // Find products matching keywords
        List<Product> matchingProducts = productRepository.findAll().stream()
                .filter(product -> matchesKeywords(product, keywords))
                .collect(Collectors.toList());
        
        // Get vendors from matching products
        Set<User> vendors = matchingProducts.stream()
                .map(Product::getVendor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // Sort vendors by premium type and performance
        return vendors.stream()
                .sorted(this::compareVendorsByPriority)
                .limit(5) // Limit to top 5 recommendations
                .map(vendor -> buildVendorRecommendation(vendor, matchingProducts))
                .collect(Collectors.toList());
    }

    private List<ChatbotResponseDto.VendorRecommendationDto> findServiceProviders(String query) {
        // For service queries, find vendors by categories
        List<String> keywords = extractKeywords(query);
        
        // Find categories matching keywords
        List<Category> matchingCategories = categoryRepository.findAll().stream()
                .filter(category -> matchesKeywords(category.getName(), keywords))
                .collect(Collectors.toList());
        
        // Find products in these categories
        List<Product> categoryProducts = productRepository.findAll().stream()
                .filter(product -> matchingCategories.contains(product.getCategory()))
                .collect(Collectors.toList());
        
        // Get vendors from category products
        Set<User> serviceProviders = categoryProducts.stream()
                .map(Product::getVendor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        return serviceProviders.stream()
                .sorted(this::compareVendorsByPriority)
                .limit(5)
                .map(vendor -> buildVendorRecommendation(vendor, categoryProducts))
                .collect(Collectors.toList());
    }

    private ChatbotResponseDto.VendorRecommendationDto buildVendorRecommendation(User vendor, List<Product> products) {
        // Get vendor's products
        List<Product> vendorProducts = products.stream()
                .filter(product -> product.getVendor().getId().equals(vendor.getId()))
                .collect(Collectors.toList());
        
        // Get vendor's categories
        List<String> categories = vendorProducts.stream()
                .map(product -> product.getCategory().getName())
                .distinct()
                .collect(Collectors.toList());
        
        // Get vendor's product names
        List<String> productNames = vendorProducts.stream()
                .map(Product::getName)
                .distinct()
                .collect(Collectors.toList());
        
        // Get performance score
        Double performanceScore = vendorRankingRepository.findByVendorId(vendor.getId())
                .map(VendorRanking::getPerformanceScore)
                .orElse(0.0);
        
        // Generate recommendation reason
        String reason = generateRecommendationReason(vendor, vendorProducts.size(), categories.size());
        
        return ChatbotResponseDto.VendorRecommendationDto.builder()
                .vendorId(vendor.getId())
                .vendorName(vendor.getName())
                .vendorEmail(vendor.getEmail())
                .vendorPhone(vendor.getPhone())
                .vendorType(vendor.getVendorType() != null ? vendor.getVendorType().name() : "BASIC")
                .performanceScore(performanceScore)
                .products(productNames)
                .categories(categories)
                .reason(reason)
                .build();
    }

    private String generateRecommendationReason(User vendor, int productCount, int categoryCount) {
        StringBuilder reason = new StringBuilder();
        
        // Premium vendor type
        if (vendor.getVendorType() != null && vendor.getVendorType() != VendorType.BASIC) {
            reason.append("Premium ").append(vendor.getVendorType().name()).append(" vendor");
        } else {
            reason.append("Verified vendor");
        }
        
        // Product diversity
        if (productCount > 1) {
            reason.append(" with ").append(productCount).append(" products");
        }
        
        // Category expertise
        if (categoryCount > 1) {
            reason.append(" across ").append(categoryCount).append(" categories");
        }
        
        return reason.toString();
    }

    private int compareVendorsByPriority(User v1, User v2) {
        // First priority: Vendor type (premium vendors first)
        VendorType type1 = v1.getVendorType() != null ? v1.getVendorType() : VendorType.BASIC;
        VendorType type2 = v2.getVendorType() != null ? v2.getVendorType() : VendorType.BASIC;
        int typeComparison = getVendorTypePriority(type1) - getVendorTypePriority(type2);
        if (typeComparison != 0) {
            return typeComparison;
        }
        
        // Second priority: Performance score
        Double score1 = vendorRankingRepository.findByVendorId(v1.getId())
                .map(VendorRanking::getPerformanceScore)
                .orElse(0.0);
        Double score2 = vendorRankingRepository.findByVendorId(v2.getId())
                .map(VendorRanking::getPerformanceScore)
                .orElse(0.0);
        
        return Double.compare(score2, score1); // Higher score first
    }

    private int getVendorTypePriority(VendorType vendorType) {
        if (vendorType == null) {
            return 5;
        }
        switch (vendorType) {
            case DIAMOND: return 1;
            case PLATINUM: return 2;
            case GOLD: return 3;
            case BASIC: return 4;
            default: return 5;
        }
    }

    private List<String> extractKeywords(String query) {
        return Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(word -> word.length() > 2) // Filter out short words
                .filter(word -> !isStopWord(word))
                .collect(Collectors.toList());
    }

    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "or", "but", "for", "with", "from", "can", "you", "what", "where", "how", "when", "why", "need", "want", "looking", "find", "get", "have", "has", "are", "is", "was", "were", "been", "being", "will", "would", "could", "should");
        return stopWords.contains(word);
    }

    private boolean matchesKeywords(Product product, List<String> keywords) {
        String productText = (product.getName() + " " + product.getDescription() + " " + product.getCategory().getName()).toLowerCase();
        return keywords.stream().anyMatch(productText::contains);
    }

    private boolean matchesKeywords(String text, List<String> keywords) {
        String lowerText = text.toLowerCase();
        return keywords.stream().anyMatch(lowerText::contains);
    }

    private boolean isProductQuery(String message) {
        String[] productKeywords = {"product", "item", "buy", "purchase", "price", "cost", "sell", "selling", "available", "stock"};
        return Arrays.stream(productKeywords).anyMatch(message::contains);
    }

    private boolean isServiceQuery(String message) {
        String[] serviceKeywords = {"service", "provider", "company", "business", "offer", "provides", "specializes", "expert", "professional"};
        return Arrays.stream(serviceKeywords).anyMatch(message::contains);
    }

    private boolean isGreeting(String message) {
        String[] greetings = {"hi", "hello", "hey", "good morning", "good afternoon", "good evening"};
        return Arrays.stream(greetings).anyMatch(message::contains);
    }

    private boolean isHelpQuery(String message) {
        String[] helpKeywords = {"help", "assist", "support", "how to", "what can", "options", "menu"};
        return Arrays.stream(helpKeywords).anyMatch(message::contains);
    }

    private String generateProductResponse(String query, List<ChatbotResponseDto.VendorRecommendationDto> recommendations) {
        if (recommendations.isEmpty()) {
            return "I couldn't find any vendors for your product query. Please try with different keywords or browse our categories.";
        }
        
        StringBuilder response = new StringBuilder();
        response.append("Here are the top recommended vendors for your product query:\n\n");
        
        for (int i = 0; i < recommendations.size(); i++) {
            ChatbotResponseDto.VendorRecommendationDto vendor = recommendations.get(i);
            response.append(String.format("%d. **%s** (%s)\n", i + 1, vendor.getVendorName(), vendor.getVendorType()));
            response.append(String.format("   📧 %s | 📞 %s\n", vendor.getVendorEmail(), vendor.getVendorPhone()));
            response.append(String.format("   🏆 %s\n", vendor.getReason()));
            response.append(String.format("   📦 Products: %s\n\n", String.join(", ", vendor.getProducts())));
        }
        
        response.append("Would you like more information about any of these vendors?");
        return response.toString();
    }

    private String generateServiceResponse(String query, List<ChatbotResponseDto.VendorRecommendationDto> recommendations) {
        if (recommendations.isEmpty()) {
            return "I couldn't find any service providers for your query. Please try with different keywords or browse our categories.";
        }
        
        StringBuilder response = new StringBuilder();
        response.append("Here are the top recommended service providers:\n\n");
        
        for (int i = 0; i < recommendations.size(); i++) {
            ChatbotResponseDto.VendorRecommendationDto vendor = recommendations.get(i);
            response.append(String.format("%d. **%s** (%s)\n", i + 1, vendor.getVendorName(), vendor.getVendorType()));
            response.append(String.format("   📧 %s | 📞 %s\n", vendor.getVendorEmail(), vendor.getVendorPhone()));
            response.append(String.format("   🏆 %s\n", vendor.getReason()));
            response.append(String.format("   🎯 Services: %s\n\n", String.join(", ", vendor.getCategories())));
        }
        
        response.append("Would you like to connect with any of these service providers?");
        return response.toString();
    }

    private String generateGreetingResponse() {
        return "Hello! 👋 Welcome to iTech! I'm here to help you find the best vendors and products on our platform.\n\n" +
               "You can ask me about:\n" +
               "• Products you're looking for\n" +
               "• Services you need\n" +
               "• Vendor recommendations\n" +
               "• Categories and pricing\n\n" +
               "What can I help you with today?";
    }

    private String generateHelpResponse() {
        return "I can help you with:\n\n" +
               "🔍 **Finding Products**: Ask me about specific products you're looking for\n" +
               "🏢 **Service Providers**: Find vendors who offer specific services\n" +
               "⭐ **Vendor Recommendations**: Get premium vendor suggestions\n" +
               "📊 **Categories**: Browse products by category\n\n" +
               "**Example queries:**\n" +
               "• \"I need electronics products\"\n" +
               "• \"Who provides web development services?\"\n" +
               "• \"Show me premium vendors for furniture\"\n" +
               "• \"Find vendors in Mumbai\"\n\n" +
               "What would you like to know?";
    }

    private String generateDefaultResponse() {
        return "I'm here to help you find the best vendors and products! Try asking me about:\n\n" +
               "• Specific products you need\n" +
               "• Services you're looking for\n" +
               "• Vendor recommendations\n\n" +
               "For example: \"I need electronics\" or \"Who provides web services?\"";
    }

    private void saveChatMessage(ChatbotRequestDto request, ChatbotResponseDto response) {
        try {
            User user = null;
            if (request.getUserId() != null) {
                user = userRepository.findById(request.getUserId()).orElse(null);
            }
            
            ChatbotMessage chatMessage = ChatbotMessage.builder()
                    .sessionId(request.getSessionId())
                    .userMessage(request.getMessage())
                    .botResponse(response.getResponse())
                    .userIp(request.getUserIp())
                    .user(user)
                    .build();
            
            chatbotMessageRepository.save(chatMessage);
        } catch (Exception e) {
            log.error("Error saving chat message: {}", e.getMessage());
        }
    }

    public List<ChatbotMessage> getChatHistory(String sessionId) {
        return chatbotMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
