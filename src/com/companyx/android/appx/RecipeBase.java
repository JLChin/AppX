package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Recipe Database
 * 
 * Manages recipe data.
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class RecipeBase {
	Map<String, List<Recipe>> recipeMap; // recipes indexed by recipe name
	Map<String, List<Recipe>> indexMap; // recipes indexed by search word
	
	RecipeBase() {
		recipeMap = new TreeMap<String, List<Recipe>>();
		indexMap = new HashMap<String, List<Recipe>>();
	}
	
	/**
	 * Class representing a recipe.
	 */
	static class Recipe {
		String name;
		int timeRequiredInMin; // TODO
		List<RecipeIngredient> recipeIngredients;
		List<Direction> directions;
		
		Recipe (String name, List<RecipeIngredient> recipeIngredients, List<Direction> directions) {
			this.name = name;
			this.recipeIngredients = recipeIngredients;
			this.directions = directions;
		}
	}
	
	/**
	 * Class representing an ingredient.
	 */
	static class Ingredient {
		String name;
		
		Ingredient (String name) {
			this.name = name;
		}
	}
	
	/**
	 * Class representing one itemized ingredient consisting of the number amount, unit of measurement, and ingredient.
	 * Example: 1-1/4 Tablespoon Sugar
	 */
	static class RecipeIngredient {
		float amount;
		String measurement;
		Ingredient ingredient;

		RecipeIngredient (float amount, String measurement, Ingredient ingredient) {
			this.amount = amount;
			this.measurement = measurement;
			this.ingredient = ingredient;
		}
	}
	
	/**
	 * Class representing one instruction or action of the recipe.
	 */
	static class Direction {
		String direction;
		
		Direction (String direction) {
			this.direction = direction;
		}
	}
	
	/**
	 * Adds a new recipe to the recipe map and index map.
	 * @param newRecipe the new recipe to be added to the database.
	 */
	public void addRecipe(Recipe newRecipe) {
		if (newRecipe == null)
			return;
		
		// INDEX RECIPE NAME
		// parse recipe name
		String[] words = newRecipe.name.split(" ");
		
		// convert words to lowercase for indexing
		for (int i = 0; i < words.length; i++)
			words[i] = words[i].toLowerCase();
		
		for (String word : words) {
			// new word, did not exist previously
			if (!indexMap.containsKey(word))
				indexMap.put(word, new ArrayList<Recipe>());
			
			// add new recipe to search index
			indexMap.get(word).add(newRecipe);
		}
		
		// INDEX INGREDIENT NAMES
		// TODO
		
		// ADD TO MASTER RECIPE MAP
		// new recipe name, did not exist previously
		if (!recipeMap.containsKey(newRecipe.name))
			recipeMap.put(newRecipe.name, new ArrayList<Recipe>());
		
		// add new recipe to master list
		recipeMap.get(newRecipe.name).add(newRecipe);
	}
	
	/**
	 * Returns a list of all recipes, sorted by name.
	 * @return a list of all recipes, sorted by name.
	 */
	public List<Recipe> getRecipes() {
		List<Recipe> result = new ArrayList<Recipe>();
		
		for (Map.Entry<String, List<Recipe>> entry : recipeMap.entrySet()) {
			for (Recipe r : entry.getValue())
				result.add(r);
		}
		
		return result;
	}
	
	/**
	 * Returns a list of all recipes matching the specified search String, sorted by name.
	 * TODO duplicate result names
	 * @param searchString String containing the specifed search term(s).
	 * @return a list of all recipes matching the specified search String, sorted by name.
	 */
	public List<Recipe> searchRecipes(String searchString) {
		if (searchString == null)
			return null;
		
		Map<String, Recipe> resultTree = new TreeMap<String, Recipe>();
		List<Recipe> list = indexMap.get(searchString);
		
		for (Recipe r : list)
			resultTree.put(r.name, r);
		
		List<Recipe> result = new ArrayList<Recipe>();
		for (Map.Entry<String, Recipe> entry : resultTree.entrySet())
			result.add(entry.getValue());
		
		return result;
	}
}
