package com.shubhodip.nutrisift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/recipe")
public class ChatController {

    private static final String COHERE_API_URL = "https://api.cohere.com/v2/chat";
    private static final String COHERE_API_KEY = "qDLLn4ntg1Lj1KnyuiBq5Rf3WjcdYmEWhtSi7f3H";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<Object> generateRecipe(@RequestBody RecipeRequest request) {
        try {
            // Create enhanced prompt format with additional recipe information
            String prompt = String.format(
                "Suggest a creative recipe using only these ingredients: %s. " +
                "Respond in this JSON format: " +
                "{" +
                "\"title\": \"Recipe Name\", " +
                "\"ingredients\": [\"ingredient 1\", \"ingredient 2\", ...], " +
                "\"steps\": [\"step 1\", \"step 2\", ...(in detail instructions)], " +
                "\"calories\": \"approximate calories per serving\", " +
                "\"diet\": \"dietary category (e.g., vegetarian, keto, vegan)\", " +
                "\"origin\": \"specific geographical origin of the recipe\", " +
                "\"course\": \"type of meal (e.g., appetizer, main dish, dessert)\", " +
                "\"cuisine\": \"type of cuisine (e.g., Indian, Italian, Mexican)\"" +
                "}",
                request.getIngredients()
            );

            // Create HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + COHERE_API_KEY);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("stream", false);
            requestBody.put("model", "command-a-03-2025");
            
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            requestBody.put("messages", List.of(message));

            // Create HTTP entity with headers and body
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make the API call
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                COHERE_API_URL, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            // Extract the recipe JSON from the response
            String responseBody = response.getBody();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            // Extract the message content from the Cohere response
            String content = "";
            if (responseMap.containsKey("message")) {
                Map<String, Object> messageObj = (Map<String, Object>) responseMap.get("message");
                if (messageObj.containsKey("content")) {
                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) messageObj.get("content");
                    if (!contentList.isEmpty()) {
                        content = (String) contentList.get(0).get("text");
                    }
                }
            }
            
            // Extract JSON from the response using regex
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                String recipeJson = matcher.group();
                Map<String, Object> recipeMap = objectMapper.readValue(recipeJson, Map.class);
                
                // Ensure all required fields exist (with defaults if missing)
                if (!recipeMap.containsKey("calories")) recipeMap.put("calories", "Not available");
                if (!recipeMap.containsKey("diet")) recipeMap.put("diet", "Not specified");
                if (!recipeMap.containsKey("origin")) recipeMap.put("origin", "Not specified");
                if (!recipeMap.containsKey("course")) recipeMap.put("course", "Not specified");
                if (!recipeMap.containsKey("cuisine")) recipeMap.put("cuisine", "Not specified");
                
                return ResponseEntity.ok(recipeMap);
            } else {
                // Fallback if no JSON found
                Map<String, Object> errorRecipe = new HashMap<>();
                errorRecipe.put("title", "Recipe Not Found");
                errorRecipe.put("ingredients", List.of());
                errorRecipe.put("steps", List.of("Sorry, I couldn't generate a recipe."));
                errorRecipe.put("calories", "Not available");
                errorRecipe.put("diet", "Not specified");
                errorRecipe.put("origin", "Not specified");
                errorRecipe.put("course", "Not specified");
                errorRecipe.put("cuisine", "Not specified");
                return ResponseEntity.ok(errorRecipe);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("title", "Error");
            errorResponse.put("ingredients", List.of());
            errorResponse.put("steps", List.of("Sorry, there was an error generating your recipe: " + e.getMessage()));
            errorResponse.put("calories", "Not available");
            errorResponse.put("diet", "Not specified");
            errorResponse.put("origin", "Not specified");
            errorResponse.put("course", "Not specified");
            errorResponse.put("cuisine", "Not specified");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/search")
    public ResponseEntity<Object> searchRecipes(@RequestBody RecipeSearchRequest request) {
        try {
            // Create search prompt for multiple recipes
            StringBuilder promptBuilder = new StringBuilder();
            
            // Add current timestamp to make each request unique
            long timestamp = System.currentTimeMillis();
            
            // Request variety in recipes
            promptBuilder.append("Find me 8-10 unique and different recipes (IMPORTANT: ensure variety and avoid repeating recipes from previous requests). ");
            promptBuilder.append("This is request timestamp: ").append(timestamp).append(". ");
            
            // Add search criteria to the prompt
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                promptBuilder.append("Matching the search query: ").append(request.getQuery()).append(". ");
            }
            
            // Add dietary preferences if specified
            if (request.getDiet() != null && !request.getDiet().equals("any")) {
                promptBuilder.append("Recipes should be ").append(request.getDiet()).append(". ");
            }
            
            // Add cuisine type if specified
            if (request.getCuisine() != null && !request.getCuisine().equals("any")) {
                promptBuilder.append("From ").append(request.getCuisine()).append(" cuisine. ");
            }
            
            // Add course type if specified
            if (request.getCourse() != null && !request.getCourse().equals("any")) {
                promptBuilder.append("For ").append(request.getCourse()).append(". ");
            }
            
            // Add calorie range if specified
            if (request.getCalorieRange() != null && !request.getCalorieRange().equals("any")) {
                switch (request.getCalorieRange()) {
                    case "under300":
                        promptBuilder.append("Under 300 calories. ");
                        break;
                    case "300-500":
                        promptBuilder.append("Between 300-500 calories. ");
                        break;
                    case "500-800":
                        promptBuilder.append("Between 500-800 calories. ");
                        break;
                    case "over800":
                        promptBuilder.append("Over 800 calories. ");
                        break;
                }
            }
            
            // Add origin if specified
            if (request.getOrigin() != null && !request.getOrigin().equals("any")) {
                promptBuilder.append("From ").append(request.getOrigin()).append(" origin. ");
            }
            
            // Explicit instruction for variety
            promptBuilder.append("IMPORTANT: Each recipe should be creative and different from one another. ");
            
            // Add JSON format specification
            promptBuilder.append("Respond with an array of recipe objects in this JSON format: ");
            promptBuilder.append("[{");
            promptBuilder.append("\"title\": \"Recipe Name\", ");
            promptBuilder.append("\"ingredients\": [\"ingredient 1\", \"ingredient 2\", ...], ");
            promptBuilder.append("\"steps\": [\"step 1\", \"step 2\", ...], ");
            promptBuilder.append("\"calories\": \"approximate calories per serving\", ");
            promptBuilder.append("\"diet\": \"dietary category\", ");
            promptBuilder.append("\"origin\": \"geographical origin\", ");
            promptBuilder.append("\"course\": \"type of meal\", ");
            promptBuilder.append("\"cuisine\": \"type of cuisine\", ");
            promptBuilder.append("\"prepTime\": \"preparation time in minutes\", ");
            promptBuilder.append("\"cookTime\": \"cooking time in minutes\"");
            promptBuilder.append("}]");
            
            // Create HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + COHERE_API_KEY);

            // Create request body with additional parameters to increase variety
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("stream", false);
            requestBody.put("model", "command-a-03-2025");
            requestBody.put("temperature", 0.9); // Higher temperature = more randomness
            requestBody.put("p", 0.75); // Nucleus sampling for more variety
            
            // Add a seed based on timestamp to prevent repetitive outputs
            requestBody.put("seed", timestamp % 1000000);
            
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", promptBuilder.toString());
            
            requestBody.put("messages", List.of(message));

            // Create HTTP entity with headers and body
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make the API call
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                COHERE_API_URL, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            // Extract the recipe JSON from the response
            String responseBody = response.getBody();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            // Extract the message content from the Cohere response
            String content = "";
            if (responseMap.containsKey("message")) {
                Map<String, Object> messageObj = (Map<String, Object>) responseMap.get("message");
                if (messageObj.containsKey("content")) {
                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) messageObj.get("content");
                    if (!contentList.isEmpty()) {
                        content = (String) contentList.get(0).get("text");
                    }
                }
            }
            
            // Extract JSON array from the response using regex
            Pattern pattern = Pattern.compile("\\[\\s*\\{.*\\}\\s*\\]", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                String recipesJson = matcher.group();
                List<Map<String, Object>> recipes = objectMapper.readValue(recipesJson, List.class);
                
                // Ensure all required fields exist for each recipe
                for (Map<String, Object> recipe : recipes) {
                    if (!recipe.containsKey("calories")) recipe.put("calories", "Not available");
                    if (!recipe.containsKey("diet")) recipe.put("diet", "Not specified");
                    if (!recipe.containsKey("origin")) recipe.put("origin", "Not specified");
                    if (!recipe.containsKey("course")) recipe.put("course", "Not specified");
                    if (!recipe.containsKey("cuisine")) recipe.put("cuisine", "Not specified");
                    if (!recipe.containsKey("prepTime")) recipe.put("prepTime", 30);
                    if (!recipe.containsKey("cookTime")) recipe.put("cookTime", 45);
                    
                    // Generate a unique ID for each recipe (simple solution for demo)
                    recipe.put("id", System.nanoTime() + recipes.indexOf(recipe));
                }
                
                return ResponseEntity.ok(recipes);
            } else {
                // Fallback if no JSON array found
                List<Map<String, Object>> fallbackRecipes = new ArrayList<>();
                Map<String, Object> errorRecipe = new HashMap<>();
                errorRecipe.put("id", 1);
                errorRecipe.put("title", "No Recipes Found");
                errorRecipe.put("ingredients", List.of("Please try a different search query"));
                errorRecipe.put("steps", List.of("Sorry, I couldn't find any recipes matching your criteria."));
                errorRecipe.put("calories", "Not available");
                errorRecipe.put("diet", "Not specified");
                errorRecipe.put("origin", "Not specified");
                errorRecipe.put("course", "Not specified");
                errorRecipe.put("cuisine", "Not specified");
                errorRecipe.put("prepTime", 0);
                errorRecipe.put("cookTime", 0);
                fallbackRecipes.add(errorRecipe);
                
                return ResponseEntity.ok(fallbackRecipes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            List<Map<String, Object>> errorResponse = new ArrayList<>();
            Map<String, Object> errorRecipe = new HashMap<>();
            errorRecipe.put("id", 0);
            errorRecipe.put("title", "Error");
            errorRecipe.put("ingredients", List.of());
            errorRecipe.put("steps", List.of("Sorry, there was an error searching for recipes: " + e.getMessage()));
            errorRecipe.put("calories", "Not available");
            errorRecipe.put("diet", "Not specified");
            errorRecipe.put("origin", "Not specified");
            errorRecipe.put("course", "Not specified");
            errorRecipe.put("cuisine", "Not specified");
            errorRecipe.put("prepTime", 0);
            errorRecipe.put("cookTime", 0);
            errorResponse.add(errorRecipe);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

// Recipe request class
class RecipeRequest {
    private String ingredients;

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
}

// Recipe search request class
class RecipeSearchRequest {
    private String query;
    private String diet;
    private String cuisine;
    private String course;
    private String calorieRange;
    private String origin;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDiet() {
        return diet;
    }

    public void setDiet(String diet) {
        this.diet = diet;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCalorieRange() {
        return calorieRange;
    }

    public void setCalorieRange(String calorieRange) {
        this.calorieRange = calorieRange;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
