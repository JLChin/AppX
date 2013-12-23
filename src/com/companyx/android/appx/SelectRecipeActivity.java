package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeTime;
import com.companyx.android.appx.RecipeDatabase.ShoppingList;

/**
 * Search/Select Recipe Activity
 * 
 * Multi-purposed recipe selection activity.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class SelectRecipeActivity extends BaseListActivity {
	// CONSTANTS
	private static final int[] RECIPE_CATEGORIES = {R.string.select_recipe_all_recipes, R.string.select_recipe_chicken, R.string.select_recipe_pork };
	
	// VIEW HOLDERS
	private LinearLayout layoutIngredients;
		
	// STATE VARIABLES
	private List<Recipe> recipes;
	private String operation;
	
	// SYSTEM
	private RecipeDatabase recipeDatabase;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_recipe);
		
		initialize();
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// singleTop flag set in manifest; handle when the user searches from this Activity and sends new search Intent to itself without restarting
		setIntent(intent);
		handleIntent(intent);
	}
	
	private void initialize() {
		layoutIngredients = (LinearLayout) findViewById(R.id.layout_select_recipe_ingredients);
		
		initializeListView();
		
		recipeDatabase = RecipeDatabase.getInstance();
	}
	
	/**
	 * Perform search if user arrived at this activity via Search, return complete recipe list otherwise.
	 * @param intent the Intent passed to this Activity.
	 */
	private void handleIntent(Intent intent) {
		// receive search action and other operations
		String action = intent.getAction();
		operation = intent.getStringExtra("operation");
		
		// reset views
		layoutIngredients.removeAllViews();
		
		if (action != null && action.equals(Intent.ACTION_SEARCH)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			loadSearchRecipes(query);
		} else if (operation != null) {
			if (operation.equals("Favorites"))
				loadFavoriteRecipes();
			else if (operation.equals("Shopping List"))
				loadShoppingListRecipes();
			else if (operation.equals("Categories"))
				loadCategories();
		}
	}

	/**
	 * Set up the ListView and attach custom listener.
	 */
	private void initializeListView() {
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (operation != null && operation.equals("Categories")) { // CATEGORIES
					loadCategory(getString(RECIPE_CATEGORIES[position]));
				} else { // RECIPES, SEARCH, FAVORITES, OR SHOPPING LIST
					int recipeId = ((RecipeListViewAdapter.RecipeView) view.getTag()).recipeId;
					
					Intent intent = new Intent(SelectRecipeActivity.this, RecipeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					intent.putExtra("recipeId", recipeId);
					startActivity(intent);
				}
			}
		});
	}
	
	/**
	 * Load recipes from the database that match the search query.
	 * @param query the user-specified search query.
	 */
	private void loadSearchRecipes(String query) {
		recipes = recipeDatabase.searchRecipes(query);
		setListAdapter(new RecipeListViewAdapter(this, recipes));
		
		// TOAST COUNT
		Toast.makeText(getApplicationContext(), getString(R.string.select_recipe_showing) + " " + recipes.size() + " " + getString(R.string.select_recipe_recipes), Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Load favorite recipes from the RecipeDatabase, sorted by Recipe name.
	 */
	private void loadFavoriteRecipes() {
		recipes = recipeDatabase.getFavoriteRecipes();
		setListAdapter(new RecipeListViewAdapter(this, recipes));
		
		// IF EMPTY
		if (recipes.size() == 0)
			new AlertDialog.Builder(this).setTitle(R.string.select_recipe_favorites_alert_title).setMessage(R.string.select_recipe_favorites_empty).setPositiveButton(R.string.select_recipe_favorites_empty_ok, null).show();
	}
	
	/**
	 * Load shopping list recipes from the RecipeDatabase, sorted by Recipe name.
	 * Show list of aggregated recipe ingredients.
	 */
	private void loadShoppingListRecipes() {
		recipes = recipeDatabase.getShoppingListRecipes();
		setListAdapter(new RecipeListViewAdapter(this, recipes));
		
		ShoppingList list = recipeDatabase.getShoppingList();
		
		// MEAT
		if (!list.meat.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_meat);
			layoutIngredients.addView(tv);
			addIngredientViews(list.meat, layoutIngredients);
		}
		
		// SEAFOOD
		if (!list.seafood.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_seafood);
			layoutIngredients.addView(tv);
			addIngredientViews(list.seafood, layoutIngredients);
		}
		
		// PRODUCE
		if (!list.produce.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_produce);
			layoutIngredients.addView(tv);
			addIngredientViews(list.produce, layoutIngredients);
		}
		
		// OTHER
		if (!list.other.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_other);
			layoutIngredients.addView(tv);
			addIngredientViews(list.other, layoutIngredients);
		}
		
		// IF EMPTY
		if (recipes.size() == 0)
			new AlertDialog.Builder(this).setTitle(R.string.select_recipe_shopping_list_alert_title).setMessage(R.string.select_recipe_shopping_list_empty).setPositiveButton(R.string.select_recipe_shopping_list_empty_ok, null).show();
	}
	
	/**
	 * Load recipe categories.
	 */
	private void loadCategories() {
		List<String> recipeCategories = new ArrayList<String>();
		for (int i : RECIPE_CATEGORIES)
			recipeCategories.add(getString(i));
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recipeCategories));
	}
	
	/**
	 * Load the Recipe List based on the category selected by the user.
	 * @param category the user-selected category to load the recipes for.
	 */
	private void loadCategory(String category) {
		operation = null; // remove "Categories" status
		
		if (category.equals(getString(R.string.select_recipe_all_recipes)))
			recipes = recipeDatabase.allRecipes();
		else
			recipes = recipeDatabase.searchRecipes(category);
		
		setListAdapter(new RecipeListViewAdapter(this, recipes));
		
		// TOAST COUNT
		Toast.makeText(getApplicationContext(), getString(R.string.select_recipe_showing) + " " + recipes.size() + " " + getString(R.string.select_recipe_recipes), Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Helper function which adds child views displaying ingredients to a parent ViewGroup.
	 * @param ingredients the List of Strings representing ingredients.
	 * @param viewGroup the parent ViewGroup to add the child views to.
	 */
	private void addIngredientViews(List<String> ingredients, ViewGroup viewGroup) {
		for (String s : ingredients) {
			TextView tv = new TextView(this);
			tv.setText(s);
			viewGroup.addView(tv);
		}
	}
	
	/**
	 * Custom Recipe list view adapter.
	 */
	private class RecipeListViewAdapter extends ArrayAdapter<Recipe> {
		private final List<Recipe> recipes;
		private final Context activity;
		
		RecipeListViewAdapter(Context activity, List<Recipe> recipes) {
			super(activity, R.layout.recipe_list_item, recipes);
			this.activity = activity;
			this.recipes = recipes;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        RecipeView recipeView = null;
	 
	        if (view == null) {
	        	LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
	            view = inflater.inflate(R.layout.recipe_list_item, null);
	 
	            // hold the view objects in an object, so they don't need to be re-fetched
	            recipeView = new RecipeView();
	            recipeView.textViewName = (TextView) view.findViewById(R.id.recipe_list_name);
	            recipeView.textViewDescription = (TextView) view.findViewById(R.id.recipe_list_description);
	            recipeView.textViewInfoRight = (TextView) view.findViewById(R.id.recipe_list_info_right);
	 
	            // cache the view objects in the tag, so they can be re-accessed later
	            view.setTag(recipeView);
	        } else
	        	recipeView = (RecipeView) view.getTag();
	 
	        // set up view, store unique ID to retrieve recipe from database when selected
	        Recipe recipe = recipes.get(position);
	        int recipeId = recipe.recipeId;
	        
	        recipeView.recipeId = recipeId;
	        recipeView.textViewName.setText(recipe.name);
	        recipeView.textViewDescription.setText("Put something cool here.");
	        if (operation != null && operation.equals("Shopping List"))
	        	recipeView.textViewInfoRight.setText(String.valueOf(recipeDatabase.getQuantity(recipeId)));
	        else
	        	recipeView.textViewInfoRight.setText(getTime(recipe.recipeTime));
	        
	        return view;
	    }
		
		class RecipeView {
			int recipeId;
			TextView textViewName;
			TextView textViewDescription;
			TextView textViewInfoRight;
		}
		
		/**
		 * Helper function that generates a string containing the formatted hour and minute representation of the recipe cooking time.
		 * @param timeRequiredInMin the time required in minutes for the Recipe.
		 * @return a string containing the formatted hour and minute representation of the recipe cooking time.
		 */
		private String getTime(RecipeTime recipeTime) {
			// retrieve total time
			short totalTimeInMin = (short) (recipeTime.prepTimeInMin + recipeTime.inactivePrepTimeInMin + recipeTime.cookTimeInMin);
			
			// construct hours string
			short hours = (short) (totalTimeInMin / 60);
			String hoursStr = "";
			if (hours != 0) {
				if (hours == 1)
					hoursStr += hours + " " + getString(R.string.select_recipe_hour) + " ";
				else
					hoursStr += hours + " " + getString(R.string.select_recipe_hours) + " ";
			}
				
			return hoursStr + (totalTimeInMin % 60) + " " + getString(R.string.select_recipe_min);
		}
	}
}
